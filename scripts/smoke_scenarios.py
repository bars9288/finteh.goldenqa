#!/usr/bin/env python3
"""46 проверок собранного приложения на локальном HTTP-моке; данные хранятся в temp.

Запуск из любого каталога: python3 scripts/smoke_scenarios.py [jar] [label]
Требуются Python 3 и Java 21+. Внешние сервисы не используются.
"""
import copy, http.server, json, pathlib, socket, sqlite3, subprocess, sys, tempfile, threading, time, urllib.request, urllib.error, uuid

ROOT = pathlib.Path(__file__).resolve().parents[1]
jar = (ROOT / (sys.argv[1] if len(sys.argv) > 1 else 'cqa-bootstrap/target/cqa-bootstrap-0.0.4.jar')).resolve()
if not jar.is_file():
    raise SystemExit('Сначала соберите приложение: mvn clean verify')
label = sys.argv[2] if len(sys.argv) > 2 else 'smoke'
work = pathlib.Path(tempfile.mkdtemp(prefix='cqa-' + label + '-'))
calls = []
class Mock(http.server.BaseHTTPRequestHandler):
    def do_POST(self):
        body = self.rfile.read(int(self.headers.get('Content-Length', 0)))
        calls.append((self.path, json.loads(body) if body else None))
        status = 503 if self.path == '/unavailable' else 200
        payload = {'user': {'id': 42, 'email': 'ivan.petrov@example.com', 'role': 'USER', 'name': 'Ivan', 'surname': 'Petrov'}}
        data = json.dumps(payload).encode()
        self.send_response(status); self.send_header('Content-Type', 'application/json'); self.end_headers(); self.wfile.write(data)
    def log_message(self, *args): pass

mock = http.server.ThreadingHTTPServer(('127.0.0.1', 0), Mock)
threading.Thread(target=mock.serve_forever, daemon=True).start()
mock_url = 'http://127.0.0.1:' + str(mock.server_port)
with socket.socket() as sock:
    sock.bind(('127.0.0.1', 0)); port = sock.getsockname()[1]
base = 'http://127.0.0.1:' + str(port)
config = work / 'application.properties'
config.write_text('\n'.join([
    'spring.application.name=cqa-server', 'server.address=127.0.0.1', f'server.port={port}',
    f'spring.datasource.url=jdbc:sqlite:{work}/journal.db?foreign_keys=true&journal_mode=WAL&busy_timeout=5000',
    'spring.sql.init.mode=never', f'cqa.journal.fallback-dir={work}/logs', f'cqa.scenarios.directory={work}/scenarios',
    'cqa.api.paths.save-and-merge=/mdm/saveAndMerge', 'cqa.api.paths.greeting-operator=/hello',
    'cqa.api.paths.greeting-service=/helloService', 'cqa.api.paths.echo=/echo', 'cqa.api.paths.scenarios=/scenarios',
    'cqa.api.paths.soap=/services', 'spring.webservices.path=/services', 'cqa.save-and-merge.mdm.scope=DEV',
    f'cqa.connections.MDM.DEV.url={mock_url}', 'cqa.connections.MDM.DEV.read-timeout=3s',
    'cqa.connections.MDM.DEV.connect-timeout=3s', 'cqa.connections.MDM.DEV.endpoints.save-and-merge=/mdm',
    f'cqa.connections.mock.DEV.url={mock_url}', 'cqa.connections.mock.DEV.read-timeout=3s',
    'scalar.telemetry=false', 'scalar.with-default-fonts=false',
]))
log = open(work / 'server.log', 'w')
process = subprocess.Popen(['java', '-jar', str(jar), '--spring.config.location=file:' + str(config)], cwd=work, stdout=log, stderr=subprocess.STDOUT)
observed = {}
def request(path, data=None, content_type='application/json', method=None):
    raw = data.encode() if isinstance(data, str) else json.dumps(data).encode() if data is not None else None
    req = urllib.request.Request(base + path, data=raw, headers={'Content-Type': content_type}, method=method)
    try: response = urllib.request.urlopen(req, timeout=15)
    except urllib.error.HTTPError as e: response = e
    with response:
        raw = response.read().decode()
        try: value = json.loads(raw)
        except ValueError: value = raw
        return response.status, value
def check(name, actual, expected):
    assert actual == expected, (name, actual, expected)
    observed[name] = actual
def create_run(name, scenario, expected):
    scenario['id'] = str(uuid.uuid5(uuid.NAMESPACE_URL, name))
    status, created = request('/scenarios', scenario)
    check(name + '/create', status, 201)
    check(name + '/identity', created, {'id': scenario['id'], 'version': '1.0'})
    status, result = request('/scenarios/' + scenario['id'] + '/executions', {'version': '1.0', 'environmentId': 'DEV', 'variables': {}})
    check(name + '/http', status, 200)
    check(name + '/status', result['status'], expected)
    return result
try:
    for _ in range(100):
        if process.poll() is not None: raise RuntimeError('Server exited: ' + (work / 'server.log').read_text()[-7000:])
        try:
            if request('/v3/api-docs')[0] == 200: break
        except (OSError, TimeoutError): pass
        time.sleep(.2)
    else: raise RuntimeError('Startup timeout: ' + str(work))
    scenario = json.loads((ROOT / 'simpeScenario.json').read_text())
    move = scenario['steps'][0]['moves'][0]
    move['path'] = '/users'; move['connectionId'] = 'mock'
    result = create_run('success', scenario, 'COMPLETED')
    check('success/assertions', [a['status'] for a in result['steps'][0]['moves'][0]['assertions']], ['PASSED'] * 5)
    check('success/mock-body', calls[-1], ('/users', json.loads(move['requestBody'])))
    check('duplicate', request('/scenarios', scenario)[0], 409)
    bad = copy.deepcopy(scenario); bad['id'] = str(uuid.uuid4()); bad['metadata']['name'] = ''
    check('invalid-scenario', request('/scenarios', bad)[0], 400)
    check('malformed-json', request('/scenarios', '{')[0], 400)
    check('missing-scenario', request('/scenarios/' + str(uuid.uuid4()) + '/executions', {'version': '1.0', 'environmentId': 'DEV'})[0], 404)
    check('invalid-execution', request('/scenarios/' + scenario['id'] + '/executions', {'version': '', 'environmentId': ''})[0], 400)
    failing = copy.deepcopy(scenario)
    failing['steps'][0]['moves'][0]['assertions'][0]['expectedValue'] = 201
    second = copy.deepcopy(failing['steps'][0]); second['id'] = 'step-002'; second['moves'][0]['id'] = 'move-002'; second['moves'][0]['assertions'] = []
    failing['steps'].append(second)
    before = len(calls); result = create_run('assertion-failure-stop', failing, 'FAILED')
    check('stop/steps', len(result['steps']), 1); check('stop/calls', len(calls) - before, 1)
    failing['execution']['continueAfterFailure'] = True
    before = len(calls); result = create_run('assertion-failure-continue', failing, 'FAILED')
    check('continue/steps', len(result['steps']), 2); check('continue/calls', len(calls) - before, 2)
    negative = copy.deepcopy(scenario); negative['steps'][0]['moves'][0]['path'] = mock_url + '/unavailable'
    negative['steps'][0]['moves'][0]['assertions'][0]['expectedValue'] = 503
    create_run('expected-503', negative, 'COMPLETED')
    extract = copy.deepcopy(scenario); extract['steps'][0]['moves'][0]['extracts'][0]['sourceExpression'] = '$.absent'
    result = create_run('extract-error', extract, 'ERROR')
    check('extract/move', result['steps'][0]['moves'][0]['status'], 'ERROR')
    check('greeting', request('/hello', {'name': 'Иван', 'family': 'Петров'}), (200, {'message': 'Привет из приложения "Единый тестировщик" оператору с ФИ = Иван Петров'}))
    check('greeting-invalid', request('/hello', {'name': 'Ivan', 'family': ''})[0], 400)
    check('service-greeting', request('/helloService', {'typeService': 'MDM', 'scope': 'DEV'})[0], 200)
    check('echo', request('/echo', {'hello': 'world'}), (200, {'hello': 'world'}))
    check('wsdl', request('/services/greeting.wsdl')[0], 200)
    envelope = '<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Body><helloRequest xmlns="http://cqa.ru/ws/greeting"><name>Ivan</name><family>Petrov</family></helloRequest></s:Body></s:Envelope>'
    status, body = request('/services', envelope, 'text/xml')
    check('soap-greeting', status, 200); check('soap-body', 'Ivan Petrov' in body, True)
    check('mdm-invalid', request('/mdm/saveAndMerge', {'party': [], 'updateType': 'FULL'})[0], 400)
    for kind in ['PHYSICAL', 'LEGAL']:
        fields = {'identificationLevel': 'FULL', 'createdDate': '15.03.2024', 'actualityDate': '15.03.2024 10:20:30'}
        if kind == 'PHYSICAL': fields.update(fullNameRawSource='Иван Петров', birthPlace='Казань')
        payload = {'party': [{'type': kind, 'sourceSystem': 'TEST', 'rawId': '123', 'field': [{'name': k, 'value': v} for k, v in fields.items()]}], 'updateType': 'FULL'}
        check('mdm-' + kind, request('/mdm/saveAndMerge', payload)[0], 200)
        check('mdm-forward-' + kind, calls[-1], ('/mdm', payload))
    with sqlite3.connect(work / 'journal.db') as db:
        check('journal/requests', db.execute('select count(*) from journal_request').fetchone()[0] > 0, True)
        check('journal/mdm-calls', db.execute('select count(*) from journal_call').fetchone()[0], 2)
    (work / 'results.json').write_text(json.dumps(observed, ensure_ascii=False, indent=2))
    print(json.dumps({'label': label, 'checks': len(observed), 'result': 'PASS', 'artifacts': str(work)}, ensure_ascii=False), flush=True)
finally:
    process.terminate()
    try: process.wait(timeout=10)
    except subprocess.TimeoutExpired: process.kill(); process.wait()
    mock.shutdown(); mock.server_close(); log.close()

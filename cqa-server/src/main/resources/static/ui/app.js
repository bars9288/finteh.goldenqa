/*
 * Единый тестировщик — веб-интерфейс.
 *
 * Обычный JavaScript без сборки и библиотек: файл отдаётся Spring Boot как есть,
 * интернет на стенде не нужен. Пути к API относительные от /ui/, поэтому интерфейс
 * работает и за обратным прокси с префиксом.
 *
 * Разделы:
 *   overview        — состояние МДМ и журнала, последние запросы
 *   save-and-merge  — консоль запроса: редактор, отправка, вердикт
 *   journal         — журнал запросов с фильтрами и деталями записи
 *   probes          — дежурные REST- и SOAP-ручки
 */
'use strict';

(() => {

    // ========================================================================
    //  Общее
    // ========================================================================

    const BASE = new URL('..', location.href).pathname;   // корень приложения, обычно "/"
    const LIVE_INTERVAL_MS = 5000;
    const INFO_INTERVAL_MS = 30000;

    const $ = (sel, root = document) => root.querySelector(sel);
    const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];

    const esc = (value) => String(value ?? '')
        .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;').replaceAll("'", '&#39;');

    const url = (path) => BASE + path.replace(/^\//, '');

    /** Запрос к серверу: ответ любым статусом — результат, а не исключение. */
    async function call(path, { method = 'GET', headers = {}, body } = {}) {
        const started = performance.now();
        let response;
        try {
            response = await fetch(url(path), { method, headers, body, cache: 'no-store' });
        } catch (e) {
            return { ok: false, status: 0, networkError: e.message, ms: Math.round(performance.now() - started) };
        }
        const text = await response.text();
        let json = null;
        try { json = text ? JSON.parse(text) : null; } catch { /* не JSON — оставляем текст */ }
        return {
            ok: response.ok,
            status: response.status,
            statusText: response.statusText,
            headers: Object.fromEntries(response.headers.entries()),
            contentType: response.headers.get('content-type') || '',
            text, json,
            ms: Math.round(performance.now() - started),
        };
    }

    async function getJson(path) {
        const r = await call(path);
        if (!r.ok) throw new Error(r.networkError || `HTTP ${r.status}`);
        return r.json;
    }

    let toastTimer;
    function toast(message) {
        const el = $('#toast');
        el.textContent = message;
        el.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => { el.hidden = true; }, 2600);
    }

    async function copy(text, done = 'Скопировано') {
        try {
            await navigator.clipboard.writeText(text);
            toast(done);
        } catch {
            // http без localhost: Clipboard API недоступен — запасной путь
            const ta = document.createElement('textarea');
            ta.value = text;
            document.body.append(ta);
            ta.select();
            document.execCommand('copy');
            ta.remove();
            toast(done);
        }
    }

    /** Время журнала уже московское со смещением — показываем как есть, без пересчёта в пояс браузера. */
    function fmtTime(iso, withDate = true) {
        if (!iso) return '—';
        const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}:\d{2}:\d{2})/.exec(iso);
        if (!m) return iso;
        return withDate ? `${m[3]}.${m[2]}.${m[1]} ${m[4]}` : m[4];
    }

    function fmtDay(iso) {
        const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso || '');
        return m ? `${m[3]}.${m[2]}` : '';
    }

    function fmtMs(ms) {
        if (ms == null) return '—';
        return ms < 1000 ? `${ms} мс` : `${(ms / 1000).toFixed(ms < 10000 ? 2 : 1)} с`;
    }

    function plural(n, one, few, many) {
        const n10 = n % 10, n100 = n % 100;
        if (n10 === 1 && n100 !== 11) return one;
        if (n10 >= 2 && n10 <= 4 && (n100 < 12 || n100 > 14)) return few;
        return many;
    }

    // ------------------------------------------------------------------------
    //  Отображение тел: JSON и XML с отступами и подсветкой
    // ------------------------------------------------------------------------

    function highlightJson(text) {
        return esc(text).replace(
            /(&quot;(?:[^&]|&(?!quot;))*?&quot;)(\s*:)?|\b(true|false|null)\b|(-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)/g,
            (m, str, colon, lit, num) => {
                if (str) return colon ? `<span class="tok-key">${str}</span>${colon}` : `<span class="tok-str">${str}</span>`;
                if (lit) return `<span class="tok-lit">${lit}</span>`;
                return `<span class="tok-num">${num}</span>`;
            });
    }

    /** Отступы по вложенности; {@code <name>Иван</name>} и пустой элемент — одной строкой. */
    function prettyXml(text) {
        const parts = text.replace(/>\s+</g, '><').match(/<[^>]+>|[^<]+/g) || [];
        const pad = (n) => '  '.repeat(Math.max(n, 0));
        const out = [];
        let depth = 0;
        for (let k = 0; k < parts.length; k++) {
            const t = parts[k];
            if (t.startsWith('</')) {
                depth--;
                out.push(pad(depth) + t);
            } else if (t.startsWith('<')) {
                const leaf = /\/>$/.test(t) || /^<[?!]/.test(t);
                const next = parts[k + 1];
                if (!leaf && next && !next.startsWith('<') && parts[k + 2]?.startsWith('</')) {
                    out.push(pad(depth) + t + next + parts[k + 2]);
                    k += 2;
                } else if (!leaf && next?.startsWith('</')) {
                    out.push(pad(depth) + t + next);
                    k += 1;
                } else {
                    out.push(pad(depth) + t);
                    if (!leaf) depth++;
                }
            } else if (t.trim()) {
                out.push(pad(depth) + t.trim());
            }
        }
        return out.join('\n');
    }

    function highlightXml(text) {
        return esc(text).replace(/(&lt;\/?)([\w:.-]+)/g, '$1<span class="tok-tag">$2</span>');
    }

    /** Тело для показа: JSON и XML — красиво, остальное — как есть. */
    function bodyHtml(text, kindHint) {
        if (text == null || text === '') return '<p class="hint">Тела нет.</p>';
        const trimmed = String(text).trim();
        if (kindHint !== 'BINARY' && (trimmed.startsWith('{') || trimmed.startsWith('['))) {
            try {
                return `<pre class="code-block">${highlightJson(JSON.stringify(JSON.parse(trimmed), null, 2))}</pre>`;
            } catch { /* невалидный JSON показываем как текст */ }
        }
        if (trimmed.startsWith('<')) {
            return `<pre class="code-block">${highlightXml(prettyXml(trimmed))}</pre>`;
        }
        const note = kindHint === 'BINARY' ? '<p class="hint">Двоичные данные, показаны в Base64.</p>' : '';
        return `${note}<pre class="code-block">${esc(text)}</pre>`;
    }

    /** Authorization и Cookie скрыты до явного нажатия — экран могут видеть другие. */
    const SECRET_HEADERS = ['authorization', 'proxy-authorization', 'cookie', 'set-cookie'];

    function headersHtml(headers) {
        if (!headers || !Object.keys(headers).length) return '<p class="hint">Заголовков нет.</p>';
        const rows = Object.entries(headers).map(([name, values]) => {
            const value = Array.isArray(values) ? values.join(', ') : String(values);
            const cell = SECRET_HEADERS.includes(name.toLowerCase())
                ? `<span class="secret" data-value="${esc(value)}">••••••</span> <button type="button" class="link reveal">показать</button>`
                : esc(value);
            return `<tr><th scope="row">${esc(name)}</th><td>${cell}</td></tr>`;
        });
        return `<table class="headers"><tbody>${rows.join('')}</tbody></table>`;
    }

    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.reveal');
        if (!btn) return;
        const secret = btn.previousElementSibling;
        const shown = btn.textContent === 'скрыть';
        secret.textContent = shown ? '••••••' : secret.dataset.value;
        btn.textContent = shown ? 'показать' : 'скрыть';
    });

    function validationBadge(status) {
        switch (status) {
            case 'PASSED': return '<span class="badge pass">пройдена</span>';
            case 'FAILED': return '<span class="badge fail">нарушения</span>';
            // у дежурных ручек проверки нет вовсе — бейдж был бы шумом
            default: return '<span class="hint" title="Проверка не выполнялась">—</span>';
        }
    }

    function statusBadge(status) {
        if (status == null) return '<span class="badge warn">нет ответа</span>';
        const cls = status < 300 ? 'pass' : status < 500 ? 'fail' : 'warn';
        return `<span class="badge ${cls}">${status}</span>`;
    }

    // ========================================================================
    //  Состояние приложения: полоса статуса
    // ========================================================================

    let info = null;

    async function loadInfo() {
        try {
            info = await getJson('api/info');
        } catch (e) {
            $('#sb-mdm').innerHTML = `<span class="dot bad"></span>Сервер не отвечает: ${esc(e.message)}`;
            $('#sb-journal').textContent = '';
            return;
        }
        const mdm = info.mdm || {};
        $('#sb-mdm').innerHTML = mdm.url
            ? `<span class="dot ok"></span>МДМ ${esc(mdm.scope)}: ${esc(mdm.url)}`
            : `<span class="dot warn"></span>МДМ ${esc(mdm.scope || '')}: адрес не задан`;
        $('#sb-journal').innerHTML = info.journalReady
            ? '<span class="dot ok"></span>Журнал в БД'
            : '<span class="dot bad"></span>Журнал в резервном файле — БД недоступна';
        $('#sb-version').textContent = `${info.application} ${info.version}`;
        $('#sam-path').textContent = info.saveAndMergePath;
    }

    // ========================================================================
    //  Маршрутизация: #/раздел[/id]
    // ========================================================================

    const views = {};

    function route() {
        const [, name = 'overview', arg] = location.hash.match(/^#\/([\w-]+)(?:\/(.+))?/) || [];
        const view = views[name] ? name : 'overview';
        for (const section of $$('.view')) section.hidden = section.dataset.view !== view;
        for (const link of $$('.rail > a')) {
            if (link.dataset.route === view) link.setAttribute('aria-current', 'page');
            else link.removeAttribute('aria-current');
        }
        for (const [key, v] of Object.entries(views)) if (key !== view) v.leave?.();
        views[view].enter?.(arg && decodeURIComponent(arg));
    }

    // ========================================================================
    //  Обзор
    // ========================================================================

    views.overview = {
        async enter() {
            await loadInfo();
            const mdm = info?.mdm || {};
            $('#ov-mdm').innerHTML = `
                <dt>Контур</dt><dd>${esc(mdm.scope || '—')}</dd>
                <dt>Адрес</dt><dd>${mdm.url ? `<code>${esc(mdm.url)}</code>` : '<span class="badge warn">не задан</span> задайте CDI_' + esc(mdm.scope || 'DEV') + '_URL в .env'}</dd>
                <dt>Ручка</dt><dd><code>${esc(mdm.saveAndMergePath || '—')}</code></dd>
                <dt>Тип адаптера</dt><dd>${esc(mdm.type || '—')}</dd>
                <dt>Принимаем на</dt><dd><code>${esc(info?.saveAndMergePath || '—')}</code></dd>`;

            try {
                const s = await getJson('api/journal/stats');
                $('#ov-stats').innerHTML = s.ready ? `
                    <div class="tally-item"><b>${s.total}</b><span>${plural(s.total, 'запрос', 'запроса', 'запросов')}</span></div>
                    <div class="tally-item pass"><b>${s.passed}</b><span>прошли проверку</span></div>
                    <div class="tally-item fail"><b>${s.failed}</b><span>с нарушениями</span></div>
                    <div class="tally-item warn"><b>${s.serverErrors}</b><span>ответов 5xx</span></div>
                    <div class="tally-item warn"><b>${s.failedCalls}</b><span>МДМ не ответила</span></div>
                    <div class="tally-item"><b>${s.lastReceivedAt ? esc(fmtTime(s.lastReceivedAt, false)) : '—'}</b><span>последний запрос${s.lastReceivedAt ? ', ' + esc(fmtDay(s.lastReceivedAt)) : ''}</span></div>`
                    : '<p class="hint">БД журнала недоступна: записи идут в резервный файл logs/&lt;дата&gt;-journal.log. Причина — в логе приложения при старте.</p>';
            } catch (e) {
                $('#ov-stats').innerHTML = `<p class="hint">Сводку получить не удалось: ${esc(e.message)}</p>`;
            }

            try {
                const page = await getJson('api/journal?limit=8');
                $('#ov-recent').innerHTML = journalTable(page.items, { compact: true });
            } catch (e) {
                $('#ov-recent').innerHTML = `<p class="hint">${esc(e.message)}</p>`;
            }
        },
    };

    $('#ov-recent').addEventListener('click', (e) => {
        const row = e.target.closest('tr[data-id]');
        if (row) location.hash = `#/journal/${row.dataset.id}`;
    });

    // ========================================================================
    //  saveAndMerge: редактор и вердикт
    // ========================================================================

    const TEMPLATES = {
        valid: {
            party: [{
                type: 'PHYSICAL',
                sourceSystem: 'SBL',
                rawId: '123456789abcdef',
                field: [
                    { name: 'identificationLevel', value: 'FULL' },
                    { name: 'createdDate', value: '01.10.2026' },
                    { name: 'actualityDate', value: '01.10.2026 12:00:00 МСК' },
                    { name: 'surname', value: 'Иванов' },
                    { name: 'name', value: 'Иван' },
                    { name: 'patronymic', value: 'Иванович' },
                    { name: 'birthdateRawSource', value: '15.05.1985' },
                    { name: 'genderRawSource', value: 'MALE' },
                    { name: 'birthPlace', value: 'г. Казань' },
                    { name: 'citizenship', value: '643' },
                    { name: 'innRawSource', value: '165012345678' },
                    { name: 'snilsRawSource', value: '123-456-789 01' },
                    { name: 'vip', value: 'FALSE' },
                ],
                attribute: [
                    {
                        type: 'DOCUMENT_PASSPORT', rawId: 'DOC-1', field: [
                            { name: 'type', value: 'PASSPORT' },
                            { name: 'qualifierType', value: 'RF_PASSPORT' },
                            { name: 'actualityDate', value: '01.10.2026 12:00:00' },
                        ],
                    },
                    {
                        type: 'PHONE', rawId: 'PH-1', field: [
                            { name: 'type', value: 'MOBILE' },
                            { name: 'rawSource', value: '+7 900 123-45-67' },
                            { name: 'actualityDate', value: '01.10.2026 12:00:00' },
                        ],
                    },
                    {
                        type: 'EMAIL', rawId: 'EM-1', field: [
                            { name: 'type', value: 'PERSONAL' },
                            { name: 'rawSource', value: 'ivanov@example.ru' },
                            { name: 'actualityDate', value: '01.10.2026 12:00:00' },
                        ],
                    },
                ],
            }],
            include: { partyInfo: ['PARTY', 'DOCUMENT', 'PHONE', 'EMAIL'] },
            updateType: 'FULL',
        },
        invalid: {
            party: [{
                type: 'PHYSICAL',
                sourceSystem: 'SBL',
                rawId: '123456789abcdef',
                field: [
                    { name: 'identificationLevel', value: 'FULL' },
                    { name: 'createdDate', value: '2026-10-01' },
                    { name: 'actualityDate', value: '01.10.2026 12:00:00 UTC' },
                    { name: 'surname', value: 'Иванов' },
                    { name: 'name', value: 'Иван' },
                    { name: 'fullNameRawSource', value: 'Иванов Иван Иванович' },
                    { name: 'birthdate', value: '15.05.1985' },
                    { name: 'gender', value: 'M' },
                    { name: 'snilsRawSource', value: '12345678901' },
                    { name: 'vip', value: 'да' },
                ],
                attribute: [
                    {
                        type: 'DOCUMENT_PASSPORT', rawId: 'DOC-1', field: [
                            { name: 'type', value: 'ID_CARD' },
                            { name: 'primary', value: 'true' },
                            { name: 'actualityDate', value: '01.10.2026' },
                        ],
                    },
                    {
                        type: 'EMAIL', rawId: 'EM-1', field: [
                            { name: 'type', value: 'PERSONAL' },
                            { name: 'rawSource', value: 'ivanov.example.ru' },
                            { name: 'actualityDate', value: '01.10.2026 12:00:00' },
                        ],
                    },
                ],
            }],
            updateType: 'FULL',
        },
        minimal: {
            party: [{ type: 'PHYSICAL', sourceSystem: '', rawId: '', field: [], attribute: [] }],
            updateType: 'FULL',
        },
    };

    const DRAFT_KEY = 'cqa.saveAndMerge.draft';
    const editor = $('#sam-body');
    const gutter = $('#sam-gutter');
    const editorWrap = $('#sam-editor-wrap');

    function storage(action, value) {
        // localStorage может быть недоступен (приватный режим, запрет сайта) — интерфейс работает и без него
        try {
            if (action === 'get') return localStorage.getItem(DRAFT_KEY);
            localStorage.setItem(DRAFT_KEY, value);
        } catch { return null; }
        return null;
    }

    function setEditor(text) {
        editor.value = text;
        onEditorInput();
    }

    function renderGutter() {
        const lines = editor.value.split('\n').length;
        let out = '';
        for (let i = 1; i <= lines; i++) out += i + '\n';
        gutter.textContent = out;
        gutter.scrollTop = editor.scrollTop;
    }

    let parseTimer;
    function onEditorInput() {
        renderGutter();
        storage('set', editor.value);
        clearTimeout(parseTimer);
        parseTimer = setTimeout(checkParse, 250);
    }

    function checkParse() {
        const hint = $('#sam-parse');
        if (!editor.value.trim()) {
            editorWrap.classList.remove('invalid');
            hint.textContent = 'Тело пустое — сервер ответит 400.';
            return;
        }
        try {
            JSON.parse(editor.value);
            editorWrap.classList.remove('invalid');
            hint.textContent = '';
        } catch (e) {
            editorWrap.classList.add('invalid');
            const pos = jsonErrorOffset(e, editor.value);
            const line = pos == null ? null : editor.value.slice(0, pos).split('\n').length;
            hint.textContent = `JSON не разбирается${line ? `, строка ${line}` : ''}: ${e.message}. Отправить можно — для негативной проверки.`;
        }
    }

    /** Позиция ошибки из сообщения JSON.parse: у Chrome «position N», у Firefox «line L column C». */
    function jsonErrorOffset(error, text) {
        let m = /position (\d+)/.exec(error.message);
        if (m) return Number(m[1]);
        m = /line (\d+) column (\d+)/.exec(error.message);
        if (m) {
            const lines = text.split('\n');
            let offset = 0;
            for (let i = 0; i < Number(m[1]) - 1; i++) offset += lines[i].length + 1;
            return offset + Number(m[2]) - 1;
        }
        return null;
    }

    editor.addEventListener('input', onEditorInput);
    editor.addEventListener('scroll', () => { gutter.scrollTop = editor.scrollTop; });
    editor.addEventListener('keydown', (e) => {
        // Tab — два пробела, а не уход фокуса; Esc отпускает фокус для клавиатурной навигации
        if (e.key === 'Tab' && !e.shiftKey && !e.ctrlKey && !e.altKey) {
            e.preventDefault();
            editor.setRangeText('  ', editor.selectionStart, editor.selectionEnd, 'end');
            onEditorInput();
        } else if (e.key === 'Escape') {
            editor.blur();
        } else if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
            e.preventDefault();
            sendSaveAndMerge();
        }
    });

    $('#sam-load').addEventListener('click', () => {
        setEditor(JSON.stringify(TEMPLATES[$('#sam-template').value], null, 2));
        editor.focus();
    });

    $('#sam-format').addEventListener('click', () => {
        try {
            setEditor(JSON.stringify(JSON.parse(editor.value), null, 2));
        } catch {
            toast('Выровнять нельзя: JSON не разбирается');
        }
    });

    // ------------------------------------------------------------------------
    //  Карта путей JSON → место в тексте. Ошибки сервера адресуют поля так:
    //    party[0].rawId, party[0].field.createdDate, party[0].attribute[1].field.rawSource
    //  — то есть элементы списков field ищутся по значению name.
    // ------------------------------------------------------------------------

    function mapJsonPaths(text) {
        const index = new Map();
        let i = 0;

        const ws = () => { while (i < text.length && /\s/.test(text[i])) i++; };
        const fail = () => { throw new Error('bad json'); };

        function str() {
            const start = i++;
            while (i < text.length && text[i] !== '"') i += text[i] === '\\' ? 2 : 1;
            i++;
            return JSON.parse(text.slice(start, i));
        }

        function value(path) {
            ws();
            const start = i;
            const c = text[i];
            let node;
            if (c === '{') {
                i++;
                node = { type: 'object', props: {} };
                ws();
                if (text[i] === '}') i++;
                else for (;;) {
                    ws();
                    if (text[i] !== '"') fail();
                    const keyStart = i;
                    const key = str();
                    ws();
                    if (text[i++] !== ':') fail();
                    const child = value(path ? `${path}.${key}` : key);
                    child.keyStart = keyStart;
                    node.props[key] = child;
                    ws();
                    if (text[i] === ',') { i++; continue; }
                    if (text[i] === '}') { i++; break; }
                    fail();
                }
            } else if (c === '[') {
                i++;
                node = { type: 'array', items: [] };
                ws();
                if (text[i] === ']') i++;
                else for (let n = 0; ; n++) {
                    node.items.push(value(`${path}[${n}]`));
                    ws();
                    if (text[i] === ',') { i++; continue; }
                    if (text[i] === ']') { i++; break; }
                    fail();
                }
            } else if (c === '"') {
                node = { type: 'string', value: str() };
            } else {
                const m = /^(-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?|true|false|null)/.exec(text.slice(i));
                if (!m) fail();
                i += m[0].length;
                node = { type: 'scalar' };
            }
            node.start = start;
            node.end = i;
            index.set(path, node);
            // «field.<имя>» — элемент списка полей с таким name
            if (node.type === 'array' && /(^|\.)field$/.test(path)) {
                for (const item of node.items) {
                    const name = item.props?.name;
                    if (name?.type === 'string') {
                        const alias = `${path}.${name.value}`;
                        if (!index.has(alias)) index.set(alias, item);
                    }
                }
            }
            return node;
        }

        value('');
        return index;
    }

    /** Выделить в редакторе место, о котором говорит ошибка; если поля нет — ближайшего родителя. */
    function locate(path) {
        let index;
        try {
            index = mapJsonPaths(editor.value);
        } catch {
            toast('JSON в редакторе не разбирается — найти поле нельзя');
            return;
        }
        let p = path.replace(/\[i\]/g, '');
        let node = index.get(p);
        let exact = true;
        while (!node && p) {
            exact = false;
            // шаг к родителю: отрезать «.имя» или «[n]»; у пути верхнего уровня родитель — само тело
            const parent = p.replace(/(\.[^.[\]]+|\[\d+\])$/, '');
            p = parent === p ? '' : parent;
            node = index.get(p);
        }
        if (!node) node = index.get('');
        const start = node.keyStart ?? node.start;
        editor.focus({ preventScroll: true });
        editor.setSelectionRange(start, node.end);
        const line = editor.value.slice(0, start).split('\n').length;
        editor.scrollTop = Math.max(0, (line - 4) * 20);
        editorWrap.classList.remove('flash');
        void editorWrap.offsetWidth;
        editorWrap.classList.add('flash');
        if (!exact) toast(`Поля в запросе нет — выделено «${p || 'тело'}», куда его добавить`);
    }

    // ------------------------------------------------------------------------
    //  Отправка и вердикт
    // ------------------------------------------------------------------------

    function authHeader() {
        const user = $('#sam-user').value;
        const password = $('#sam-password').value;
        if (!user && !password) return null;
        const bytes = new TextEncoder().encode(`${user}:${password}`);
        return 'Basic ' + btoa(String.fromCharCode(...bytes));
    }

    function requestHeaders() {
        const headers = { 'Content-Type': 'application/json; charset=UTF-8', Accept: 'application/json' };
        const auth = authHeader();
        if (auth) headers.Authorization = auth;
        return headers;
    }

    async function sendSaveAndMerge() {
        if (!info) await loadInfo();
        if (!info) return;
        const button = $('#sam-send');
        button.disabled = true;
        button.textContent = 'Отправляю…';
        const r = await call(info.saveAndMergePath, { method: 'POST', headers: requestHeaders(), body: editor.value });
        button.disabled = false;
        button.textContent = 'Отправить запрос';
        renderVerdict(r);
    }

    $('#sam-send').addEventListener('click', sendSaveAndMerge);

    $('#sam-curl').addEventListener('click', () => {
        const target = new URL(url(info?.saveAndMergePath || ''), location.origin).href;
        const user = $('#sam-user').value;
        const password = $('#sam-password').value;
        const q = (s) => `'${s.replaceAll("'", "'\\''")}'`;
        const parts = [`curl -i -X POST ${q(target)}`, `-H ${q('Content-Type: application/json; charset=UTF-8')}`];
        if (user || password) parts.push(`-u ${q(`${user}:${password}`)}`);
        parts.push(`--data-binary ${q(editor.value)}`);
        copy(parts.join(' \\\n  '), 'Команда curl скопирована');
    });

    /** Ответ нашей проверки: {"message": "Ошибка", "errors": [...]} — так его и отличаем от ответа МДМ. */
    const isOurVerdict = (json) => json && typeof json === 'object' && Array.isArray(json.errors) && 'message' in json;

    function renderVerdict(r) {
        const box = $('#sam-verdict');
        const meta = `<p class="verdict-meta"><span>HTTP ${r.status || '—'}</span><span>${fmtMs(r.ms)}</span>
            <span><button type="button" class="link" data-open-last>Открыть в журнале</button></span></p>`;

        if (r.status === 0) {
            box.dataset.state = 'warn';
            box.innerHTML = `<div class="verdict-head"><p class="verdict-title">Тестировщик не ответил</p>
                <p class="verdict-meta">${esc(r.networkError)}</p></div>
                <div class="verdict-body"><p>Проверьте, что приложение запущено и доступно с этого компьютера.</p></div>`;
            return;
        }

        if (r.status === 400 && isOurVerdict(r.json)) {
            const errors = r.json.errors;
            box.dataset.state = 'fail';
            box.innerHTML = `<div class="verdict-head">
                    <p class="verdict-title">${errors.length} ${plural(errors.length, 'нарушение', 'нарушения', 'нарушений')} — в МДМ не отправлено</p>
                    ${meta}
                </div>
                <ol class="violations">${errors.map(violationHtml).join('')}</ol>`;
            return;
        }

        if (r.status === 400 && r.json?.detail) {
            box.dataset.state = 'fail';
            box.innerHTML = `<div class="verdict-head"><p class="verdict-title">Запрос не разобран — в МДМ не отправлено</p>${meta}</div>
                <div class="verdict-body"><p>${esc(r.json.detail)}</p></div>`;
            return;
        }

        if (isOurVerdict(r.json) && r.status >= 500) {
            box.dataset.state = 'warn';
            const title = r.status === 502 ? 'Проверка пройдена, но МДМ не ответила' : 'Проверка пройдена, но МДМ не настроена';
            box.innerHTML = `<div class="verdict-head"><p class="verdict-title">${title}</p>${meta}</div>
                <div class="verdict-body"><ul class="err-list">${r.json.errors.map((e) => `<li>${esc(e)}</li>`).join('')}</ul>
                ${r.status === 500 ? '<p class="hint">Адрес и таймауты МДМ берутся из connections.yml, сам адрес — из переменной CDI_&lt;контур&gt;_URL (.env).</p>' : ''}</div>`;
            return;
        }

        const pass = r.status >= 200 && r.status < 300;
        box.dataset.state = pass ? 'pass' : 'warn';
        box.innerHTML = `<div class="verdict-head">
                <p class="verdict-title">${pass ? 'Проверка пройдена, МДМ приняла запрос' : `Проверка пройдена, МДМ ответила ошибкой ${r.status}`}</p>
                ${meta}
            </div>
            <div class="verdict-body">
                <h3>Ответ МДМ</h3>
                ${bodyHtml(r.text)}
                <h3>Заголовки ответа</h3>
                ${headersHtml(r.headers)}
            </div>`;
    }

    function violationHtml(error) {
        // «путь: сообщение»; у общих правил пути может не быть
        const m = /^([\w.[\]]+?)(?:\.\s|:\s)(.*)$/s.exec(error);
        if (!m) return `<li><span class="path static"></span><span class="msg">${esc(error)}</span></li>`;
        return `<li><button type="button" class="link path" data-path="${esc(m[1])}" title="Показать в запросе">${esc(m[1])}</button>
                <span class="msg">${esc(m[2])}</span></li>`;
    }

    $('#sam-verdict').addEventListener('click', async (e) => {
        const path = e.target.closest('[data-path]');
        if (path) { locate(path.dataset.path); return; }
        if (e.target.closest('[data-open-last]')) {
            // ответ не несёт id записи журнала — берём самую свежую по этой ручке
            try {
                const page = await getJson(`api/journal?limit=1&endpoint=${encodeURIComponent('saveAndMerge')}`);
                if (page.items[0]) location.hash = `#/journal/${page.items[0].id}`;
                else toast('В журнале пока нет записей');
            } catch (err) {
                toast(`Журнал недоступен: ${err.message}`);
            }
        }
    });

    views['save-and-merge'] = {
        enter() {
            if (!info) loadInfo();
            if (!editor.value) {
                const draft = storage('get');
                setEditor(draft || JSON.stringify(TEMPLATES.valid, null, 2));
            }
            renderGutter();
        },
    };

    /** Из журнала: подставить тело записи в консоль и повторить. */
    function openInConsole(body) {
        let text = body;
        try { text = JSON.stringify(JSON.parse(body), null, 2); } catch { /* как есть */ }
        location.hash = '#/save-and-merge';
        setEditor(text);
        $('#sam-verdict').dataset.state = 'idle';
        $('#sam-verdict').innerHTML = '<p class="verdict-idle">Тело подставлено из журнала. Учётные данные из журнала не берутся — введите их, если МДМ их требует.</p>';
    }

    // ========================================================================
    //  Журнал
    // ========================================================================

    const journalState = { offset: 0, limit: 50, selected: null, timer: null, total: 0 };
    const filtersForm = $('#jr-filters');

    function journalTable(items, { compact = false } = {}) {
        if (!items.length) {
            return compact
                ? '<p class="empty">Запросов ещё не было. Отправьте первый из раздела <a href="#/save-and-merge">saveAndMerge</a>.</p>'
                : '<p class="empty">Под условия фильтра ничего не подходит.</p>';
        }
        const rows = items.map((it) => `
            <tr data-id="${esc(it.id)}" tabindex="0" ${it.id === journalState.selected ? 'aria-selected="true"' : ''}>
                <td class="when">${esc(fmtTime(it.receivedAt, false))}<small>${esc(fmtDay(it.receivedAt))}</small></td>
                <td class="ep" title="${esc(it.endpoint)}"><span class="method">${esc(it.httpMethod)}</span>${esc(shortEndpoint(it.endpoint))}${it.queryString ? `<span class="qs">?${esc(decodeQuery(it.queryString))}</span>` : ''}</td>
                <td>${validationBadge(it.validationStatus)}</td>
                <td>${statusBadge(it.respStatus)}${it.hasInternalError ? ' <span class="badge warn" title="Ошибка самого тестировщика">сбой</span>' : ''}</td>
                <td class="hide-sm hide-detail">${it.calls ? (it.lastCallError ? '<span class="badge warn">нет ответа</span>' : statusBadge(it.lastCallStatus)) : '<span class="hint">—</span>'}</td>
                <td class="num hide-sm hide-detail">${esc(fmtMs(it.durationMs))}</td>
            </tr>`).join('');
        return `<table class="jr-table">
            <thead><tr><th>Время</th><th>Запрос</th><th>Проверка</th><th>Ответ</th><th class="hide-sm hide-detail">МДМ</th><th class="num hide-sm hide-detail">Длительность</th></tr></thead>
            <tbody>${rows}</tbody></table>`;
    }

    /** Длинный путь saveAndMerge сокращаем до последней части — полный в подсказке и в деталях. */
    function shortEndpoint(endpoint) {
        const parts = endpoint.split('/').filter(Boolean);
        return parts.length > 2 ? '…/' + parts.at(-1) : endpoint;
    }

    /** name=%D0%98%D0%B2%D0%B0%D0%BD → name=Иван; битую кодировку показываем как есть. */
    function decodeQuery(query) {
        try { return decodeURIComponent(query.replace(/\+/g, ' ')); } catch { return query; }
    }

    function filterQuery() {
        const data = new FormData(filtersForm);
        const params = new URLSearchParams();
        for (const key of ['text', 'endpoint', 'validation', 'statusClass']) {
            const v = data.get(key);
            if (v) params.set(key, v);
        }
        params.set('limit', journalState.limit);
        params.set('offset', journalState.offset);
        return params.toString();
    }

    async function loadJournal() {
        try {
            const page = await getJson(`api/journal?${filterQuery()}`);
            journalState.total = page.total;
            $('#jr-table').innerHTML = journalTable(page.items);
            const from = page.total ? page.offset + 1 : 0;
            const to = page.offset + page.items.length;
            $('#jr-pager').innerHTML = page.total > page.limit || page.offset ? `
                <span>${from}–${to} из ${page.total}</span>
                <span class="actions" style="margin:0">
                    <button type="button" class="btn small" data-page="-1" ${page.offset ? '' : 'disabled'}>Новее</button>
                    <button type="button" class="btn small" data-page="1" ${to < page.total ? '' : 'disabled'}>Старее</button>
                </span>` : `<span>${page.total} ${plural(page.total, 'запись', 'записи', 'записей')}</span>`;
        } catch (e) {
            $('#jr-table').innerHTML = `<p class="empty">Журнал не загрузился: ${esc(e.message)}</p>`;
            $('#jr-pager').innerHTML = '';
        }
    }

    function scheduleLive() {
        clearInterval(journalState.timer);
        if (filtersForm.elements.live.checked) {
            journalState.timer = setInterval(() => {
                if (!document.hidden) loadJournal();
            }, LIVE_INTERVAL_MS);
        }
    }

    let filterTimer;
    filtersForm.addEventListener('input', (e) => {
        if (e.target.name === 'live') { scheduleLive(); return; }
        clearTimeout(filterTimer);
        filterTimer = setTimeout(() => { journalState.offset = 0; loadJournal(); }, 300);
    });
    filtersForm.addEventListener('submit', (e) => e.preventDefault());

    $('#jr-pager').addEventListener('click', (e) => {
        const btn = e.target.closest('[data-page]');
        if (!btn) return;
        journalState.offset = Math.max(0, journalState.offset + Number(btn.dataset.page) * journalState.limit);
        loadJournal();
    });

    function openRow(row) {
        if (row) location.hash = `#/journal/${row.dataset.id}`;
    }
    $('#jr-table').addEventListener('click', (e) => openRow(e.target.closest('tr[data-id]')));
    $('#jr-table').addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); openRow(e.target.closest('tr[data-id]')); }
    });

    async function showDetail(id) {
        const panel = $('#jr-detail');
        journalState.selected = id;
        for (const tr of $$('#jr-table tr[data-id]')) {
            if (tr.dataset.id === id) tr.setAttribute('aria-selected', 'true');
            else tr.removeAttribute('aria-selected');
        }
        $('.journal').classList.add('with-detail');
        panel.hidden = false;
        panel.innerHTML = '<p class="empty">Загружаю запись…</p>';

        const r = await call(`api/journal/${encodeURIComponent(id)}`);
        if (r.status === 404) { panel.innerHTML = '<p class="empty">Такой записи в журнале нет.</p>'; return; }
        if (!r.ok) { panel.innerHTML = `<p class="empty">Запись не загрузилась: ${esc(r.networkError || 'HTTP ' + r.status)}</p>`; return; }
        renderDetail(r.json);
    }

    function renderDetail(d) {
        const panel = $('#jr-detail');
        const isSam = /saveAndMerge/i.test(d.endpoint) && d.reqBody;
        const tabs = [
            ['request', 'Запрос'],
            ['response', 'Ответ клиенту'],
            ['calls', `Вызовы МДМ${d.calls.length ? ` (${d.calls.length})` : ''}`],
            ['check', 'Проверка'],
        ];
        const content = {
            request: `
                <dl class="facts">
                    <dt>Адрес</dt><dd><code>${esc(d.httpMethod)} ${esc(d.endpoint)}${d.queryString ? '?' + esc(decodeQuery(d.queryString)) : ''}</code></dd>
                    <dt>Клиент</dt><dd>${esc(d.clientIp || '—')}${d.clientPort ? ':' + esc(d.clientPort) : ''}</dd>
                    <dt>Получен</dt><dd>${esc(fmtTime(d.receivedAt))}</dd>
                    <dt>Тип тела</dt><dd>${esc(d.reqBodyKind)}${d.reqContentType ? `, <code>${esc(d.reqContentType)}</code>` : ''}</dd>
                </dl>
                <h3>Тело</h3>${bodyHtml(d.reqBody, d.reqBodyKind)}
                <h3>Заголовки</h3>${headersHtml(d.reqHeaders)}`,
            response: `
                <dl class="facts">
                    <dt>Статус</dt><dd>${statusBadge(d.respStatus)}</dd>
                    <dt>Отправлен</dt><dd>${esc(fmtTime(d.finishedAt))}</dd>
                    <dt>Доставлен</dt><dd>${d.delivered == null ? '—' : d.delivered ? 'да' : `<span class="call-error">нет: ${esc(d.deliveryError)}</span>`}</dd>
                    ${d.internalError ? `<dt>Сбой</dt><dd class="call-error">${esc(d.internalError)}</dd>` : ''}
                </dl>
                <h3>Тело</h3>${bodyHtml(d.respBody)}
                <h3>Заголовки</h3>${headersHtml(d.respHeaders)}`,
            calls: d.calls.length ? d.calls.map((c) => `
                <div class="call">
                    <dl class="facts">
                        <dt>Сервис</dt><dd>${esc(c.service)}</dd>
                        <dt>Адрес</dt><dd><code>${esc(c.targetUrl)}</code></dd>
                        <dt>Результат</dt><dd>${c.callError ? `<span class="call-error">не ответил: ${esc(c.callError)}</span>` : statusBadge(c.respStatus)}</dd>
                        <dt>Время</dt><dd>${esc(fmtTime(c.sentAt, false))}, ${esc(fmtMs(c.durationMs))}</dd>
                    </dl>
                    <h3>Ответ сервиса</h3>${bodyHtml(c.respBody, c.respBodyKind)}
                    <h3>Ушедшие заголовки</h3>${headersHtml(c.reqHeaders)}
                    <h3>Заголовки ответа</h3>${headersHtml(c.respHeaders)}
                    <p class="hint">Тело запроса к сервису равно телу входящего запроса — прокси пересылает его байт в байт.</p>
                </div>`).join('')
                : `<p class="hint">${d.validationStatus === 'FAILED'
                    ? 'В МДМ не обращались: запрос не прошёл проверку.'
                    : 'Внешние сервисы не вызывались.'}</p>`,
            check: d.validationStatus === 'FAILED'
                ? `<p>${validationBadge(d.validationStatus)}</p><ol class="err-list">${(d.validationErrors || []).map((e) => `<li>${esc(e)}</li>`).join('')}</ol>`
                : `<p>${validationBadge(d.validationStatus)}</p><p class="hint">${d.validationStatus === 'PASSED'
                    ? 'Нарушений нет.'
                    : 'Проверка не выполнялась: у этой ручки её нет, или запрос упал раньше — например, тело не JSON.'}</p>`,
        };

        panel.innerHTML = `
            <div class="jd-head">
                <div>
                    <h2>${esc(d.id)}</h2>
                    <p>${esc(fmtTime(d.receivedAt))} · ${esc(fmtMs(d.durationMs))}</p>
                </div>
                <button type="button" class="btn small" data-close aria-label="Закрыть запись">Закрыть</button>
            </div>
            <div class="tabs" role="tablist">${tabs.map(([key, label], n) =>
                `<button type="button" role="tab" data-tab="${key}" aria-selected="${n === 0}">${esc(label)}</button>`).join('')}</div>
            <div class="jd-body">
                <div class="jd-actions">
                    ${isSam ? '<button type="button" class="btn small" data-replay>Открыть в консоли saveAndMerge</button>' : ''}
                    <button type="button" class="btn small" data-copy-id>Скопировать id</button>
                </div>
                <div data-tab-body></div>
            </div>`;

        const showTab = (key) => {
            for (const b of $$('[data-tab]', panel)) b.setAttribute('aria-selected', String(b.dataset.tab === key));
            $('[data-tab-body]', panel).innerHTML = content[key];
        };
        showTab(d.validationStatus === 'FAILED' ? 'check' : 'request');

        panel.onclick = (e) => {
            const tab = e.target.closest('[data-tab]');
            if (tab) showTab(tab.dataset.tab);
            if (e.target.closest('[data-close]')) location.hash = '#/journal';
            if (e.target.closest('[data-replay]')) openInConsole(d.reqBody);
            if (e.target.closest('[data-copy-id]')) copy(d.id, 'id записи скопирован');
        };
    }

    function hideDetail() {
        journalState.selected = null;
        $('#jr-detail').hidden = true;
        $('.journal').classList.remove('with-detail');
        for (const tr of $$('#jr-table tr[aria-selected]')) tr.removeAttribute('aria-selected');
    }

    views.journal = {
        enter(id) {
            loadJournal();
            scheduleLive();
            if (id) showDetail(id);
            else hideDetail();
        },
        leave() {
            clearInterval(journalState.timer);
        },
    };

    // ========================================================================
    //  Проверка связи
    // ========================================================================

    const ENVELOPES = {
        hello: `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <helloRequest xmlns="http://cqa.ru/ws/greeting">
      <name>Сергей</name>
      <family>Борискин</family>
    </helloRequest>
  </soap:Body>
</soap:Envelope>`,
        service: `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <serviceHelloRequest xmlns="http://cqa.ru/ws/greeting">
      <typeService>KAFKA-CLIENT</typeService>
      <scope>DEV</scope>
    </serviceHelloRequest>
  </soap:Body>
</soap:Envelope>`,
        echo: `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <echoRequest xmlns="http://cqa.ru/ws/echo">
      <message>эхо, проверка связи</message>
    </echoRequest>
  </soap:Body>
</soap:Envelope>`,
        fault: `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <helloRequest xmlns="http://cqa.ru/ws/greeting">
      <name></name>
      <family>Борискин</family>
    </helloRequest>
  </soap:Body>
</soap:Envelope>`,
    };

    const soapForm = $('[data-probe="soap"]');
    const fillEnvelope = () => { soapForm.elements.body.value = ENVELOPES[soapForm.elements.envelope.value]; };
    soapForm.elements.envelope.addEventListener('change', fillEnvelope);
    fillEnvelope();

    function probeResult(form, r, label) {
        const out = $('.probe-out', form);
        if (r.status === 0) {
            out.innerHTML = `<p class="probe-status">${statusBadge(null)} ${esc(r.networkError)}</p>`;
            return;
        }
        out.innerHTML = `<p class="probe-status">${statusBadge(r.status)} ${esc(label)} · ${esc(fmtMs(r.ms))}</p>${bodyHtml(r.text)}`;
    }

    const probes = {
        async hello(form, method) {
            const name = form.elements.name.value;
            const family = form.elements.family.value;
            return method === 'GET'
                ? [await call(`helloAkBars?${new URLSearchParams({ name, family })}`), 'GET /helloAkBars']
                : [await call('helloAkBars', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name, family }) }), 'POST /helloAkBars'];
        },
        async service(form, method) {
            const typeService = form.elements.typeService.value;
            const scope = form.elements.scope.value;
            return method === 'GET'
                ? [await call(`helloService?${new URLSearchParams({ typeService, scope })}`), 'GET /helloService']
                : [await call('helloService', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ typeService, scope }) }), 'POST /helloService'];
        },
        async echo(form, mode) {
            const path = mode === 'details' ? 'echo/details' : 'echo';
            const query = form.elements.query.value.trim().replace(/^\?/, '');
            const target = query ? `${path}?${query}` : path;
            return [await call(target, { method: 'POST', headers: { 'Content-Type': form.elements.contentType.value }, body: form.elements.body.value }), `POST /${target}`];
        },
        async soap(form) {
            // SOAP 1.1 принимает только text/xml: с application/xml сервер ответит 500 без пояснений
            return [await call('services', { method: 'POST', headers: { 'Content-Type': 'text/xml; charset=UTF-8' }, body: form.elements.body.value }), 'POST /services'];
        },
    };

    for (const form of $$('.probe')) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const submitter = e.submitter;
            const value = submitter?.value;
            if (submitter) submitter.disabled = true;
            const [r, label] = await probes[form.dataset.probe](form, value);
            if (submitter) submitter.disabled = false;
            probeResult(form, r, label);
        });
    }

    views.probes = {};

    // ========================================================================
    //  Старт
    // ========================================================================

    window.addEventListener('hashchange', route);
    if (!location.hash) history.replaceState(null, '', '#/overview');
    loadInfo().then(route);
    setInterval(() => { if (!document.hidden) loadInfo(); }, INFO_INTERVAL_MS);
})();

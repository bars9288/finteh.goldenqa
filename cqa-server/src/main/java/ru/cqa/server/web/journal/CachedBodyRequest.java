package ru.cqa.server.web.journal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Запрос, тело которого уже прочитано целиком и отдаётся из памяти.
 *
 * <p>Поток тела у servlet-запроса читается один раз. Журналу тело нужно до
 * обработки, контроллеру — во время неё, поэтому {@link JournalFilter} читает
 * его сам, а дальше по цепочке передаёт эту обёртку: каждый вызов
 * {@link #getInputStream()} отдаёт тело с начала.
 *
 * <p>Ограничение: параметры формы ({@code application/x-www-form-urlencoded})
 * в теле Tomcat разбирает из исходного потока, который к этому моменту уже прочитан, —
 * через {@code getParameter()} они не видны. REST-ручки cqa формы не принимают.
 */
class CachedBodyRequest extends HttpServletRequestWrapper {

    private final byte[] body;

    CachedBodyRequest(HttpServletRequest request, byte[] body) {
        super(request);
        this.body = body;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream input = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() {
                return input.read();
            }

            @Override
            public int read(byte[] buffer, int offset, int length) {
                return input.read(buffer, offset, length);
            }

            @Override
            public boolean isFinished() {
                return input.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            /** Неблокирующее чтение не нужно: тело уже в памяти. */
            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /** Кодировка — из запроса; если её нет, ISO-8859-1, как требует спецификация Servlet. */
    @Override
    public BufferedReader getReader() {
        String encoding = getCharacterEncoding();
        Charset charset = encoding != null ? Charset.forName(encoding) : StandardCharsets.ISO_8859_1;
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
}

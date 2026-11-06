package ru.cqa.server.db.sqlite.journal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Тело HTTP-сообщения в виде для журнала: тип и текст.
 *
 * <p>Тип определяется по содержимому, а не по Content-Type: тестировщику
 * присылают и JSON с {@code text/decompressedBody}, и мусор с {@code application/json}.
 * Content-Type используется только чтобы узнать кодировку.
 *
 * <p>Сжатое тело ({@code Content-Encoding: gzip} или {@code deflate}) для журнала
 * распаковывается — иначе ответ МДМ на запрос с {@code Accept-Encoding: gzip}
 * (так шлёт, например, Postman) лёг бы в БД нечитаемым BINARY. Распаковывается
 * только копия: клиенту и МДМ байты уходят как есть. Что тело было сжато, видно
 * по заголовку Content-Encoding в той же записи журнала.
 *
 * @param kind тип тела
 * @param text текст (распакованный, если был сжат); для {@link BodyKind#BINARY} —
 *             исходные байты в Base64; для {@link BodyKind#EMPTY} — null
 */
record JournalBody(BodyKind kind, String text) {

    static final JournalBody EMPTY = new JournalBody(BodyKind.EMPTY, null);

    /**
     * Предел распакованного тела. Защита от «zip-бомбы»: несколько килобайт gzip
     * могут развернуться в гигабайты. Больше предела — тело остаётся BINARY.
     */
    private static final int MAX_DECOMPRESSED_BODY_BYTES = 16 * 1024 * 1024;

    static JournalBody of(byte[] body, HttpHeaders headers, JsonMapper jsonMapper) {
        if (body == null || body.length == 0) {
            return EMPTY;
        }
        byte[] decompressedBody = decompress(body, headers.get(HttpHeaders.CONTENT_ENCODING));
        if (decompressedBody == null) {
            return binary(body);
        }
        String text;
        try {
            // REPORT: байты, недопустимые в кодировке, — ошибка, а не тихая замена на «�»
            text = charsetOf(headers.getFirst(HttpHeaders.CONTENT_TYPE)).newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(decompressedBody))
                    .toString();
        } catch (CharacterCodingException exception) {
            return binary(body);
        }
        return new JournalBody(kindOf(text, jsonMapper), text);
    }

    private static JournalBody binary(byte[] body) {
        return new JournalBody(BodyKind.BINARY, Base64.getEncoder().encodeToString(body));
    }

    /**
     * Снимает сжатие по Content-Encoding. Кодировок может быть несколько
     * ({@code gzip, identity}) — они снимаются в обратном порядке, как предписывает HTTP.
     *
     * @return распакованные байты; те же байты, если сжатия нет; null — распаковать
     *         не удалось (br и прочие неизвестные кодировки, битые данные, больше предела)
     */
    private static byte[] decompress(byte[] body, List<String> contentEncoding) {
        if (contentEncoding == null) {
            return body;
        }
        List<String> codings = new ArrayList<>();
        for (String value : contentEncoding) {
            for (String coding : value.split(",")) {
                if (!coding.isBlank()) {
                    codings.add(coding.strip().toLowerCase(Locale.ROOT));
                }
            }
        }
        Collections.reverse(codings);
        byte[] result = body;
        try {
            for (String coding : codings) {
                switch (coding) {
                    case "identity":
                        break;
                    case "gzip":
                    case "x-gzip":
                        result = inflate(new GZIPInputStream(new ByteArrayInputStream(result)));
                        break;
                    case "deflate":
                        result = inflateDeflate(result);
                        break;
                    default:
                        return null;
                }
                if (result == null) {
                    return null;
                }
            }
        } catch (IOException exception) {
            return null;
        }
        return result;
    }

    /**
     * По стандарту HTTP {@code deflate} — данные в обёртке zlib, но часть серверов
     * шлёт «голый» deflate без неё. Сначала пробуем по стандарту, затем без обёртки.
     */
    private static byte[] inflateDeflate(byte[] body) throws IOException {
        try {
            return inflate(new InflaterInputStream(new ByteArrayInputStream(body)));
        } catch (IOException exception) {
            return inflate(new InflaterInputStream(new ByteArrayInputStream(body), new Inflater(true)));
        }
    }

    /** @return распакованные байты; null — больше {@link #MAX_DECOMPRESSED_BODY_BYTES} */
    private static byte[] inflate(InputStream input) throws IOException {
        try (input) {
            byte[] result = input.readNBytes(MAX_DECOMPRESSED_BODY_BYTES + 1);
            return result.length > MAX_DECOMPRESSED_BODY_BYTES ? null : result;
        }
    }

    private static BodyKind kindOf(String text, JsonMapper jsonMapper) {
        if (text.isBlank()) {
            return BodyKind.TEXT;
        }
        if (text.strip().startsWith("<")) {
            return BodyKind.XML;
        }
        try {
            jsonMapper.readTree(text);
            return BodyKind.JSON;
        } catch (JacksonException exception) {
            return BodyKind.TEXT;
        }
    }

    /** Кодировка из Content-Type; если её нет или заголовок кривой — UTF-8. */
    private static Charset charsetOf(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        try {
            Charset charset = MediaType.parseMediaType(contentType).getCharset();
            return charset != null ? charset : StandardCharsets.UTF_8;
        } catch (IllegalArgumentException exception) {
            // и кривой заголовок (InvalidMediaTypeException), и неизвестная кодировка (UnsupportedCharsetException)
            return StandardCharsets.UTF_8;
        }
    }
}

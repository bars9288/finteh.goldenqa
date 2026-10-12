package ru.cqa.server.db.sqlite.journal;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.MediaType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Тело HTTP-сообщения в виде для журнала: тип и текст.
 *
 * <p>Тип определяется по содержимому, а не по Content-Type: тестировщику
 * присылают и JSON с {@code text/plain}, и мусор с {@code application/json}.
 * Content-Type используется только чтобы узнать кодировку.
 *
 * @param kind тип тела
 * @param text текст как пришёл; для {@link BodyKind#BINARY} — Base64; для {@link BodyKind#EMPTY} — null
 */
record JournalBody(BodyKind kind, String text) {

    static final JournalBody EMPTY = new JournalBody(BodyKind.EMPTY, null);

    static JournalBody of(byte[] body, String contentType, JsonMapper jsonMapper) {
        if (body == null || body.length == 0) {
            return EMPTY;
        }
        String text;
        try {
            // REPORT: байты, недопустимые в кодировке, — ошибка, а не тихая замена на «�»
            text = charsetOf(contentType).newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(body))
                    .toString();
        } catch (CharacterCodingException e) {
            return new JournalBody(BodyKind.BINARY, Base64.getEncoder().encodeToString(body));
        }
        return new JournalBody(kindOf(text, jsonMapper), text);
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
        } catch (JacksonException e) {
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
        } catch (IllegalArgumentException e) {
            // и кривой заголовок (InvalidMediaTypeException), и неизвестная кодировка (UnsupportedCharsetException)
            return StandardCharsets.UTF_8;
        }
    }
}

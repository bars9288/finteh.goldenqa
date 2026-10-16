package ru.cqa.server.web.journal;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.server.db.sqlite.journal.JournalReader;
import ru.cqa.server.db.sqlite.journal.ValidationStatus;
import ru.cqa.server.web.ApiPaths;

/**
 * Чтение журнала запросов: список, одна запись целиком, сводка.
 *
 * <p>Только чтение — журнал пишет {@link JournalFilter}. Сами эти ручки в журнал
 * не попадают (см. исключения в фильтре): иначе веб-интерфейс, опрашивающий журнал,
 * засыпал бы его собственными запросами.
 */
@Tag(name = "Журнал", description = "Входящие запросы, ответы клиенту и вызовы МДМ")
@RestController
@RequestMapping(path = ApiPaths.JOURNAL, produces = MediaType.APPLICATION_JSON_VALUE)
public class JournalController {

    private final JournalReader reader;

    public JournalController(JournalReader reader) {
        this.reader = reader;
    }

    @Operation(summary = "Страница журнала, новые записи сверху")
    @GetMapping
    public JournalReader.Page list(
            @Parameter(description = "Подстрока адреса ручки") @RequestParam(required = false) String endpoint,
            @Parameter(description = "Итог проверки") @RequestParam(required = false) ValidationStatus validation,
            @Parameter(description = "Класс HTTP-статуса ответа: 2, 4, 5; 0 — ответа нет")
            @RequestParam(required = false) Integer statusClass,
            @Parameter(description = "Подстрока в телах, ошибках или id") @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return reader.list(new JournalReader.Filter(endpoint, validation, statusClass, text, limit, offset));
    }

    @Operation(summary = "Сводка по журналу")
    @GetMapping("/stats")
    public JournalReader.Stats stats() {
        return reader.stats();
    }

    @Operation(summary = "Запись журнала целиком: запрос, ответ, вызовы МДМ")
    @GetMapping("/{id}")
    public ResponseEntity<JournalReader.Details> get(@PathVariable UUID id) {
        return ResponseEntity.of(reader.get(id));
    }
}

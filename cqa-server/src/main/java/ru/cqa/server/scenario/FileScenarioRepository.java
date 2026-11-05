package ru.cqa.server.scenario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.cqa.scenario.creation.ScenarioAlreadyExistsException;
import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.repository.ScenarioRepository;
import ru.cqa.scenario.serialization.ScenarioParser;
import ru.cqa.scenario.serialization.ScenarioSerializer;

/** Файловый адаптер ScenarioRepository; каталог задаётся относительно корня запуска приложения. */
@Repository
public class FileScenarioRepository implements ScenarioRepository {
    private final Path directory;
    private final ScenarioParser parser;
    private final ScenarioSerializer serializer;

    public FileScenarioRepository(
            @Value("${cqa.scenarios.directory:scenarios}") String directory,
            ScenarioParser parser,
            ScenarioSerializer serializer) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
        this.parser = parser;
        this.serializer = serializer;
    }

    @Override
    public Optional<Scenario> findById(UUID id) {
        if (!Files.isDirectory(directory)) {
            return Optional.empty();
        }
        try (var files = Files.list(directory)) {
            return files.filter(path -> path.getFileName().toString().startsWith(id + "-")
                            && path.getFileName().toString().endsWith(".json"))
                    .map(this::read)
                    .findFirst();
        } catch (IOException exception) {
            throw storageFailure("Не удалось найти сценарий " + id, exception);
        }
    }

    @Override
    public Optional<Scenario> findByIdAndVersion(UUID id, String version) {
        Path file = scenarioFile(id, version);
        return Files.exists(file) ? Optional.of(read(file)) : Optional.empty();
    }

    @Override
    public Scenario save(Scenario scenario) {
        Path target = scenarioFile(scenario.id(), scenario.version());
        Path temporary = null;
        try {
            Files.createDirectories(directory);
            if (Files.exists(target)) {
                throw new ScenarioAlreadyExistsException(scenario.id(), scenario.version());
            }
            temporary = Files.createTempFile(directory, scenario.id() + "-", ".tmp");
            Files.writeString(temporary, serializer.serialize(scenario), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target);
            }
            return scenario;
        } catch (FileAlreadyExistsException exception) {
            throw new ScenarioAlreadyExistsException(scenario.id(), scenario.version());
        } catch (IOException exception) {
            throw storageFailure("Не удалось сохранить сценарий " + scenario.id(), exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Временный файл уже не влияет на результат сохранения.
                }
            }
        }
    }

    @Override
    public void delete(UUID id) {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (var files = Files.list(directory)) {
            for (Path file : files.filter(path -> path.getFileName().toString().startsWith(id + "-")
                    && path.getFileName().toString().endsWith(".json")).toList()) {
                Files.deleteIfExists(file);
            }
        } catch (IOException exception) {
            throw storageFailure("Не удалось удалить сценарий " + id, exception);
        }
    }

    private Scenario read(Path file) {
        try {
            return parser.parse(Files.readString(file, StandardCharsets.UTF_8));
        } catch (RuntimeException exception) {
            throw new ScenarioStorageException("Файл сценария повреждён: " + file, exception);
        } catch (IOException exception) {
            throw storageFailure("Не удалось прочитать сценарий из " + file, exception);
        }
    }

    private Path scenarioFile(UUID id, String version) {
        String encodedVersion = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(version.getBytes(StandardCharsets.UTF_8));
        return directory.resolve(id + "-" + encodedVersion + ".json");
    }

    private static ScenarioStorageException storageFailure(String message, IOException cause) {
        return new ScenarioStorageException(message, cause);
    }
}

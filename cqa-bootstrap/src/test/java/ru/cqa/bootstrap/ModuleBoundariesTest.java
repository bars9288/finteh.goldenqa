package ru.cqa.bootstrap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Защищает независимость библиотек при добавлении новых Maven-зависимостей. */
class ModuleBoundariesTest {
    private static final Set<String> CONTRACTS = Set.of("cqa-api", "cqa-model");
    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "cqa-model", Set.of(),
            "cqa-api", Set.of("cqa-model"),
            "cqa-validation", CONTRACTS,
            "cqa-business", CONTRACTS,
            "cqa-scenario-engine", CONTRACTS,
            "cqa-server", CONTRACTS,
            "cqa-bootstrap", Set.of("cqa-api", "cqa-model", "cqa-validation",
                    "cqa-business", "cqa-scenario-engine", "cqa-server"));

    @Test
    void implementationsDependOnlyOnContractsAndModels() throws Exception {
        Path root = projectRoot();
        Document parent = readPom(root.resolve("pom.xml"));
        assertTrue(XPathFactory.newInstance().newXPath().evaluate(
                "count(/project/dependencies/dependency)", parent).equals("0"),
                "Родитель не должен добавлять зависимости всем модулям");

        for (var entry : ALLOWED.entrySet()) {
            Document pom = readPom(root.resolve(entry.getKey()).resolve("pom.xml"));
            var dependencies = pom.getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                var xpath = XPathFactory.newInstance().newXPath();
                String group = xpath.evaluate("groupId", dependencies.item(i));
                String artifact = xpath.evaluate("artifactId", dependencies.item(i));
                if (group.equals("ru.cqa")) {
                    assertTrue(entry.getValue().contains(artifact),
                            entry.getKey() + " не должен зависеть от " + artifact);
                }
                if (!Set.of("cqa-server", "cqa-bootstrap").contains(entry.getKey())) {
                    assertFalse(group.startsWith("org.springframework"),
                            entry.getKey() + " должен оставаться независимым от Spring");
                }
            }
        }
    }

    @Test
    void sourceReferencesRespectModuleBoundaries() throws Exception {
        Path root = projectRoot();
        Map<String, String> owners = new HashMap<>();
        for (String module : ALLOWED.keySet()) {
            Path sourceRoot = root.resolve(module).resolve("src/main/java");
            try (var paths = Files.walk(sourceRoot)) {
                for (Path file : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                    String type = sourceRoot.relativize(file).toString()
                            .replace(file.getFileSystem().getSeparator(), ".").replaceFirst("\\.java$", "");
                    if (!type.endsWith("package-info")) {
                        owners.put(type, module);
                    }
                }
            }
        }
        for (String module : ALLOWED.keySet()) {
            try (var paths = Files.walk(root.resolve(module).resolve("src/main/java"))) {
                for (Path file : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                    String source = Files.readString(file);
                    for (var type : owners.entrySet()) {
                        if (!type.getValue().equals(module) && source.contains(type.getKey())) {
                            assertTrue(ALLOWED.get(module).contains(type.getValue()),
                                    file + " ссылается на реализацию из " + type.getValue());
                        }
                    }
                }
            }
        }
    }

    private static Document readPom(Path path) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
    }

    private static Path projectRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.isDirectory(current.resolve("cqa-bootstrap"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Не найден корень Maven-проекта CQA");
        }
        return current;
    }
}

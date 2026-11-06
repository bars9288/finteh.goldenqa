package ru.cqa.scenario.legacy.plugin;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import ru.cqa.api.execution.MoveExecutor;

/** Загружает SPI-расширения из JAR до начала прогонов и удерживает их classloader-ы. */
public final class LegacyPluginManager implements AutoCloseable {
    private final Path pluginDirectory;
    private final LegacyPluginRegistry registry;
    private final Map<String, String> options;
    private final List<URLClassLoader> classLoaders = new ArrayList<>();
    private List<LegacyPluginDescriptor> loadedPlugins = List.of();

    public LegacyPluginManager(Path pluginDirectory, LegacyPluginRegistry registry, Map<String, String> options) {
        this.pluginDirectory = Objects.requireNonNull(pluginDirectory, "pluginDirectory");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.options = Map.copyOf(Objects.requireNonNull(options, "options"));
    }

    /** Идемпотентно загружает JAR-ы из каталога; каждый JAR публикует SPI через META-INF/services. */
    public List<LegacyPluginDescriptor> loadPlugins() throws IOException {
        if (!loadedPlugins.isEmpty()) {
            return loadedPlugins;
        }
        if (!Files.isDirectory(pluginDirectory)) {
            loadedPlugins = List.of();
            return loadedPlugins;
        }

        List<LegacyPluginDescriptor> descriptors = new ArrayList<>();
        try (var paths = Files.list(pluginDirectory)) {
            for (Path jar : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList()) {
                loadJar(jar, descriptors);
            }
        }
        loadedPlugins = List.copyOf(descriptors);
        return loadedPlugins;
    }

    private void loadJar(Path jar, List<LegacyPluginDescriptor> descriptors) throws IOException {
        URLClassLoader classLoader = new URLClassLoader(
                new java.net.URL[] {jar.toUri().toURL()}, LegacyScenarioPlugin.class.getClassLoader());
        classLoaders.add(classLoader);
        LegacyPluginContext context = new LegacyPluginContext(jar, options);

        ServiceLoader.load(LegacyScenarioPlugin.class, classLoader).forEach(plugin -> {
            plugin.register(context, registry);
            descriptors.add(plugin.getDescriptor());
        });
        ServiceLoader.load(LegacyMoveExecutorProvider.class, classLoader).forEach(provider -> {
            for (MoveExecutor<?> executor : provider.getMoveExecutors(context)) {
                registry.registerMoveExecutor(executor);
            }
            descriptors.add(provider.getDescriptor());
        });
    }

    public List<LegacyPluginDescriptor> getLoadedPlugins() {
        return loadedPlugins;
    }

    @Override
    public void close() throws IOException {
        IOException failure = null;
        for (URLClassLoader classLoader : classLoaders) {
            try {
                classLoader.close();
            } catch (IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        classLoaders.clear();
        if (failure != null) {
            throw failure;
        }
    }
}

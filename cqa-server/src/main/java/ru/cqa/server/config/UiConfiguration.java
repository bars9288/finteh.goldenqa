package ru.cqa.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.cqa.server.web.ApiPaths;

/**
 * Веб-интерфейс: статика лежит в {@code classpath:/static/ui/} и попадает в jar вместе
 * с приложением — отдельного сервера, сборки на Node и доступа в интернет не нужно.
 *
 * <p>Spring сам отдаёт файлы по их путям ({@code /ui/app.js}), но не знает, что
 * {@code /ui/} — это {@code index.html}. Здесь это и задано, плюс корень сайта
 * ведёт на интерфейс.
 */
@Configuration
public class UiConfiguration implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", ApiPaths.UI + "/");
        registry.addRedirectViewController(ApiPaths.UI, ApiPaths.UI + "/");
        registry.addViewController(ApiPaths.UI + "/").setViewName("forward:" + ApiPaths.UI + "/index.html");
    }
}

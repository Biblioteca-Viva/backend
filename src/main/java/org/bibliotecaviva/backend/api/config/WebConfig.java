package org.bibliotecaviva.backend.api.config;

import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.enums.WorkTypes;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, WorkTypes.class, WorkTypes::fromString);
        registry.addConverter(String.class, Status.class, Status::fromString);
    }
}

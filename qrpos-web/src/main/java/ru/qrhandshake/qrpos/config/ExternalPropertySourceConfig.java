package ru.qrhandshake.qrpos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(ignoreResourceNotFound = true,
        value = {
                "${" + ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION + "}"
        })
public class ExternalPropertySourceConfig {
}

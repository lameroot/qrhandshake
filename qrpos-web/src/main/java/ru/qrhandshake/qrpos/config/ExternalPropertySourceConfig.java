package ru.qrhandshake.qrpos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Configuration
public class ExternalPropertySourceConfig {

        private final static Logger logger = LoggerFactory.getLogger(ExternalPropertySourceConfig.class);

        @javax.annotation.Resource
        private ApplicationContext applicationContext;
        @javax.annotation.Resource
        private ConfigurableEnvironment environment;

        @PostConstruct
        public void externalPropertiesPropertySource() {
                try {
                        Resource resource = applicationContext.getResource(System.getProperty(ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION));
                        if ( resource.exists() ) {
                                Properties properties = new Properties();
                                properties.load(resource.getInputStream());
                                PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("externalConfig",properties);
                                environment.getPropertySources().addFirst(propertiesPropertySource);
                                logger.debug("External file property: {} add as first to property source list", System.getProperty(ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION));
                        }
                } catch (Exception e) {
                        logger.error("Error load external config from: {}",System.getProperty(ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION), e);
                }
        }
}

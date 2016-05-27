package ru.qrhandshake.qrpos.config;

/**
 * Created by lameroot on 20.05.16.
 */
public class Test {

    /*
    package ru.qrhandshake.qrpos.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import ru.qrhandshake.qrpos.integration.IntegrationFacade;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

    @Configuration
    @PropertySource(value = {"classpath:config.properties"})
    @ComponentScan(basePackages = {"ru.qrhandshake.qrpos.service"})
    @Import(value = {
            EntityManagerConfig.class,
            DatabaseConfig.class,
            RbsIntegrationConfig.class,
            ServletConfig.class
    })
    @EnableJpaRepositories(basePackages = {"ru.qrhandshake.qrpos.repository"})
    public class ApplicationConfig {

        @Resource
        private ApplicationContext applicationContext;

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();
            return standardPasswordEncoder;
        }

        @Bean
        public Map<IntegrationSupport, IntegrationFacade> integrationFacades() {
            Map<IntegrationSupport,IntegrationFacade> map = new HashMap<>();
            applicationContext.getBeansOfType(IntegrationFacade.class).entrySet().stream()
                    .filter(e -> e.getValue().isApplicable())
                    .map(e -> map.put(e.getValue().getIntegrationSupport(),e.getValue()));
            return map;
        }

        @Bean
        public IntegrationService integrationService() {
            return new IntegrationService(integrationFacades());
        }

    }

    */
}

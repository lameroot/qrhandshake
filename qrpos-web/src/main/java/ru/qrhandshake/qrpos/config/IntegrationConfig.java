package ru.qrhandshake.qrpos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.Collection;

@Configuration
@EnableIntegration
@IntegrationComponentScan(value = {"ru.qrhandshake.qrpos.integration","ru.qrhandshake.qrpos.config"})
public class IntegrationConfig {

    @MessagingGateway
    public interface IntegrationTest {

        @Gateway(requestChannel = "upcase.input")
        Collection<String> upcase(Collection<String> strings);

    }

    @Bean
    public IntegrationFlow upcase() {
        return f -> f
                .split()                                         // 1
                .<String, String>transform(String::toUpperCase)  // 2
                .aggregate();                                    // 3
    }
}

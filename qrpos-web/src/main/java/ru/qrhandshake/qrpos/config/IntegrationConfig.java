package ru.qrhandshake.qrpos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.Collection;

@Configuration
@IntegrationComponentScan(value = {"ru.qrhandshake.qrpos.integration"})
public class IntegrationConfig {

    @MessagingGateway
    public interface Upcase {

        //https://spring.io/blog/2014/11/25/spring-integration-java-dsl-line-by-line-tutorial
    }
}

package ru.qrhandshake.qrpos.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import ru.qrhandshake.qrpos.service.sms.ProstorSmsSender;
import ru.qrhandshake.qrpos.service.sms.SmsSender;

import javax.annotation.Resource;

@Configuration
public class SmsConfig {

    @Resource
    private Environment environment;

    @Bean
    public SmsSender smsSender() {
        return new ProstorSmsSender(
                environment.getRequiredProperty("sms.prostor.login"),
                environment.getRequiredProperty("sms.prostor.password")
        ).setHttpClient(httpClient());
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }


}

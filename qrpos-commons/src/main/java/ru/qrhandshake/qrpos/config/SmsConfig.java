package ru.qrhandshake.qrpos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.qrhandshake.qrpos.service.sms.ProstorSmsSender;
import ru.qrhandshake.qrpos.service.sms.SmsSender;

@Configuration
public class SmsConfig {

    @Bean
    public SmsSender smsSender() {
        return new ProstorSmsSender();
    }
}

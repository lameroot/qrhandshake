package ru.qrhandshake.qrpos.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import ru.qrhandshake.qrpos.service.mail.JavaMailSenderBuilder;
import ru.qrhandshake.qrpos.service.mail.MailObject;
import ru.qrhandshake.qrpos.service.mail.MailSender;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Resource
    private Environment environment;

    @Bean
    public MailSender mailSender() {
        MailSender mailSender = new MailSender(Arrays.asList(javaMailSender()));
        return mailSender;
    }

    protected JavaMailSender javaMailSender() {
        return new JavaMailSenderBuilder()
                .setHost(environment.getRequiredProperty("mail.host"))
                .setPort(environment.getRequiredProperty("mail.port", Integer.class))
                .setUsername(environment.getProperty("mail.username"))
                .setPassword(environment.getProperty("mail.password"))
                //.setProtocol(environment.getProperty("mail.protocol", "smtp"))
                .setJavaMailProperties(getGeneralProperties())
                .build();
    }

    protected Properties getGeneralProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.starttls.enable",environment.getProperty("mail.smtp.auth","true"));
        properties.setProperty("mail.smtp.auth",environment.getProperty("mail.smtp.starttls.enable","true"));
        properties.setProperty("mail.debug","true");
//        properties.setProperty("mail.smtp.quitwait",environment.getProperty("mail.smtp.quitwait","false"));
//        properties.setProperty("mail.smtp.timeout",environment.getProperty("mail.smtp.timeout","8500"));
//        properties.setProperty("mail.smtp.connectiontimeout",environment.getProperty("mail.smtp.timeout","8500"));
//        properties.setProperty("mail.smtp.writetimeout",environment.getProperty("mail.smtp.timeout","8500"));
//        if ( StringUtils.isNotBlank(environment.getProperty("mail.sender")) ) properties.setProperty(MailObject.SENDER_PARAM, environment.getProperty("mail.sender"));
//        if ( StringUtils.isNotBlank(environment.getProperty("mail.recipients")) ) properties.setProperty(MailObject.RECIPIENTS_PARAM,environment.getProperty("mail.recipients"));
//        if ( StringUtils.isNotBlank(environment.getProperty("mail.subject")) ) properties.setProperty(MailObject.SUBJECT_PARAM,environment.getProperty("mail.subject"));
        return properties;
    }

}

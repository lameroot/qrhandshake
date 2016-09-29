package ru.qrhandshake.qrpos.service.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

/**
 * User: Krainov
 * Date: 17.07.2015
 * Time: 19:11
 */
public class JavaMailSenderBuilder {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String protocol;
    private String defaultEncoding = "UTF-8";
    private Properties javaMailProperties;

    public JavaMailSenderBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public JavaMailSenderBuilder setPort(Integer port) {
        this.port = port;
        return this;
    }

    public JavaMailSenderBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public JavaMailSenderBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public JavaMailSenderBuilder setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public JavaMailSenderBuilder setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
        return this;
    }

    public JavaMailSenderBuilder setJavaMailProperties(Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
        return this;
    }

    public JavaMailSenderBuilder addProperty(Object key, Object value) {
        if ( null == javaMailProperties ) javaMailProperties = new Properties();
        javaMailProperties.put(key, value);
        return this;
    }

    public JavaMailSenderBuilder addProperties(Map<Object,Object> properties) {
        if ( null == javaMailProperties ) javaMailProperties = new Properties();
        javaMailProperties.putAll(properties);
        return this;
    }


    public JavaMailSender build() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setDefaultEncoding("UTF-8");
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setProtocol(protocol);

        sender.setJavaMailProperties(javaMailProperties);
        return sender;
    }
}

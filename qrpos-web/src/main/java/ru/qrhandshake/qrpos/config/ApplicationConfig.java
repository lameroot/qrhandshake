package ru.qrhandshake.qrpos.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.integration.IntegrationFacade;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.integration.P2pIntegrationFacade;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;
import ru.rbs.commons.util.SyncSimpleDateFormat;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(ignoreResourceNotFound = true,
        value = {
                "classpath:config.properties"
        })
@ComponentScan(basePackages = {"ru.qrhandshake.qrpos.service","ru.qrhandshake.qrpos.converter"})
@Import(value = {
        ExternalPropertySourceConfig.class,//must be first
        EntityManagerConfig.class,
        DatabaseConfig.class,
        RbsIntegrationConfig.class,
        MailConfig.class,
        SmsConfig.class
})
@EnableJpaRepositories(basePackages = {"ru.qrhandshake.qrpos.repository"})
public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    public static final String SYSTEM_VARIABLE_CONFIG_LOCATION = "qrConfigLocation";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.'f'ZZZZZ";

    @Resource
    private ApplicationContext applicationContext;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        return configurer;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.setDateFormat(new SyncSimpleDateFormat(DATE_FORMAT));
        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper());
        mappingJackson2HttpMessageConverter.setPrettyPrint(false);
        return mappingJackson2HttpMessageConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();
        return standardPasswordEncoder;
    }

    @Bean
    public Map<IntegrationSupport, IntegrationFacade> integrationFacades() {
        Map<IntegrationSupport,IntegrationFacade> map = new HashMap<>();
        for (Map.Entry<String, IntegrationFacade> entry : applicationContext.getBeansOfType(IntegrationFacade.class).entrySet()) {
            if ( entry.getValue().isApplicable() ) map.put(entry.getValue().getIntegrationSupport(),entry.getValue());
        }
        return map;
    }

    @Bean
    public Map<IntegrationSupport, P2pIntegrationFacade> p2pIntegrationFacades() {
        Map<IntegrationSupport, P2pIntegrationFacade> map = new HashMap<>();
        for (Map.Entry<String, P2pIntegrationFacade> entry : applicationContext.getBeansOfType(P2pIntegrationFacade.class).entrySet()) {
            if ( entry.getValue().isApplicable() ) map.put(entry.getValue().getIntegrationSupport(),entry.getValue());
        }
        return map;
    }

    @Bean
    public IntegrationService integrationService() {
        return new IntegrationService(integrationFacades(), p2pIntegrationFacades());
    }

    /**
     * Установить системную переменную 'qrConfigLocation' как значение по умолчанию, иначе если её не будет, приложение не запустится
     */
    public final static void setSystemVariableConfigLocation() {
        logger.debug("System variable with name '" + ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION + "' has value = '" + System.getProperty(ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION) + "'");
        if ( null == System.getProperty(ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION) ) {
            logger.debug("Set system variable with name '" + ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION + "' as fake value.");
            System.setProperty(ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION,"fake value");
        }
    }

    @Bean
    public ConversionService conversionService() {
        FormattingConversionService formattingConversionService = new FormattingConversionService();
        for (Map.Entry<String, Converter> entry : applicationContext.getBeansOfType(Converter.class).entrySet()) {
            formattingConversionService.addConverter(entry.getValue());
        }
        return formattingConversionService;
    }

}

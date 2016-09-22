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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import ru.qrhandshake.qrpos.converter.OrderTemplateRequestToOrderTemplateParamsConverter;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.integration.IntegrationFacade;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;
import ru.rbs.util.SyncSimpleDateFormat;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(ignoreResourceNotFound = true,
        value = {
                "classpath:config.properties",
                "${" + ApplicationConfig.SYSTEM_VARIABLE_CONFIG_LOCATION + "}"
        })
@ComponentScan(basePackages = {"ru.qrhandshake.qrpos.service","ru.qrhandshake.qrpos.converter"})
@Import(value = {
        EntityManagerConfig.class,
        DatabaseConfig.class,
        RbsIntegrationConfig.class
})
@EnableJpaRepositories(basePackages = {"ru.qrhandshake.qrpos.repository"})
public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    public static final String SYSTEM_VARIABLE_CONFIG_LOCATION = "qrConfigLocation";

    @Resource
    private ApplicationContext applicationContext;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
        SimpleDateFormat simpleDateFormat = new SyncSimpleDateFormat("yyyyMMddHHmmss");
        builder.indentOutput(true).dateFormat(simpleDateFormat);
        return new MappingJackson2HttpMessageConverter(builder.build());
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
    public IntegrationService integrationService() {
        return new IntegrationService(integrationFacades());
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

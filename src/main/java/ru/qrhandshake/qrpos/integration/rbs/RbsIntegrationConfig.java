package ru.qrhandshake.qrpos.integration.rbs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import ru.bpc.phoenix.proxy.api.NamePasswordToken;
import ru.qrhandshake.qrpos.integration.IntegrationFacade;
import ru.qrhandshake.qrpos.integration.IntegrationSupport;

import javax.annotation.Resource;

/**
 * Created by lameroot on 19.05.16.
 */
@Configuration
@PropertySource(value = {"classpath:integration/rbs.properties"})
@Profile(value = {RbsIntegrationConfig.RBS_PROFILE})
public class RbsIntegrationConfig {

    public final static String RBS_PROFILE = "rbs";

    @Resource
    private Environment environment;

    @Bean
    public NamePasswordToken rbsSbrfNamePasswordToken() {
        return new NamePasswordToken(environment.getRequiredProperty("rbs.sbrf.login"),
                environment.getRequiredProperty("rbs.sbrf.password"));
    }

    @Bean
    public IntegrationFacade rbsSbrfIntegrationService() {
        return new RbsIntegrationFacade(rbsSbrfNamePasswordToken(), environment.getRequiredProperty("rbs.sbrf.wsdlLocation"), IntegrationSupport.RBS_SBRF);
    }
}

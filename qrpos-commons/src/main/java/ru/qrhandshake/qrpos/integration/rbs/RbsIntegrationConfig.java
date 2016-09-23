package ru.qrhandshake.qrpos.integration.rbs;


import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.integration.IntegrationFacade;
import ru.qrhandshake.qrpos.integration.P2pIntegrationFacade;

@Configuration
@PropertySource(value = {"classpath:integration/rbs.properties"})
@Profile(value = {RbsIntegrationConfig.RBS_PROFILE})
public class RbsIntegrationConfig {

    public final static String RBS_PROFILE = "rbs";

    @Bean
    public IntegrationFacade rbsSbrfIntegrationService() {
        return new RbsIntegrationFacade(IntegrationSupport.RBS_SBRF);
    }

    @Bean
    public P2pIntegrationFacade rbsP2pIntegrationFacade() {
        return new RbsP2PIntegrationFacade(IntegrationSupport.RBS_SBRF_P2P);
    }

    @Bean(name = "loggingInInterceptor")
    public LoggingInInterceptor loggingInInterceptor() {
        return new LoggingInInterceptor();
    }

    @Bean(name = "loggingOutInterceptor")
    public LoggingOutInterceptor loggingOutInterceptor() {
        return new LoggingOutInterceptor();
    }
}

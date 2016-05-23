package ru.qrhandshake.qrpos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

/**
 * Created by lameroot on 20.05.16.
 */
public class QrWebApplicationInitializer  implements WebApplicationInitializer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static String[] activeProfiles = new String[]{
        RbsIntegrationConfig.RBS_PROFILE,
            "prod"
    };


    @Override
    public void onStartup(ServletContext container) throws ServletException {
        logger.debug("ON START UP Web application [QR-POS]");
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();

        rootContext.register(ApplicationConfig.class);
        rootContext.getEnvironment().setActiveProfiles(activeProfiles);

        for (String profile : rootContext.getEnvironment().getActiveProfiles()) {
            logger.debug("Active profile: {}",profile);
        }
        //listerns
        ContextLoaderListener contextLoaderListener = new ContextLoaderListener(rootContext);
        container.addListener(contextLoaderListener);

        //filters
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setForceEncoding(true);
        characterEncodingFilter.setEncoding("UTF-8");

        container.addFilter("characterEncodingFilter",characterEncodingFilter)
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/*");

        //appServlet
        AnnotationConfigWebApplicationContext dispatcherContext  = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(ServletConfig.class);
        DispatcherServlet appServlet = new DispatcherServlet(dispatcherContext);
        ServletRegistration.Dynamic dispatcher = container.addServlet("appServlet", appServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        logger.debug("Finish load web application.");
    }
}

package ru.qrhandshake.qrpos.config;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationConfig.class, loader = AnnotationConfigContextLoader.class)
public class IntegrationConfigTest extends TestCase {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private IntegrationConfig.IntegrationTest integrationTest;

    @Test
    public void testExists() {
        for (String s : applicationContext.getBeanDefinitionNames()) {
            System.out.println(s);
        }
        assertNotNull(integrationTest);
        System.out.println(integrationTest.upcase(Arrays.asList("111", "222", "333")));
    }
}

package ru.qrhandshake.qrpos;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import ru.qrhandshake.qrpos.config.ApplicationConfig;

import javax.annotation.Resource;

/**
 * Created by lameroot on 18.05.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles(value = {"test"})
public class GeneralTest extends TestCase {

    @Resource
    protected ApplicationContext applicationContext;

    @Test
    public void testExists() {
        assertNotNull(applicationContext);
    }
}

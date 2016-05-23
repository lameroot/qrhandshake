package ru.qrhandshake.qrpos;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.qrhandshake.qrpos.config.ApplicationConfig;
import ru.qrhandshake.qrpos.config.ServletConfig;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;

import javax.annotation.Resource;

/**
 * Created by lameroot on 23.05.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        ApplicationConfig.class,
        ServletConfig.class
})
@ActiveProfiles(value = {"prod", RbsIntegrationConfig.RBS_PROFILE})
public class ServletConfigTest extends TestCase {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    protected WebApplicationContext wac;
    protected MockMvc mockMvc;

    @Before
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}

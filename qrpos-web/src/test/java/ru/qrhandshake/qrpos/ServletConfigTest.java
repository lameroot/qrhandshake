package ru.qrhandshake.qrpos;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import ru.qrhandshake.qrpos.config.ApplicationConfig;
import ru.qrhandshake.qrpos.config.ServletConfig;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
import ru.qrhandshake.qrpos.repository.OrderTemplateRepository;
import ru.qrhandshake.qrpos.repository.TerminalRepository;
import ru.qrhandshake.qrpos.repository.UserRepository;

import javax.annotation.Resource;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    protected Environment environment;
    @Resource
    protected WebApplicationContext wac;
    @Resource
    protected RestTemplate restTemplate;
    @Resource
    protected ObjectMapper objectMapper;
    @Resource
    protected MerchantRepository merchantRepository;
    @Resource
    protected UserRepository userRepository;
    @Resource
    protected OrderTemplateRepository orderTemplateRepository;
    @Resource
    protected TransactionTemplate transactionTemplate;
    @Resource
    protected TerminalRepository terminalRepository;
    protected MockMvc mockMvc;

    @Before
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}

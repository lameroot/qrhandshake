package ru.qrhandshake.qrpos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
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
import ru.qrhandshake.qrpos.service.OrderService;

import javax.annotation.Resource;

/**
 * Created by lameroot on 23.05.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        AbstractTest.TestDataSourceConfig.class,
        ApplicationConfig.class,
        ServletConfig.class
})
@ActiveProfiles(value = {"test", RbsIntegrationConfig.RBS_PROFILE})
public class ServletConfigTest extends AbstractTest {

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
    @Resource
    protected OrderService orderService;
    @Resource
    protected JdbcTemplate jdbcTemplate;
    protected MockMvc mockMvc;

    @Before
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}

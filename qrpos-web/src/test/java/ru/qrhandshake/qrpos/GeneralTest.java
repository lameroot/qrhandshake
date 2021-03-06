package ru.qrhandshake.qrpos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import ru.qrhandshake.qrpos.config.ApplicationConfig;
import ru.qrhandshake.qrpos.repository.*;
import ru.qrhandshake.qrpos.service.OrderTemplateHistoryService;

import javax.annotation.Resource;

/**
 * Created by lameroot on 18.05.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AbstractTest.TestDataSourceConfig.class, ApplicationConfig.class}, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles(value = {"prod"})
public class GeneralTest extends AbstractTest {

    @Resource
    protected ApplicationContext applicationContext;
    @Resource
    protected TerminalRepository terminalRepository;
    @Resource
    protected ConversionService conversionService;
    @Resource
    protected MerchantRepository merchantRepository;
    @Resource
    protected OrderTemplateRepository orderTemplateRepository;
    @Resource
    protected MerchantOrderRepository merchantOrderRepository;
    @Resource
    protected OrderTemplateHistoryRepository orderTemplateHistoryRepository;
    @Resource
    protected OrderTemplateHistoryService orderTemplateHistoryService;

    @Test
    public void testExists() {
        assertNotNull(applicationContext);
    }
}

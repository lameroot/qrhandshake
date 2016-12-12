package ru.qrhandshake.qrpos.service.stats;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Statistic;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.dto.StatisticMetrics;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

public class StatisticServiceTest extends GeneralTest {

    @Resource
    private StatisticService statisticService;

    private StatisticMetrics build(Statistic.StatisticType type, Merchant merchant, OrderTemplate orderTemplate, Long value, Date date) {
        return StatisticMetrics.create(type, merchant, orderTemplate, value, date);
    }

    @Test
    @Rollback(true)
    public void testUpdate() throws Exception {
        Merchant merchant = new Merchant();
        merchant.setName(UUID.randomUUID().toString());
        merchant.setCreatedDate(new Date());
        merchantRepository.save(merchant);

        Terminal terminal = new Terminal();
        terminal.setEnabled(true);
        terminal.setMerchant(merchant);
        terminal.setAuthName(UUID.randomUUID().toString());
        terminal.setAuthPassword("password");
        terminalRepository.save(terminal);

        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setTerminal(terminal);
        orderTemplate.setName("test");
        orderTemplate.setAmount(1000L);
        orderTemplate.setDescription("test");
        orderTemplateRepository.save(orderTemplate);

        Date start = new Date();
        for (int i = 0; i < 5; i++) {
            statisticService.update(build(Statistic.StatisticType.TEMPLATE, merchant, null, 1L, new Date()));
        }
        Thread.sleep(5000L);
        Date finish = new Date();
        long sum = statisticService.sumByPeriod(Statistic.StatisticType.TEMPLATE, merchant, null, start, finish);
        assertEquals(100L, sum);

    }
}

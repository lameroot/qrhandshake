package ru.qrhandshake.qrpos.service.stats;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Statistic;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.dto.StatisticMetric;
import ru.qrhandshake.qrpos.repository.StatisticRepository;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class StatisticServiceTest extends GeneralTest {

    @Resource
    private StatisticService statisticService;
    @Resource
    private StatisticRepository statisticRepository;

    private StatisticMetric build(Statistic.StatisticType type, Merchant merchant, Terminal terminal, OrderTemplate orderTemplate, Long value, Date date) {
        return StatisticMetric.create(type, merchant.getId(), terminal.getId(), null != orderTemplate ? orderTemplate.getId() : null, value, date);
    }

    @Before
    public void clean() {
        statisticRepository.deleteAll();
    }

    @Test
    @Rollback(true)
    public void testUpdateInOneTimeSlot() throws Exception {
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

        Terminal terminal2 = new Terminal();
        terminal2.setEnabled(true);
        terminal2.setMerchant(merchant);
        terminal2.setAuthName(UUID.randomUUID().toString());
        terminal2.setAuthPassword("password");
        terminalRepository.save(terminal2);

        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setTerminal(terminal);
        orderTemplate.setName("test");
        orderTemplate.setAmount(1000L);
        orderTemplate.setDescription("test");
        orderTemplateRepository.save(orderTemplate);

        OrderTemplate orderTemplate2 = new OrderTemplate();
        orderTemplate2.setTerminal(terminal2);
        orderTemplate2.setName("test");
        orderTemplate2.setAmount(1000L);
        orderTemplate2.setDescription("test");
        orderTemplateRepository.save(orderTemplate2);

        int count = 5;

        Date start = new Date();
        for (int i = 0; i < count; i++) {
            Thread.sleep(500L);

            statisticService.update(build(Statistic.StatisticType.COUNT_PAID, merchant, terminal, null, 1L, new Date()));
            statisticService.update(build(Statistic.StatisticType.AMOUNT_PAID, merchant, terminal, null, 1L, new Date()));

            statisticService.update(build(Statistic.StatisticType.COUNT_PAID, merchant, terminal, orderTemplate, 1L, new Date()));
            statisticService.update(build(Statistic.StatisticType.AMOUNT_PAID, merchant, terminal, orderTemplate, 1L, new Date()));

            statisticService.update(build(Statistic.StatisticType.COUNT_PAID, merchant, terminal2, orderTemplate2, 1L, new Date()));
            statisticService.update(build(Statistic.StatisticType.AMOUNT_PAID, merchant, terminal2, orderTemplate2, 1L, new Date()));
        }
        Thread.sleep(100L);
        Date finish = new Date();

        assertEquals(6,statisticRepository.count());
        long sum = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), null);
        assertEquals(count*3, sum);
        long sumAmount = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), null);
        assertEquals(count*3, sumAmount);
        long sumTerminal = statisticService.sumByPeriodForTerminals(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), terminal.getId());
        assertEquals(count*2, sumTerminal);
        long sumAmountTerminal = statisticService.sumByPeriodForTerminals(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), terminal.getId());
        assertEquals(count*2, sumAmountTerminal);
        long sumTerminals = statisticService.sumByPeriodForTerminals(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), terminal.getId(), terminal2.getId());
        assertEquals(count*3, sumTerminals);
        long sumAmountTerminals = statisticService.sumByPeriodForTerminals(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), terminal.getId(), terminal2.getId());
        assertEquals(count*3, sumAmountTerminals);

        long sum2 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), orderTemplate.getId());
        assertEquals(count, sum2);
        long sumAmount2 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), orderTemplate.getId());
        assertEquals(count, sumAmount2);

        long sum3 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), orderTemplate2.getId());
        assertEquals(count, sum3);
        long sumAmount3 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), orderTemplate2.getId());
        assertEquals(count, sumAmount3);

        long sum4 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), orderTemplate.getId(), orderTemplate2.getId());
        assertEquals(count*2, sum4);
        long sumAmount4 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), orderTemplate.getId(), orderTemplate2.getId());
        assertEquals(count*2, sumAmount4);

    }

    @Test
    @Rollback(true)
    public void testUpdateInDifferentTimeSlot() throws Exception {
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

        int count = 5;

        Integer slotInMinutes = environment.getProperty("statistics.slotInMin",Integer.class, 60);
        Date start = new Date();
        for (int i = 0; i < count; i++) {
            Thread.sleep(500L);
            Calendar date = Calendar.getInstance();
            date.add(Calendar.MINUTE, slotInMinutes + 1);

            statisticService.update(build(Statistic.StatisticType.COUNT_PAID, merchant, terminal, orderTemplate, 1L, date.getTime()));
            statisticService.update(build(Statistic.StatisticType.AMOUNT_PAID, merchant, terminal, orderTemplate, 1L, date.getTime()));
        }
        Thread.sleep(100L);
        Date finish = new Date();

        assertEquals(2 * count,statisticRepository.count());
        long sum = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), null);
        assertEquals(count, sum);
        long sumAmount = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), null);
        assertEquals(count, sumAmount);

        long sum2 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), orderTemplate.getId());
        assertEquals(count, sum2);
        long sumAmount2 = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), orderTemplate.getId());
        assertEquals(count, sumAmount2);

    }

    @Test
    @Rollback(true)
    public void testUpdateInOneTimeSlotWithDifferentDate() throws Exception {
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

        int count = 5;

        Integer slotInMinutes = environment.getProperty("statistics.slotInMin",Integer.class, 60);
        Date start = new Date();
        for (int i = 0; i < count; i++) {
            Thread.sleep(100L);
            Calendar date = Calendar.getInstance();
            date.add(Calendar.MINUTE, slotInMinutes -5);

            statisticService.update(build(Statistic.StatisticType.COUNT_PAID, merchant, terminal, orderTemplate, 1L, date.getTime()));
            statisticService.update(build(Statistic.StatisticType.AMOUNT_PAID, merchant, terminal, orderTemplate, 1L, date.getTime()));
        }
        Thread.sleep(100L);
        Date finish = new Date();

        assertEquals(2,statisticRepository.count());
        long sum = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.COUNT_PAID, start, finish, merchant.getId(), null);
        assertEquals(count, sum);
        long sumAmount = statisticService.sumByPeriodByOrderTemplates(Statistic.StatisticType.AMOUNT_PAID, start, finish, merchant.getId(), null);
        assertEquals(count, sumAmount);

    }
}

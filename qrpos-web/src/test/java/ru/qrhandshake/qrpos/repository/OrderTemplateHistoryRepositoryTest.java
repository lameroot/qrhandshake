package ru.qrhandshake.qrpos.repository;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lameroot on 10.08.16.
 */
@Transactional
@Rollback
public class OrderTemplateHistoryRepositoryTest extends GeneralTest {


    @Test
    public void testFindSuccessUsePageable() {
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


        Calendar calendar = Calendar.getInstance();
        Date start = calendar.getTime();
        int count = 10;

        List<OrderTemplateHistory> success = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MerchantOrder merchantOrder = new MerchantOrder();
            merchantOrder.setOrderId(UUID.randomUUID().toString());
            merchantOrder.setAmount(1000L);
            merchantOrderRepository.save(merchantOrder);

            calendar.add(Calendar.MINUTE, 1);

            OrderTemplateHistory orderTemplateHistory = new OrderTemplateHistory();
            orderTemplateHistory.setClientId(null);
            orderTemplateHistory.setDate(calendar.getTime());
            orderTemplateHistory.setDeviceId(UUID.randomUUID().toString());
            orderTemplateHistory.setDeviceModel(i % 2 == 0 ? "Samsung" : "Nokia");
            orderTemplateHistory.setHumanOrderNumber(StringUtils.leftPad(String.valueOf(i),6,"0"));
            orderTemplateHistory.setMerchantOrderId(merchantOrder.getId());
            orderTemplateHistory.setOrderTemplateId(orderTemplate.getId());
            if ( i % 2 == 0 ) {
                orderTemplateHistory.setStatus(true);
                success.add(orderTemplateHistory);
            }
            else {
                orderTemplateHistory.setStatus(false);
            }

            orderTemplateHistoryRepository.save(orderTemplateHistory);
        }

        assertEquals(count, orderTemplateHistoryRepository.count());
        List<Long> successIds = success.stream().map(s -> s.getId()).collect(Collectors.toList());
        assertEquals(count / 2, successIds.size());

        Page<OrderTemplateHistory> orderTemplateHistoryPageLessThan = orderTemplateHistoryRepository.findByOrderTemplateIdAndStatusAndIdLessThan(orderTemplate.getId(), true, successIds.get(successIds.size() - successIds.size() + 1), new PageRequest(0, 10));
        assertNotNull(orderTemplateHistoryPageLessThan);
        assertEquals(successIds.get(1),orderTemplateHistoryPageLessThan.getContent().get(1).getId());

        Page<OrderTemplateHistory> orderTemplateHistoryPageGreaterThan = orderTemplateHistoryRepository.findByOrderTemplateIdAndStatusAndIdGreaterThan(orderTemplate.getId(), true, successIds.get(successIds.size() - successIds.size() + 1), new PageRequest(0,10));
        assertNotNull(orderTemplateHistoryPageGreaterThan);
        assertEquals(successIds.get(2), orderTemplateHistoryPageGreaterThan.getContent().get(1).getId());
    }

    @Test
    public void testFindLastSuccessFromDate() {
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


        Calendar calendar = Calendar.getInstance();
        Date start = calendar.getTime();
        int count = 10;
        for (int i = 0; i < count; i++) {
            MerchantOrder merchantOrder = new MerchantOrder();
            merchantOrder.setOrderId(UUID.randomUUID().toString());
            merchantOrder.setAmount(1000L);
            merchantOrderRepository.save(merchantOrder);

            calendar.add(Calendar.MINUTE, 1);

            OrderTemplateHistory orderTemplateHistory = new OrderTemplateHistory();
            orderTemplateHistory.setClientId(null);
            orderTemplateHistory.setDate(calendar.getTime());
            orderTemplateHistory.setDeviceId(UUID.randomUUID().toString());
            orderTemplateHistory.setDeviceModel(i % 2 == 0 ? "Samsung" : "Nokia");
            orderTemplateHistory.setHumanOrderNumber(StringUtils.leftPad(String.valueOf(i),6,"0"));
            orderTemplateHistory.setMerchantOrderId(merchantOrder.getId());
            orderTemplateHistory.setOrderTemplateId(orderTemplate.getId());
            orderTemplateHistory.setStatus(i % 2 == 0 ? true : false);

            orderTemplateHistoryRepository.save(orderTemplateHistory);
        }

        assertEquals(count, orderTemplateHistoryRepository.count());
        List<OrderTemplateHistory> orderTemplateHistories = orderTemplateHistoryRepository.findLastSuccessFromDate(start, orderTemplate.getId());
        assertNotNull(orderTemplateHistories);
        assertEquals(count / 2, orderTemplateHistories.size());

        Calendar newDate = Calendar.getInstance();
        newDate.setTime(start);
        newDate.add(Calendar.MINUTE, 6);
        List<OrderTemplateHistory> orderTemplateHistories2 = orderTemplateHistoryRepository.findLastSuccessFromDate(newDate.getTime(), orderTemplate.getId());
        assertNotNull(orderTemplateHistories2);
        assertEquals((count - 6) / 2, orderTemplateHistories2.size());


    }
}

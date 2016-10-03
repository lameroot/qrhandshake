package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryParams;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryResult;
import ru.qrhandshake.qrpos.domain.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lameroot on 01.09.16.
 */
@Transactional
@Rollback
public class OrderTemplateHistoryServiceTest extends GeneralTest{

    @Test
    public void testGetOrders() throws Exception {
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
            orderTemplateHistory.setHumanOrderNumber(StringUtils.leftPad(String.valueOf(i), 6, "0"));
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
        System.out.println(successIds);

        OrderTemplateHistoryParams orderTemplateHistoryParams = new OrderTemplateHistoryParams();
        orderTemplateHistoryParams.setStatus(true);
        orderTemplateHistoryParams.setOrderTemplateId(orderTemplate.getId());
        orderTemplateHistoryParams.setId(successIds.get(2));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC,"id"));
        OrderTemplateHistoryResult orderTemplateHistoryResult = orderTemplateHistoryService.getOrders(orderTemplateHistoryParams, new PageRequest(0, 10, sort));
        assertNotNull(orderTemplateHistoryResult);

        for (OrderTemplateHistoryResult.OrderTemplateHistoryData orderTemplateHistoryData : orderTemplateHistoryResult.getOrders()) {
            System.out.println(orderTemplateHistoryData.toMap());
        }

        //
        sort = new Sort(new Sort.Order(Sort.Direction.DESC,"id"));
        OrderTemplateHistoryResult orderTemplateHistoryResultDesc = orderTemplateHistoryService.getOrders(orderTemplateHistoryParams, new PageRequest(0, 10, sort));
        assertNotNull(orderTemplateHistoryResultDesc);

        for (OrderTemplateHistoryResult.OrderTemplateHistoryData orderTemplateHistoryData : orderTemplateHistoryResultDesc.getOrders()) {
            System.out.println(orderTemplateHistoryData.toMap());
        }

        //
        sort = null;
        OrderTemplateHistoryResult orderTemplateHistoryResultAscNull = orderTemplateHistoryService.getOrders(orderTemplateHistoryParams, new PageRequest(0, 10, sort));
        assertNotNull(orderTemplateHistoryResultAscNull);

        for (OrderTemplateHistoryResult.OrderTemplateHistoryData orderTemplateHistoryData : orderTemplateHistoryResultAscNull.getOrders()) {
            System.out.println(orderTemplateHistoryData.toMap());
        }
    }
}

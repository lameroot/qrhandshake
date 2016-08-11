package ru.qrhandshake.qrpos.service;

import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.exception.AuthException;

import javax.annotation.Resource;
import javax.persistence.criteria.Order;
import java.util.UUID;

/**
 * Created by lameroot on 08.08.16.
 */
@Transactional
@Rollback
public class OrderTemplateServiceTest extends GeneralTest {

    @Resource
    private OrderTemplateService orderTemplateService;
    @Resource
    private MerchantService merchantService;


    @Test
    public void testCreate() throws AuthException {
        MerchantRegisterRequest merchantRegisterRequest = new MerchantRegisterRequest();
        merchantRegisterRequest.setName(UUID.randomUUID().toString());
        MerchantRegisterResponse merchantRegisterResponse = merchantService.register(merchantRegisterRequest);

        assertEquals(ResponseStatus.SUCCESS,merchantRegisterResponse.getStatus());
        assertNotNull(merchantRegisterResponse.getMerchantId());

        Terminal terminal = terminalRepository.findByAuthName(merchantRegisterResponse.getAuth().getAuthName());
        assertNotNull(terminal);
        OrderTemplateRequest orderTemplateRequest = new OrderTemplateRequest();
        orderTemplateRequest.setName("transport");
        orderTemplateRequest.setDescription("template for payment transport");
        orderTemplateRequest.setAmount(20000L);
        orderTemplateRequest.setTerminalId(Long.valueOf(terminal.getId()));

        OrderTemplateParams orderTemplateParams = conversionService.convert(orderTemplateRequest, OrderTemplateParams.class);
        OrderTemplateResult orderTemplateResult = orderTemplateService.create(orderTemplateParams);
        OrderTemplateResponse orderTemplateResponse = conversionService.convert(orderTemplateResult,OrderTemplateResponse.class);
        assertNotNull(orderTemplateResponse);
        assertNotNull(orderTemplateResponse.getId());
        assertEquals(ResponseStatus.SUCCESS,orderTemplateResponse.getStatus());
    }
}

package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.repository.BindingRepository;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * Created by lameroot on 31.05.16.
 */
@Service
public class BindingService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private BindingRepository bindingRepository;
    @Resource
    private JsonService jsonService;
    @Resource
    private IntegrationService integrationService;

    public Binding register(Client client, PaymentParams paymentParams, MerchantOrder merchantOrder, boolean enabled) {
        try {
            Binding binding = new Binding();
            binding.setPaymentSecureType(merchantOrder.getPaymentSecureType());
            binding.setClient(client);
            binding.setExternalBindingId(null);
            binding.setIntegrationSupport(merchantOrder.getIntegrationSupport());
            binding.setPaymentParams(jsonService.paymentParamsToJsonString(paymentParams));
            binding.setEnabled(enabled);
            binding.setBindingId(UUID.randomUUID().toString());
            binding.setOrderId(merchantOrder.getOrderId());

            return bindingRepository.save(binding);
        } catch (Exception e) {
            logger.error("Error create binding for order: " + merchantOrder + " and client: " + client,e);
            return null;
        }
    }

    public void update(MerchantOrder merchantOrder, BindingInfo bindingInfo) {
        if ( null == merchantOrder || null == bindingInfo ) return;
        Binding binding = bindingRepository.findByOrderId(merchantOrder.getOrderId());
        if ( null == binding ) {
            logger.warn("Unable to find binding by orderId: {}", merchantOrder.getOrderId());
            return;
        }
        if ( OrderStatus.PAID.equals(merchantOrder.getOrderStatus()) && binding.getClient().getClientId().equals(bindingInfo.getClientId())) {
            binding.setEnabled(true);
            binding.setExternalBindingId(bindingInfo.getBindingId());
            bindingRepository.save(binding);
        }
        else if ( binding.isEnabled() ) {
            binding.setEnabled(false);
            bindingRepository.save(binding);
        }
    }

    //todo: должен быть крон который проверяет новые биндинги и делает внешние запросы на получение внешнего binding_id
}

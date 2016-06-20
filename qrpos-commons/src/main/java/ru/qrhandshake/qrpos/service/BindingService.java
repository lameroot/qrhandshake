package ru.qrhandshake.qrpos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.repository.BindingRepository;

import javax.annotation.Resource;
import java.util.List;
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
            binding.setPaymentWay(merchantOrder.getPaymentWay());

            return bindingRepository.save(binding);
        } catch (Exception e) {
            logger.error("Error create BINDING for order: " + merchantOrder + " and client: " + client,e);
            return null;
        }
    }

    public void update(MerchantOrder merchantOrder, BindingInfo bindingInfo) {
        if ( null == merchantOrder || null == bindingInfo ) return;
        Binding binding = bindingRepository.findByOrderId(merchantOrder.getOrderId());
        if ( null == binding ) {
            logger.warn("Unable to find BINDING by orderId: {}", merchantOrder.getOrderId());
            return;
        }
        if ( binding.isCompleted() ) {
            logger.debug("{} has already competed");
            return;
        }
        if ( OrderStatus.PAID.equals(merchantOrder.getOrderStatus()) && binding.getClient().getClientId().equals(bindingInfo.getClientId())) {
            binding.setEnabled(true);
            binding.setPaymentSecureType(merchantOrder.getPaymentSecureType());
            binding.setExternalBindingId(bindingInfo.getBindingId());
            bindingRepository.save(binding);
        }
        else if ( binding.isEnabled() ) {
            binding.setEnabled(false);
            bindingRepository.save(binding);
        }
    }

    public List<Binding> getBindings(Client client, PaymentWay... paymentWays) {
        return null != paymentWays && paymentWays.length > 0
                ? bindingRepository.findByClientAndPaymentsWays(client, paymentWays)
                : bindingRepository.findByClient(client);
    }

    public boolean isExists(Client client, PaymentParams paymentParams, PaymentWay paymentWay) {
        return getBindings(client,paymentWay).stream()
                .filter(b-> jsonService.jsonToPaymentParams(b.getPaymentParams(), paymentWay).equals(paymentParams))
                .findFirst().isPresent();
    }

    public Binding findByBindingId(String bindingId) {
        return bindingRepository.findByBindingId(bindingId);
    }

    //todo: должен быть крон который проверяет новые биндинги и делает внешние запросы на получение внешнего binding_id
}

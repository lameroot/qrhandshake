package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.binding.BindingDeleteResponse;
import ru.qrhandshake.qrpos.api.binding.GetBindingsRequest;
import ru.qrhandshake.qrpos.api.binding.GetBindingsResponse;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.BindingDto;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentRequest;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentResponse;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.repository.BindingRepository;
import ru.qrhandshake.qrpos.repository.EndpointRepository;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;

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
    @Resource
    private IntegrationSupportService integrationSupportService;
    @Resource
    private EndpointRepository endpointRepository;
    @Resource
    private MerchantOrderRepository merchantOrderRepository;


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
        if ( merchantOrder.getPaymentWay() == PaymentWay.BINDING ) return;
        if ( null == merchantOrder || null == bindingInfo ) return;
        Binding binding = bindingRepository.findByOrderId(merchantOrder.getOrderId());
        if ( null == binding ) {
            logger.debug("For order with orderId: {} not found binding.", merchantOrder.getOrderId());
            return;
        }
        if ( binding.isCompleted() ) {
            logger.debug("{} has already competed", binding);
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

    //todo: объединить с методом выше
    public void update(String orderId, BindingInfo bindingInfo, OrderStatus orderStatus, PaymentSecureType paymentSecureType) {
        if (StringUtils.isBlank(orderId) || null == bindingInfo ) return;
        Binding binding = bindingRepository.findByOrderId(orderId);
        if ( null == binding ) {
            logger.warn("Unable to find BINDING by orderId: {}", orderId);
            return;
        }
        if ( binding.isCompleted() ) {
            logger.debug("{} has already competed", binding);
            return;
        }
        if ( OrderStatus.PAID.equals(orderStatus) && binding.getClient().getClientId().equals(bindingInfo.getClientId())) {
            binding.setEnabled(true);
            binding.setPaymentSecureType(paymentSecureType);
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
                : bindingRepository.findByClientAndEnabled(client,true);
    }

    public boolean isExists(Client client, PaymentParams paymentParams, PaymentWay paymentWay) {
        return null != findByClientAndPaymentParams(client, paymentParams, paymentWay);
    }

    public Binding findByClientAndPaymentParams(Client client, PaymentParams paymentParams, PaymentWay paymentWay) {
        return getBindings(client,paymentWay).stream()
                .filter(b-> jsonService.jsonToPaymentParams(b.getPaymentParams(), paymentWay).equals(paymentParams))
                .findFirst()
                .orElse(null);
    }

    public Binding findByBindingId(String bindingId) {
        return bindingRepository.findByBindingId(bindingId);
    }

    //todo: должен быть крон который проверяет новые биндинги и делает внешние запросы на получение внешнего binding_id

    @Deprecated
    public PaymentResult createBinding(Client client, Long amount, PaymentParams paymentParams, PaymentWay paymentWay, String orderId) {
        PaymentResult paymentResult = new PaymentResult();
        paymentResult.setOrderId(orderId);
        Binding binding = findByClientAndPaymentParams(client, paymentParams, paymentWay);
        if ( null != binding ) {
            logger.info("Binding for {} and {} and paymentWay: {} has already exist", client, paymentParams, paymentWay);
            paymentResult.setBindingId(binding.getBindingId());
            paymentResult.setCode(0);
            paymentResult.setMessage("Binding has already exists");

            return paymentResult;
        }

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(orderId);
        if ( null == merchantOrder ) {
            logger.error("Unable to find merchantOrder by orderId: {}", orderId);
            paymentResult.setCode(0);
            paymentResult.setMessage("Order not found with orderId: " + orderId);
            return paymentResult;
        }
        IntegrationSupport integrationSupport = integrationSupportService.checkIntegrationSupport(merchantOrder.getMerchant(), paymentParams);
        Endpoint endpoint = endpointRepository.findByMerchantAndIntegrationSupport(merchantOrder.getMerchant(), integrationSupport);
        IntegrationPaymentRequest integrationPaymentRequest = new IntegrationPaymentRequest(endpoint);
        integrationPaymentRequest.setAmount(amount);
        integrationPaymentRequest.setOrderId(orderId);
        integrationPaymentRequest.setPaymentParams(paymentParams);
        integrationPaymentRequest.setClient(client);
        integrationPaymentRequest.setDescription("create binding request");
        integrationPaymentRequest.setIp(paymentParams.getIp());
        integrationPaymentRequest.setPaymentWay(paymentWay);
        integrationPaymentRequest.setReturnUrl(paymentParams.getReturnUrl());

        try {
            IntegrationPaymentResponse integrationPaymentResponse = integrationService.payment(integrationPaymentRequest);

            if ( integrationPaymentResponse.isSuccess() ) {
                logger.debug("Payment on {} and orderId {} via {} was success, let's try to create binding", amount, orderId, integrationSupport);
                binding = new Binding();
                binding.setPaymentSecureType(integrationPaymentResponse.getPaymentSecureType());
                binding.setClient(client);
                binding.setExternalBindingId(null);
                binding.setIntegrationSupport(integrationSupport);
                binding.setPaymentParams(jsonService.paymentParamsToJsonString(paymentParams));
                binding.setEnabled(false);
                binding.setBindingId(UUID.randomUUID().toString());
                binding.setOrderId(integrationPaymentRequest.getOrderId());
                binding.setPaymentWay(paymentWay);
                binding.setPaymentSecureType(integrationPaymentResponse.getPaymentSecureType());

                bindingRepository.save(binding);

                paymentResult.setReturnUrlObject(integrationPaymentResponse.getReturnUrlObject());
                paymentResult.setBindingId(binding.getBindingId());
                paymentResult.setMessage("Binding created");
                paymentResult.setCode(1);
                paymentResult.setOrderId(orderId);
                paymentResult.setOrderStatus(integrationPaymentResponse.getOrderStatus());

                logger.debug("{} was created successfully", binding);
            }
            else {
                paymentResult.setCode(0);
                paymentResult.setMessage("Error integration via " + integrationSupport);
            }

        } catch (IntegrationException e) {
            logger.error("Error integration payment for create binding",e);
            paymentResult.setCode(0);
            paymentResult.setMessage("Error create binding");
        }

        return paymentResult;
    }

    public FinishResult finish(FinishParams finishParams) {
        FinishResult finishResult = new FinishResult();

        Binding binding = bindingRepository.findByOrderId(finishParams.getOrderId());
        if ( null == binding ) {
            return new FinishResult.Result().setErrorMessage("Binding not exists").build();
        }

        //todo: нужно проводить операцию создания связки для основного мерчанта, а здесь использовать уже его параметры, так как бещ заказа и мерчанта такое не получится
        //возможно использовтаь методы bindCard


        return finishResult;
    }

    public BindingDeleteResponse delete(Client client, String bindingId, boolean returnNewBindingList) {
        BindingDeleteResponse bindingDeleteResponse = new BindingDeleteResponse();
        Binding binding = bindingRepository.findByBindingId(bindingId);
        if ( null == binding ) {
            bindingDeleteResponse.setStatus(ResponseStatus.FAIL);
            bindingDeleteResponse.setMessage("Binding with id: " + bindingId + " not found");
            return bindingDeleteResponse;
        }
        if ( client.getId() != binding.getClient().getId() ) {
            bindingDeleteResponse.setStatus(ResponseStatus.FAIL);
            bindingDeleteResponse.setMessage("Binding with id: " + bindingId + " not belongs to client");
            return bindingDeleteResponse;
        }
        bindingRepository.delete(binding);
        if ( returnNewBindingList ) {
            List<Binding> bindings = bindingRepository.findByClientAndEnabled(client, true);
            if ( null != bindings && !bindings.isEmpty() ) {
                for (Binding newBinding : bindings) {
                    BindingDto bindingDto = new BindingDto();
                    bindingDto.setBindingId(newBinding.getBindingId());
                    bindingDto.setPaymentWay(newBinding.getPaymentWay());
                    bindingDto.setPaymentParams(jsonService.jsonToPaymentParams(newBinding.getPaymentParams(), PaymentParams.class));
                    bindingDeleteResponse.getBindings().add(bindingDto);
                }
            }
        }
        bindingDeleteResponse.setStatus(ResponseStatus.SUCCESS);
        bindingDeleteResponse.setMessage("Binding with id: " + bindingId + " was deleted successfully");
        return bindingDeleteResponse;
    }

    public GetBindingsResponse getBindings(Client client, GetBindingsRequest getBindingsRequest) {
        GetBindingsResponse getBindingsResponse = new GetBindingsResponse();
        if ( null == client ) {
            getBindingsResponse.setStatus(ResponseStatus.FAIL);
            getBindingsResponse.setMessage("GetBinding request requires client auth");
            return getBindingsResponse;
        }

        List<Binding> bindings = getBindings(client, null != getBindingsRequest.getPaymentWays() ? getBindingsRequest.getPaymentWays().toArray(new PaymentWay[]{}) : null);
        for (Binding binding : bindings) {
            BindingDto bindingDto = new BindingDto();
            bindingDto.setBindingId(binding.getBindingId());
            bindingDto.setPaymentWay(binding.getPaymentWay());
            bindingDto.setPaymentParams(jsonService.jsonToPaymentParams(binding.getPaymentParams(), PaymentParams.class));
            getBindingsResponse.getBindings().add(bindingDto);
        }
        getBindingsResponse.setStatus(ResponseStatus.SUCCESS);
        getBindingsResponse.setMessage("Get bindings success");

        return getBindingsResponse;
    }
}

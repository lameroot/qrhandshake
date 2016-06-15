package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.BindingDto;
import ru.qrhandshake.qrpos.integration.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lameroot on 25.05.16.
 */
@Service
public class OrderService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private MerchantOrderRepository merchantOrderRepository;
    @Resource
    private IntegrationService integrationService;
    @Resource
    private IntegrationSupportService integrationSupportService;
    @Resource
    private BindingService bindingService;

    public MerchantOrderRegisterResponse register(Terminal terminal, MerchantOrderRegisterRequest merchantOrderRegisterRequest) {
        Merchant merchant = terminal.getMerchant();

        MerchantOrder merchantOrder = new MerchantOrder();
        merchantOrder.setMerchant(merchant);
        merchantOrder.setTerminal(terminal);
        merchantOrder.setDeviceId(merchantOrderRegisterRequest.getDeviceId());
        merchantOrder.setAmount(merchantOrderRegisterRequest.getAmount());
        merchantOrder.setDescription(merchantOrderRegisterRequest.getDescription());
        merchantOrder.setOrderStatus(OrderStatus.REGISTERED);
        merchantOrder.setSessionId(merchantOrderRegisterRequest.getSessionId());
        merchantOrderRepository.save(merchantOrder);

        String orderId = generateUniqueIdOrder(merchantOrder);
        String paymentUrl = buildPaymentUrl(merchantOrder, orderId);
        merchantOrder.setOrderId(orderId);
        merchantOrderRepository.save(merchantOrder);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = new MerchantOrderRegisterResponse();
        merchantOrderRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        merchantOrderRegisterResponse.setMessage("Merchant order success created");
        merchantOrderRegisterResponse.setOrderId(orderId);
        merchantOrderRegisterResponse.setPaymentUrl(paymentUrl);

        return merchantOrderRegisterResponse;
    }

    public MerchantOrderStatusResponse getOrderStatus(Terminal terminal, MerchantOrderStatusRequest merchantOrderStatusRequest) throws AuthException {
        MerchantOrderStatusResponse merchantOrderStatusResponse = new MerchantOrderStatusResponse();
        MerchantOrder merchantOrder = findByOrderId(merchantOrderStatusRequest.getOrderId());
        if ( null == merchantOrder ) {
            merchantOrderStatusResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderStatusResponse.setMessage("Order with id: " + merchantOrderStatusRequest.getOrderId() + " not found");
            return merchantOrderStatusResponse;
        }
        if ( !terminal.getMerchant().equals(merchantOrder.getMerchant()) ) {
            throw new AuthException("Order with id:" + merchantOrderStatusRequest.getOrderId() + " not belongs to own merchant");
        }
        if ( doExternalOrderStatusRequest(merchantOrder) || merchantOrderStatusRequest.isExternalRequest() ) {//делаем в этом случае всегда запрос к внешней системе
            try {
                IntegrationOrderStatusResponse integrationOrderStatusResponse = doInnerGetOrderStatus(merchantOrder);
                merchantOrderStatusResponse.setMessage(integrationOrderStatusResponse.getMessage());
                if ( integrationOrderStatusResponse.isSuccess() ) {
                    merchantOrderStatusResponse.setStatus(ResponseStatus.SUCCESS);
                }
                else {
                    merchantOrderStatusResponse.setStatus(ResponseStatus.FAIL);
                }
            } catch (IntegrationException e) {
                merchantOrderStatusResponse.setStatus(ResponseStatus.FAIL);
                merchantOrderStatusResponse.setMessage("Unable to get external order status for order:" + merchantOrder.getId());
                merchantOrderStatusResponse.setAmount(merchantOrder.getAmount());
                merchantOrderStatusResponse.setOrderId(merchantOrder.getOrderId());
                merchantOrderStatusResponse.setOrderStatus(merchantOrder.getOrderStatus());
                return merchantOrderStatusResponse;
            }
        }
        merchantOrderStatusResponse.setAmount(merchantOrder.getAmount());
        merchantOrderStatusResponse.setStatus(ResponseStatus.SUCCESS);
        merchantOrderStatusResponse.setMessage("Order status for: " + merchantOrder.getId() + " received");
        merchantOrderStatusResponse.setOrderStatus(merchantOrder.getOrderStatus());
        merchantOrderStatusResponse.setOrderId(merchantOrder.getOrderId());

        return merchantOrderStatusResponse;
    }

    public PaymentResponse payment(Client client, PaymentRequest paymentRequest, Model model) throws AuthException {
        PaymentResponse paymentResponse = new PaymentResponse();

        MerchantOrder merchantOrder = findByOrderId(paymentRequest.getOrderId());
        if ( null == merchantOrder ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("Order: " + paymentRequest.getOrderId() + " not found");
            paymentResponse.setOrderId(paymentRequest.getOrderId());
            return paymentResponse;
        }
        if ( !merchantOrder.canPayment() ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setOrderStatus(merchantOrder.getOrderStatus());
            paymentResponse.setMessage("Order: " + merchantOrder.getOrderId() + " has invalid status: " + merchantOrder.getOrderStatus() + " for payment.");
            return paymentResponse;
        }

        if ( paymentRequest instanceof CardPaymentRequest ) {
            paymentResponse = cardPayment(client, (CardPaymentRequest)paymentRequest, model, merchantOrder);
        }
        else if ( paymentRequest instanceof BindingPaymentRequest ) {
            paymentResponse = bindingPayment(client, (BindingPaymentRequest) paymentRequest, model, merchantOrder);
        }
        else {
            logger.warn("Unknown type of payment request: {}", paymentRequest);
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("Unknown type of payment request: " +  paymentRequest);
        }
        return paymentResponse;
    }

    private PaymentResponse bindingPayment(Client client, BindingPaymentRequest paymentRequest, Model model, MerchantOrder merchantOrder) throws AuthException {
        PaymentResponse paymentResponse = new PaymentResponse();
        if ( null != client ) {
            paymentResponse.setPaymentAuthType(PaymentAuthType.CLIENT_AUTH);
        }
        else {
            throw new AuthException("Client not auth");
        }
        Binding binding = bindingService.findByBindingId(paymentRequest.getPaymentParams().getBindingId());
        if ( null == binding ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("Unable to find BINDING by bindingId: " + paymentRequest.getPaymentParams().getBindingId());
            return paymentResponse;
        }
        if ( !client.getClientId().equals(binding.getClient().getClientId()) ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("ClientId: " + binding.getClient().getClientId() + " not equals clientId of current client");
            return paymentResponse;
        }
        IntegrationSupport integrationSupport = binding.getIntegrationSupport();
        if ( null == integrationSupport ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setOrderStatus(merchantOrder.getOrderStatus());
            paymentResponse.setMessage("Unknown integration support for orderId: " + paymentRequest.getOrderId());
            return paymentResponse;
        }
        merchantOrder.setIntegrationSupport(integrationSupport);
        merchantOrder.setPaymentWay(paymentRequest.getPaymentWay());
        IntegrationPaymentBindingRequest integrationPaymentBindingRequest = new IntegrationPaymentBindingRequest(integrationSupport,binding.getExternalBindingId());
        integrationPaymentBindingRequest.setPaymentParams(paymentRequest.getPaymentParams());
        integrationPaymentBindingRequest.setAmount(merchantOrder.getAmount());
        integrationPaymentBindingRequest.setClient(client);
        integrationPaymentBindingRequest.setDescription(merchantOrder.getDescription());
        integrationPaymentBindingRequest.setOrderId(paymentRequest.getOrderId());
        integrationPaymentBindingRequest.setReturnUrl(paymentRequest.getReturnUrl());
        integrationPaymentBindingRequest.setParams(integrationParams(merchantOrder));
        integrationPaymentBindingRequest.setOrderStatus(merchantOrder.getOrderStatus());
        integrationPaymentBindingRequest.setPaymentWay(paymentRequest.getPaymentWay());
        integrationPaymentBindingRequest.setModel(model);
        integrationPaymentBindingRequest.setIp(paymentRequest.getIp());
        integrationPaymentBindingRequest.setBindingId(binding.getBindingId());

        paymentResponse.setBindingId(binding.getBindingId());
        try {
            IntegrationPaymentResponse integrationPaymentResponse = integrationService.paymentBinding(integrationPaymentBindingRequest);
            paymentResponse.setStatus(ResponseStatus.SUCCESS);
            paymentResponse.setOrderId(merchantOrder.getOrderId());
            paymentResponse.setMessage(integrationPaymentResponse.getMessage());
            merchantOrder.setPaymentSecureType(integrationPaymentResponse.getPaymentSecureType());
            if ( integrationPaymentResponse.isSuccess() ) {
                if (!merchantOrder.getOrderStatus().equals(integrationPaymentResponse.getOrderStatus())) {
                    merchantOrder.setOrderStatus(integrationPaymentResponse.getOrderStatus());
                    merchantOrder.setExternalOrderStatus(integrationPaymentResponse.getIntegrationOrderStatus().getStatus());
                    merchantOrder.setExternalId(integrationPaymentResponse.getExternalId());
                    paymentResponse.setMessage("Paid by BINDING successfully");

                } else {
                    paymentResponse.setMessage("Paid by BINDING successfully but external status wasn't changed");
                }
            } else {
                paymentResponse.setStatus(ResponseStatus.FAIL);
            }
            paymentResponse.setOrderStatus(merchantOrder.getOrderStatus());
            paymentResponse.setRedirectUrlOrPagePath(integrationPaymentResponse.getRedirectUrlOrPagePath());
            merchantOrderRepository.save(merchantOrder);
        } catch (IntegrationException e) {
            logger.error("Error BINDING payment by orderId:" + paymentRequest.getOrderId(),e);
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("Error external BINDING payment by id: " + paymentRequest.getOrderId());
            paymentResponse.setOrderId(paymentRequest.getOrderId());
        }
        return paymentResponse;
    }

    private PaymentResponse cardPayment(Client client, CardPaymentRequest paymentRequest, Model model, MerchantOrder merchantOrder) {
        PaymentResponse paymentResponse = new PaymentResponse();
        if ( null != client ) {
            paymentResponse.setPaymentAuthType(PaymentAuthType.CLIENT_AUTH);
        }
        IntegrationSupport integrationSupport = integrationSupportService.checkIntegrationSupport(merchantOrder.getMerchant(), paymentRequest);
        if ( null == integrationSupport ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setOrderStatus(merchantOrder.getOrderStatus());
            paymentResponse.setMessage("Unknown integration support for orderId: " + paymentRequest.getOrderId());
            return paymentResponse;
        }
        merchantOrder.setPaymentWay(paymentRequest.getPaymentWay());
        merchantOrder.setIntegrationSupport(integrationSupport);
        merchantOrder.setClient(client);
        IntegrationPaymentRequest integrationPaymentRequest = new IntegrationPaymentRequest(integrationSupport);
        integrationPaymentRequest.setPaymentParams(paymentRequest.getPaymentParams());
        integrationPaymentRequest.setAmount(merchantOrder.getAmount());
        integrationPaymentRequest.setClient(client);
        integrationPaymentRequest.setDescription(merchantOrder.getDescription());
        integrationPaymentRequest.setOrderId(paymentRequest.getOrderId());
        integrationPaymentRequest.setReturnUrl(paymentRequest.getReturnUrl());
        integrationPaymentRequest.setParams(integrationParams(merchantOrder));
        integrationPaymentRequest.setOrderStatus(merchantOrder.getOrderStatus());
        integrationPaymentRequest.setPaymentWay(paymentRequest.getPaymentWay());
        integrationPaymentRequest.setModel(model);
        integrationPaymentRequest.setIp(paymentRequest.getIp());

        try {
            IntegrationPaymentResponse integrationPaymentResponse = integrationService.payment(integrationPaymentRequest);
            paymentResponse.setStatus(ResponseStatus.SUCCESS);
            paymentResponse.setOrderId(merchantOrder.getOrderId());
            paymentResponse.setMessage(integrationPaymentResponse.getMessage());
            merchantOrder.setPaymentSecureType(integrationPaymentResponse.getPaymentSecureType());
            merchantOrder.setOrderStatus(integrationPaymentResponse.getOrderStatus());
            merchantOrder.setExternalOrderStatus(integrationPaymentResponse.getIntegrationOrderStatus().getStatus());
            merchantOrder.setExternalId(integrationPaymentResponse.getExternalId());
            merchantOrder.setPaymentType(integrationPaymentResponse.getPaymentType());
            if ( integrationPaymentResponse.isSuccess() ) {
                if (merchantOrder.getOrderStatus() == OrderStatus.PAID) {
                    paymentResponse.setMessage("Paid successfully");

                    if ( null != client && merchantOrder.getMerchant().isCreateBinding()
                            && !bindingService.isExists(client, paymentRequest.getPaymentParams(), paymentRequest.getPaymentWay() )) {
                        Binding binding = bindingService.register(client, paymentRequest.getPaymentParams(), merchantOrder, false);
                        if ( null != binding ) {
                            logger.debug("Successfully BINDING created: {}", binding);
                            paymentResponse.setBindingId(binding.getBindingId());
                        }
                        else {
                            logger.warn("Binding not created for order: {}", merchantOrder);
                        }
                    }
                } else {
                    paymentResponse.setMessage("Paid successfully but external status wasn't changed");
                }
            } else {
                paymentResponse.setStatus(ResponseStatus.FAIL);
            }
            paymentResponse.setOrderStatus(merchantOrder.getOrderStatus());
            paymentResponse.setRedirectUrlOrPagePath(integrationPaymentResponse.getRedirectUrlOrPagePath());
            merchantOrderRepository.save(merchantOrder);
        } catch (IntegrationException e) {
            logger.error("Error payment by orderId:" + paymentRequest.getOrderId(),e);
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("Error external payment by id: " + paymentRequest.getOrderId());
            paymentResponse.setOrderId(paymentRequest.getOrderId());
        }
        return paymentResponse;
    }

    public MerchantOrderReverseResponse reverse(Terminal terminal, MerchantOrderReverseRequest merchantOrderReverseRequest) throws AuthException {
        MerchantOrderReverseResponse merchantOrderReverseResponse = new MerchantOrderReverseResponse();
        merchantOrderReverseResponse.setOrderId(merchantOrderReverseRequest.getOrderId());

        MerchantOrder merchantOrder = findByOrderId(merchantOrderReverseRequest.getOrderId());
        if ( null == merchantOrder ) {
            merchantOrderReverseResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderReverseResponse.setMessage("Order with id: " + merchantOrderReverseResponse.getOrderId() + " not found");
            return merchantOrderReverseResponse;
        }
        if ( !isSessionValid(merchantOrder, merchantOrderReverseRequest.getSessionId()) ) {
            throw new AuthException("Invalid sessionId");
        }
        if ( !terminal.getMerchant().equals(merchantOrder.getMerchant()) ) {
            throw new AuthException("Order with id:" + merchantOrderReverseRequest.getOrderId() + " not belongs to own merchant");
        }
        if ( !merchantOrder.getOrderStatus().equals(OrderStatus.PAID) ) {
            merchantOrderReverseResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderReverseResponse.setMessage("Order with id: " + merchantOrderReverseRequest.getOrderId() + " has invalid status: " + merchantOrder.getOrderStatus() + ", must be PAID");
            return merchantOrderReverseResponse;
        }
        try {
            IntegrationReverseRequest integrationReverseRequest = new IntegrationReverseRequest(merchantOrder.getIntegrationSupport(),merchantOrder.getExternalId());
            integrationReverseRequest.setOrderId(merchantOrderReverseRequest.getOrderId());

            IntegrationReverseResponse integrationReverseResponse = integrationService.reverse(integrationReverseRequest);
            if ( integrationReverseResponse.isSuccess() ) {
                MerchantOrderStatusRequest merchantOrderStatusRequest = new MerchantOrderStatusRequest();
                merchantOrderStatusRequest.setOrderId(merchantOrder.getOrderId());
                merchantOrderStatusRequest.setExternalRequest(true);
                MerchantOrderStatusResponse merchantOrderStatusResponse = getOrderStatus(terminal, merchantOrderStatusRequest);
                if ( merchantOrderStatusResponse.getStatus().equals(ResponseStatus.SUCCESS) ) {
                    merchantOrderReverseResponse.setStatus(ResponseStatus.SUCCESS);
                }
                else {
                    merchantOrderReverseResponse.setMessage(merchantOrderStatusResponse.getMessage());
                }
            }
            merchantOrderReverseResponse.setMessage(integrationReverseResponse.getMessage());
        } catch (IntegrationException e) {
            logger.error("Error reverse order by orderId: " + merchantOrderReverseRequest.getOrderId(),e);
            merchantOrderReverseResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderReverseResponse.setMessage("Error external reverse by orderId: " + merchantOrderReverseRequest.getOrderId());

        }
        return merchantOrderReverseResponse;
    }

    public FinishResponse finish(FinishRequest finishRequest) {
        FinishResponse finishResponse = new FinishResponse();
        MerchantOrder merchantOrder = findByOrderId(finishRequest.getOrderId());
        if ( null == merchantOrder ) {
            finishResponse.setStatus(ResponseStatus.FAIL);
            finishResponse.setMessage("Order with id:" + finishRequest.getOrderId() + " doesn't exists");
            return finishResponse;
        }
        if ( null == merchantOrder.getIntegrationSupport() || StringUtils.isBlank(merchantOrder.getExternalId()) ) {
            finishResponse.setStatus(ResponseStatus.FAIL);
            finishResponse.setMessage("Order with id: " + finishRequest.getOrderId() + " has invalid params");
            return finishResponse;
        }
        try {
            IntegrationOrderStatusResponse integrationOrderStatusResponse = doInnerGetOrderStatus(merchantOrder);
            finishResponse.setStatus(integrationOrderStatusResponse.isSuccess() ? ResponseStatus.SUCCESS : ResponseStatus.FAIL);
            finishResponse.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
            finishResponse.setOrderId(integrationOrderStatusResponse.getOrderId());
            finishResponse.setMessage(integrationOrderStatusResponse.getMessage());
        } catch (IntegrationException e) {
            finishResponse.setStatus(ResponseStatus.FAIL);
            finishResponse.setMessage("Error finish payment with order:" + finishRequest.getOrderId() + ", cause: " + e.getMessage());
        }
        return finishResponse;
    }

    public GetBindingsResponse getBindings(Client client, GetBindingsRequest getBindingsRequest) {
        GetBindingsResponse getBindingsResponse = new GetBindingsResponse();
        if ( null == client ) {
            getBindingsResponse.setStatus(ResponseStatus.FAIL);
            getBindingsResponse.setMessage("GetBinding request requires client auth");
            return getBindingsResponse;
        }

        List<Binding> bindings = bindingService.getBindings(client, null != getBindingsRequest.getPaymentWays() ? getBindingsRequest.getPaymentWays().toArray(new PaymentWay[]{}) : null);
        for (Binding binding : bindings) {
            BindingDto bindingDto = new BindingDto();
            bindingDto.setBindingId(binding.getBindingId());
            bindingDto.setPaymentWay(binding.getPaymentWay());
            bindingDto.setPaymentParams(binding.getPaymentParams());
            getBindingsResponse.getBindings().add(bindingDto);
            getBindingsResponse.setStatus(ResponseStatus.SUCCESS);
            getBindingsResponse.setMessage("Get bindings success");
        }

        return getBindingsResponse;
    }

    public MerchantOrder findByOrderId(String orderId) {
        return merchantOrderRepository.findByOrderId(orderId);
    }

    private IntegrationOrderStatusResponse doInnerGetOrderStatus(MerchantOrder merchantOrder) throws IntegrationException {
        IntegrationOrderStatusRequest integrationOrderStatusRequest = new IntegrationOrderStatusRequest(merchantOrder.getIntegrationSupport(),merchantOrder.getExternalId());
        IntegrationOrderStatusResponse integrationOrderStatusResponse = integrationService.getOrderStatus(integrationOrderStatusRequest);
        integrationOrderStatusResponse.setOrderId(merchantOrder.getOrderId());
        if ( !merchantOrder.getOrderStatus().equals(integrationOrderStatusResponse.getOrderStatus()) ) {
            merchantOrder.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
            merchantOrder.setExternalOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus().getStatus());
            merchantOrderRepository.save(merchantOrder);
        }
        bindingService.update(merchantOrder, integrationOrderStatusResponse.getBindingInfo());
        return integrationOrderStatusResponse;
    }

    private boolean doExternalOrderStatusRequest(MerchantOrder merchantOrder) {
        OrderStatus orderStatus = merchantOrder.getOrderStatus();
        return null != orderStatus && orderStatus.equals(OrderStatus.REGISTERED) && StringUtils.isNotBlank(merchantOrder.getExternalId());
    }

    private String buildPaymentUrl(MerchantOrder merchantOrder, String orderId) {
        return MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH + "/" + orderId;
    }

    private String generateUniqueIdOrder(MerchantOrder merchantOrder) {
        return String.valueOf(merchantOrder.getId());
    }

    private boolean isSessionValid(MerchantOrder merchantOrder, String sessionId) {
        return merchantOrder.getSessionId().equals(sessionId);
    }

    private Map<String,String> integrationParams(MerchantOrder merchantOrder) {
        Map<String,String> params = new HashMap<>();
        params.put("merchant.name",merchantOrder.getMerchant().getName());
        params.put("merchant.id",String.valueOf(merchantOrder.getMerchant().getId()));
        params.put("merchant.terminal.id",String.valueOf(merchantOrder.getTerminal().getId()));
        params.put("merchant.terminal.deviceId",merchantOrder.getDeviceId());
        params.put("merchant.terminal.sessionId",merchantOrder.getSessionId());
        if ( null != merchantOrder.getClient() ) {
            params.put("client.id", String.valueOf(merchantOrder.getClient().getId()));
        }
        return params;
    }
}

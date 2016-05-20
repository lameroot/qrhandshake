package ru.qrhandshake.qrpos.service;

import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.dto.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.IllegalOrderStatusException;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by lameroot on 18.05.16.
 */
@Service
public class MerchantOrderService {

    @Resource
    private MerchantOrderRepository merchantOrderRepository;
    @Resource
    private MerchantService merchantService;
    @Resource
    private QrService qrService;
    @Resource
    private IntegrationService integrationService;//возможно надо перенсти в одно место

    public MerchantOrderRegisterResponse register(String contextPath, MerchantOrderRegisterRequest merchantOrderRegisterRequest) throws AuthException {
        Merchant merchant = merchantService.loadMerchant(merchantOrderRegisterRequest);
        MerchantOrder merchantOrder = new MerchantOrder();
        merchantOrder.setDescription(merchantOrderRegisterRequest.getDescription());
        merchantOrder.setMerchant(merchant);
        merchantOrder.setAmount(merchantOrderRegisterRequest.getAmount());
        merchantOrder.setClient(toClient(merchantOrderRegisterRequest.getClient()));

        merchantOrderRepository.save(merchantOrder);
        String paymentUrl = buildPaymentUrl(contextPath,merchantOrder);
        File qrImg = qrService.generate(paymentUrl);
        String qrUrl = contextPath + MerchantOrderController.QR_PATH + "/" + qrImg.getName();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = new MerchantOrderRegisterResponse();
        merchantOrderRegisterResponse.setPaymentUrl(paymentUrl);
        merchantOrderRegisterResponse.setQrUrl(qrUrl);
        merchantOrderRegisterResponse.setResponseCode(ResponseCode.SUCCESS);
        merchantOrderRegisterResponse.setResponseMessage("Success");

        return merchantOrderRegisterResponse;
    }

    public MerchantOrderStatusResponse getOrderStatus(MerchantOrderStatusRequest merchantOrderStatusRequest)
            throws MerchantOrderNotFoundException, AuthException, IllegalOrderStatusException {
        Merchant merchant = merchantService.loadMerchant(merchantOrderStatusRequest);
        MerchantOrder merchantOrder = Optional.ofNullable(merchantOrderRepository.findOne(Long.parseLong(merchantOrderStatusRequest.getOrderId())))
                .orElseThrow(()-> new MerchantOrderNotFoundException("Order not found by id: " + merchantOrderStatusRequest.getOrderId()));
        if ( !merchantOrder.getMerchant().equals(merchant) ) {
            throw new AuthException("Order with id:" + merchantOrderStatusRequest.getOrderId() + " not belongs to own merchant");
        }
        MerchantOrderStatusResponse merchantOrderStatusResponse = new MerchantOrderStatusResponse();
        merchantOrderStatusResponse.setAmount(merchantOrder.getAmount());
        merchantOrderStatusResponse.setOrderId(merchantOrderStatusRequest.getOrderId());
        if ( null == merchantOrder.getStatus() || merchantOrderStatusRequest.isExternalRequest() ) {
            IntegrationOrderStatusRequest integrationOrderStatusRequest = new IntegrationOrderStatusRequest(merchantOrder.getExternalId());
            try {
                IntegrationOrderStatusResponse integrationOrderStatusResponse = integrationService.getOrderStatus(integrationOrderStatusRequest);
                merchantOrderStatusResponse.setStatus(integrationOrderStatusResponse.getOrderStatus());

            } catch (IntegrationException e) {
                throw new IllegalOrderStatusException("Unknown status",null);
            }
        }
        else {
            merchantOrderStatusResponse.setStatus(merchantOrder.getStatus());
        }

        return merchantOrderStatusResponse;
    }

    public MerchantOrder findMerchantOrderByGeneratedId(String uniqueId) throws MerchantOrderNotFoundException{
        return Optional.ofNullable(merchantOrderRepository.findOne(Long.parseLong(uniqueId)))
                .orElseThrow(()->new MerchantOrderNotFoundException("Order not found by id:" + uniqueId));//данное выражение должно соспадать с политикой как генерился этот идентификатор в generateUniqueIdOrder
    }

    public IntegrationPaymentRequest toIntegrationPaymentRequest(String contextPath, PaymentRequest paymentRequest) throws MerchantOrderNotFoundException {
        MerchantOrder merchantOrder = findMerchantOrderByGeneratedId(paymentRequest.getOrderId());
        IntegrationPaymentRequest integrationPaymentRequest = new IntegrationPaymentRequest();
        integrationPaymentRequest.setAmount(merchantOrder.getAmount());
        integrationPaymentRequest.setCardHolderName(paymentRequest.getCardHolderName());
        integrationPaymentRequest.setClient(null);//todo: set data as ip
        integrationPaymentRequest.setCurrency(merchantOrder.getCurrency());
        integrationPaymentRequest.setCvc(paymentRequest.getCvc());
        integrationPaymentRequest.setDescription(merchantOrder.getDescription());
        integrationPaymentRequest.setLanguage(merchantOrder.getLanguage());
        integrationPaymentRequest.setOrderId(paymentRequest.getOrderId());
        integrationPaymentRequest.setMonth(paymentRequest.getMonth());
        integrationPaymentRequest.setYear(paymentRequest.getYear());
        integrationPaymentRequest.setPan(paymentRequest.getPan());
        integrationPaymentRequest.setReturnUrl("");//todo: set return url from contextPath
        integrationPaymentRequest.setParams(new HashMap<>());//todo: set params
        integrationPaymentRequest.setOrderStatus(merchantOrder.getStatus());

        return integrationPaymentRequest;
    }

    public void toMerchantOrder(IntegrationPaymentResponse integrationPaymentResponse) throws MerchantOrderNotFoundException {
        MerchantOrder merchantOrder = findMerchantOrderByGeneratedId(integrationPaymentResponse.getOrderId());
        merchantOrder.setStatus(integrationPaymentResponse.getStatus());
        merchantOrder.setExternalId(integrationPaymentResponse.getOrderId());

        merchantOrderRepository.save(merchantOrder);
    }

    private String buildPaymentUrl(String contextPath, MerchantOrder merchantOrder) {
        return contextPath + MerchantOrderController.PAYMENT_PATH + "/" + generateUniqueIdOrder(merchantOrder);
    }

    private String generateUniqueIdOrder(MerchantOrder merchantOrder) {
        return String.valueOf(merchantOrder.getId());
    }

    private Client toClient(ClientDto clientDto) {
        if ( null == clientDto ) return null;
        Client client = new Client();
        client.setAddress(clientDto.getAddress());
        client.setEmail(clientDto.getEmail());
        if ( null != clientDto.getLocation() ) {
            client.setLat(clientDto.getLocation().getLat());
            client.setLon(clientDto.getLocation().getLon());
        }
        client.setName(clientDto.getName());
        client.setPhone(clientDto.getPhone());
        return client;

    }

}

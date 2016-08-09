package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.BindingPaymentByOrderTemplateParams;
import ru.qrhandshake.qrpos.api.BindingPaymentByOrderTemplateRequest;
import ru.qrhandshake.qrpos.repository.OrderTemplateRepository;

import javax.annotation.Resource;

/**
 * Created by lameroot on 09.08.16.
 */
@Component
public class BindingPaymentByOrderTemplateRequestToBindingPaymentByOrderTemplateParamsConverter implements Converter<BindingPaymentByOrderTemplateRequest, BindingPaymentByOrderTemplateParams> {

    @Resource
    private OrderTemplateRepository orderTemplateRepository;

    @Override
    public BindingPaymentByOrderTemplateParams convert(BindingPaymentByOrderTemplateRequest bindingPaymentByOrderTemplateRequest) {
        BindingPaymentByOrderTemplateParams bindingPaymentByOrderTemplateParams = new BindingPaymentByOrderTemplateParams();
        bindingPaymentByOrderTemplateParams.setBindingId(bindingPaymentByOrderTemplateRequest.getBindingId());
        bindingPaymentByOrderTemplateParams.setDeviceId(bindingPaymentByOrderTemplateRequest.getDeviceId());
        bindingPaymentByOrderTemplateParams.setOrderTemplate(orderTemplateRepository.findOne(bindingPaymentByOrderTemplateRequest.getOrderTemplateId()));
        bindingPaymentByOrderTemplateParams.setSessionId(bindingPaymentByOrderTemplateRequest.getSessionId());
        return bindingPaymentByOrderTemplateParams;
    }
}

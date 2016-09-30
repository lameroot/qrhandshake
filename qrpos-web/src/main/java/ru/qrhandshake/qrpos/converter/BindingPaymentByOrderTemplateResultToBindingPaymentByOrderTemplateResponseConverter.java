package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentByOrderTemplateResponse;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentByOrderTemplateResult;
import ru.qrhandshake.qrpos.api.ResponseStatus;

/**
 * Created by lameroot on 09.08.16.
 */
@Component
public class BindingPaymentByOrderTemplateResultToBindingPaymentByOrderTemplateResponseConverter implements Converter<BindingPaymentByOrderTemplateResult, BindingPaymentByOrderTemplateResponse> {

    @Override
    public BindingPaymentByOrderTemplateResponse convert(BindingPaymentByOrderTemplateResult bindingPaymentByOrderTemplateResult) {
        BindingPaymentByOrderTemplateResponse bindingPaymentByOrderTemplateResponse = new BindingPaymentByOrderTemplateResponse();
        bindingPaymentByOrderTemplateResponse.setOrderId(bindingPaymentByOrderTemplateResult.getOrderId());
        bindingPaymentByOrderTemplateResponse.setStatus(bindingPaymentByOrderTemplateResult.isStatus() ? ResponseStatus.SUCCESS : ResponseStatus.FAIL);
        return bindingPaymentByOrderTemplateResponse;
    }
}

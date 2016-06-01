package ru.qrhandshake.qrpos.util;

import ru.paymentgate.engine.webservices.merchant.ServiceParam;

/**
 * Created by lameroot on 01.06.16.
 */
public class Util {

    public final static ServiceParam createServiceParam(String name, String value) {
        ServiceParam serviceParam = new ServiceParam();
        serviceParam.setName(name);
        serviceParam.setValue(value);
        return serviceParam;
    }
}

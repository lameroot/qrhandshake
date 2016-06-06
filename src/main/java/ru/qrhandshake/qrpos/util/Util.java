package ru.qrhandshake.qrpos.util;

import ru.paymentgate.engine.webservices.merchant.ServiceParam;

import java.util.Random;

/**
 * Created by lameroot on 01.06.16.
 */
public class Util {

    private static final char[] _base62chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnpqrstuvwxyz".toCharArray();

    public final static ServiceParam createServiceParam(String name, String value) {
        ServiceParam serviceParam = new ServiceParam();
        serviceParam.setName(name);
        serviceParam.setValue(value);
        return serviceParam;
    }

    public final static String generatePseudoUnique(int length) {
        Random _random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i=0; i<length; i++) {
            sb.append(_base62chars[_random.nextInt(36)]);
        }
        return sb.toString();
    }
}

package ru.qrhandshake.qrpos.util;

import ru.paymentgate.engine.webservices.merchant.ServiceParam;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by lameroot on 01.06.16.
 */
public class Util {

    private static final char[] _base62chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnpqrstuvwxyz".toCharArray();
    private static final char[] _base62Numbers = "1234567890".toCharArray();

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

    public final static String generatePseudoUniqueNumber(int length) {
        if ( length > _base62Numbers.length ) throw new IllegalArgumentException("Length param more than available");
        Random _random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i=0; i<length; i++) {
            sb.append(_base62Numbers[_random.nextInt(10)]);
        }
        return sb.toString();
    }

}

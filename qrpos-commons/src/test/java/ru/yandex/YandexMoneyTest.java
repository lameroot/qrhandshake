package ru.yandex;


/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 NBCO Yandex.Money LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


import com.yandex.money.api.exceptions.InsufficientScopeException;
import com.yandex.money.api.exceptions.InvalidRequestException;
import com.yandex.money.api.exceptions.InvalidTokenException;
import com.yandex.money.api.methods.*;
import com.yandex.money.api.methods.params.PhoneParams;
import com.yandex.money.api.model.Error;
import com.yandex.money.api.model.SimpleStatus;
import com.yandex.money.api.net.ApiRequest;
import com.yandex.money.api.net.DefaultApiClient;
import com.yandex.money.api.net.OAuth2Session;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

/**
 *
 */
public class YandexMoneyTest extends TestCase {

    private final String phoneNumber = "79267796753";
    private final BigDecimal amount = new BigDecimal(300.00);
    private final static String CLIENT_ID = "333CFEAD690EAA3120CFF3E38F3CF52FAFB6C49C1F216EB50627A2D3034DDF4B";
    private final static String ACCESS_TOKEN = "410011792756615.01B0D239BB9A466244A6304962103E64DF166E94F90C55A7E01429FEA2B72498DCC06A5F3BA12275A8037BED605E6F68A76A9B67092D8F634EA1BA5C93FE5195C9030B0866C016E847E685D0D980781D222838BC93DDB468EE10B2FFC8B8F2AB8A84AB84ED0D4E639B74E79278A9F843784B01C95BD4E6E04F02B7560C3F7FAE";

    private OAuth2Session session;

    private InstanceId respInstanceId;
    private InstanceId.Request reqInstanceId;

    private RequestExternalPayment respRequestExternalPayment;
    private RequestExternalPayment.Request reqRequestExternalPayment;
    private ProcessExternalPayment.Request reqProcessExternalPayment;

    @Test
    public void testInstanceIdSuccess()
            throws IOException, InsufficientScopeException, InvalidTokenException, InvalidRequestException {

        reqInstanceId = new InstanceId.Request(CLIENT_ID);
        respInstanceId = session.execute(reqInstanceId);

        Assert.assertEquals(respInstanceId.statusInfo.status, SimpleStatus.SUCCESS);
        Assert.assertNotNull(respInstanceId.instanceId);
        Assert.assertNull(respInstanceId.statusInfo.error);
    }

    @Test
    public void testInstanceIdFail()
            throws IOException, InsufficientScopeException, InvalidTokenException, InvalidRequestException {

        reqInstanceId = new InstanceId.Request(" ");
        respInstanceId = session.execute(reqInstanceId);

        Assert.assertEquals(respInstanceId.statusInfo.status, SimpleStatus.REFUSED);
        Assert.assertNotNull(respInstanceId.statusInfo.error);
        Assert.assertEquals(respInstanceId.statusInfo.error, Error.ILLEGAL_PARAM_CLIENT_ID);
        Assert.assertNull(respInstanceId.instanceId);
    }

    @Test
    public void testRequestExternalPayment()
            throws IOException, InsufficientScopeException, InvalidTokenException, InvalidRequestException {

        reqRequestExternalPayment = createRequestExternalPayment();
        respRequestExternalPayment = testRequestPayment(reqRequestExternalPayment);
    }

    @Test
    public void testRequestPayment()
            throws InvalidTokenException, InsufficientScopeException, InvalidRequestException, IOException {

        session.setAccessToken(ACCESS_TOKEN);
        testRequestPayment(createRequestPayment());
        session.setAccessToken(null);
    }

    @Test
    public void testRequestExternalFail()
            throws IOException, InsufficientScopeException, InvalidTokenException, InvalidRequestException {

        reqInstanceId = new InstanceId.Request(CLIENT_ID);
        respInstanceId = session.execute(reqInstanceId);

        HashMap<String, String> params = successRequestParams();
        reqRequestExternalPayment = RequestExternalPayment.Request.newInstance(
                respInstanceId.instanceId, " ", params);
        respRequestExternalPayment = testRequestPaymentFail(reqRequestExternalPayment);

        params = successRequestParams();
        params.remove("amount");
        reqRequestExternalPayment = RequestExternalPayment.Request.newInstance(
                respInstanceId.instanceId, PhoneParams.PATTERN_ID, params);
        respRequestExternalPayment = testRequestPaymentFail(reqRequestExternalPayment);

        params = successRequestParams();
        params.remove("phone-number");
        reqRequestExternalPayment = RequestExternalPayment.Request.newInstance(
                respInstanceId.instanceId, PhoneParams.PATTERN_ID, params);
        respRequestExternalPayment = testRequestPaymentFail(reqRequestExternalPayment);
    }

    @Test
    public void testProcessExternalPayment()
            throws IOException, InsufficientScopeException, InvalidTokenException, InvalidRequestException {

        reqRequestExternalPayment = createRequestExternalPayment();
        respRequestExternalPayment = session.execute(reqRequestExternalPayment);

        String successUri = "https://elbrus.yandex.ru/success";
        String failUri = "https://elbrus.yandex.ru/fail";
        reqProcessExternalPayment = new ProcessExternalPayment.Request(
                respInstanceId.instanceId, respRequestExternalPayment.requestId,
                successUri, failUri, false);
        testProcessPayment(reqProcessExternalPayment);
    }

    @Test
    public void testProcessPayment()
            throws InvalidTokenException, InsufficientScopeException, InvalidRequestException, IOException {

        session = new OAuth2Session(new DefaultApiClient(CLIENT_ID, true));
        session.setDebugLogging(true);
        session.setAccessToken(ACCESS_TOKEN);

        reqInstanceId = new InstanceId.Request(CLIENT_ID);
        respInstanceId = session.execute(reqInstanceId);

        RequestPayment requestPayment = session.execute(createRequestPayment());
        if (requestPayment.status == BaseRequestPayment.Status.SUCCESS) {
            ProcessPayment processPayment = session.execute(
                    new ProcessPayment.Request(requestPayment.requestId)
                            .setTestResult(ProcessPayment.TestResult.SUCCESS));
            Assert.assertNotNull(processPayment);
        } else {
            if (requestPayment.error != Error.TECHNICAL_ERROR) {
                Assert.assertEquals(requestPayment.error, Error.NOT_ENOUGH_FUNDS);
            }
        }
        session.setAccessToken(null);
    }

    @BeforeClass
    public void setUp() {

    }

    @Before
    public void beforeTest() {
        session = new OAuth2Session(new DefaultApiClient(CLIENT_ID, true));
        session.setDebugLogging(true);

        reqInstanceId = null;
        respInstanceId = null;

        reqRequestExternalPayment = null;
        respRequestExternalPayment = null;

        reqProcessExternalPayment = null;
    }

    private <T extends BaseRequestPayment> T testRequestPayment(ApiRequest<T> request)
            throws InvalidTokenException, InsufficientScopeException, InvalidRequestException, IOException {

        T response = session.execute(request);

        Assert.assertNotNull(response);
        if (response.status == BaseRequestPayment.Status.SUCCESS) {
            Assert.assertEquals(response.status, BaseRequestPayment.Status.SUCCESS);
            Assert.assertNull(response.error);
            Assert.assertNotNull(response.requestId);
            Assert.assertTrue(response.requestId.length() > 0);
            Assert.assertEquals(response.contractAmount.compareTo(amount), 0);
        } else {
            if (response.error != Error.TECHNICAL_ERROR) {
                Assert.assertEquals(response.error, Error.NOT_ENOUGH_FUNDS);
            }
        }

        return response;
    }

    private HashMap<String, String> successRequestParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("amount", amount.toPlainString());
        params.put("phone-number", phoneNumber);
        return params;
    }

    private <T extends BaseRequestPayment> T testRequestPaymentFail(ApiRequest<T> request)
            throws InvalidTokenException, InsufficientScopeException, InvalidRequestException, IOException {

        T response = session.execute(request);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.status, RequestExternalPayment.Status.REFUSED);
        Assert.assertNotNull(response.error);
        Assert.assertNotEquals(response.error, Error.UNKNOWN);
        Assert.assertNull(response.requestId);

        return response;
    }

    private <T extends BaseProcessPayment> void testProcessPayment(ApiRequest<T> request)
            throws InvalidTokenException, InsufficientScopeException, InvalidRequestException, IOException {

        T response = session.execute(request);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.status, ProcessExternalPayment.Status.EXT_AUTH_REQUIRED);
    }

    private RequestExternalPayment.Request createRequestExternalPayment()
            throws InvalidTokenException, InsufficientScopeException, InvalidRequestException, IOException {

        reqInstanceId = new InstanceId.Request(CLIENT_ID);
        respInstanceId = session.execute(reqInstanceId);

        HashMap<String, String> params = successRequestParams();
        return RequestExternalPayment.Request.newInstance(
                respInstanceId.instanceId, PhoneParams.PATTERN_ID, params);
    }

    private RequestPayment.Request createRequestPayment() {
        return RequestPayment.Request.newInstance(PhoneParams.newInstance(phoneNumber, amount))
                .setTestResult(RequestPayment.TestResult.SUCCESS);
    }
}


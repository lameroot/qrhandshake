package ru.qrhandshake.qrpos.integration.yandex;

import com.yandex.money.api.exceptions.InsufficientScopeException;
import com.yandex.money.api.exceptions.InvalidRequestException;
import com.yandex.money.api.exceptions.InvalidTokenException;
import com.yandex.money.api.methods.*;
import com.yandex.money.api.methods.params.P2pTransferParams;
import com.yandex.money.api.methods.params.PaymentParams;
import com.yandex.money.api.methods.params.PhoneParams;
import com.yandex.money.api.model.YandexMoneyCard;
import com.yandex.money.api.model.showcase.Showcase;
import com.yandex.money.api.net.DefaultApiClient;
import com.yandex.money.api.net.OAuth2Session;
import ru.qrhandshake.qrpos.api.PaymentRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Created by lameroot on 16.06.16.
 */
public class TestYandex {

    //http://money.yandex.ru/oauth/authorize?client_id=333CFEAD690EAA3120CFF3E38F3CF52FAFB6C49C1F216EB50627A2D3034DDF4B&response_type=code&redirect_uri=http://qwe.ddns.net/yandex.html&scope=account%2Dinfo%20operation%2Dhistory payment-p2p
    //http://qwe.ddns.net/yandex.html?code=D41467B60238C0167782B86EED95A8FDD55FF016468C15DB5930D3E0F99ED8C2E655E97C18362ED3A37FC0AF2B635599983EF864ADFEEF4871E1FAAF6B25FB62EBAFA24FC87D32097486172D8FCBFACC9D70556BEFC931501B7C32AAFC60DBE17DAA57AFA5641222D4B7478A5139476C73C551FB2D09CAD641D6B3A79296ADE9

    //https://app.getpostman.com/oauth2/callback
    //http://money.yandex.ru/oauth/token?code=0C1E877973208DE98875FEA1E273169DE9A99BDA71513EC49E2B5F6E603C9E39CAA35E88DBCFBC1CBEFB5DF4B64ACBD95D5073D1A609A5E7695004085F6BB700F92055706B6EB3C1FF55897CCEDDD17757294333D2FD7989E8463BAB3F8C03EB901A8C281DE207770BA7544969838F08BEBF0BA20F47D7F4B78D265005BBD4DA&client_id=333CFEAD690EAA3120CFF3E38F3CF52FAFB6C49C1F216EB50627A2D3034DDF4B&grant_type=authorization_code&redirect_uri=https://app.getpostman.com/oauth2/callback
    //{"access_token":"410011792756615.BBFD78005293C1FEAEF23EFDAD5FB8E5512E59280A38001B856A86EBE41EF80EB0C85FF58C62A23B62E5C2B4493A2B68EEAC4D356C8827A2F1BB9C4AB915E3F8C55437C907D8EA021958D50020478FBF7782FD30021AB4FBB825276231160EDD9D213439169DE8655688F12189FE4B324C5E9E505CD7B9AAF3C6758DA05C8871"}

    private final static String CLIENT_ID = "333CFEAD690EAA3120CFF3E38F3CF52FAFB6C49C1F216EB50627A2D3034DDF4B";
    private final static String ACCESS_TOKEN = "410011792756615.008D64DB62E52A52E5BE0CA7180250E8B0E623718C14B0EA2F29B8F7D4C6EA281662B719F585F872B0EDFD2EC0E6E2689052E9408EFAA87F486A60CB532E8FE8365E1D4B88E3FB740FF364A9C8755E712112D1D37B77EC259F87F9CEA94D80766B1F16FACC4C1C624C0C62296C6CE93CE3905A92E65DBD5F220AC90AD59EAF4D";

    private final static String MY_WALLET = "410011792756615";
    private final static String DESTINATION_WALLET = "41001390256356";

    public static void main(String[] args) throws InvalidTokenException, InsufficientScopeException, InvalidRequestException, IOException {
        InstanceId respInstanceId;
        InstanceId.Request reqInstanceId;

        RequestExternalPayment respRequestExternalPayment;
        RequestExternalPayment.Request reqRequestExternalPayment;
        ProcessExternalPayment.Request reqProcessExternalPayment;

        OAuth2Session session = new OAuth2Session(new DefaultApiClient(CLIENT_ID, true));
        session.setDebugLogging(true);

        session.setAccessToken(ACCESS_TOKEN);

        //reqInstanceId = new InstanceId.Request(CLIENT_ID);
        //respInstanceId = session.execute(reqInstanceId);

//        HashMap<String, String> params = successRequestParams();
//        RequestExternalPayment.Request rr = RequestExternalPayment.Request.newInstance(
//                respInstanceId.instanceId, PhoneParams.PATTERN_ID, params);

        /*
        RequestPayment requestPayment = session.execute(createRequestPayment());
        System.out.println(requestPayment);
        if (requestPayment.status == BaseRequestPayment.Status.SUCCESS) {
            ProcessPayment processPayment = session.execute(
                    new ProcessPayment.Request(requestPayment.requestId)
                            //.setTestResult(ProcessPayment.TestResult.SUCCESS)
            );
            System.out.println(processPayment);
            System.out.println(processPayment.status);

        }
        */


//        AccountInfo accountInfo = session.execute(createAccountInfoRequest());
//        System.out.println(accountInfo);


        /*
        OperationHistory operationHistory = session.execute(createOperationHistoryRequest());
        System.out.println(operationHistory);
        */



        RequestPayment requestPayment = session.execute(createRequestPayment());
        System.out.println(requestPayment);
        String requestId = requestPayment.requestId;
        System.out.println("requestId = " + requestId);
        ProcessPayment processPayment = session.execute(createProcessPayment(requestId));
        System.out.println(processPayment);



        session.setAccessToken(null);
    }

    private static HashMap<String, String> successRequestParams() {
        HashMap<String, String> params = new HashMap<>();
        //params.put("pattern_id", "phone-topup");
        params.put("amount", "10");
        params.put("phone-number", "79267796753");
        return params;
    }

    private static RequestPayment.Request createRequestPayment() {
        PaymentParams paymentParams = PhoneParams.newInstance("79267796753", new BigDecimal(10.00));
        PaymentParams paymentParams1 = new P2pTransferParams.Builder(DESTINATION_WALLET).setAmount(new BigDecimal(10.0))
                .create();
        return RequestPayment.Request.newInstance(paymentParams1).setTestResult(RequestPayment.TestResult.SUCCESS);
        /*
        return RequestPayment.Request.newInstance(
                PhoneParams.newInstance("79267796753", new BigDecimal(10.00)))
                //..setTestResult(RequestPayment.TestResult.SUCCESS)
                ;
        */
    }

    private static ProcessPayment.Request createProcessPayment(String requestId) {
        return new ProcessPayment.Request(requestId).setTestResult(ProcessPayment.TestResult.SUCCESS);
    }

    private static AccountInfo.Request createAccountInfoRequest() {
        return new AccountInfo.Request();
    }

    private static OperationHistory.Request createOperationHistoryRequest() {
        OperationHistory.Request historyRequest = new OperationHistory.Request.Builder()
                .create();
        return historyRequest;
    }

}

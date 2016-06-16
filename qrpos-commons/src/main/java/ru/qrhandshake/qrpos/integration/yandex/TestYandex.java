package ru.qrhandshake.qrpos.integration.yandex;

import com.yandex.money.api.exceptions.InsufficientScopeException;
import com.yandex.money.api.exceptions.InvalidRequestException;
import com.yandex.money.api.exceptions.InvalidTokenException;
import com.yandex.money.api.methods.*;
import com.yandex.money.api.methods.params.PhoneParams;
import com.yandex.money.api.model.YandexMoneyCard;
import com.yandex.money.api.net.DefaultApiClient;
import com.yandex.money.api.net.OAuth2Session;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Created by lameroot on 16.06.16.
 */
public class TestYandex {

    //http://money.yandex.ru/oauth/authorize?client_id=333CFEAD690EAA3120CFF3E38F3CF52FAFB6C49C1F216EB50627A2D3034DDF4B&response_type=code&redirect_uri=http://qwe.ddns.net/yandex.html&scope=payment-p2p
    //http://qwe.ddns.net/yandex.html?code=CD732027D7C419222CEFBD96B5972E57DA4E8F43666513BC536FBC945464137A069337000C80C2DCD87A2980BFC7BEED11E8E742AB06FAF18F657A52BD0E3748BEA95A2CE80677228BD4B2162F9B2CD67874E32AF2A51952356F308FE3B6095A7D8684451DD2450B139B79EAA059CF8B9483FE261A729DFE687D69B963684670

    //http://money.yandex.ru/oauth/token?code=D3D6F355F227C5056AD9A07DBC7FFDACED06D3541813FD4B86BC05C0F8D09B99BF5454F97FA95400E7D06EFFBF8A80DCF2B1A37C1CC717C39E3DACD8A1B45EC45B1E112DC2A961C6E2892C8792AD36E8A45B2E711F112EAC4A6FF3DFB208C4172223F91670C61B2D188BCDC4CDEA5D0345B90C807C751C61411229069AEC4B99&client_id=333CFEAD690EAA3120CFF3E38F3CF52FAFB6C49C1F216EB50627A2D3034DDF4B&grant_type=authorization_code&redirect_uri=http://qwe.ddns.net/yandex.html
    //{"access_token":"410011792756615.BBFD78005293C1FEAEF23EFDAD5FB8E5512E59280A38001B856A86EBE41EF80EB0C85FF58C62A23B62E5C2B4493A2B68EEAC4D356C8827A2F1BB9C4AB915E3F8C55437C907D8EA021958D50020478FBF7782FD30021AB4FBB825276231160EDD9D213439169DE8655688F12189FE4B324C5E9E505CD7B9AAF3C6758DA05C8871"}

    private final static String CLIENT_ID = "333CFEAD690EAA3120CFF3E38F3CF52FAFB6C49C1F216EB50627A2D3034DDF4B";
    private final static String ACCESS_TOKEN = "410011792756615.01B0D239BB9A466244A6304962103E64DF166E94F90C55A7E01429FEA2B72498DCC06A5F3BA12275A8037BED605E6F68A76A9B67092D8F634EA1BA5C93FE5195C9030B0866C016E847E685D0D980781D222838BC93DDB468EE10B2FFC8B8F2AB8A84AB84ED0D4E639B74E79278A9F843784B01C95BD4E6E04F02B7560C3F7FAE";

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

        reqInstanceId = new InstanceId.Request(CLIENT_ID);
        respInstanceId = session.execute(reqInstanceId);

//        HashMap<String, String> params = successRequestParams();
//        RequestExternalPayment.Request rr = RequestExternalPayment.Request.newInstance(
//                respInstanceId.instanceId, PhoneParams.PATTERN_ID, params);


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
        return RequestPayment.Request.newInstance(
                PhoneParams.newInstance("79267796753", new BigDecimal(300.00)))
                //..setTestResult(RequestPayment.TestResult.SUCCESS)
                ;
    }

}

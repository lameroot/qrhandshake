package ru.qrhandshake.qrpos.service.sms;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringWriter;

public class ProstorSmsSender implements SmsSender {

    private final static Logger logger = LoggerFactory.getLogger(ProstorSmsSender.class);

    @Value("${sms.prostor.url:http://gate.prostor-sms.ru/send/}")
    private String url;
    @Value("${sms.prostor.login}")
    private String login;
    @Value("${sms.prostor.password}")
    private String password;
    @Value("${sms.prostor.flash:0}")
    private boolean flash;
    @Value("${sms.prostor.sender:Paystudio.ru}")
    private String defaultSender;

    @Override
    public boolean send(SmsObject smsObject) throws SmsSenderException {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            String encodedUri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("login", login)
                    .queryParam("password", password)
                    .queryParam("phone", smsObject.getPhone())
                    .queryParam("text", smsObject.getText())
                    //.queryParam("sender", StringUtils.isNotBlank(smsObject.getSender()) ? smsObject.getSender() : defaultSender)
                    .build().encode().toUriString();
            HttpGet httpGet = new HttpGet(encodedUri);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if ( httpResponse.getStatusLine().getStatusCode() != 200 || null == httpResponse.getEntity() ) {
                logger.error("Error response code: {} from: {} or response is empty", httpResponse.getStatusLine().getStatusCode(), url);
                return false;
            }
            StringWriter writer = new StringWriter();
            IOUtils.copy(httpResponse.getEntity().getContent(),writer);
            String body = writer.toString();
            String[] responseParams = body.split("=");
            if ( null == responseParams || responseParams.length != 2 ) {
                logger.error("Invalid response body: {}", body);
                return false;
            }
            String id = responseParams[0];
            String status = responseParams[1];
            if ( !"accepted".equalsIgnoreCase(status) ) {
                logger.error("Error status of response body: {}, expect : 'accepted', id: {}", status, id);
                return false;
            }
            //может стоит сделать проверку доставки сообщения?
            logger.debug("Sms [{}] was sent successfully via {}, id: {}. If you want to check status of sms, you can get request: {}"
                    ,smsObject, url, id, "http://gate.prostor-sms.ru/status/?id=" + id);
            return true;
        } catch (Exception e) {
            throw new SmsSenderException("Error send sms to " + smsObject + ", via :" + url,e);
        }
    }

}



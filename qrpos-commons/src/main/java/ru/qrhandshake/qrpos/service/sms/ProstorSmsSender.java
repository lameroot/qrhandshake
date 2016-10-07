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
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.StringWriter;

public class ProstorSmsSender implements SmsSender {

    private final static Logger logger = LoggerFactory.getLogger(ProstorSmsSender.class);

    @Value("${sms.prostor.url:http://api.prostor-sms.ru/messages/v2/send/}")
    private String url;
    @Value("${sms.prostor.balanceUrl:http://api.prostor-sms.ru/messages/v2/balance/}")
    private String balanceUrl;
    @Value("${sms.prostor.statusUrl:http://api.prostor-sms.ru/messages/v2/status/}")
    private String statusUrl;
    private final String login;
    private final String password;
    @Value("${sms.prostor.flash:0}")
    private boolean flash;
    @Value("${sms.prostor.sender:Prosto-R}")
    private String sender;
    @Value("${sms.prostor.debug:false}")
    private boolean debug;
    private HttpClient httpClient = HttpClientBuilder.create().build();

    public ProstorSmsSender(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public ProstorSmsSender setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public boolean send(SmsObject smsObject) throws SmsSenderException {
        Assert.notNull(smsObject,"SmsObject is null");
        try {
            String encodedUri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("login", login)
                    .queryParam("password", password)
                    .queryParam("phone", smsObject.getPhone())
                    .queryParam("text", debug ? modifyText(smsObject.getText()) : smsObject.getText())
                    .queryParam("sender", sender)
                    .build().encode().toUriString();
            logger.debug("Try to send {} via {}",smsObject.toString(debug),debug ? encodedUri : url);
            HttpGet httpGet = new HttpGet(encodedUri);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if ( httpResponse.getStatusLine().getStatusCode() != 200 || null == httpResponse.getEntity() ) {
                logger.error("Error response code: {} from: {} or response is empty", httpResponse.getStatusLine().getStatusCode(), url);
                return false;
            }
            StringWriter writer = new StringWriter();
            IOUtils.copy(httpResponse.getEntity().getContent(),writer);
            String body = writer.toString();
            String[] responseParams = body.split(";");
            if ( null == responseParams || responseParams.length != 2 ) {
                logger.error("Invalid response body: {}", body);
                return false;
            }
            String status = responseParams[0];
            String id = responseParams[1];
            if ( !"accepted".equalsIgnoreCase(status) ) {
                logger.error("Error status of response body: {}, expect : 'accepted', id: {}", status, id);
                return false;
            }
            //может стоит сделать проверку доставки сообщения?
            logger.debug("Sms [{}] was sent successfully via {}, id: {}. If you want to check status of sms, you can get request: {}"
                    ,smsObject, url, id, statusUrl + "?id=" + id);
            if ( debug ) {
                checkStatus(id);
            }
            return true;
        } catch (Exception e) {
            throw new SmsSenderException("Error send sms to " + smsObject + ", via :" + url,e);
        }
    }

    private String modifyText(String text) {
        String encodedUri = UriComponentsBuilder.fromUriString(balanceUrl)
                .queryParam("login", login)
                .queryParam("password", password)
                .build().encode().toUriString();
        HttpGet httpGet = new HttpGet(encodedUri);
        logger.debug("Try to get balance via {}",balanceUrl);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            StringWriter writer = new StringWriter();
            IOUtils.copy(httpResponse.getEntity().getContent(),writer);
            String body = writer.toString();
            logger.debug("Balance: {}",body);
            String newText = text + ". Balance: " + body;
            newText = newText.replaceAll(";"," ");
            return newText;
        } catch (IOException e) {
            logger.error("Error get balance via: " + balanceUrl,e);
        }
        return text;
    }

    private void checkStatus(String id) {
        String encodedUri = UriComponentsBuilder.fromUriString(statusUrl)
                .queryParam("login", login)
                .queryParam("password", password)
                .queryParam("id",id)
                .build().encode().toUriString();
        new Thread(() -> {
            HttpGet httpGet = new HttpGet(encodedUri);
            logger.debug("Try to check status via {}",statusUrl + "?id=" + id);
            String body = "";
            int count = 0;
            while ( !body.contains("delivered") ) {
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(httpResponse.getEntity().getContent(), writer);
                    body = writer.toString();
                    logger.debug("Status: {}", body);
                    Thread.sleep(1000L);
                    count++;
                    if ( count > 20 ) break;
                } catch (Exception e) {
                    logger.error("Error check status via: " + statusUrl + "?id=" + id, e);
                }
            }
        }).start();
    }
}



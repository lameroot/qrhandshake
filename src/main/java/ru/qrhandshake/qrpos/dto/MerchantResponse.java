package ru.qrhandshake.qrpos.dto;

/**
 * Created by lameroot on 18.05.16.
 */
public abstract class MerchantResponse {

    private ResponseCode responseCode;
    private String responseMessage;

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public static class ErrorMerchantResponse extends MerchantResponse {
        public ErrorMerchantResponse(ResponseCode responseCode, String responseMessage) {
            setResponseCode(responseCode);
            setResponseMessage(responseMessage);
        }
    }
}

package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 24.05.16.
 */
public class ApiResponse {
    private ResponseStatus status;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class ErrorApiResponse extends ApiResponse {
        public ErrorApiResponse(ResponseStatus status, String message) {
            setStatus(status);
            setMessage(message);
        }
    }
}

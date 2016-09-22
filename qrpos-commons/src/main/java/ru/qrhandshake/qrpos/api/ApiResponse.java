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

    public ApiResponse setStatus(ResponseStatus status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ApiResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public static class ErrorApiResponse extends ApiResponse {
        public ErrorApiResponse(ResponseStatus status, String message) {
            setStatus(status);
            setMessage(message);
        }
    }
}

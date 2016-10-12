package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 24.05.16.
 */
public class ApiResponse {
    private ResponseStatus status;
    private String message;
    private Error error;

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

    public Error getError() {
        return error;
    }

    public ApiResponse setError(Error error) {
        this.error = error;
        return this;
    }

    public static class ErrorApiResponse extends ApiResponse {
        public ErrorApiResponse(ResponseStatus status, String message) {
            super.setStatus(status);
            super.setMessage(message);
        }
    }

    private final static class Error {
        private ErrorCode errorCode;

        public ErrorCode getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }
    }

    public static final class SuccessApiResponseBuilder {
        private String message;
        public SuccessApiResponseBuilder message(String message) {
            this.message = message;
            return this;
        }
        public ApiResponse build() {
            ApiResponse apiResponse = new ApiResponse(ResponseStatus.SUCCESS,message);
            return apiResponse;
        }
    }

    public static final class ErrorApiResponseBuilder {
        private ApiResponse.Error error = new Error();
        private String message;

        public ErrorApiResponseBuilder code(ErrorCode errorCode) {
            error.setErrorCode(errorCode);
            return this;
        }
        public ErrorApiResponseBuilder message(String message) {
            this.message = message;
            return this;
        }
        public ApiResponse build() {
            ApiResponse apiResponse = new ApiResponse(ResponseStatus.FAIL,message);
            apiResponse.setError(error);
            return apiResponse;
        }
    }

    public static SuccessApiResponseBuilder successBuilder() {
        return new SuccessApiResponseBuilder();
    }

    public static ErrorApiResponseBuilder errorBuilder() {
        return new ErrorApiResponseBuilder();
    }
}

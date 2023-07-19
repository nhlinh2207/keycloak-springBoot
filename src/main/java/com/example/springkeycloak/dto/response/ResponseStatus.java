package com.example.springkeycloak.dto.response;

public enum ResponseStatus {
    BAD_REQUEST(400, "400 Bad Request"),
    FORBIDDEN(403, "403 FORBIDDEN"),
    UNAUTHORIZE(401, "401 Unauthorized"),
    BAD_CREDENTIAL(1003, "Bad Credentials"),
    DO_SERVICE_SUCCESSFUL(1000, "Success"),
    SUBSCRIBER_NOT_SET(1001, "Subscriber not set"),
    USER_DISABLE(1002, "User is disable"),
    USERNAME_EXISTS(1003, "Username exists"),
    UNHANDLED_ERROR(1004, "Unhandled error"),
    OBJECT_INVALID(1005, "Object invalid"),
    CALL_HTTP_HAS_ERROR(1006, "The system has error, please try again later"),
    KEY_CONFIG_NOT_FOUND(1007, "Config Not Found"),
    JOB_NOT_FOUND(1008, "Job Not Found"),
    FIXED_DELAY_INVALID(1009, "Job not fixed delay type"),
    FIXED_TIME_INVALID(1010, "Job not fixed time type"),
    CREATE_UPDATE_SELF(1011, "Please update, create with routes self"),
    USER_NOT_FOUND(1012, "User not found"),
    CERTIFICATE_NOT_FOUND(1013, "Certificate not found."),
    CERTIFICATE_PERMISSION(1013, "You don't have permission with this certificate."),
    JWT_HAS_EXPIRED(1014, "JWT Token has expired."),
    EXISTS(1015, "exists"),;

    ;


    public int code;
    public String message;
    private String messageFormat;

    ResponseStatus(int code, String message, String messageFormat) {
        this.code = code;
        this.message = message;
        this.messageFormat = messageFormat;
    }

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseStatus formatMessage(Object... str) {
        if (this.messageFormat != null) {
            this.message = String.format(this.messageFormat, str);
        }
        return this;
    }

    public ResponseStatus[] getListResponseStatus() {
        return ResponseStatus.values();
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

}

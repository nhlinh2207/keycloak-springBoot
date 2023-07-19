package com.example.springkeycloak.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
public class ResponseObject<T> implements Serializable {
    private Boolean result;
    private Integer code;
    private String message;
    private T data;
    public ResponseObject(Boolean result, ResponseStatus status) {
        this.result = result;
        this.code = status.getCode();
        this.message = status.getMessage();
    }
    public ResponseObject(Boolean result, ResponseStatus status, String message) {
        this.result = result;
        this.code = status.getCode();
        this.message = message;
    }
    public ResponseObject(Boolean result, ResponseStatus status, T data) {
        this.result = result;
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = data;
    }

    @JsonCreator
    public ResponseObject(@JsonProperty("result") Boolean result, @JsonProperty("code") Integer code, @JsonProperty("message") String message, @JsonProperty("data") T data) {
        this.result = result;
        this.code = code;
        this.message = message;
        this.data = data;
    }
}

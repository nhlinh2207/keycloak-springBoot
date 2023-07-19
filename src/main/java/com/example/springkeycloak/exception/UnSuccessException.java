package com.example.springkeycloak.exception;

public class UnSuccessException extends RuntimeException{
    public UnSuccessException(String message){
        super(message);
    }

    public UnSuccessException(String message, Throwable cause){
        super(message, cause);
    }
}

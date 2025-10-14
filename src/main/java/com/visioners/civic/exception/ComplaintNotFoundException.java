package com.visioners.civic.exception;

public class ComplaintNotFoundException extends RuntimeException {
    public ComplaintNotFoundException(String message){
        super(message);
    }
}

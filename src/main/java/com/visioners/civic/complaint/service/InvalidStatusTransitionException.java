package com.visioners.civic.complaint.service;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message){
        super(message);
    }
}

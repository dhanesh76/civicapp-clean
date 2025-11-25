package com.visioners.civic.complaint.exception;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message){
        super(message);
    }
}

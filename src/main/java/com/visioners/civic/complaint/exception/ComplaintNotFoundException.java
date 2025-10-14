package com.visioners.civic.complaint.exception;


public class ComplaintNotFoundException extends RuntimeException{
    public ComplaintNotFoundException(String message){
        super(message);
    }
}

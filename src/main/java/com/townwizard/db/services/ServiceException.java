package com.townwizard.db.services;

public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ServiceException(){}
    public ServiceException(String message){
        super(message);
    }

}

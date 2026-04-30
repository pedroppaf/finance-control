package com.pedro.finance_control.exception;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException (String message){
        super(message);
    }
}

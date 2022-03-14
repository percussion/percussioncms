package com.percussion.delivery.exceptions;

public class PSEmailException extends Exception{

    public PSEmailException(Exception e){
        super(e);
    }

    public PSEmailException(String s){
        super(s);
    }
}

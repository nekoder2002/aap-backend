package com.dhu.exception;

public class NotExistException extends RuntimeException{
    public NotExistException(String message) {
        super(message);
    }
}

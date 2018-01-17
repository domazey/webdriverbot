package com.github.webdriverbot.exceptions;

public class NewWindowNotOpenedException extends RuntimeException {

    public NewWindowNotOpenedException(String message) {
        super(message);
    }

    public NewWindowNotOpenedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NewWindowNotOpenedException(Throwable cause) {
        super(cause);
    }

    public NewWindowNotOpenedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}

package com.github.webdriverbot.exceptions;

public class InvalidAnnotationConfigurationException extends IllegalArgumentException {

    public InvalidAnnotationConfigurationException() {
    }

    public InvalidAnnotationConfigurationException(String s) {
        super(s);
    }

    public InvalidAnnotationConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAnnotationConfigurationException(Throwable cause) {
        super(cause);
    }
    
}

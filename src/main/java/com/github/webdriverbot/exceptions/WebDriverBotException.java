package com.github.webdriverbot.exceptions;

public class WebDriverBotException extends RuntimeException {

    public WebDriverBotException() {
    }

    public WebDriverBotException(String message) {
        super(message);
    }

    public WebDriverBotException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebDriverBotException(Throwable cause) {
        super(cause);
    }

}

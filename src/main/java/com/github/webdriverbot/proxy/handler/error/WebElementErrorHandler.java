package com.github.webdriverbot.proxy.handler.error;

import org.openqa.selenium.support.pagefactory.ElementLocator;

public abstract class WebElementErrorHandler {

    private final ElementLocator elementLocator;
    private final String[] handlerArgs;

    public WebElementErrorHandler(ElementLocator elementLocator, String[] handlerArgs) {
        this.elementLocator = elementLocator;
        this.handlerArgs = handlerArgs;
    }
    
    public abstract void handle();
    
    
}

package com.github.webdriverbot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementListHandler;

public class ExtendedLocatingElementListHandler extends LocatingElementListHandler {

    private final ElementLocator elementLocator;
    private final BotContext botContext;
    private final Field elemField;
    private final WebDriver webDriver;

    public ExtendedLocatingElementListHandler(ElementLocator elementLocator, Field elemField, BotContext botContext, WebDriver webDriver) {
        super(elementLocator);
        this.elementLocator = elementLocator;
        this.botContext = botContext;
        this.elemField = elemField;
        this.webDriver = webDriver;
    }

    @Override
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        List<WebElement> elements = elementLocator.findElements();

        try {
            return method.invoke(elements, objects);
        } catch (InvocationTargetException e) {
            // Unwrap the underlying exception
            throw e.getCause();
        }

    }
}

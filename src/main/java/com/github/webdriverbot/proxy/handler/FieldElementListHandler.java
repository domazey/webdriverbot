package com.github.webdriverbot.proxy.handler;

import com.github.webdriverbot.context.ConfigContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementListHandler;

public class FieldElementListHandler extends LocatingElementListHandler {

    private final ElementLocator elementLocator;
    private final ConfigContext botContext;
    private final Field elemField;
    private final WebDriver webDriver;

    public FieldElementListHandler(ElementLocator elementLocator, Field elemField, ConfigContext botContext, WebDriver webDriver) {
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

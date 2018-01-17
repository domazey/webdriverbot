/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.proxy.handler;

import com.github.webdriverbot.context.ConfigContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementListHandler;

public class ByElementListHandler extends LocatingElementListHandler {

    private final ElementLocator elementLocator;
    private final By by;
    private final ConfigContext botContext;
    private final WebDriver webDriver;

    public ByElementListHandler(ElementLocator elementLocator, By by, ConfigContext botContext, WebDriver webDriver) {
        super(elementLocator);
        this.elementLocator = elementLocator;
        this.by = by;
        this.botContext = botContext;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.decorator;

import com.github.webdriverbot.context.ConfigContext;
import com.github.webdriverbot.locator.ByElementLocatorFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementListHandler;

public class WDBByDecorator implements ByDecorator {

    private final Class<? extends LocatingElementHandler> handlerClass;
    private final Class<? extends LocatingElementListHandler> handlerListClass;
    private final ByElementLocatorFactory factory;
    private final ConfigContext botContext;
    private final WebDriver webDriver;

    public WDBByDecorator(ByElementLocatorFactory factory,
            Class<? extends LocatingElementHandler> handlerClass,
            Class<? extends LocatingElementListHandler> listHandlerClass,
            ConfigContext botContext,
            WebDriver webDriver) {
        this.handlerClass = handlerClass;
        this.handlerListClass = listHandlerClass;
        this.factory = factory;
        this.botContext = botContext;
        this.webDriver = webDriver;
    }

    protected WebElement proxyForLocator(ClassLoader loader, ElementLocator locator, By by) {
        InvocationHandler handler = getHandler(locator, by);

        WebElement proxy;
        System.out.println("handler: " + handler);
        proxy = (WebElement) Proxy.newProxyInstance(
                loader, new Class[]{WebElement.class, WrapsElement.class, Locatable.class}, handler);
        return proxy;
    }

    protected List<WebElement> proxyForListLocator(ClassLoader loader, ElementLocator locator, By by) {
        InvocationHandler handler = getListHandler(locator, by);

        List<WebElement> proxy;
        proxy = (List<WebElement>) Proxy.newProxyInstance(
                loader, new Class[]{List.class}, handler);
        return proxy;
    }

    protected InvocationHandler getHandler(ElementLocator locator, By by) {
        InvocationHandler handler = null;
        try {
            handler = handlerClass.getConstructor(ElementLocator.class, By.class, ConfigContext.class, WebDriver.class)
                    .newInstance(locator, by, botContext, webDriver);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            ex.printStackTrace(); //shallow
        }
        return handler;
    }

    @Override
    public WebElement decorate(ClassLoader loader, By by) {

        ElementLocator locator = factory.createLocator(by);
        return proxyForLocator(loader, locator, by);

    }

    @Override
    public List<WebElement> decorateList(ClassLoader loader, By by)  {
         ElementLocator locator = factory.createLocator(by);
         return proxyForListLocator(loader, locator, by);
    }

    protected InvocationHandler getListHandler(ElementLocator locator, By by) {
        InvocationHandler handler = null;
        try {
            handler = handlerListClass.getConstructor(ElementLocator.class, By.class, ConfigContext.class, WebDriver.class)
                    .newInstance(locator, by, botContext, webDriver);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            // shallow
        }
        return handler;
    }

    protected boolean isDecoratableList(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return false;
        }

        // Type erasure in Java isn't complete. Attempt to discover the generic
        // type of the list.
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }

        Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];

        if (!WebElement.class.equals(listType)) {
            return false;
        }

        if (field.getAnnotation(FindBy.class) == null
                && field.getAnnotation(FindBys.class) == null
                && field.getAnnotation(FindAll.class) == null) {
            return false;
        }

        return true;
    }

}

package com.github.webdriverbot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementListHandler;

public class WebDriverBotFieldDecorator implements FieldDecorator {

    private final Class<? extends LocatingElementHandler> handlerClass;
    private final Class<? extends LocatingElementListHandler> handlerListClass;
    private final ElementLocatorFactory factory;
    private final BotContext botContext;
    private final WebDriver webDriver;

    public WebDriverBotFieldDecorator(ElementLocatorFactory factory,
            Class<? extends LocatingElementHandler> handlerClass,
            Class<? extends LocatingElementListHandler> listHandlerClass,
            BotContext botContext,
            WebDriver webDriver) {
        this.handlerClass = handlerClass;
        this.handlerListClass = listHandlerClass;
        this.factory = factory;
        this.botContext = botContext;
        this.webDriver = webDriver;
    }

    protected WebElement proxyForLocator(ClassLoader loader, ElementLocator locator, Field elemField) {
        InvocationHandler handler = getHandler(locator, elemField);

        WebElement proxy;
        System.out.println("handler: " + handler);
        proxy = (WebElement) Proxy.newProxyInstance(
                loader, new Class[]{WebElement.class, WrapsElement.class, Locatable.class}, handler);
        return proxy;
    }

    protected List<WebElement> proxyForListLocator(ClassLoader loader, ElementLocator locator, Field elemField) {
        InvocationHandler handler = getListHandler(locator, elemField);

        List<WebElement> proxy;
        proxy = (List<WebElement>) Proxy.newProxyInstance(
                loader, new Class[]{List.class}, handler);
        return proxy;
    }

    protected InvocationHandler getHandler(ElementLocator locator, Field elemField) {
        InvocationHandler handler = null;
        try {
            handler = handlerClass.getConstructor(ElementLocator.class, Field.class, BotContext.class, WebDriver.class)
                    .newInstance(locator, elemField, botContext, webDriver);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            ex.printStackTrace(); //shallow
        }
        return handler;
    }

    protected InvocationHandler getListHandler(ElementLocator locator, Field elemField) {
        InvocationHandler handler = null;
        try {
            handler = handlerListClass.getConstructor(ElementLocator.class, Field.class, BotContext.class, WebDriver.class)
                    .newInstance(locator, elemField, botContext, webDriver);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            // shallow
        }
        return handler;
    }

    @Override
    public Object decorate(ClassLoader loader, Field field) {
        if (!(WebElement.class.isAssignableFrom(field.getType())
                || isDecoratableList(field))) {
            return null;
        }

        ElementLocator locator = factory.createLocator(field);
        if (locator == null) {
            return null;
        }

        if (WebElement.class.isAssignableFrom(field.getType())) {
            return proxyForLocator(loader, locator, field);
        } else if (List.class.isAssignableFrom(field.getType())) {
            return proxyForListLocator(loader, locator, field);
        } else {
            return null;
        }
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

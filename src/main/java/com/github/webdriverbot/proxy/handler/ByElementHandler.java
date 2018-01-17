/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.proxy.handler;

import com.github.webdriverbot.context.ConfigContext;
import com.github.webdriverbot.proxy.handler.interpret.ByElementInterpreter;
import java.lang.reflect.Method;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;

public class ByElementHandler extends LocatingElementHandler {

    private final By by;
    private final ConfigContext botContext;
    private final WebDriver webDriver;
    private Object proxy;

    private int onErrorAttempts;

    public ByElementHandler(ElementLocator elementLocator, By by, ConfigContext botContext, WebDriver webDriver) {
        super(elementLocator);
        this.by = by;
        this.botContext = botContext;
        this.webDriver = webDriver;
    }

    @Override
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        
        proxy = null;
        ByElementInterpreter elemInterpreter = new ByElementInterpreter(botContext, by, webDriver);

        onErrorAttempts = elemInterpreter.getOnErrorAttempts();
        Throwable failCause = null;
        
        do {
            failCause = processTargetElementSearch(elemInterpreter, object, method, objects, failCause);
            System.out.println("onErrorAttempts " + onErrorAttempts);
        } while (failCause != null && onErrorAttempts > 0);

        if (failCause != null) {
            //final attempt
            failCause = processTargetElementSearch(elemInterpreter, object, method, objects, failCause);
        }

        if (failCause != null) {
            throw failCause;
        }

        return proxy;
    }

    protected Throwable processTargetElementSearch(ByElementInterpreter elemInterpreter, Object object, Method method, Object[] objects, Throwable failCause) throws Throwable {
        int findAttempts = elemInterpreter.getAttempts();
        while (findAttempts > 0) {
            System.out.println("findAttempts " + findAttempts);
            --findAttempts;
            try {
                proxy = super.invoke(object, method, objects);
                failCause = null;
                break;
            } catch (Exception ex) {
                failCause = ex;
                if (findAttempts > 0) {
                    elemInterpreter.processDelay();
                }
            }
        }
        return failCause;
    }
}

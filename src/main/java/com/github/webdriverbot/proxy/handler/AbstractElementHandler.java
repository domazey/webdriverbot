/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.proxy.handler;

import com.github.webdriverbot.proxy.handler.interpret.AbstractElementInterpreter;
import com.github.webdriverbot.context.ConfigContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;

public class AbstractElementHandler extends LocatingElementHandler {

    protected final ConfigContext botContext;
    protected final WebDriver webDriver;
    protected Object proxy;

    protected int onErrorAttempts;
    protected boolean onErrorPerformed;
    protected final AbstractElementInterpreter interpreter;

    public AbstractElementHandler(ConfigContext botContext,
            WebDriver webDriver,
            ElementLocator locator,
            AbstractElementInterpreter interpreter) {
        super(locator);
        this.botContext = botContext;
        this.webDriver = webDriver;
        this.interpreter = interpreter;
    }

    protected final Throwable processTargetElementSearch(Object object, Method method, Object[] objects, Throwable failCause) throws Throwable {
        int findAttempts = interpreter.getAttempts();
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
                    interpreter.processDelay();
                }
            }
        }
        return failCause;
    }

}

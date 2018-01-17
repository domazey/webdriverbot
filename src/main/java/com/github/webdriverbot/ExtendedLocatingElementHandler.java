package com.github.webdriverbot;

import static com.github.webdriverbot.BotBot.*;
import com.github.webdriverbot.annotations.RedirectTo;
import com.github.webdriverbot.exceptions.InvalidAnnotationConfigurationException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;

public class ExtendedLocatingElementHandler extends LocatingElementHandler {

    private final ElementLocator elementLocator;
    private final BotContext botContext;
    private final Field elemField;
    private final WebDriver webDriver;
    private Object proxy;
    
    private int onErrorAttempts;
    private boolean onErrorPerformed;
    
    public ExtendedLocatingElementHandler(ElementLocator elementLocator, Field elemField, BotContext botContext, WebDriver webDriver) {
        super(elementLocator);
        this.elementLocator = elementLocator;
        this.botContext = botContext;
        this.elemField = elemField;
        this.webDriver = webDriver;
    }

    @Override
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        proxy = null;
        ExtendedWebElementInterpreter elemInterpreter = new ExtendedWebElementInterpreter(elementLocator, botContext, elemField, webDriver);
        
        elemInterpreter.checkPreconditions();
        
        onErrorAttempts = elemInterpreter.getOnErrorAttempts();
        Throwable failCause = null;
        onErrorPerformed = false;
        do {
            failCause = processTargetElementSearch(elemInterpreter, object, method, objects, failCause);

            System.out.println("onErrorAttempts " + onErrorAttempts);
            if (failCause != null && elemInterpreter.hasOnErrorAnnotation()) {
                failCause = processOnError(elemInterpreter, failCause);
            }

        } while (failCause != null && elemInterpreter.hasOnErrorAnnotation() && onErrorAttempts > 0);

        if (onErrorPerformed == true) {
            //final attempt
            failCause = processTargetElementSearch(elemInterpreter, object, method, objects, failCause);
        }

        if (failCause != null) {
            throw failCause;
        }
        
        //TODO: make this handle-aware. I don't know if "click" can open new tab, but beware of "sendKeys"
        if(method.getName().equals("click")) {
            if(elemField.getAnnotation(RedirectTo.class) != null) {
                WebDriverBotContext.setPage(currentWindowHandle(), elemField.getAnnotation(RedirectTo.class).value());
            }
        }

        return proxy;
    }

    protected Throwable processOnError(ExtendedWebElementInterpreter elemInterpreter, Throwable failCause) throws InvalidAnnotationConfigurationException {
        --onErrorAttempts;
        try {
            System.out.println("Attempting to perform onError");
            onErrorPerformed = true;
            elemInterpreter.handleError(failCause);
        } catch (InvalidAnnotationConfigurationException ex) {
            throw ex; //bad configuration, instant exit
        } catch (Throwable ex) {
            failCause = ex;
            // an action failed, but bot may try to perform it again
        }
        return failCause;
    }

    protected Throwable processTargetElementSearch(ExtendedWebElementInterpreter elemInterpreter, Object object, Method method, Object[] objects, Throwable failCause) throws Throwable {
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

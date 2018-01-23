package com.github.webdriverbot.proxy.handler;

import com.github.webdriverbot.context.ConfigContext;
import com.github.webdriverbot.proxy.handler.interpret.FieldElementInterpreter;
import com.github.webdriverbot.context.WebDriverBotContext;
import static com.github.webdriverbot.bot.BotBot.*;
import com.github.webdriverbot.exceptions.InvalidAnnotationConfigurationException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import com.github.webdriverbot.annotations.RedirectsTo;

public class FieldElementHandler extends AbstractElementHandler {
    
    private final Field elemField;
    public FieldElementHandler(ElementLocator elementLocator, Field elemField, ConfigContext botContext, WebDriver webDriver) {
        super(botContext, webDriver, elementLocator, new FieldElementInterpreter(botContext, elemField, webDriver));
        this.elemField = elemField;
    }

    @Override
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        proxy = null;
        FieldElementInterpreter fieldInterpreter = (FieldElementInterpreter) interpreter;
        fieldInterpreter.checkPreconditions();
        
        onErrorAttempts = interpreter.getOnErrorAttempts();
        Throwable failCause = null;
        onErrorPerformed = false;
        do {
            failCause = processTargetElementSearch(object, method, objects, failCause);

            System.out.println("onErrorAttempts " + onErrorAttempts);
            if (failCause != null && fieldInterpreter.hasOnErrorAnnotation()) {
                failCause = processOnError(fieldInterpreter, failCause);
            }

        } while (failCause != null && fieldInterpreter.hasOnErrorAnnotation() && onErrorAttempts > 0);

        if (onErrorPerformed == true) {
            //final attempt
            failCause = processTargetElementSearch(object, method, objects, failCause);
        }

        if (failCause != null) {
            throw failCause;
        }
        
        //TODO: make this handle-aware. I don't know if "click" can open new tab, but beware of "sendKeys"
        if(method.getName().equals("click")) {
            if(elemField.getAnnotation(RedirectsTo.class) != null) {
                WebDriverBotContext.setPage(currentWindowHandle(), elemField.getAnnotation(RedirectsTo.class).value());
            }
        }

        return proxy;
    }

    protected Throwable processOnError(FieldElementInterpreter elemInterpreter, Throwable failCause) throws InvalidAnnotationConfigurationException {
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
}

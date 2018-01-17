package com.github.webdriverbot;

import com.github.webdriverbot.annotations.FindAttempts;
import com.github.webdriverbot.annotations.FindDelay;
import com.github.webdriverbot.annotations.OnError;
import static com.github.webdriverextensions.Bot.waitFor;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import com.github.webdriverbot.annotations.Action;
import com.github.webdriverbot.annotations.OnErrorAttempts;
import com.github.webdriverbot.annotations.Precondition;
import com.github.webdriverbot.annotations.Preconditions;
import com.github.webdriverbot.exceptions.InvalidAnnotationConfigurationException;
import java.util.Arrays;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ElementLocator;

public class ExtendedWebElementInterpreter {

    private final ElementLocator elementLocator;

    private final BotContext botContext;
    private final Field elemField;
    private final WebDriver driver;

    public ExtendedWebElementInterpreter(final ElementLocator elementLocator, final BotContext botContext, final Field elemField, WebDriver driver) {
        this.elementLocator = elementLocator;
        this.botContext = botContext;
        this.elemField = elemField;
        this.driver = driver;
    }

    int getAttempts() {
        int findAttempts = elemField.getAnnotation(FindAttempts.class) != null
                ? elemField.getAnnotation(FindAttempts.class).value() : botContext.getFindAttempts();
        if (findAttempts < 1) {
            System.out.println("Ignoring invalid value of annotation FindAttemps: " + findAttempts + " -> " + 1);
            findAttempts = 1;
        }
        return findAttempts;
    }

    void processDelay() {
        FindDelay delay = elemField.getAnnotation(FindDelay.class);
        double value = (delay != null) ? delay.value() : botContext.getFindDelay();
        TimeUnit unit = (delay != null) ? delay.unit() : botContext.getFindDelayUnit();
        waitFor(value, unit);
    }

    void handleError(Throwable originalException) throws Throwable {
        OnError annotation = null;
        if (elemField.getAnnotation(OnError.class) != null) {
            annotation = elemField.getAnnotation(OnError.class);
        } else {
            throw originalException;
        }

        Action[] actions = null;
        if (annotation.action().action() != ActionEnum.NONE) {
            actions = new Action[]{annotation.action()};
        } else {
            actions = annotation.value();
        }

        if (actions.length <= 0) {
            Class handlerClass = annotation.handlerClass();
            if (handlerClass.equals(void.class)) {
                throw new InvalidAnnotationConfigurationException("Annotation @OnError has been used, but no @Action annotation or handler was provided", originalException);
            } else {
                useHandler(handlerClass, annotation.handlerArgs(), originalException);
            }
        } else {
            handleActions(actions, originalException);
        }

    }

    private void handleActions(Action[] actions, Throwable originalException) throws RuntimeException {
        for (Action action : actions) {
            switch (action.action()) {
                case OPEN: {
                    String arg = getArgArgument(action, originalException);
                    driver.get(arg);
                }
                break;
                case CLICK: {
                    // use other types of locators
                    String xpath = getFindByLocator(action, originalException);
                    driver.findElement(By.xpath(xpath)).click(); //use safe click
                }
                break;
                case TYPE: {
                    // use other types + safe action
                    String xpath = getFindByLocator(action, originalException);
                    driver.findElement(By.xpath(xpath)).sendKeys(getArgArgument(action, originalException));
                }
                break;
                //TODO other
            }
        }
    }

    private String getArgArgument(Action action, Throwable originalException) throws RuntimeException {
        String arg = null;
        // arg and args set
        if (!action.arg().equals("") && action.args().length > 0) {
            throw new InvalidAnnotationConfigurationException("Use <arg> or <args> @Action parameter, NOT BOTH.", originalException);
        }
        // none set
        if (action.arg().equals("") && action.args().length <= 0) {
            throw new InvalidAnnotationConfigurationException("Provide <arg> or <args> @Action parameter, NOT NONE", originalException);
        }
        // arg set
        if (!action.arg().equals("")) {
            arg = action.arg();
        }
        // args set
        if (action.args().length > 0) {
            if (action.args().length > 1) {
                throw new InvalidAnnotationConfigurationException("OPEN action takes onlye one parameter, but provided " + Arrays.asList(action.args()));
            }
            arg = action.args()[0];
        }
        return arg;
    }

    private void useHandler(Class handlerClass, String[] handlerArgs, Throwable originalException) {
        // TODO
    }

    int getOnErrorAttempts() {
        int onErrorAttempts = elemField.getAnnotation(OnErrorAttempts.class) != null
                ? elemField.getAnnotation(FindAttempts.class).value() : botContext.getOnErrorAttempts();
        if (onErrorAttempts < 1) {
            System.out.println("Ignoring invalid value of annotation FindAttemps: " + onErrorAttempts + " -> " + 1);
            onErrorAttempts = 1;
        }
        return onErrorAttempts;
    }

    boolean hasOnErrorAnnotation() {
        return elemField.getAnnotation(OnError.class) != null;
    }

    private String getFindByLocator(Action action, Throwable originalException) {
        return action.findBy().xpath(); //todo rest
    }

    void checkPreconditions() {
        Preconditions preconditionsAnnotation = elemField.getAnnotation(Preconditions.class);

        if(preconditionsAnnotation == null) {
            return;
        }
        Precondition[] preconditions = preconditionsAnnotation.value();

        for (Precondition precond : preconditions) {

            if(!checkPrecondition(precond)) {
                //for now only throwing exception
                throw new RuntimeException("Precondition failed.\nPrecondition: " 
                        + precond.toString() 
                        + "\nFor element: " 
                        + elemField.getDeclaringClass() 
                        + "$" 
                        + elemField.getName());
            }

        }
    }

    private boolean checkPrecondition(Precondition precond) {
        
        switch (precond.cond()) {
            case NONE:
                return false;
            case ELEMENT_PRESENT: {
                //TODO: handle other locators
                if ("".equals(precond.elem())) {
                    return false; //throw some malformed annotation exception
                }

                return checkElementPresentPrecondition(precond.elem());
            }

            default:
                throw new AssertionError();
        }
        
    }

    private boolean checkElementPresentPrecondition(String elem) {
        String[] splitted = elem.split("\\$");
        
        if(splitted.length != 2) {
            throw new RuntimeException("@Precondition \"elem\" value {" + elem + "} is malformed (class/field $ separator is missing)");
        }
        
        Class enclosingClass;
        Field preconditionField;  
        try {
            enclosingClass = Class.forName(splitted[0]);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("@Precondition \"elem\" value malformed (class for name \"" + splitted[0] + "\" not found", ex);
        }
        
        try {
            preconditionField = enclosingClass.getDeclaredField(splitted[1]);
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException("@Precondition \"elem\" value malformed (there is no \"" + splitted[1] + "\" field in class " + splitted[0] + ")", ex);
        } catch (SecurityException ex) {
            throw new RuntimeException("@Precondition failed. You don't have access to this field\"" + splitted[1] + "\" from class " + splitted[0] + ")", ex);
        }
        
        FindBy findBy = preconditionField.getAnnotation(FindBy.class);
        if(findBy == null) {
            throw new RuntimeException("Temporarily only @FindBy annotation is handled for preconditions elements");
        }

         //handling only xpath for now
        String xpath = findBy.xpath();
        if ("".equals(xpath)) {
            throw new RuntimeException("Temporarily only xpath argument of @FindBy annotation can be handled for preconditions");
        }

        return !driver.findElements(By.xpath(xpath)).isEmpty();
        
    }

}

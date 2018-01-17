/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.proxy.handler.interpret;

import com.github.webdriverbot.context.ConfigContext;
import static com.github.webdriverextensions.Bot.waitFor;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import org.openqa.selenium.WebDriver;

public abstract class AbstractElementInterpreter {

    protected final ConfigContext botContext;
    protected final WebDriver driver;

    public AbstractElementInterpreter(final ConfigContext botContext, WebDriver driver) {
        this.botContext = botContext;
        this.driver = driver;
    }

    public final int getAttempts() {
        return validateGreaterThanZero(calculateAttempts());
    }

    public final void processDelay() {
        Pair<Double, TimeUnit> delay = calculateDelay();
        waitFor(delay.getKey(), delay.getValue());
    }

    public final int getOnErrorAttempts() {
        return validateGreaterThanZero(calculateOnErrorAttempts());
    }

    protected abstract int calculateAttempts();
    protected abstract Pair<Double, TimeUnit> calculateDelay();
    protected abstract void handleError(Throwable originalException) throws Throwable;
    protected abstract int calculateOnErrorAttempts();
    
    private int validateGreaterThanZero(int attempts) {
        if (attempts < 1) {
            System.out.println("Ignoring invalid value of annotation FindAttemps: " + attempts + " -> " + 1);
            attempts = 1;
        }
        return attempts;
    }

}

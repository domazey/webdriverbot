/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.proxy.handler.interpret;

import com.github.webdriverbot.context.ConfigContext;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ByElementInterpreter extends AbstractElementInterpreter {

    private final By by;

    public ByElementInterpreter(ConfigContext botContext, By by, WebDriver driver) {
        super(botContext, driver);
        this.by = by;
    }

    @Override
    protected int calculateAttempts() {
        return botContext.getFindAttempts();
    }

    @Override
    protected Pair<Double, TimeUnit> calculateDelay() {
        double value = botContext.getFindDelay();
        TimeUnit unit = botContext.getFindDelayUnit();
        return new Pair<>(value, unit);
    }

    @Override
    protected void handleError(Throwable originalException) throws Throwable {
        // user can't specify how to handle error here (not planned for now)
    }

    @Override
    protected int calculateOnErrorAttempts() {
        return botContext.getOnErrorAttempts();
    }

}

package com.github.webdriverbot;

import java.util.concurrent.TimeUnit;

public class BotContext {

    private int findAttempts = 1;
    private int onErrorAttempts = 1;
    private double findDelay = 500;
    
    private TimeUnit findDelayUnit = TimeUnit.MILLISECONDS;

    public int getFindAttempts() {
        return findAttempts;
    }

    public void setFindAttempts(int findAttempts) {
        this.findAttempts = findAttempts;
    }

    public double getFindDelay() {
        return findDelay;
    }

    public TimeUnit getFindDelayUnit() {
        return findDelayUnit;
    }

    public void setDelay(double value, TimeUnit unit) {
        this.findDelay = value;
        this.findDelayUnit = unit;
    }

    int getOnErrorAttempts() {
        return onErrorAttempts;
    }

    public void setOnErrorAttempts(int onErrorAttempts) {
        this.onErrorAttempts = onErrorAttempts;
    }

    public void setFindDelay(double findDelay) {
        this.findDelay = findDelay;
    }

    public void setFindDelayUnit(TimeUnit findDelayUnit) {
        this.findDelayUnit = findDelayUnit;
    }

}

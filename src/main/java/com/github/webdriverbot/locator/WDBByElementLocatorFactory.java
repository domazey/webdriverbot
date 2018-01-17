/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.locator;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ElementLocator;

public class WDBByElementLocatorFactory implements ByElementLocatorFactory {

    private final WebDriver driver;
    
    public WDBByElementLocatorFactory(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public ElementLocator createLocator(By by) {
        return new ByElementLocator(driver, by);
    }

}

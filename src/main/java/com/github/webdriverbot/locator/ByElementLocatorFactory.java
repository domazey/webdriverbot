/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.locator;

import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.ElementLocator;

public interface ByElementLocatorFactory {

    public ElementLocator createLocator(By by);
}

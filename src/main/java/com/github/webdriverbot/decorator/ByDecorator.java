/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.decorator;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public interface ByDecorator {
    public abstract WebElement decorate(ClassLoader loader, By by);
    public abstract List<WebElement> decorateList(ClassLoader loader, By by);
}

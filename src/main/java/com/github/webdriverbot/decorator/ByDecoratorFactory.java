/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.decorator;

import org.openqa.selenium.WebDriver;

public interface ByDecoratorFactory {

    public WDBByDecorator create(WebDriver driver);

}

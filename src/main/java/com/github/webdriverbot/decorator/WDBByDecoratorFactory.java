/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.decorator;

import com.github.webdriverbot.context.ConfigContext;
import com.github.webdriverbot.locator.WDBByElementLocatorFactory;
import com.github.webdriverbot.proxy.handler.ByElementListHandler;
import com.github.webdriverbot.proxy.handler.ByElementHandler;
import org.openqa.selenium.WebDriver;

public class WDBByDecoratorFactory implements ByDecoratorFactory {
    @Override
    public WDBByDecorator create(WebDriver driver) {
        return new WDBByDecorator(new WDBByElementLocatorFactory(driver),
                        ByElementHandler.class,
                        ByElementListHandler.class,
                        new ConfigContext(), driver);
    }
}

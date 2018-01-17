package com.github.webdriverbot.decorator;

import com.github.webdriverbot.context.ConfigContext;
import com.github.webdriverbot.proxy.handler.FieldElementHandler;
import com.github.webdriverbot.proxy.handler.FieldElementListHandler;
import com.github.webdriverextensions.internal.WebDriverExtensionElementLocatorFactory;
import org.openqa.selenium.WebDriver;

public class WDBFieldDecoratorFactory implements FieldDecoratorFactory {

    @Override
    public WDBFieldDecorator create(WebDriver driver) {
        return new WDBFieldDecorator(new WebDriverExtensionElementLocatorFactory(driver, driver),
                        FieldElementHandler.class,
                        FieldElementListHandler.class,
                        new ConfigContext(), driver);
    }

}

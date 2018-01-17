package com.github.webdriverbot;

import com.github.webdriverextensions.internal.WebDriverExtensionElementLocatorFactory;
import org.openqa.selenium.WebDriver;

public class DefaultFieldDecoratorFactory implements FieldDecoratorFactory {

    @Override
    public WebDriverBotFieldDecorator create(WebDriver driver) {
        return new WebDriverBotFieldDecorator(new WebDriverExtensionElementLocatorFactory(driver, driver),
                        ExtendedLocatingElementHandler.class,
                        ExtendedLocatingElementListHandler.class,
                        new BotContext(), driver);
    }

}

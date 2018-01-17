package com.github.webdriverbot;

import org.openqa.selenium.WebDriver;

public interface FieldDecoratorFactory {

    public WebDriverBotFieldDecorator create(WebDriver drivera);
    
}

package com.github.webdriverbot.decorator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.FieldDecorator;

public interface FieldDecoratorFactory {

    public FieldDecorator create(WebDriver driver);
    
}

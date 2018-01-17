package com.github.webdriverbot;

import com.github.webdriverbot.DefaultFieldDecoratorFactory;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import com.github.webdriverextensions.WebRepository;
import org.openqa.selenium.support.PageFactory;

public class BotWebRepository extends WebRepository {

    public BotWebRepository() {
        PageFactory.initElements(new DefaultFieldDecoratorFactory().create(WebDriverExtensionsContext.getDriver()), this);
    }
    
   
}

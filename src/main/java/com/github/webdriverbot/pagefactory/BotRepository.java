package com.github.webdriverbot.pagefactory;

import com.github.webdriverbot.decorator.WDBFieldDecoratorFactory;
import com.github.webdriverbot.decorator.WDBFieldDecoratorFactory;
import static com.github.webdriverextensions.Bot.driver;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import com.github.webdriverextensions.WebRepository;
import org.openqa.selenium.support.PageFactory;

public class BotRepository extends WebRepository {

    public BotRepository() {
        PageFactory.initElements(new WDBFieldDecoratorFactory().create(driver()), this);
    }

}

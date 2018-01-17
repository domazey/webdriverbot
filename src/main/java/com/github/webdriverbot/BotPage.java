package com.github.webdriverbot;

import com.github.webdriverbot.exceptions.WebDriverBotException;
import static com.github.webdriverextensions.Bot.driver;
import com.github.webdriverextensions.WebPage;
import org.openqa.selenium.support.PageFactory;

public class BotPage <T> extends WebPage {

    @Override
    public void open(Object... arguments) {
        throw new WebDriverBotException("Class " + this.getClass().getSimpleName() + "doesn't implement WebPage's open method.\n*Note that not every BotPage can be simply opened");
    }

    @Override
    public void assertIsOpen(Object... arguments) throws AssertionError {
        throw new WebDriverBotException("Class " + this.getClass().getSimpleName() + "doesn't implement WebPage's assertIsOpen method.\n*Note that BotPage can still implement assertIsOpen method, even if it doesn't implement open method");
    }

    public BotPage() {
        PageFactory.initElements(new DefaultFieldDecoratorFactory().create(driver()), this);
    }
    
    

}

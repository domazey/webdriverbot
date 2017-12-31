package com.github.webdriverbot.example;

import com.github.webdriverbot.ActionEnum;
import com.github.webdriverbot.BotContext;
import com.github.webdriverbot.ExtendedLocatingElementHandler;
import com.github.webdriverbot.ExtendedLocatingElementListHandler;
import com.github.webdriverbot.WebDriverBotFieldDecorator;
import com.github.webdriverbot.annotations.Action;
import com.github.webdriverbot.annotations.OnError;
import static com.github.webdriverextensions.Bot.textIn;
import com.github.webdriverextensions.internal.WebDriverExtensionElementLocatorFactory;
import com.github.webdriverextensions.internal.junitrunner.DriverPathLoader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class Example {

    @FindBy(xpath = "//*[@data-zci-link='images']") // element locator
    @OnError({ // machanism that runs when there is exception concerning the element
        @Action(type = ActionEnum.OPEN, arg = "https://duckduckgo.com/?q=&t=hf"), // open right page
        @Action(type = ActionEnum.TYPE, findBy = @FindBy(xpath = "//*[@id='search_form_input_homepage']"), arg = "lol"), // input text in text field
        @Action(type = ActionEnum.CLICK, findBy = @FindBy(xpath = "//*[@id='search_button_homepage']")) // click "search"
    }) // after mechanism finishes, search for element again (configurable how many times)
    private WebElement elem;

    // elements initialization
    public Example(WebDriver driver) {
        PageFactory.initElements(
                new WebDriverBotFieldDecorator(new WebDriverExtensionElementLocatorFactory(driver, driver),
                        ExtendedLocatingElementHandler.class,
                        ExtendedLocatingElementListHandler.class,
                        new BotContext(), driver), this);
    }

    public static void main(String[] args) {
        DriverPathLoader.loadDriverPaths(null); // use default fields
        WebDriver driver = new FirefoxDriver(); //random driver
        try {
            Example bot = new Example(driver); // create object and initialize it's element
            driver.get("https://www.google.com"); //open wrong page
            
            // try get text from element. Element does not exist on www.google.com,
            // so exception is thrown, but instead of giving it to the outside, the
            // @OnError mechanism is launched. In this case, it opens the right page,
            // types some text in search field, and presses "search". After this,
            // the "Images" tab of DuckDuckGo search result is present, so we can
            // get the text from the element :) This is first of many planned 
            // features of this library.
            System.out.println("bot.getElem() = " + textIn(bot.elem));
        } finally {
            driver.quit();
        }

    }

}

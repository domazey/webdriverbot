package com.github.webdriverbot.example;

import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.github.webdriverbot.BotPage;

public class GoogleSearchResults extends BotPage {

    @FindBy(xpath = "//h3[@class = 'r']/a")
    List<WebElement> links;

}

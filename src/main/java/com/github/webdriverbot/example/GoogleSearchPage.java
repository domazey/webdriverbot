package com.github.webdriverbot.example;

import com.github.webdriverbot.BotPage;
import com.github.webdriverbot.annotations.RedirectTo;
import com.github.webdriverbot.annotations.Url;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Url("https://www.google.com")
public class GoogleSearchPage extends BotPage {

    @FindBy(id = "lst-ib")
    public WebElement searchField;
    
    @RedirectTo(GoogleSearchResults.class)
    @FindBy(name = "btnK")
    public WebElement submitButton;

}

package com.github.webdriverbot.example;

import com.github.webdriverbot.pagefactory.BotPage;
import com.github.webdriverbot.annotations.RedirectTo;
import com.github.webdriverbot.annotations.Url;
import com.github.webdriverbot.metamodel.GenerateMetaModel;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@GenerateMetaModel
@Url("https://www.google.com")
public class GoogleSearchPage extends BotPage {

    @FindBy(id = "lst-ib")
    public WebElement searchField;
    
    @RedirectTo(GoogleSearchResults.class)
    @FindBy(name = "btnK")
    public WebElement submitButton;

}

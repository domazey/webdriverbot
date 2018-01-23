package com.github.webdriverbot.examples.example1;

import com.github.webdriverbot.pagefactory.BotPage;
import com.github.webdriverbot.annotations.Url;
import com.github.webdriverbot.metamodel.GenerateMetaModel;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.github.webdriverbot.annotations.RedirectsTo;

@GenerateMetaModel
@Url("https://www.google.com")
public class GoogleSearchPage extends BotPage {

    @FindBy(id = "lst-ib")
    public WebElement searchField;
    
    @RedirectsTo(GoogleSearchResults.class)
    @FindBy(name = "btnK")
    public WebElement submitButton;

}

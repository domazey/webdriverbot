package com.github.webdriverbot.examples.example1;

import com.github.webdriverbot.metamodel.GenerateMetaModel;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.github.webdriverbot.pagefactory.BotPage;

@GenerateMetaModel
public class GoogleSearchResults extends BotPage {

    @FindBy(xpath = "//h3[@class = 'r']/a")
    List<WebElement> links;

}

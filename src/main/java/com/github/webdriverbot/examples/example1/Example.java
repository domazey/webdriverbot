package com.github.webdriverbot.examples.example1;

import static com.github.webdriverbot.bot.BotBot.*;
import com.github.webdriverbot.context.WebDriverBotContext;
import com.github.webdriverextensions.internal.junitrunner.DriverPathLoader;
import org.openqa.selenium.WebDriver;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Example {

    public static void main(String[] args) {

        init();

        openPage(GoogleSearchPage.class); // no need to store page in variable
        
        type("pizza", GoogleSearchPage_.searchField.class);
        
        click(GoogleSearchPage_.submitButton.class);

        List<String> urls = new ArrayList<>();
        
        // opens each link in new tab, performs operation, and closes that tab
        // It's made to prevent list invalidation (can't iterate over list if the page changed)
        forEachRedirect(GoogleSearchResults_.links.class, () -> {
            urls.add(currentUrl());
        });

        System.out.println("Found urls: " + urls);

        driver().quit();
    }

    private static void init() {
        DriverPathLoader.loadDriverPaths(null); // use default fields
        WebDriver driver = new FirefoxDriver(); //random driver
        WebDriverExtensionsContext.setDriver(driver);
        driver().manage().window().maximize();
        WebDriverBotContext.initBotData();
    }

}

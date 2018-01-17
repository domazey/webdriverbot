package com.github.webdriverbot.example;

import static com.github.webdriverbot.BotBot.*;
import com.github.webdriverextensions.internal.junitrunner.DriverPathLoader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import java.util.ArrayList;
import java.util.List;

public class Example {

    public static void main(String[] args) {

        init();
        
        GoogleSearchPage googleSearchPage = openPage(GoogleSearchPage.class);

        type("pizza", googleSearchPage.searchField);

        click(googleSearchPage.submitButton);

        GoogleSearchResults googleSearchResults = getCurrentPage(GoogleSearchResults.class);

        List<String> result = new ArrayList<>();

        executeForEachLink(googleSearchResults.links, () -> {
            result.add(currentUrl());
        });
        
        for(String url : result) {
            System.out.println("url: " + url);
        }
        
        driver().quit();

    }

    private static void init() {
        DriverPathLoader.loadDriverPaths(null); // use default fields
        WebDriver driver = new FirefoxDriver(); //random driver
        WebDriverExtensionsContext.setDriver(driver);
    }

}

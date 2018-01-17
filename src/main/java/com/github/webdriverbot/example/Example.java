package com.github.webdriverbot.example;

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

        GoogleSearchPage googleSearchPage = openPage(GoogleSearchPage.class);
        type("pizza", googleSearchPage.searchField);
        click(GoogleSearchPage_.submitButton.class);

        List<String> urls = new ArrayList<>();
        forEachRedirect(GoogleSearchResults_.links.class, () -> {
            urls.add(currentUrl());
        });

        System.out.println("Found urls: " + urls);

        driver().quit();

    }

    private static void init() {
        DriverPathLoader.loadDriverPaths(null); // use default fields
        WebDriver driver = new FirefoxDriver(); //random driver
        driver.manage().window().maximize();
        WebDriverExtensionsContext.setDriver(driver);
        WebDriverBotContext.init();
    }

}

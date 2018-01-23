/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.examples.example2;

import static com.github.webdriverbot.bot.BotBot.click;
import static com.github.webdriverbot.bot.BotBot.forEachRedirect;
import static com.github.webdriverbot.bot.BotBot.openPage;
import static com.github.webdriverbot.bot.BotBot.type;
import com.github.webdriverbot.context.WebDriverBotContext;
import com.github.webdriverbot.examples.example1.GoogleSearchPage;
import com.github.webdriverbot.examples.example1.GoogleSearchPage_;
import com.github.webdriverbot.examples.example1.GoogleSearchResults_;
import static com.github.webdriverextensions.Bot.currentUrl;
import static com.github.webdriverextensions.Bot.driver;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import com.github.webdriverextensions.internal.junitrunner.DriverPathLoader;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Example {

    public static void main(String[] args) {

        init();

        openPage(GoogleSearchPage.class); // no need to store result

        type("pizza", GoogleSearchPage_.searchField.class);

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
        WebDriverExtensionsContext.setDriver(driver);
        driver().manage().window().maximize();
        WebDriverBotContext.initBotData();
    }
}

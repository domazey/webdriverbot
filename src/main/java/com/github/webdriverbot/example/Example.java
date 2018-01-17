package com.github.webdriverbot.example;

import static com.github.webdriverbot.bot.BotBot.*;
import com.github.webdriverbot.context.WebDriverBotContext;
import com.github.webdriverextensions.internal.junitrunner.DriverPathLoader;
import org.openqa.selenium.WebDriver;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

public class Example {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 3; ++i) {
            executor.execute(() -> {
                init();

                GoogleSearchPage googleSearchPage = openPage(GoogleSearchPage.class);
                type("pizza", googleSearchPage.searchField);
                click(GoogleSearchPage_.submitButton.class);

                List<String> urls = new ArrayList<>();
                forEachRedirect(GoogleSearchResults_.links.class, () -> {
                    urls.add(currentUrl());
                });

                for (String url : urls) {
                    System.out.println("url: " + url);
                }

                driver().quit();
            });
        }

    }

    private static void init() {
        DriverPathLoader.loadDriverPaths(null); // use default fields
        WebDriver driver = new FirefoxDriver(); //random driver
        driver.manage().window().maximize();
        WebDriverExtensionsContext.setDriver(driver);
        WebDriverBotContext.init();
    }

}

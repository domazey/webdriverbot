package com.github.webdriverbot;

import com.github.webdriverbot.annotations.Url;
import com.github.webdriverbot.exceptions.NewWindowNotOpenedException;
import com.github.webdriverextensions.Bot;
import static com.github.webdriverextensions.internal.BotUtils.asNanos;
import com.sun.jna.Platform;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BotBot extends Bot {

    /**
     * Clicks link and redirects result to new tab
     *
     * @param element
     * @return Window handle of old tab
     */
    public static String openInNewTabWithRedirect(WebElement element) {

        String oldTabWindowHandle = currentWindowHandle();
        Set<String> oldWindowHandles = availableWindowHandles();

        openInNewTab(element);

        waitForNewWindowToOpen(oldWindowHandles);
        Set<String> newWindowHandles = availableWindowHandles();
        newWindowHandles.removeAll(oldWindowHandles);
        String newTabHandle = newWindowHandles.iterator().next();
        switchToWindow(newTabHandle);

        waitForPageToLoad();

        return oldTabWindowHandle;
    }

    public static void openInNewTab(WebElement element) {
        type(Keys.chord(getPlatformControlKey(), Keys.RETURN), element);
    }

    public static Set<String> availableWindowHandles() {
        return driver().getWindowHandles();
    }

    public static String currentWindowHandle() {
        return driver().getWindowHandle();
    }

    public static Keys getPlatformControlKey() {
        return platform().equals(Platform.MAC) ? Keys.COMMAND : Keys.CONTROL;
    }

    public static void switchToWindow(String handle) {
        driver().switchTo().window(handle);
    }

    public static void waitForNewWindowToOpen(Set<String> oldWindowHandles) {
        waitForNewWindowToOpen(oldWindowHandles, 5, TimeUnit.SECONDS);
    }

    public static void waitForNewWindowToOpen(Set<String> oldWindowHandles, double time, TimeUnit unit) {

        long toSleepNano = asNanos(time, unit);
        long defaultSleepTime = asNanos(0.5, TimeUnit.SECONDS);

        if (availableWindowHandles().size() > oldWindowHandles.size()) {
            return;
        }

        try {

            do {
                if (toSleepNano > defaultSleepTime) {
                    TimeUnit.NANOSECONDS.sleep(defaultSleepTime);
                    toSleepNano -= defaultSleepTime;
                } else {
                    TimeUnit.NANOSECONDS.sleep(toSleepNano);
                    toSleepNano = 0;
                }

                if (availableWindowHandles().size() > oldWindowHandles.size()) {
                    return;
                }
            } while (toSleepNano > 0);

            throw new NewWindowNotOpenedException("Waited for new tab to open but reached timeout");
        } catch (InterruptedException ex) {
            // Swallow exception
            ex.printStackTrace();
        }
    }

    public static void executeForEachLink(Collection<WebElement> links, Runnable function) {

        for (WebElement link : links) {
            String oldWindowHandle = openInNewTabWithRedirect(link);

            function.run();

            driver().close();

            driver().switchTo().window(oldWindowHandle);
        }

    }

    public static <U extends BotPage> U openPage(Class<U> webPageClass) {
        if (webPageClass.getAnnotation(Url.class) != null) {
            open(webPageClass.getAnnotation(Url.class).value());
        }

        WebDriverBotContext.setPage(currentWindowHandle(), webPageClass);

        return (U) WebDriverBotContext.getPage(currentWindowHandle());
    }

    public static <U extends BotPage> U getCurrentPage(Class<U> webPageClass) {
        return (U) WebDriverBotContext.getPage(webPageClass);
    }

    public static void waitForPageToLoad() {
        try {
        waitForPageToLoad(10);
        } catch(TimeoutException ex) {
            // don't throw if page is still loading
        }
    }

    public static void waitForPageToLoad(int seconds) {
        Wait<WebDriver> wait = new WebDriverWait(driver(), seconds);
        wait.until((WebDriver driver) -> {
            return String
                    .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                    .equals("complete");
        });
    }
}

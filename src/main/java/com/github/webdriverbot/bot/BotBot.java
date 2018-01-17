package com.github.webdriverbot.bot;

import com.github.webdriverbot.context.WebDriverBotContext;
import com.github.webdriverbot.decorator.WDBByDecoratorFactory;
import com.github.webdriverbot.pagefactory.BotPage;
import com.github.webdriverbot.annotations.Url;
import com.github.webdriverbot.exceptions.NewWindowNotOpenedException;
import com.github.webdriverbot.exceptions.WebDriverBotException;
import com.github.webdriverbot.metamodel.RepositoryMetaData;
import com.github.webdriverextensions.Bot;
import com.github.webdriverextensions.internal.BotUtils;
import static com.github.webdriverextensions.internal.BotUtils.asNanos;
import com.sun.jna.Platform;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BotBot extends Bot {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BotBot.class);
    
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

    public static void forEachRedirect(Collection<WebElement> links, Runnable function) {

        for (WebElement link : links) {
            String oldWindowHandle = openInNewTabWithRedirect(link);

            function.run();

            driver().close();

            driver().switchTo().window(oldWindowHandle);
        }

    }

    public static void forEachRedirect(Class metaElemClass, Runnable function) {

        Field targetField = getFieldFromMetamodel(metaElemClass);

        // also for findall and findbys 
        By by = new FindBy.FindByBuilder().buildIt(targetField.getAnnotation(FindBy.class), targetField);

        for (WebElement link : driver().findElements(by)) {
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
        } catch (TimeoutException ex) {
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

    private static Field getFieldFromMetamodel(Class metaElemClass) {
        RepositoryMetaData repositoryMetaData = (RepositoryMetaData) metaElemClass
                .getEnclosingClass()
                .getAnnotation(RepositoryMetaData.class);
        if (repositoryMetaData == null) {
            throw new RuntimeException("temporary exception thrown. Do not deploy!");
        }
        Class repositoryClass;
        try {
            repositoryClass = Class.forName(repositoryMetaData.packageName() + "." + repositoryMetaData.className());
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Repository class not found!", ex);
        }
        Field targetField;
        try {
            targetField = repositoryClass.getDeclaredField((String) metaElemClass.getDeclaredField("name").get(null));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("field not found in repository", ex);
        }
        return targetField;
    }

    protected static By getByFromField(Field field) throws SecurityException, RuntimeException {
        if (field.getAnnotation(FindBy.class) != null) {
            return new FindBy.FindByBuilder().buildIt(field.getAnnotation(FindBy.class), field);
        } else if (field.getAnnotation(FindAll.class) != null) {
            return new FindAll.FindByBuilder().buildIt(field.getAnnotation(FindAll.class), field);
        } else if (field.getAnnotation(FindBys.class) != null) {
            return new FindBys.FindByBuilder().buildIt(field.getAnnotation(FindBys.class), field);
        } else {
            throw new WebDriverBotException("Field " + field + " doesn't have any of @FindBy, @FindBys, @FindAll annotations");
        }
    }

    protected static By getByFromMetamodel(Class metaElemClass) {
        return getByFromField(getFieldFromMetamodel(metaElemClass));
    }

    protected static WebElement getDecoratedElement(By by) {
        return new WDBByDecoratorFactory().create(driver()).decorate(WebElement.class.getClassLoader(), by);
    }

    protected static WebElement getDecoratedElement(Class metaElemClass) {
        return getDecoratedElement(getByFromMetamodel(metaElemClass));
    }


    
    
    /* Click */
    public static void click(Class metaElemClass) {
        click(getDecoratedElement(metaElemClass));
    }



    /* Double Click */
    public static void doubleClick(Class metaElemClass) {
        doubleClick(getDecoratedElement(metaElemClass));
    }



    /* Type */
    public static void type(String text, Class metaElemClass) {
        type(text, getDecoratedElement(metaElemClass));
    }

    public static void type(double number, Class metaElemClass) {
        type(number, getDecoratedElement(metaElemClass));
    }



    /* Clear */
    public static void clear(Class metaElemClass) {
        clear(getDecoratedElement(metaElemClass));
    }

    public static void clearAndType(String text, Class metaElemClass) {
        clearAndType(text, getDecoratedElement(metaElemClass));
    }

    public static void clearAndType(double number, Class metaElemClass) {
        clear(getDecoratedElement(metaElemClass));
        type(number, getDecoratedElement(metaElemClass));
    }



    /* Press Keys */
    public static void pressEnter(Class metaElemClass) {
        pressEnter(getDecoratedElement(metaElemClass));
    }

    public static void pressKeys(Class metaElemClass, CharSequence... keys) {
        pressKeys(getDecoratedElement(metaElemClass));
    }



    /* Select/Deselect */
    public static void select(Class metaElemClass) {
        select(getDecoratedElement(metaElemClass));
    }

    public static void deselect(Class metaElemClass) {
        deselect(getDecoratedElement(metaElemClass));
    }

    public static void selectOption(String text, Class metaElemClass) {
        selectOption(text, getDecoratedElement(metaElemClass));
    }

    public static void deselectOption(String text, Class metaElemClass) {
        Bot.deselectOption(text, getDecoratedElement(metaElemClass));
    }

    public static void selectAllOptions(Class metaElemClass) {
        selectAllOptions(getDecoratedElement(metaElemClass));
    }

    public static void deselectAllOptions(Class metaElemClass) {
        deselectAllOptions(getDecoratedElement(metaElemClass));
    }

    public static void selectOptionWithValue(String value, Class metaElemClass) {
        Bot.selectOptionWithValue(value, getDecoratedElement(metaElemClass));
    }

    public static void deselectOptionWithValue(String value, Class metaElemClass) {
        Bot.deselectOptionWithValue(value, getDecoratedElement(metaElemClass));
    }

    public static void selectOptionWithIndex(int index, Class metaElemClass) {
        Bot.selectOptionWithIndex(index, getDecoratedElement(metaElemClass));
    }

    public static void deselectOptionWithIndex(int index, Class metaElemClass) {
        Bot.deselectOptionWithIndex(index, getDecoratedElement(metaElemClass));
    }



    /* Check/Uncheck */
    public static void check(Class metaElemClass) {
        check(getDecoratedElement(metaElemClass));
    }

    public static void uncheck(Class metaElemClass) {
        uncheck(getDecoratedElement(metaElemClass));
    }



    public static void waitForElementToDisplay(Class metaElemClass) {
        waitForElementToDisplay(getDecoratedElement(metaElemClass));
    }

    public static void waitForElementToDisplay(Class metaElemClass, long secondsToWait) {
        Bot.waitForElementToDisplay(getDecoratedElement(metaElemClass), secondsToWait);
    }

    public static void waitForElementToDisplay(Class metaElemClass, double timeToWait, TimeUnit unit) {
        Bot.waitForElementToDisplay(getDecoratedElement(metaElemClass), timeToWait, unit);
    }

    public static void waitForElementToDisplay(Class metaElemClass, long secondsToWait, long sleepInMillis) {
        Bot.waitForElementToDisplay(getDecoratedElement(metaElemClass), secondsToWait, sleepInMillis);
    }

    public static void waitForElementToDisplay(Class metaElemClass, double timeToWait, TimeUnit unit, long sleepInMillis) {
        Bot.waitForElementToDisplay(getDecoratedElement(metaElemClass), timeToWait, unit, sleepInMillis);
    }


    /* Scrolling */
    public static Object scrollTo(Class metaElemClass) {
        return scrollTo(getDecoratedElement(metaElemClass));
    }

    public static void debug(Class metaElemClass) {
        debug(getDecoratedElement(metaElemClass));
    }


    /* Is Displayed */
    public static boolean isDisplayed(Class metaElemClass) {
        return isDisplayed(getDecoratedElement(metaElemClass));
    }

    public static boolean isNotDisplayed(Class metaElemClass) {
        return isNotDisplayed(getDecoratedElement(metaElemClass));
    }

    public static boolean isDisplayed(Class metaElemClass, long secondsToWait) {
        return isDisplayed(getDecoratedElement(metaElemClass), secondsToWait);
    }

    public static boolean isNotDisplayed(Class metaElemClass, long secondsToWait) {
        return isNotDisplayed(getDecoratedElement(metaElemClass), secondsToWait);
    }

    public static void assertIsDisplayed(Class metaElemClass) {
        assertIsDisplayed(getDecoratedElement(metaElemClass));
    }

    public static void assertIsNotDisplayed(Class metaElemClass) {
        assertIsNotDisplayed(getDecoratedElement(metaElemClass));
    }

    public static void assertIsDisplayed(Class metaElemClass, long secondsToWait) {
        assertIsDisplayed(getDecoratedElement(metaElemClass), secondsToWait);
    }

    public static void assertIsNotDisplayed(Class metaElemClass, long secondsToWait) {
        assertIsNotDisplayed(getDecoratedElement(metaElemClass), secondsToWait);
    }


    /* Tag Name */
    public static String tagNameOf(Class metaElemClass) {
        return tagNameOf(getDecoratedElement(metaElemClass));
    }

    public static boolean tagNameEquals(String value, Class metaElemClass) {
        return tagNameEquals(value, getDecoratedElement(metaElemClass));
    }

    public static boolean tagNameNotEquals(String value, Class metaElemClass) {
        return tagNameNotEquals(value, getDecoratedElement(metaElemClass));
    }

//    public static void assertTagNameEquals(String value, Class metaElemClass) {
//        BotUtils.assertEquals("Tag name", value, tagNameOf(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTagNameNotEquals(String value, Class metaElemClass) {
//        BotUtils.assertNotEquals("Tag name", value, tagNameOf(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Attribute */
//    /**
//     * Returns a {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} attribute value.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input title="Some title"/>
//     * attributeIn("title", input) = "Some title"
//     *
//     * no input in html
//     * attributeIn("title", "input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * an attribute to return
//     * @return the id attribute
//     */
//    public static String attributeIn(String name, Class metaElemClass) {
//        return getDecoratedElement(metaElemClass).getAttribute(name);
//    }
//
//    public static boolean hasAttribute(String name, Class metaElemClass) {
//        return getDecoratedElement(metaElemClass).getAttribute(name) != null;
//    }
//
//    public static boolean hasNotAttribute(String name, Class metaElemClass) {
//        return !hasAttribute(name, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean attributeEquals(String name, String value, Class metaElemClass) {
//        return BotUtils.isEqual(value, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeNotEquals(String name, String value, Class metaElemClass) {
//        return BotUtils.notEquals(value, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeContains(String name, String searchText, Class metaElemClass) {
//        return BotUtils.contains(searchText, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeNotContains(String name, String searchText, Class metaElemClass) {
//        return BotUtils.notContains(searchText, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeStartsWith(String name, String prefix, Class metaElemClass) {
//        return BotUtils.startsWith(prefix, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeNotStartsWith(String name, String prefix, Class metaElemClass) {
//        return BotUtils.notStartsWith(prefix, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeEndsWith(String name, String suffix, Class metaElemClass) {
//        return BotUtils.endsWith(suffix, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeNotEndsWith(String name, String suffix, Class metaElemClass) {
//        return BotUtils.notEndsWith(suffix, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeMatches(String name, String regExp, Class metaElemClass) {
//        return BotUtils.matches(regExp, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeNotMatches(String name, String regExp, Class metaElemClass) {
//        return BotUtils.notMatches(regExp, attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static void assertHasAttribute(String name, Class metaElemClass) {
//        if (hasNotAttribute(name, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element does not have attribute " + quote(name), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotAttribute(String name, Class metaElemClass) {
//        if (hasAttribute(name, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has attribute " + quote(name) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertAttributeEquals(String name, String value, Class metaElemClass) {
//        BotUtils.assertEquals("Element attribute " + name, value, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeNotEquals(String name, String value, Class metaElemClass) {
//        BotUtils.assertNotEquals("Element attribute " + name, value, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeContains(String name, String searchText, Class metaElemClass) {
//        BotUtils.assertContains("Element attribute " + name, searchText, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeNotContains(String name, String searchText, Class metaElemClass) {
//        BotUtils.assertNotContains("Element attribute " + name, searchText, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeStartsWith(String name, String prefix, Class metaElemClass) {
//        BotUtils.assertStartsWith("Element attribute " + name, prefix, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeNotStartsWith(String name, String prefix, Class metaElemClass) {
//        BotUtils.assertNotStartsWith("Element attribute " + name, prefix, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeEndsWith(String name, String suffix, Class metaElemClass) {
//        BotUtils.assertEndsWith("Element attribute " + name, suffix, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeNotEndsWith(String name, String suffix, Class metaElemClass) {
//        BotUtils.assertNotEndsWith("Element attribute " + name, suffix, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeMatches(String name, String regExp, Class metaElemClass) {
//        BotUtils.assertMatches("Element attribute " + name, regExp, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeNotMatches(String name, String regExp, Class metaElemClass) {
//        BotUtils.assertNotMatches("Element attribute " + name, regExp, attributeIn(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Attribute as Number */
//    public static double attributeInAsNumber(String name, Class metaElemClass) {
//        return createDouble(attributeIn(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeIsNumber(String name, Class metaElemClass) {
//        try {
//            attributeInAsNumber(name, getDecoratedElement(metaElemClass));
//            return true;
//        } catch (NumberFormatException e) {
//            return false;
//        }
//    }
//
//    public static boolean attributeIsNotNumber(String name, Class metaElemClass) {
//        return !attributeIsNumber(name, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean attributeEquals(String name, double number, Class metaElemClass) {
//        return BotUtils.isEqual(number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeNotEquals(String name, double number, Class metaElemClass) {
//        return BotUtils.notEquals(number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeLessThan(String name, double number, Class metaElemClass) {
//        return BotUtils.lessThan(number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeLessThanOrEquals(String name, double number, Class metaElemClass) {
//        return BotUtils.lessThanOrEquals(number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeGreaterThan(String name, double number, Class metaElemClass) {
//        return BotUtils.greaterThan(number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean attributeGreaterThanOrEquals(String name, double number, Class metaElemClass) {
//        return BotUtils.greaterThanOrEquals(number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)));
//    }
//
//    public static void assertAttributeIsNumber(String name, Class metaElemClass) {
//        if (attributeIsNotNumber(name, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element attribute " + name + " is not a number", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertAttributeIsNotNumber(String name, Class metaElemClass) {
//        if (attributeIsNumber(name, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element attribute " + name + " is a number when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertAttributeEquals(String name, double number, Class metaElemClass) {
//        BotUtils.assertEquals(name, number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeNotEquals(String name, double number, Class metaElemClass) {
//        BotUtils.assertNotEquals(name, number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeLessThan(String name, double number, Class metaElemClass) {
//        BotUtils.assertLessThan(name, number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeLessThanOrEquals(String name, double number, Class metaElemClass) {
//        BotUtils.assertLessThanOrEquals(name, number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeGreaterThan(String name, double number, Class metaElemClass) {
//        BotUtils.assertGreaterThan(name, number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertAttributeGreaterThanOrEquals(String name, double number, Class metaElemClass) {
//        BotUtils.assertGreaterThanOrEquals(name, number, attributeInAsNumber(name, getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Id */
//    /**
//     * Returns the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} id attribute.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input id="some-id"/>
//     * idIn(input) = "some-id"
//     *
//     * no input in html
//     * idIn(input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * an id attribute
//     * @return the id attribute
//     */
//    public static String idIn(Class metaElemClass) {
//        return attributeIn("id", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasId(Class metaElemClass) {
//        return hasAttribute("id", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasNotId(Class metaElemClass) {
//        return hasNotAttribute("id", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idEquals(String value, Class metaElemClass) {
//        return attributeEquals("id", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idNotEquals(String value, Class metaElemClass) {
//        return attributeNotEquals("id", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idContains(String searchText, Class metaElemClass) {
//        return attributeContains("id", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idNotContains(String searchText, Class metaElemClass) {
//        return attributeNotContains("id", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idStartsWith(String prefix, Class metaElemClass) {
//        return attributeStartsWith("id", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idNotStartsWith(String prefix, Class metaElemClass) {
//        return attributeNotStartsWith("id", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idEndsWith(String suffix, Class metaElemClass) {
//        return attributeEndsWith("id", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idNotEndsWith(String suffix, Class metaElemClass) {
//        return attributeNotEndsWith("id", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idMatches(String regExp, Class metaElemClass) {
//        return attributeMatches("id", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean idNotMatches(String regExp, Class metaElemClass) {
//        return attributeNotMatches("id", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasId(Class metaElemClass) {
//        assertHasAttribute("id", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasNotId(Class metaElemClass) {
//        assertHasNotAttribute("id", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdEquals(String value, Class metaElemClass) {
//        assertAttributeEquals("id", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdNotEquals(String value, Class metaElemClass) {
//        assertAttributeNotEquals("id", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdContains(String searchText, Class metaElemClass) {
//        assertAttributeContains("id", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdNotContains(String searchText, Class metaElemClass) {
//        assertAttributeNotContains("id", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeStartsWith("id", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdNotStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeNotStartsWith("id", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeEndsWith("id", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdNotEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeNotEndsWith("id", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdMatches(String regExp, Class metaElemClass) {
//        assertAttributeMatches("id", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIdNotMatches(String regExp, Class metaElemClass) {
//        assertAttributeNotMatches("id", regExp, getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Name */
//    /**
//     * Returns the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} name attribute.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input name="some-name"/>
//     * nameIn(input) = "some-name"
//     *
//     * no input in html
//     * nameIn(input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * a name attribute
//     * @return the name attribute
//     */
//    public static String nameIn(Class metaElemClass) {
//        return attributeIn("name", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasName(Class metaElemClass) {
//        return hasAttribute("name", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasNotName(Class metaElemClass) {
//        return hasNotAttribute("name", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameEquals(String value, Class metaElemClass) {
//        return attributeEquals("name", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameNotEquals(String value, Class metaElemClass) {
//        return attributeNotEquals("name", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameContains(String searchText, Class metaElemClass) {
//        return attributeContains("name", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameNotContains(String searchText, Class metaElemClass) {
//        return attributeNotContains("name", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameStartsWith(String prefix, Class metaElemClass) {
//        return attributeStartsWith("name", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameNotStartsWith(String prefix, Class metaElemClass) {
//        return attributeNotStartsWith("name", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameEndsWith(String suffix, Class metaElemClass) {
//        return attributeEndsWith("name", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameNotEndsWith(String suffix, Class metaElemClass) {
//        return attributeNotEndsWith("name", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameMatches(String regExp, Class metaElemClass) {
//        return attributeMatches("name", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean nameNotMatches(String regExp, Class metaElemClass) {
//        return attributeNotMatches("name", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasName(Class metaElemClass) {
//        assertHasAttribute("name", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasNotName(Class metaElemClass) {
//        assertHasNotAttribute("name", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameEquals(String value, Class metaElemClass) {
//        assertAttributeEquals("name", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameNotEquals(String value, Class metaElemClass) {
//        assertAttributeNotEquals("name", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameContains(String searchText, Class metaElemClass) {
//        assertAttributeContains("name", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameNotContains(String searchText, Class metaElemClass) {
//        assertAttributeNotContains("name", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeStartsWith("name", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameNotStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeNotStartsWith("name", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeEndsWith("name", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameNotEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeNotEndsWith("name", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameMatches(String regExp, Class metaElemClass) {
//        assertAttributeMatches("name", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertNameNotMatches(String regExp, Class metaElemClass) {
//        assertAttributeNotMatches("name", regExp, getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Class */
//    /**
//     * Returns the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} class attribute.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input class="a-class another-class"/>
//     * classIn(input) = "a-class another-class"
//     *
//     * no input in html
//     * classIn(input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * a class attribute
//     * @return the class attribute
//     */
//    public static String classIn(Class metaElemClass) {
//        return attributeIn("class", getDecoratedElement(metaElemClass));
//    }
//
//    /**
//     * Returns the classes in the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} class
//     * attribute.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input class=" a-class   another-class "/>
//     * classesIn(input) = "a-class", "another-class"
//     *
//     * no input in html
//     * classIn(input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * a class attribute
//     * @return the classes in the class attribute
//     */
//    public static List<String> classesIn(Class metaElemClass) {
//        return Arrays.asList(StringUtils.split(classIn(getDecoratedElement(metaElemClass))));
//    }
//
//    public static boolean hasClass(Class metaElemClass) {
//        return hasAttribute("class", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasNotClass(Class metaElemClass) {
//        return hasNotAttribute("class", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasClass(String className, Class metaElemClass) {
//        List<String> classes = classesIn(getDecoratedElement(metaElemClass));
//        for (String clazz : classes) {
//            if (BotUtils.isEqual(className, clazz)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean hasNotClass(String className, Class metaElemClass) {
//        return !hasClass(className, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasClassContaining(String searchText, Class metaElemClass) {
//        List<String> classes = classesIn(getDecoratedElement(metaElemClass));
//        for (String clazz : classes) {
//            if (BotUtils.contains(searchText, clazz)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean hasNotClassContaining(String searchText, Class metaElemClass) {
//        return !hasClassContaining(searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasClassStartingWith(String prefix, Class metaElemClass) {
//        List<String> classes = classesIn(getDecoratedElement(metaElemClass));
//        for (String clazz : classes) {
//            if (BotUtils.startsWith(prefix, clazz)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean hasNotClassStartingWith(String prefix, Class metaElemClass) {
//        return !hasClassStartingWith(prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasClassEndingWith(String suffix, Class metaElemClass) {
//        List<String> classes = classesIn(getDecoratedElement(metaElemClass));
//        for (String clazz : classes) {
//            if (BotUtils.endsWith(suffix, clazz)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean hasNotClassEndingWith(String suffix, Class metaElemClass) {
//        return !hasClassEndingWith(suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasClassMatching(String regExp, Class metaElemClass) {
//        List<String> classes = classesIn(getDecoratedElement(metaElemClass));
//        for (String clazz : classes) {
//            if (BotUtils.matches(regExp, clazz)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean hasNotClassMatching(String regExp, Class metaElemClass) {
//        return !hasClassMatching(regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasClass(Class metaElemClass) {
//        assertHasAttribute("class", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasNotClass(Class metaElemClass) {
//        assertHasNotAttribute("class", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasClass(String className, Class metaElemClass) {
//        if (hasNotClass(className, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element does not have class " + quote(className.trim()), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotClass(String className, Class metaElemClass) {
//        if (hasClass(className, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has class " + quote(className.trim()) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasClassContaining(String searchText, Class metaElemClass) {
//        if (hasNotClassContaining(searchText, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element does not have class containing text " + quote(searchText.trim()), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotClassContaining(String searchText, Class metaElemClass) {
//        if (hasClassContaining(searchText, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has class containing text " + quote(searchText.trim()) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasClassStartingWith(String prefix, Class metaElemClass) {
//        if (hasNotClassStartingWith(prefix, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element does not have class containing prefix " + quote(prefix.trim()), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotClassStartingWith(String prefix, Class metaElemClass) {
//        if (hasClassStartingWith(prefix, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has class containing prefix " + quote(prefix.trim()) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasClassEndingWith(String suffix, Class metaElemClass) {
//        if (hasNotClassEndingWith(suffix, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element does not have class containing suffix " + quote(suffix.trim()), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotClassEndingWith(String suffix, Class metaElemClass) {
//        if (hasClassEndingWith(suffix, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has class containing suffix " + quote(suffix.trim()) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasClassMatching(String regExp, Class metaElemClass) {
//        if (hasNotClassMatching(regExp, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element does not have class matching regExp " + quote(regExp.trim()), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotClassMatching(String regExp, Class metaElemClass) {
//        if (hasClassMatching(regExp, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has class matching regExp " + quote(regExp.trim()) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//
//
//    /* Value */
//    /**
//     * Returns the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} value attribute.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input value="Some value"/>
//     * valueIn(input) = "Some value"
//     *
//     * no input in html
//     * valueIn(input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * a value attribute
//     * @return the value attribute
//     */
//    public static String valueIn(Class metaElemClass) {
//        return attributeIn("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasValue(Class metaElemClass) {
//        return hasAttribute("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasNotValue(Class metaElemClass) {
//        return hasNotAttribute("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueEquals(String value, Class metaElemClass) {
//        return attributeEquals("value", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueNotEquals(String value, Class metaElemClass) {
//        return attributeNotEquals("value", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueContains(String searchText, Class metaElemClass) {
//        return attributeContains("value", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueNotContains(String searchText, Class metaElemClass) {
//        return attributeNotContains("value", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueStartsWith(String prefix, Class metaElemClass) {
//        return attributeStartsWith("value", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueNotStartsWith(String prefix, Class metaElemClass) {
//        return attributeNotStartsWith("value", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueEndsWith(String suffix, Class metaElemClass) {
//        return attributeEndsWith("value", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueNotEndsWith(String suffix, Class metaElemClass) {
//        return attributeNotEndsWith("value", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueMatches(String regExp, Class metaElemClass) {
//        return attributeMatches("value", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueNotMatches(String regExp, Class metaElemClass) {
//        return attributeNotMatches("value", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasValue(Class metaElemClass) {
//        assertHasAttribute("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasNotValue(Class metaElemClass) {
//        assertHasNotAttribute("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueEquals(String value, Class metaElemClass) {
//        assertAttributeEquals("value", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueNotEquals(String value, Class metaElemClass) {
//        assertAttributeNotEquals("value", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueContains(String searchText, Class metaElemClass) {
//        assertAttributeContains("value", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueNotContains(String searchText, Class metaElemClass) {
//        assertAttributeNotContains("value", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeStartsWith("value", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueNotStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeNotStartsWith("value", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeEndsWith("value", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueNotEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeNotEndsWith("value", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueMatches(String regExp, Class metaElemClass) {
//        assertAttributeMatches("value", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueNotMatches(String regExp, Class metaElemClass) {
//        assertAttributeNotMatches("value", regExp, getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Value as Number */
//    /**
//     * Returns the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} value attribute as a
//     * number.
//     *
//     * <p>If the value attribute in the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)}
//     * does not contain a valid number a {@code java.util.NumberFormatException}
//     * will be thrown. If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not
//     * exist in the html a {@code org.openqa.selenium.NoSuchElementException}
//     * will be thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input value="42"/>
//     * valueInAsNumber(input) = 42.0
//     *
//     * <input value="Some value"/>
//     * valueInAsNumber(input) throws java.util.NumberFormatException
//     *
//     * <input value=""/>
//     * valueInAsNumber(input) throws java.util.NumberFormatException
//     *
//     * no input in html
//     * valueInAsNumber(input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * a value attribute with a number
//     * @return the value attribute as a number
//     */
//    public static double valueInAsNumber(Class metaElemClass) {
//        return attributeInAsNumber("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueIsNumber(Class metaElemClass) {
//        return attributeIsNumber("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueIsNotNumber(Class metaElemClass) {
//        return attributeIsNotNumber("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueEquals(double number, Class metaElemClass) {
//        return attributeEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueNotEquals(double number, Class metaElemClass) {
//        return attributeNotEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueLessThan(double number, Class metaElemClass) {
//        return attributeLessThan("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueLessThanOrEquals(double number, Class metaElemClass) {
//        return attributeLessThanOrEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueGreaterThan(double number, Class metaElemClass) {
//        return attributeGreaterThan("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean valueGreaterThanOrEquals(double number, Class metaElemClass) {
//        return attributeGreaterThanOrEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueIsNumber(Class metaElemClass) {
//        assertAttributeIsNumber("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueIsNotNumber(Class metaElemClass) {
//        assertAttributeIsNotNumber("value", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueEquals(double number, Class metaElemClass) {
//        assertAttributeEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueNotEquals(double number, Class metaElemClass) {
//        assertAttributeNotEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueLessThan(double number, Class metaElemClass) {
//        assertAttributeLessThan("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueLessThanOrEquals(double number, Class metaElemClass) {
//        assertAttributeLessThanOrEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueGreaterThan(double number, Class metaElemClass) {
//        assertAttributeGreaterThan("value", number, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertValueGreaterThanOrEquals(double number, Class metaElemClass) {
//        assertAttributeGreaterThanOrEquals("value", number, getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Href */
//    /**
//     * Returns the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} href attribute.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <input href="www.href.com"/>
//     * hrefIn(input) = "www.href.com"
//     *
//     * no input in html
//     * hrefIn(input) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * a href attribute
//     * @return the href attribute
//     */
//    public static String hrefIn(Class metaElemClass) {
//        return attributeIn("href", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasHref(Class metaElemClass) {
//        return hasAttribute("href", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hasNotHref(Class metaElemClass) {
//        return hasNotAttribute("href", getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefEquals(String value, Class metaElemClass) {
//        return attributeEquals("href", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefNotEquals(String value, Class metaElemClass) {
//        return attributeNotEquals("href", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefContains(String searchText, Class metaElemClass) {
//        return attributeContains("href", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefNotContains(String searchText, Class metaElemClass) {
//        return attributeNotContains("href", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefStartsWith(String prefix, Class metaElemClass) {
//        return attributeStartsWith("href", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefNotStartsWith(String prefix, Class metaElemClass) {
//        return attributeNotStartsWith("href", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefEndsWith(String suffix, Class metaElemClass) {
//        return attributeEndsWith("href", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefNotEndsWith(String suffix, Class metaElemClass) {
//        return attributeNotEndsWith("href", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefMatches(String regExp, Class metaElemClass) {
//        return attributeMatches("href", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean hrefNotMatches(String regExp, Class metaElemClass) {
//        return attributeNotMatches("href", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasHref(Class metaElemClass) {
//        assertHasAttribute("href", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHasNotHref(Class metaElemClass) {
//        assertHasNotAttribute("href", getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefEquals(String value, Class metaElemClass) {
//        assertAttributeEquals("href", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefNotEquals(String value, Class metaElemClass) {
//        assertAttributeNotEquals("href", value, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefContains(String searchText, Class metaElemClass) {
//        assertAttributeContains("href", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefNotContains(String searchText, Class metaElemClass) {
//        assertAttributeNotContains("href", searchText, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeStartsWith("href", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefNotStartsWith(String prefix, Class metaElemClass) {
//        assertAttributeNotStartsWith("href", prefix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeEndsWith("href", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefNotEndsWith(String suffix, Class metaElemClass) {
//        assertAttributeNotEndsWith("href", suffix, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefMatches(String regExp, Class metaElemClass) {
//        assertAttributeMatches("href", regExp, getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertHrefNotMatches(String regExp, Class metaElemClass) {
//        assertAttributeNotMatches("href", regExp, getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Text */
//    /**
//     * Returns the visible text in a {@link org.openqa.selenium.getDecoratedElement(metaElemClass)}.
//     *
//     * <p>If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in the
//     * html a {@code org.openqa.selenium.NoSuchElementException} will be thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <span>Some text</span>
//     * textIn(span) = "Some text"
//     *
//     * <span>
//     *     Some text containing <b>html</b>
//     * </span>
//     * textIn(span) = "Some text containing html"
//     *
//     * <span style="display: none">Some invisible text</span>
//     * textIn(span) = ""
//     *
//     * no span in html
//     * textIn(span) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * the text
//     * @return the visible text
//     */
//    public static String textIn(Class metaElemClass) {
//        // Text is trimmed to normalize behavior since Chrome and PhantomJS driver incorrectly returns spaces around the text (Not according the the getDecoratedElement(metaElemClass) tetText docs), see bug report https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/7473 remove this when bug is solved!
//        return StringUtils.trim(getDecoratedElement(metaElemClass).getText());
//    }
//
//    public static boolean hasText(Class metaElemClass) {
//        return BotUtils.notEquals("", textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean hasNotText(Class metaElemClass) {
//        return BotUtils.isEqual("", textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textEquals(String text, Class metaElemClass) {
//        return BotUtils.isEqual(text, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotEquals(String text, Class metaElemClass) {
//        return BotUtils.notEquals(text, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textEqualsIgnoreCase(String text, Class metaElemClass) {
//        return BotUtils.equalsIgnoreCase(text, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotEqualsIgnoreCase(String text, Class metaElemClass) {
//        return BotUtils.notEqualsIgnoreCase(text, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textContains(String searchText, Class metaElemClass) {
//        return BotUtils.contains(searchText, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotContains(String searchText, Class metaElemClass) {
//        return BotUtils.notContains(searchText, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textContainsIgnoreCase(String searchText, Class metaElemClass) {
//        return BotUtils.containsIgnoreCase(searchText, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotContainsIgnoreCase(String searchText, Class metaElemClass) {
//        return BotUtils.notContainsIgnoreCase(searchText, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textStartsWith(String prefix, Class metaElemClass) {
//        return BotUtils.startsWith(prefix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotStartsWith(String prefix, Class metaElemClass) {
//        return BotUtils.notStartsWith(prefix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textStartsWithIgnoreCase(String prefix, Class metaElemClass) {
//        return BotUtils.startsWithIgnoreCase(prefix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotStartsWithIgnoreCase(String prefix, Class metaElemClass) {
//        return BotUtils.notStartsWithIgnoreCase(prefix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textEndsWith(String suffix, Class metaElemClass) {
//        return BotUtils.endsWith(suffix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotEndsWith(String suffix, Class metaElemClass) {
//        return BotUtils.notEndsWith(suffix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textEndsWithIgnoreCase(String suffix, Class metaElemClass) {
//        return BotUtils.endsWithIgnoreCase(suffix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotEndsWithIgnoreCase(String suffix, Class metaElemClass) {
//        return BotUtils.notEndsWithIgnoreCase(suffix, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textMatches(String regExp, Class metaElemClass) {
//        return BotUtils.matches(regExp, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotMatches(String regExp, Class metaElemClass) {
//        return BotUtils.notMatches(regExp, textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static void assertHasText(Class metaElemClass) {
//        if (hasNotText(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has no text", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotText(Class metaElemClass) {
//        if (hasText(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has text " + quote(textIn(getDecoratedElement(metaElemClass))) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertTextEquals(String text, Class metaElemClass) {
//        BotUtils.assertEquals("Text", text, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotEquals(String text, Class metaElemClass) {
//        BotUtils.assertNotEquals("Text", text, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextEqualsIgnoreCase(String text, Class metaElemClass) {
//        BotUtils.assertEqualsIgnoreCase("Text", text, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotEqualsIgnoreCase(String text, Class metaElemClass) {
//        BotUtils.assertNotEqualsIgnoreCase("Text", text, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextContains(String searchText, Class metaElemClass) {
//        BotUtils.assertContains("Text", searchText, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotContains(String searchText, Class metaElemClass) {
//        BotUtils.assertNotContains("Text", searchText, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextContainsIgnoreCase(String searchText, Class metaElemClass) {
//        BotUtils.assertContainsIgnoreCase("Text", searchText, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotContainsIgnoreCase(String searchText, Class metaElemClass) {
//        BotUtils.assertNotContainsIgnoreCase("Text", searchText, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextStartsWith(String prefix, Class metaElemClass) {
//        BotUtils.assertStartsWith("Text", prefix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotStartsWith(String prefix, Class metaElemClass) {
//        BotUtils.assertNotStartsWith("Text", prefix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextStartsWithIgnoreCase(String prefix, Class metaElemClass) {
//        BotUtils.assertStartsWithIgnoreCase("Text", prefix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotStartsWithIgnoreCase(String prefix, Class metaElemClass) {
//        BotUtils.assertNotStartsWithIgnoreCase("Text", prefix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextEndsWith(String suffix, Class metaElemClass) {
//        BotUtils.assertEndsWith("Text", suffix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotEndsWith(String suffix, Class metaElemClass) {
//        BotUtils.assertNotEndsWith("Text", suffix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextEndsWithIgnoreCase(String suffix, Class metaElemClass) {
//        BotUtils.assertEndsWithIgnoreCase("Text", suffix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotEndsWithIgnoreCase(String suffix, Class metaElemClass) {
//        BotUtils.assertNotEndsWithIgnoreCase("Text", suffix, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextMatches(String regExp, Class metaElemClass) {
//        BotUtils.assertMatches("Text", regExp, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotMatches(String regExp, Class metaElemClass) {
//        BotUtils.assertNotMatches("Text", regExp, textIn(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Text as Number */
//    /**
//     * Returns the visible text in a {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} as a
//     * number.
//     *
//     * <p>If the text in the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not
//     * contain a valid number a {@code java.util.NumberFormatException} will be
//     * thrown. If the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} does not exist in
//     * the html a {@code org.openqa.selenium.NoSuchElementException} will be
//     * thrown.</p>
//     *
//     * <p>
//     * <b>Examples:</b>
//     * <pre>
//     * {@code
//     * <span>42</span>
//     * textInAsNumber(span) = 42.0
//     *
//     * <span>
//     *     42
//     * </span>
//     * textInAsNumber(span) = 42.0
//     *
//     * <span>Some text</span>
//     * textInAsNumber(span) throws java.util.NumberFormatException
//     *
//     * <span style="display: none">42</span>
//     * textInAsNumber(span) throws java.util.NumberFormatException
//     *
//     * no span in html
//     * textInAsNumber(span) throws org.openqa.selenium.NoSuchElementException}</pre>
//     * </p>
//     *
//     * @param getDecoratedElement(metaElemClass) the {@link org.openqa.selenium.getDecoratedElement(metaElemClass)} containing
//     * a text with a number
//     * @return the visible text as a number
//     */
//     public static double textInAsNumber(Class metaElemClass) {
//        return createDouble(textIn(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textIsNumber(Class metaElemClass) {
//        try {
//            textInAsNumber(getDecoratedElement(metaElemClass));
//            return true;
//        } catch (NumberFormatException e) {
//            return false;
//        }
//    }
//
//    public static boolean textIsNotNumber(Class metaElemClass) {
//        return !textIsNumber(getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean textEquals(double number, Class metaElemClass) {
//        return BotUtils.isEqual(number, textInAsNumber(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textNotEquals(double number, Class metaElemClass) {
//        return BotUtils.notEquals(number, textInAsNumber(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textLessThan(double number, Class metaElemClass) {
//        return BotUtils.lessThan(number, textInAsNumber(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textLessThanOrEquals(double number, Class metaElemClass) {
//        return BotUtils.lessThanOrEquals(number, textInAsNumber(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textGreaterThan(double number, Class metaElemClass) {
//        return BotUtils.greaterThan(number, textInAsNumber(getDecoratedElement(metaElemClass)));
//    }
//
//    public static boolean textGreaterThanOrEquals(double number, Class metaElemClass) {
//        return BotUtils.greaterThanOrEquals(number, textInAsNumber(getDecoratedElement(metaElemClass)));
//    }
//
//    public static void assertTextIsNumber(Class metaElemClass) {
//        if (textIsNotNumber(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element text is not a number", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertTextIsNotNumber(Class metaElemClass) {
//        if (textIsNumber(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element text is a number when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertTextEquals(double number, Class metaElemClass) {
//        BotUtils.assertEquals("Text", number, textInAsNumber(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextNotEquals(double number, Class metaElemClass) {
//        BotUtils.assertNotEquals("Text", number, textInAsNumber(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextLessThan(double number, Class metaElemClass) {
//        BotUtils.assertLessThan("Text", number, textInAsNumber(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextLessThanOrEquals(double number, Class metaElemClass) {
//        BotUtils.assertLessThanOrEquals("Text", number, textInAsNumber(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextGreaterThan(double number, Class metaElemClass) {
//        BotUtils.assertGreaterThan("Text", number, textInAsNumber(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertTextGreaterThanOrEquals(double number, Class metaElemClass) {
//        BotUtils.assertGreaterThanOrEquals("Text", number, textInAsNumber(getDecoratedElement(metaElemClass)), getDecoratedElement(metaElemClass));
//    }
//
//
//
//    /* Selected/Deselected */
//    public static boolean isSelected(Class metaElemClass) {
//        return getDecoratedElement(metaElemClass).isSelected();
//    }
//
//    public static boolean isDeselected(Class metaElemClass) {
//        return !isSelected(getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIsSelected(Class metaElemClass) {
//        if (isDeselected(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element is not selected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertIsDeselected(Class metaElemClass) {
//        if (isSelected(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element is not deselected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//
//
//
//    /* Checked/Unchecked */
//    public static boolean isChecked(Class metaElemClass) {
//        return getDecoratedElement(metaElemClass).isSelected();
//    }
//
//    public static boolean isUnchecked(Class metaElemClass) {
//        return !isChecked(getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIsChecked(Class metaElemClass) {
//        if (isUnchecked(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element is not checked", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertIsUnchecked(Class metaElemClass) {
//        if (isChecked(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element is not unchecked", getDecoratedElement(metaElemClass));
//        }
//    }
//
//
//
//    /* Enabled/Disabled */
//    public static boolean isEnabled(Class metaElemClass) {
//        return getDecoratedElement(metaElemClass).isEnabled();
//    }
//
//    public static boolean isDisabled(Class metaElemClass) {
//        return !isEnabled(getDecoratedElement(metaElemClass));
//    }
//
//    public static void assertIsEnabled(Class metaElemClass) {
//        if (isDisabled(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element is not enabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertIsDisabled(Class metaElemClass) {
//        if (isEnabled(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element is not disabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//
//
//    /* Option */
//    public static boolean hasOption(String text, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (textEquals(text, option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean hasNotOption(String text, Class metaElemClass) {
//        return !hasOption(text, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean optionIsEnabled(String text, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (textEquals(text, option) && isEnabled(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean optionIsDisabled(String text, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (textEquals(text, option) && isDisabled(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean optionIsSelected(String text, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (textEquals(text, option) && isSelected(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean optionIsDeselected(String text, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (textEquals(text, option) && isDeselected(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean allOptionsAreSelected(Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (isDeselected(option)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public static boolean noOptionIsSelected(Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (isSelected(option)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public static void assertHasOption(String text, Class metaElemClass) {
//        if (hasNotOption(text, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has no option " + quote(text.trim()), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotOption(String text, Class metaElemClass) {
//        if (hasOption(text, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has option " + quote(text.trim()) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionIsEnabled(String text, Class metaElemClass) {
//        assertHasOption(text, getDecoratedElement(metaElemClass));
//        if (optionIsDisabled(text, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option " + quote(text.trim()) + " is not enabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionIsDisabled(String text, Class metaElemClass) {
//        assertHasOption(text, getDecoratedElement(metaElemClass));
//        if (optionIsEnabled(text, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option " + quote(text.trim()) + " is not disabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionIsSelected(String text, Class metaElemClass) {
//        assertHasOption(text, getDecoratedElement(metaElemClass));
//        if (optionIsDeselected(text, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option " + quote(text.trim()) + " is not selected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionIsDeselected(String text, Class metaElemClass) {
//        assertHasOption(text, getDecoratedElement(metaElemClass));
//        if (optionIsSelected(text, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option " + quote(text.trim()) + " is not deselected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertAllOptionsAreSelected(Class metaElemClass) {
//        if (!allOptionsAreSelected(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("All options are not selected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertNoOptionIsSelected(Class metaElemClass) {
//        if (!noOptionIsSelected(getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("All options are not deselected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//
//
//    /* Option Value */
//    public static boolean hasOptionWithValue(String value, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (valueEquals(value, option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean hasNotOptionWithValue(String value, Class metaElemClass) {
//        return !hasOptionWithValue(value, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean optionWithValueIsEnabled(String value, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (valueEquals(value, option) && isEnabled(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean optionWithValueIsDisabled(String value, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (valueEquals(value, option) && isDisabled(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean optionWithValueIsSelected(String value, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (valueEquals(value, option) && isSelected(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean optionWithValueIsDeselected(String value, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        for (getDecoratedElement(metaElemClass) option : options) {
//            if (valueEquals(value, option) && isDeselected(option)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static void assertHasOptionWithValue(String value, Class metaElemClass) {
//        if (hasNotOptionWithValue(value, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has no option with value " + quote(value.trim()), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotOptionWithValue(String value, Class metaElemClass) {
//        if (hasOptionWithValue(value, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has option with value " + quote(value.trim()) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithValueIsEnabled(String value, Class metaElemClass) {
//        assertHasOptionWithValue(value, getDecoratedElement(metaElemClass));
//        if (optionWithValueIsDisabled(value, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with value " + quote(value.trim()) + " is not enabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithValueIsDisabled(String value, Class metaElemClass) {
//        assertHasOptionWithValue(value, getDecoratedElement(metaElemClass));
//        if (optionWithValueIsEnabled(value, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with value " + quote(value.trim()) + " is not disabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithValueIsSelected(String value, Class metaElemClass) {
//        assertHasOptionWithValue(value, getDecoratedElement(metaElemClass));
//        if (optionWithValueIsDeselected(value, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with value " + quote(value.trim()) + " is not selected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithValueIsDeselected(String value, Class metaElemClass) {
//        assertHasOptionWithValue(value, getDecoratedElement(metaElemClass));
//        if (optionWithValueIsSelected(value, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with value " + quote(value.trim()) + " is not deselected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//
//
//    /* Option Index */
//    public static boolean hasOptionWithIndex(int index, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        try {
//            return options.get(index) != null;
//        } catch (IndexOutOfBoundsException e) {
//            return false;
//        }
//    }
//
//    public static boolean hasNotOptionWithIndex(int index, Class metaElemClass) {
//        return !hasOptionWithIndex(index, getDecoratedElement(metaElemClass));
//    }
//
//    public static boolean optionWithIndexIsEnabled(int index, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        try {
//            return isEnabled(options.get(index));
//        } catch (IndexOutOfBoundsException e) {
//            return false;
//        }
//    }
//
//    public static boolean optionWithIndexIsDisabled(int index, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        try {
//            return isDisabled(options.get(index));
//        } catch (IndexOutOfBoundsException e) {
//            return false;
//        }
//    }
//
//    public static boolean optionWithIndexIsSelected(int index, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        try {
//            return isSelected(options.get(index));
//        } catch (IndexOutOfBoundsException e) {
//            return false;
//        }
//    }
//
//    public static boolean optionWithIndexIsDeselected(int index, Class metaElemClass) {
//        List<getDecoratedElement(metaElemClass)> options = new Select(getDecoratedElement(metaElemClass)).getOptions();
//        try {
//            return isDeselected(options.get(index));
//        } catch (IndexOutOfBoundsException e) {
//            return false;
//        }
//    }
//
//    public static void assertHasOptionWithIndex(int index, Class metaElemClass) {
//        if (hasNotOptionWithIndex(index, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element  has no option with index " + quote(index), getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertHasNotOptionWithIndex(int index, Class metaElemClass) {
//        if (hasOptionWithIndex(index, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Element has option with index " + quote(index) + " when it shouldn't", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithIndexIsEnabled(int index, Class metaElemClass) {
//        assertHasOptionWithIndex(index, getDecoratedElement(metaElemClass));
//        if (optionWithIndexIsDisabled(index, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with index " + quote(index) + " is not enabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithIndexIsDisabled(int index, Class metaElemClass) {
//        assertHasOptionWithIndex(index, getDecoratedElement(metaElemClass));
//        if (optionWithIndexIsEnabled(index, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with index " + quote(index) + " is not disabled", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithIndexIsSelected(int index, Class metaElemClass) {
//        assertHasOptionWithIndex(index, getDecoratedElement(metaElemClass));
//        if (optionWithIndexIsDeselected(index, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with index " + quote(index) + " is not selected", getDecoratedElement(metaElemClass));
//        }
//    }
//
//    public static void assertOptionWithIndexIsDeselected(int index, Class metaElemClass) {
//        assertHasOptionWithIndex(index, getDecoratedElement(metaElemClass));
//        if (optionWithIndexIsSelected(index, getDecoratedElement(metaElemClass))) {
//            throw new WebAssertionError("Option with index " + quote(index) + " is not deselected", getDecoratedElement(metaElemClass));
//        }
//    }

}

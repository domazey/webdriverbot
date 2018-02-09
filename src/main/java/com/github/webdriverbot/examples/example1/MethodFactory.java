/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.examples.example1;

import com.github.webdriverbot.context.WebDriverBotContext;
import static com.github.webdriverextensions.Bot.*;
import com.github.webdriverextensions.WebDriverExtensionsContext;
import com.github.webdriverextensions.internal.junitrunner.DriverPathLoader;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class MethodFactory {

    private static final String parentOf_SCRIPT = "return arguments[0].parentNode;";

    private static final String generateXPath_SCRIPT = "function getXPath(element, generated) {"
            + "var childTag = element.tagName;"
            + "if (document.documentElement === element) {"
            + "    return '/' + document.documentElement.tagName + generated;"
            + "}"
            + "var parentElement = element.parentElement;"
            + "var childrenElements = parentElement.children;"
            + "var count = 0;"
            + "for (i = 0; i < childrenElements.length; i++) {"
            + "    var childElement = childrenElements[i];"
            + "    var childElementTag = childElement.tagName;"
            + "    if(childTag == childElementTag) {"
            + "        count += 1;"
            + "    }"
            + "    if(element == childElement) {"
            + "        return getXPath(parentElement, '/' + childTag + '[' + count + ']' + generated);"
            + "    }"
            + "}"
            + "return null;"
            + "}"
            + ""
            + "return getXPath(arguments[0], '');";

    public static WebElement parentOf(WebElement child) {
        return (WebElement) executeJavascript(parentOf_SCRIPT, child);
    }

    public static String innerHtmlOf(WebElement elem) {
        return elem.getAttribute("innerHTML");
    }

    public static boolean isDocumentRoot(WebElement elem) {
        try {
            return (boolean) executeJavascript("return arguments[0] == document.documentElement;", elem);
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isPageObject(WebElement elem) {
        try {
            return isPageObjectDefined() && parentOf(elem) == null;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String generateXPath(WebElement element) {
        Long start = System.nanoTime();
        if (isPageObject(element)) {
            return null;
        }
        String result = (String) executeJavascript(generateXPath_SCRIPT, element);
        Long end = System.nanoTime();
        Double duration = (end - start) / 1000000000.0;
        System.out.println("Created xpath in " + duration + " seconds");
        return result;
    }

    public static boolean isPageObjectDefined() {
        WebElement documentRoot = driver().findElement(By.xpath("/*"));
        return parentOf(documentRoot) != null;
    }

    public static boolean isChildOf(WebElement child, WebElement parent) {
        try {
            return parentOf(child).equals(parent);
        } catch (Exception ex) {
            return false;
        }
    }

    public static List<WebElement> childrenOf(WebElement element) {
        return element.findElements(By.xpath("./*"));
    }

    public static boolean isDescendantOf(WebElement element, WebElement descendand) {
        if (element == null) {
            return false;
        }
        if (descendand == null) {
            return false;
        }
        if (element == descendand) {
            return false;
        }
        if (element.equals(descendand)) {
            return false;
        }
        if (isPageObject(element)) {
            return true;
        }
        return generateXPath(descendand).startsWith(generateXPath(element));
    }

    public static boolean isSiblingOf(WebElement element, WebElement sibling) {
        if (element == null) {
            return false;
        }
        if (sibling == null) {
            return false;
        }
        if (element == sibling) {
            return false;
        }
        if (element.equals(sibling)) {
            return false;
        }
        return parentOf(element) != null
                && parentOf(sibling) != null
                && parentOf(element).equals(parentOf(sibling));
    }

    // internal
    private static String findLongestPrefixSuffix(String s1, String s2) {
        String result = "";

        for (int i = 0; i < s1.length() && i < s2.length(); ++i) {
            if (s1.charAt(i) != s2.charAt(i)) {
                break;
            }
            result += s1.charAt(i);
        }
        return result;
    }

    //TODO: sprawdzanie overlappu po tokenach, nie po znakach
    public static String relativeXPath(WebElement from, WebElement to) {
        Long start = System.nanoTime();
        String fromXPath = generateXPath(from);
        String toXPath = generateXPath(to);
        String overlap = findLongestPrefixSuffix(fromXPath, toXPath);
        int overlapLength = overlap.length();
        String truncatedFromXPath = fromXPath.substring(overlapLength, fromXPath.length());
        String truncatedToXPath = toXPath.substring(overlapLength, toXPath.length());
        String result = "./";
        for (int i = 0; i < StringUtils.countMatches(truncatedFromXPath, "/")
                + /* same line */ (truncatedToXPath.length() > 0 ? 1 : 0); ++i) {
            result += "../";
        }
        result += truncatedToXPath;
        Long end = System.nanoTime();
        Double duration = (end - start) / 1000000000.0;
        System.out.println("Calculated relative xpath in " + duration + " seconds");
        return result;

    }

    public static void main(String[] args) {

        init();
        try {
            run();
        } finally {
            driver().quit();
        }

    }

    private static void run() {
        open("http://jkorpela.fi/www/testel.html");
        WebElement elem = driver().findElement(By.xpath("/html/body/ul[4]"));
        WebElement from = driver().findElement(By.xpath("/html/body/ul[4]/li[2]"));
        WebElement to = driver().findElement(By.xpath("/html/body/div[2]/a"));
        System.out.println("Relative xpath: " + relativeXPath(from, to));
        System.out.println("Is page object defined: " + isPageObjectDefined());
        System.out.println("child:");
        System.out.println("is Root: " + isDocumentRoot(elem));
        System.out.println("is Page Object: " + isPageObject(elem));
        System.out.println("xpath of: " + generateXPath(elem));
        System.out.println("Is parent of itself: " + isChildOf(elem, elem));
        List<WebElement> rootChildren = childrenOf(elem);
        System.out.println("These are siblings: " + isSiblingOf(rootChildren.get(0), rootChildren.get(1)));
        System.out.println("Body is descendand of html: "
                + isDescendantOf(elem, rootChildren.get(1)));
        System.out.println("Head is descendand of html: "
                + isDescendantOf(elem, rootChildren.get(0)));
        System.out.println("Html is descendand of body: "
                + isDescendantOf(rootChildren.get(1), elem));
        System.out.println("Html is descendand of head: "
                + isDescendantOf(rootChildren.get(0), elem));

        WebElement parent = parentOf(elem);

        System.out.println("parent:");
        System.out.println("is Root: " + isDocumentRoot(parent));
        System.out.println("is Page Object: " + isPageObject(parent));
        System.out.println("xpath of: " + generateXPath(parent));
        System.out.println("Is page object parent of document root: " + isChildOf(elem, parent));

    }

    private static void init() {
        DriverPathLoader.loadDriverPaths(null); // use default fields
        WebDriver driver = new ChromeDriver(); //random driver
        WebDriverExtensionsContext.setDriver(driver);
        driver().manage().window().maximize();
        WebDriverBotContext.initBotData();
    }
}

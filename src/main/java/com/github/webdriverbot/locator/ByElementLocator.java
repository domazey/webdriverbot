/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.locator;

import com.github.webdriverextensions.annotations.ResetSearchContext;
import com.github.webdriverextensions.internal.WebDriverExtensionAnnotations;
import java.lang.reflect.Field;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

public class ByElementLocator implements ElementLocator {

    private final SearchContext searchContext;
    private final By by;

    public ByElementLocator(SearchContext searchContext, By by) {
        this.searchContext = searchContext;
        this.by = by;
        
    }

    @Override
    public WebElement findElement() {
        return searchContext.findElement(by);
    }

    @Override
    public List<WebElement> findElements() {
        return  searchContext.findElements(by);
    }

}

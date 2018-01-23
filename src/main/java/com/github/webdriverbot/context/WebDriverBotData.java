package com.github.webdriverbot.context;

import com.github.webdriverbot.pagefactory.BotPage;
import java.util.LinkedHashMap;
import java.util.Map;
import static com.github.webdriverbot.bot.BotBot.*;
import com.github.webdriverbot.exceptions.WebDriverBotException;

public class WebDriverBotData {

    private final Map<String, BotPage> mappedHandlesPages;
    private final Map<String, Object> propertyMap;

    public WebDriverBotData() {
        mappedHandlesPages = new LinkedHashMap<>();
        propertyMap = new LinkedHashMap<>();
    }

    public void setPageForHandle(String handle, Class<? extends BotPage> botPageClass) {

        if (!availableWindowHandles().contains(handle)) {
            throw new WebDriverBotException("There is no \'" + handle + "\' handle. You can't map BotPage to page that doesn't yet exist");
        }

        try {
            mappedHandlesPages.put(handle, botPageClass.newInstance());
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new WebDriverBotException("Class " + botPageClass + " doesn't provide public no-arg constructor", ex);
        }

    }

    public BotPage getPageForHandle(String handle) {

        if (!mappedHandlesPages.containsKey(handle)) {
            throw new WebDriverBotException("There is no " + handle + " handle registered in WebDriverBotData in thread " + Thread.currentThread().getName());
        }

        if (mappedHandlesPages.get(handle) == null) {
            throw new WebDriverBotException("BotPage for " + handle + " handle is not set in thread " + Thread.currentThread().getName() + ". Call setPageForHandle to set it");
        }

        return mappedHandlesPages.get(handle);
    }

    public BotPage getUniquePage(Class<? extends BotPage> pageClass) {
        int pageCount = 0;
        BotPage result = null;
        for (BotPage page : mappedHandlesPages.values()) {
            if (page.getClass().equals(pageClass)) {
                ++pageCount;
                result = page;
            }
        }

        if (result == null) {
            throw new WebDriverBotException("There is no " + pageClass.getSimpleName() + " page mapped in WebDriverBotContext in thread " + Thread.currentThread().getName());
        }

        if (pageCount > 1) {
            throw new WebDriverBotException("There are " + pageCount + " mapped pages of type " + pageClass.getSimpleName() + " in thread " + Thread.currentThread().getName() + ". Method getUniquePage requires that there is only one page of given type. Please specify window handle if there are multiple pages of same type");
        }

        return result;
    }

    public void setProperty(String key, Object value) {
        propertyMap.put(key, value);
    }

    public Object getProperty(String key) {
        if(!propertyMap.keySet().contains(key)) {
            throw new WebDriverBotException("failed to get property [" + key + "] from WebDriverBotContext. Property doesn't exist");
        }
        return propertyMap.get(key);
    }

    public <U> Object getProperty(String key, Class<U> propertyClass) {   
        return (U) getProperty(key);
    }
    
    public void removeProperty(String key) {
        if(!propertyMap.keySet().contains(key)) {
            throw new WebDriverBotException("failed to remove property [" + key + "] from WebDriverBotContext. Property doesn't exist");
        }
        propertyMap.remove(key);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.webdriverbot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In the Url, you can use additional following syntax:
 * {name} - reads the property you have set with WebDriverBotContext.setProperty(...)
 * [name] - reads value from @UrlParam of subclass (or same class, but using parameter would be pointless)
 * {{name}} - same as single curly bracket version, but interprets as GET parameter, f.e. generates name=YourName
 * [[name]] - same as above, but uses @UrlParam instead of property
 * @author xinaiz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ParametrizedUrl {

    String value();
}

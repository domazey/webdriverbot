package com.github.webdriverbot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Action {
    // mandatory field
    public ActionEnum action();

    // if action uses locator, use use of these
    public FindBy findBy() default @FindBy();
    public FindAll findAll() default @FindAll({});
    public FindBys findBys() default @FindBys({});
    
    // use these in other cases, like navigating etc
    public String[] args() default {};
    public String arg() default "";
}

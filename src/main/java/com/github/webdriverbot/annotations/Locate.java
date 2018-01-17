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
public @interface Locate {
    FindBy findBy() default @FindBy();
    FindBys findBys() default @FindBys({});
    FindAll findAll() default @FindAll({});
    String xpath() default "";
    String className() default "";
    String css() default "";
    String id() default "";
    String name() default "";
    String linkText() default "";
    String partialLinkText() default "";
    String tagNmae() default "";
    Class value() default void.class;
}

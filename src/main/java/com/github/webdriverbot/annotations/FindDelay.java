package com.github.webdriverbot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FindDelay {
    public double value() default 0;
    public TimeUnit unit() default TimeUnit.MILLISECONDS;
}

package com.github.webdriverbot.annotations;

import com.github.webdriverbot.BotPage;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RedirectTo {
    Class<? extends BotPage> value();
}
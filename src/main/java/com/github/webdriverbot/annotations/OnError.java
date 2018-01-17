package com.github.webdriverbot.annotations;

import com.github.webdriverbot.ActionEnum;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OnError {
    //use this
    public Action[] value() default {};
    
    //or this
    public Class handlerClass() default void.class;
    public String[] handlerArgs() default {};
    
    //or this
    public Action action() default @Action(action = ActionEnum.NONE);
}


package com.github.webdriverbot.annotations;

public @interface Execute {

    Locate click() default @Locate();

    Locate doubleClick() default @Locate();

    String type() default "";

    Locate clear() default @Locate();

    String clearAndType() default "";

    Locate pressEnter() default @Locate();

    String pressKeys() default "";

    Locate select() default @Locate();

    Locate deselect() default @Locate();

    String selectOption() default "";

    String deselectOption() default "";

    Locate selectAllOptions() default @Locate();

    Locate deselectAllOptions() default @Locate();

    String selectOptionWithValue() default "";

    String deselectOptionWithValue() default "";

    int selectOptionWithIndex() default -1;

    int deselectOptionWithIndex() default -1;

    Locate check() default @Locate();

    Locate uncheck() default @Locate();

    String open() default "";

    Class openPage() default void.class;

    ActionEnum action() default ActionEnum.NONE; // use for navigations only

    Locate waitFoeElementToDisplay() default @Locate();

    /* waitUntil not implemented */
    Locate scrollTo() default @Locate();

    String executeJavascript() default "";

    String executeJavascriptAsynchronously() default "";

    String takeScreenshot() default "";

    String debug() default "";

    Locate debugElement() default @Locate();

    Locate debugElements() default @Locate();

    Class assertIsOpen() default void.class;

    Class assertIsNotOpen() default void.class;

    Locate assertIsDisplayed() default @Locate();

    Locate assertIsNotDisplayed() default @Locate();

    /* Url */
    String assertCurrentUrlEquals() default "";

    String assertCurrentUrlNotEquals() default "";

    String assertCurrentUrlContains() default "";

    String assertCurrentUrlNotContains() default "";

    String assertCurrentUrlStartsWith() default "";

    String assertCurrentUrlNotStartsWith() default "";

    String assertCurrentUrlEndsWith() default "";

    String assertCurrentUrlNotEndsWith() default "";

    String assertCurrentUrlMatches() default "";

    String assertCurrentUrlNotMatches() default "";

    /* Title */
    String assertTitleEquals() default "";

    String assertTitleNotEquals() default "";

    String assertTitleContains() default "";

    String assertTitleNotContains() default "";

    String assertTitleStartsWith() default "";

    String assertTitleNotStartsWith() default "";

    String assertTitleEndsWith() default "";

    String assertTitleNotEndsWith() default "";

    String assertTitleMatches() default "";

    String assertTitleNotMatches() default "";

    /* Tag name */
    String assertTagNameEquals() default "";

    String assertTagNameNotEquals() default "";

    /* Attribute */
    String assertHasAttribute() default "";

    String assertHasNotAttribute() default "";

    String assertAttributeEquals() default "";

    String assertAttributeNotEquals() default "";

    String assertAttributeContains() default "";

    String assertAttributeNotContains() default "";

    String assertAttributeStartsWith() default "";

    String assertAttributeNotStartsWith() default "";

    String assertAttributeEndsWith() default "";

    String assertAttributeNotEndsWith() default "";

    String assertAttributeMatches() default "";

    String assertAttributeNotMatches() default "";

    String assertAttributeIsNumber() default "";

    String assertAttributeIsNotNumber() default "";

    /* Id */
    String assertHasId() default "";

    String assertHasNotId() default "";

    String assertIdEquals() default "";

    String assertIdNotEquals() default "";

    String assertIdContains() default "";

    String assertIdNotContains() default "";

    String assertIdStartsWith() default "";

    String assertIdNotStartsWith() default "";

    String assertIdEndsWith() default "";

    String assertIdNotEndsWith() default "";

    String assertIdMatches() default "";

    String assertIdNotMatches() default "";

    /* Name */
    String assertHasName() default "";

    String assertHasNotName() default "";

    String assertNameEquals() default "";

    String assertNameNotEquals() default "";

    String assertNameContains() default "";

    String assertNameNotContains() default "";

    String assertNameStartsWith() default "";

    String assertNameNotStartsWith() default "";

    String assertNameEndsWith() default "";

    String assertNameNotEndsWith() default "";

    String assertNameMatches() default "";

    String assertNameNotMatches() default "";

    /* Class */
    String assertHasClass() default "";

    String assertHasNotClass() default "";

    String assertClassEquals() default "";

    String assertClassNotEquals() default "";

    String assertClassContains() default "";

    String assertClassNotContains() default "";

    String assertClassStartsWith() default "";

    String assertClassNotStartsWith() default "";

    String assertClassEndsWith() default "";

    String assertClassNotEndsWith() default "";

    String assertClassMatches() default "";

    String assertClassNotMatches() default "";

    /* Value */
    String assertHasValue() default "";

    String assertHasNotValue() default "";

    String assertValueEquals() default "";

    String assertValueNotEquals() default "";

    String assertValueContains() default "";

    String assertValueNotContains() default "";

    String assertValueStartsWith() default "";

    String assertValueNotStartsWith() default "";

    String assertValueEndsWith() default "";

    String assertValueNotEndsWith() default "";

    String assertValueMatches() default "";

    String assertValueNotMatches() default "";

    String assertHasHref() default "";

    String assertHasNotHref() default "";

    String assertHrefEquals() default "";

    String assertHrefNotEquals() default "";

    String assertHrefContains() default "";

    String assertHrefNotContains() default "";

    String assertHrefStartsWith() default "";

    String assertHrefNotStartsWith() default "";

    String assertHrefEndsWith() default "";

    String assertHrefNotEndsWith() default "";

    String assertHrefMatches() default "";

    String assertHrefNotMatches() default "";

    String assertHasText() default "";

    String assertHasNotText() default "";

    String assertTextEquals() default "";

    String assertTextNotEquals() default "";

    String assertTextContains() default "";

    String assertTextNotContains() default "";

    String assertTextStartsWith() default "";

    String assertTextNotStartsWith() default "";

    String assertTextEndsWith() default "";

    String assertTextNotEndsWith() default "";

    String assertTextMatches() default "";

    String assertTextNotMatches() default "";

    String assertTextEqualsIgnoreCase() default "";

    String assertTextNotEqualsIgnoreCase() default "";

    String assertTextContainsIgnoreCase() default "";

    /* Selection */
    Locate assertIsSelected() default @Locate();
    
    Locate assertIsDeselected()default @Locate();
    
    /* Check */
    
    Locate assertIsChecked() default @Locate();

    Locate assertIsUnchecked() default @Locate();
    
    /* Enabled */
    
    String assertHasOption() default "";
    
    String assertHasNotOption() default "";
    
    String assertOptionIsEnabled() default "";
    
    String assertOptionIsDisabled() default "";
    
    String assertOptionIsSelected() default "";
    
    String assertOptionIsDeselected() default "";
    
    String assertAllOptionsAreSelected() default "";
    
    String assertNoOptionsIsSelected() default "";
    
    String assertHasOptionWithValue() default "";
    
    String assertHasNotOptionWithValue() default "";
    
    String assertOptionWithValueIsEnabled() default "";
    
    String assertOptionWithValueIsDisabled() default "";
    
    String assertOptionWithValueIsSelected() default "";
    
    String assertOptionWtihValueIsDeselected() default "";
    
    int assertHasOptionWithIndex() default -1;
    
    int assertHasNotOptionWithIndex() default -1;
    
    int assertOptionWithIndexIsEnabled() default -1;
    
    int assertOptionWithIndexIsDisabled() default -1;
    
    int assertOptionWithIndexIsSelected() default -1;
    
    int assertOptionWithIndexIsDeselected() default -1;
    
    Locate target() default @Locate();

    String arg() default "";

    String[] args() default {};

}

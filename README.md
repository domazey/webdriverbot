# webdriverbot
Extension for WebDriver project

### TODO:
- css property functions (see https://www.w3schools.com/cssref/default.asp and http://www.seleniumeasy.com/selenium-tutorials/webdriver-event-listener-example) using `getCssValue()`
- js property getters and executors
- attribute getters (and setters)
- 
- change takeScreenshotOf to use already implemented mechanism
- mechanism that will allow to modify DOM
- create advanced and very advanced action functions
- delegate implicit-context functions to use explicit context functions
- getRect() replacement (ChromeDriver throws exception)
- window related function (f.e. scrollWidth (window.width - innerWindow.width)
- additional By classes based on
     - css values
     - js values
     - screenshots (o_O)
     - position and relation to other elements
     
Ideas (Api based): 
- inputTextFromElement(Element target, Element source)

Ideas (JS based):
- getInnerHtml(WebElement elem) // gets inner html
- getOuterHtml(WebElement elem) // gets outer html
- setInnerHtml(WebElement elem, String html) // sets html of element
- setInnerHtml(WebElement target, WebElement source) // sets of based on other element
- setInnerHtml(WebElement target, WebElement source, Function<String, String> htmlModifier)
// getAttribute is already defined
- setAttribute(WebElement elem, String attribute, String value)
- copyAttribute(WebElement target, WebElement source, String attribute)
- copyAttribute(WebElement target, WebElement source, String attribute, Function<String, String> attributeModifier)
- copyAttributes(WebElement target, WebElement source)
- getStyleProperty(WebElement elem, String property)
- setStyleProperty(WebElement elem, String property, String value)
- copyStyleProperty(WebElement target, WebElement source, String property)
- copyStyleProperty(WebElement target, WebElement source, String property, Function<String, String> propertyModifier)
- deleteElement(WebElement element)
- createElement(...)
- appendChild(WebElement root, ...)
- removeChild(WebElement root, ...)
- replaceChild(WebElement root, ...)
- replaceElement(WebElement target, WebElement source)
- replaceElement(WebElement target, String htmlSource)
- write(String text) //writes to html output stream
... 
all Finding HTML Objects from https://www.w3schools.com/js/js_htmldom_document.asp
all listeners (onclick) for definining on dom, and adapters for java
more element functions from: https://www.w3schools.com/jsref/dom_obj_all.asp
events are from here: https://www.w3schools.com/jsref/dom_obj_event.asp
style object functions: https://www.w3schools.com/jsref/dom_obj_style.asp
more here: https://www.w3schools.com/jsref/
...
referrer
- findElementByPosition(int x, int y)
- findElementByStyle(...)
- findElementByText(String text)
- elementLeftOf(WebElement element)
- elementTopOf(WebElement elemenet)
- elementRightOf(WebElement element)
- elementBottomOf(WebElement eleement)
+ maybe some constraints and additional arguments
more location based elements
...
useful: http://javascript.info/coordinates


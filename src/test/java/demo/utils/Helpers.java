package demo.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {
    private static final ThreadLocal<WebDriver> ACTIVE_DRIVER = new ThreadLocal<>();
    private final WebDriver driver;
    private final JavascriptExecutor js;
    public Helpers(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        ACTIVE_DRIVER.set(driver);
    }

    public static void logCommand(String action, String value) {
        String message = action + ": " + value;
        System.out.println(message);
        WebDriver driver = ACTIVE_DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            ((JavascriptExecutor) driver).executeScript("console.log(arguments[0]);", message);
        } catch (TimeoutException ignored) {
        }
    }

    public void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    public void scrollToElement(By locator) {
        WebElement element = driver.findElement(locator);
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }


    public void scrollDown() {
        js.executeScript("window.scrollBy(0, window.innerHeight);");
    }

    public void scrollDown(int pixels) {
        if (pixels <= 0 || pixels > 10000) {
            logCommand("Scroll Down", "Invalid pixel value: " + pixels);
            scrollDown();
        } else {
            logCommand("Scroll Down", "Scrolling down by " + pixels + " pixels");
            js.executeScript("window.scrollBy(0, arguments[0]);", pixels);
        }
    }

    public void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");   
    }

    public void scrollTop() {
        js.executeScript("window.scrollBy(0, window.innerHeight);");
    }

    public void scrollTop(int pixels) {
        if (pixels <= 0 || pixels > 10000) {
            logCommand("Scroll Top", "Invalid pixel value: " + pixels);
            scrollTop();
        } else {
            logCommand("Scroll Top", "Scrolling up by " + pixels + " pixels");
            js.executeScript("window.scrollBy(0, -arguments[0]);", pixels);
        }
    }

    public void scrollToTop() {
        js.executeScript("window.scrollTo(0, 0);");   
    }
}

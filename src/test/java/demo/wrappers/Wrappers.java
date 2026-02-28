package demo.wrappers;

import demo.utils.*;
import static demo.utils.Helpers.logCommand;

import org.apache.poi.ss.formula.functions.T;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Wrappers {
    private final WebDriver driver;
    private final FluentWait<WebDriver> wait;
    private final JavascriptExecutor js;
    public final Helpers helpers;
    private String filmCardlocator, musicCardLocator, newsCardLocator;

    public Wrappers(WebDriver driver) {
        this.driver = driver;
        this.wait = new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(25))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(StaleElementReferenceException.class);
        this.js = (JavascriptExecutor) driver;
        helpers = new Helpers(driver);
    }

    public void navigate(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL must be provided.");
        }
        logCommand("Navigate", url);
        driver.get(url);
    }

    public void search(String text) {
        String locator = "//input[@name='search_query' or contains(@placeholder,'Search') and not(@readonly)]";
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));

        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        if (input.getAttribute("value") != null && !input.getAttribute("value").isBlank()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value='';", input);
        }

        input.sendKeys(text+Keys.ENTER);
        waitForResults();
    }

    public void waitForResults() {
        String locator = ".//div[@id='content'][1]/div[position() > 1 and position() < last()]";
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//body")));
        List<WebElement> rows = wait.until(ExpectedConditions.refreshed(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(locator))
        ));
        if (rows.isEmpty()) {
            throw new RuntimeException("No search results found");
        }
    }

    public Boolean filter(String filter) {
        if(filter == null || filter.isBlank()) {
            logCommand("filter", "Empty filter");
            return false;
        }
        return filter(filter, null, null);
    }

    public Boolean filter(Integer position) {
        if(position == null || position < 0) {
            logCommand("filter", "Invalid position");
            return false;
        }
        return filter(null, position, null);
    }

    public Boolean filter(Map<String, String> filterMap) {
        if(filterMap == null || filterMap.isEmpty()) {
            logCommand("filter", "Empty filter map");
            return false;
        }
        return filter(null, null, filterMap);
    }
    
    private Boolean filter(String filter, Integer position, Map<String, String> filterMap) {
        Boolean applied = false;

        logCommand("filter", filter);
        // TODO: Implement filter logic based on the provided parameters (filter, position, filterMap)

        return applied;
    }

    // Wrapper method for navigating to sidebar tabs
    public Boolean navigateSidebar(String tab) {
        if(tab == null || tab.isBlank()) {
            logCommand("navigateSidebar", "Empty tab");
            return false;
        }
        logCommand("Navigate Sidebar", tab);
        String titleLocator = "//ytd-guide-entry-renderer//a[@title='%s']";
        String textOrAriaLocator =
            "//ytd-guide-entry-renderer//a[normalize-space()='%1$s' or .//*[normalize-space()='%1$s']]";
        String guideLocator = "//div[contains(@id,'guide-links')]/a[contains(@text(),'%s') or normalize-space()='%1$s' or contains(@href,'%s')]";

        String locators = 
            String.format(titleLocator, tab) + "|" +
            String.format(textOrAriaLocator, tab) + "|" +
            String.format(guideLocator, tab, tab.toLowerCase(Locale.ROOT));
        String showMoreLocator =
            String.format(textOrAriaLocator, "Show more") + "|" +
            String.format(guideLocator, "Show more", "show more".toLowerCase(Locale.ROOT));

        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(showMoreLocator))).click();
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(locators)));
            helpers.scrollToElement(element);
            element.click();
            return true;
        } catch (TimeoutException ignored) {
           logCommand("navigateSidebar", String.format("'Show more' button or '%s' not found.", tab));
        }
        return false;
    }

    // Wrapper method for printing page content like headings and paragraphs
    public Boolean printPageContent() {
        String locator = "//h1 | //h2[contains(@class,'%s')] | //h3 | //p";
        List<WebElement> elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(locator)));
        if (elements.isEmpty()) {
            logCommand("Page Content", "No content found");
            return false;
        }
        logCommand("Page Content","");
        for (WebElement element : elements) {
            helpers.scrollToElement(element);
            logCommand("", element.getText());
        }
        return true;
    }

    // Wrapper method for validating locator of Film Card
    private String checkFilm(String section, String position) {
        waitForResults();
        if (filmCardlocator != null && !filmCardlocator.isBlank()) {
            return filmCardlocator;
        }

        String sectionLocator = "//ytd-item-section-renderer%s";
        String filmLocator = "//ytd-grid-movie-renderer[position()=%s]";
        

        if (section != null && !section.isBlank()) {
            sectionLocator = String.format(sectionLocator, String.format("[.//span[contains(text(),'%s')]]", section));
        } else {
            sectionLocator = String.format("("+sectionLocator+"[contains(@class,'ytd-section-list-renderer')])[2]","");
        }
        WebElement rightScrollButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("("+sectionLocator+"//button)[3]")));
        helpers.scrollToElement(rightScrollButton);
        if (position != null && !position.isBlank()) {
            if (position.matches("last") || position.matches("last()")) {
                rightScrollButton.click();
                rightScrollButton.click();
                filmLocator = sectionLocator + String.format(filmLocator, "last()");
            } else if (12<Integer.parseInt(position) && Integer.parseInt(position)<=16) {
                rightScrollButton.click();
                rightScrollButton.click();
                filmLocator = sectionLocator + String.format(filmLocator, position);
            } else if (6<Integer.parseInt(position) && Integer.parseInt(position)<=12) {
                rightScrollButton.click();
                filmLocator = sectionLocator + String.format(filmLocator, position);
            } else if (1<=Integer.parseInt(position) && Integer.parseInt(position)<=6) {
                filmLocator = sectionLocator + String.format(filmLocator, position);
            } else if (position.matches("first") || position.matches("first()")) {
                filmLocator = sectionLocator + String.format(filmLocator, "1");
            } else {
                logCommand("checkFilm", "Invalid position: " + position);
                filmLocator = sectionLocator + String.format(filmLocator, "1");
            }
        } else {
            filmLocator = sectionLocator + String.format(filmLocator, "1");
        }
        filmCardlocator = filmLocator;
        return filmLocator;
    }

    // Wrapper method for validating Film Certificate
    public Boolean checkFilmCertificate(String section, String position) {
        String filmLocator = checkFilm(section, position);
        try {
            WebElement certificateElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(" + filmLocator + "//badge-shape)[3]/div")));
            helpers.scrollToElement(certificateElement);
            String certificate = certificateElement.getText();
            Set<String> validCertificates = Set.of("A", "U/A", "U", "PG", "R", "NC-17");
            if (certificate == null || certificate.isBlank()) {
                logCommand("Film Certificate", "Certificate not found");
                return false;
            } else if (validCertificates.stream().anyMatch(c -> c.equalsIgnoreCase(certificate))) {
                logCommand("Film Certificate", "Certificate found : " + certificate);
                return true;
            } else {
                logCommand("Film Certificate", "Invalid certificate found: " + certificate);
                return false;
            }
        } catch (TimeoutException e) {
            logCommand("Film Certificate", "Certificate element not found");
            return false;
        }
    }

    // Wrapper method for validating Film Category
    public Boolean checkFilmCategory(String section, String position) {
        String filmLocator = checkFilm(section, position);
        try {
            WebElement categoryElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath(filmLocator)));
            List<String> lines = (List<String>) js.executeScript(
                "return arguments[0].innerText.split('\\n').map(t => t.trim()).filter(t => t.length > 0);",
                categoryElement
            );
            helpers.scrollToElement(categoryElement);
            String category = lines.get(5).split(" ")[0].trim();

            Set<String> validCategories = Set.of("Action and adventure", "Comedy", "Crime", "Drama", "Horror", "Indian cinema", "Romance", "Science fiction", "Thriller", "Animation", "Documentary", "Family", "Fantasy", "Music", "War");
            if (category == null || category.isBlank()) {
                logCommand("Film Category", "Category not found");
                return false;
            } else if (validCategories.stream().anyMatch(c -> c.equalsIgnoreCase(category))) {
                logCommand("Film Category", "Category found : " + category);
                return true;
            } else {
                logCommand("Film Category", "Invalid category found: " + category);
                return false;
            }
        } catch (TimeoutException e) {
            logCommand("Film Category", "Category element not found");
            return false;
        }
    }

    // Wrapper method for validating locator of Music Playlist Card
    private String checkMusic(String section, String position) throws InterruptedException {
        waitForResults();
        if (musicCardLocator != null && !musicCardLocator.isBlank()) {
            return musicCardLocator;
        }

        String sectionLocator = "//ytd-rich-shelf-renderer%s";
        String musicLocator = "//ytd-rich-item-renderer[position()=%s]";
        

        if (section != null && !section.isBlank()) {
            if (section.matches("\\d+")) {
                Integer n = Integer.parseInt(section);
                if (0 < n && n <= 12) {
                    sectionLocator = String.format(sectionLocator, String.format("[%s]", n));
                } else {
                    logCommand("checkMusic", "Invalid section number: " + section+". Defaulting to first section.");
                    sectionLocator = String.format(sectionLocator, "[1]");
                }
            } else {
                sectionLocator = String.format(sectionLocator, String.format("[.//span[contains(text(),'%s')]]", section));
            }
        } else {
            sectionLocator = String.format("("+sectionLocator+")[1]","");
        }
        WebElement showMoreButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("("+sectionLocator+"//button)[1]")));
        helpers.scrollToElement(showMoreButton);
        if (position != null && !position.isBlank()) {
            if (position.matches("last") || position.matches("last()")) {
                showMoreButton.click();
                musicCardLocator = sectionLocator + String.format(musicLocator, "last()");
            } else if (4<Integer.parseInt(position) && Integer.parseInt(position)<=12) {
                showMoreButton.click();
                musicCardLocator = sectionLocator + String.format(musicLocator, position);
            } else if (1<=Integer.parseInt(position) && Integer.parseInt(position)<=4) {
                musicCardLocator = sectionLocator + String.format(musicLocator, position);
            } else if (position.matches("first") || position.matches("first()")) {
                musicCardLocator = sectionLocator + String.format(musicLocator, "1");
            } else {
                logCommand("checkMusic", "Invalid position: " + position);
                musicCardLocator = sectionLocator + String.format(musicLocator, "1");
            }
        } else {
            musicCardLocator = sectionLocator + String.format(musicLocator, "1");
        }
        return musicCardLocator;
    }

    // Wrapper method for validating number of tracks in Music Playlist
    public Integer checkMusicTrackCount(String section, String position) throws InterruptedException {
        String musicLocator = checkMusic(section, position);
        try {
            WebElement trackCountElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("("+musicLocator+"//a//yt-thumbnail-overlay-badge-view-model//div)[3]")));
            helpers.scrollToElement(trackCountElement);
            String trackCountText = trackCountElement.getText();
            int trackCount = Integer.parseInt(trackCountText.split(" ")[0].trim());
            if (trackCount > 0) {
                logCommand("Music Track Count", "Track count found : " + trackCount);
                return trackCount;
            } else {
                logCommand("Music Track Count", "No tracks found");
                return 0;
            }
        } catch (TimeoutException e) {
            logCommand("Music Track Count Exception", musicLocator);
            return 0;
        }
    }

    // Wrapper method for validating locator of News Card
    private String checkNews(String section, String position) throws InterruptedException {
        waitForResults();
        String sectionLocator = "//ytd-rich-shelf-renderer%s";
        String newsLocator = "//ytd-rich-item-renderer[position()=%s]";

        if (section != null && !section.isBlank()) {
            if (section.matches("\\d+")) {
                Integer n = Integer.parseInt(section);
                if (0 < n && n <= 12) {
                    sectionLocator = String.format(sectionLocator, String.format("[%s]", n));
                } else {
                    logCommand("checkNews", "Invalid section number: " + section+". Defaulting to first section.");
                    sectionLocator = String.format(sectionLocator, "[1]");
                }
            } else {
                sectionLocator = String.format(sectionLocator, String.format("[.//span[contains(text(),'%s')]]", section));
            }
        } else {
            sectionLocator = String.format("("+sectionLocator+")[1]","");
        }
        String showMoreLocator = String.format("("+sectionLocator+"//button)[contains(text(),'%s') or contains(@aria-label,'%s')]", "Show more", "Show more");
        WebElement showMoreButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(showMoreLocator)));
        if (position != null && !position.isBlank()) {
            if (position.matches("last") || position.matches("last()")) {
                showMoreButton.click();
                newsCardLocator = sectionLocator + String.format(newsLocator, "last()");
            } else if (4<=Integer.parseInt(position) && Integer.parseInt(position)<=12) {
                showMoreButton.click();
                newsCardLocator = sectionLocator + String.format(newsLocator, position);
            } else if (1<=Integer.parseInt(position) && Integer.parseInt(position)<4) {
                newsCardLocator = sectionLocator + String.format(newsLocator, position);
            } else if (position.matches("first") || position.matches("first()")) {
                newsCardLocator = sectionLocator + String.format(newsLocator, "1");
            } else {
                logCommand("checkNews", "Invalid position: " + position);
                newsCardLocator = sectionLocator + String.format(newsLocator, "1");
            }
        } else {
            newsCardLocator = sectionLocator + String.format(newsLocator, "1");
        }
        return newsCardLocator;
    }

    // Wrapper method for getting details of News Card
    public Map<String, String> getNewsDetails(String section, String position) throws InterruptedException {
        String newsLocator = checkNews(section, position);
        if (newsLocator == null || newsLocator.isBlank()) {
            logCommand("News Details", "News locator not found");
            return getNewsPost(section, position);
        }
        try {
            WebElement newsCardElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(newsLocator)));
            helpers.scrollToElement(newsCardElement);
            List<String> lines = (List<String>) js.executeScript(
                "return arguments[0].innerText.split('\\n').map(t => t.trim()).filter(t => t.length > 0);",
                newsCardElement
            );
            String title = lines.get(0).trim();
            String body = lines.size() > 1 ? lines.get(1).trim() : "";
            logCommand("News Details", "Title: " + title + " | Body: " + body);
            Map<String, String> details = new LinkedHashMap<>();
            details.put("title", title);
            details.put("body", body);
            return details;
        } catch (TimeoutException e) {
            logCommand("News Details Exception", newsLocator);
            return Map.of();
        }
    }

    // Wrapper method for getting details of News Post along with likes and comments count
    public Map<String, String> getNewsPost(String section, String position) throws InterruptedException {
        String newsLocator = checkNews(section, position);
        try {
            WebElement newsCardElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath(newsLocator)));
            helpers.scrollToElement(By.xpath(newsLocator));
            List<String> lines = (List<String>) js.executeScript(
                "return arguments[0].innerText.split('\\n').map(t => t.trim()).filter(t => t.length > 0);",
                newsCardElement
            );
            String line = lines.stream().collect(Collectors.joining(" | "));
            String[] parts = line.split("\\|");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
            if (parts.length < 1) {
                logCommand("News Post Details Warning", "Unexpected news post format: \n\t" + line);
            }
            Map<String, String> details = new LinkedHashMap<>();
            details.put("channel", parts.length > 0 ? parts[0] : "");
            details.put("posted", parts.length > 2 ? parts[2] : "");
            details.put("title", parts.length > 3 ? parts[3] : "");
            details.put("likes", parts.length > 4 ? parts[4] : "0");
            details.put("comments", parts.length > 5 ? parts[5] : "0");
            return details;
        } catch (TimeoutException e) {
            return Map.of();
        }
    }

    // Wrapper method for validating search result details like title, views and posted time
    public Map<String, String> searchResult(String keyword, String position) throws InterruptedException {
        Map<String, String> results = new LinkedHashMap<>();
        String locator = String.format("(//ytd-video-renderer)[%s]", position);
        try {
            Thread.sleep(1000);
            String script = "return document.querySelectorAll('ytd-video-renderer')[" + Integer.parseInt(position) + "];";
            WebElement searchResult = (WebElement) js.executeScript( 
                script
            );
            
            helpers.scrollToElement(searchResult);
            
            List<String> lines = (List<String>) js.executeScript(
                "return arguments[0].innerText.split('\\n').map(t => t.trim()).filter(t => t.length > 0);",
                searchResult
            );
            String line = lines.stream().collect(Collectors.joining(" | "));
            String[] parts = line.split("\\|");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
            Map<String, String> details = new LinkedHashMap<>();
            for (String part : parts) {
                String p = part.trim();

                if (p.matches("(?i).*views$")) {
                    details.put("views", part.trim());
                } else if (p.matches("(?i)\\d+\\s+(years?|months?|days?|hours?|minutes?|mins?)\\s+ago")) {
                    details.put("posted", part.trim());
                } else {
                    details.computeIfAbsent("title", k -> "").concat(part.trim());
                }
            }

            results.putAll(details);
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("Cannot read properties of null (reading 'innerText')") 
                || e.getMessage().contains("Cannot read properties of null (reading 'scrollIntoView')"))) {
                logCommand("Search Result Warning", "No search result found at position: " + position);
                helpers.scrollDown(100);
                Thread.sleep(1000);
                return searchResult(keyword, position);
            } else {
                logCommand("Search Result Exception", "Error retrieving search result at position: " + position + "\nException: " + e.getMessage());
                return Map.of();
            }
        }
        return results;
    }
}

package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.Assert;
import org.testng.Reporter;

import java.beans.Transient;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import demo.utils.ExcelDataProvider;
import demo.utils.Helpers;
import demo.wrappers.Wrappers;
import static demo.utils.Helpers.logCommand;

public class TestCases extends ExcelDataProvider{
    ChromeDriver driver;
    Wrappers wrappers;
    Helpers h;
    SoftAssert softAssert;

    @BeforeTest
    public void startBrowser() {
        System.out.println("Starting browser for test execution...");
        System.setProperty("java.util.logging.config.file", "logging.properties");
        Logger.getLogger("org.openqa.selenium.remote.http.WebSocket$Listener").setLevel(Level.SEVERE);

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log");

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
        wrappers = new Wrappers(driver);
        h = new Helpers(driver);
        softAssert = new SoftAssert();
    }
    
    @AfterTest
    public void endTest() {
        if (driver != null) {
            driver.quit();
        }
        System.out.println("Browser closed. Test execution completed.");
    }
    
    @Test(description = "Testcase 01: YouTube About Page Validation")
    public void testCase01() throws Exception {
        Reporter.log("testCase01: "
                    +"Go to YouTube.com and Assert you are on the correct URL."
                    +"Click on \"About\" at the bottom of the sidebar."
                    +"Print the message on the screen.");
        logCommand("Start Testcase", "testCase01");

        String url = "https://www.youtube.com";
        wrappers.navigate(url);
        Assert.assertTrue(driver.getCurrentUrl().contains("youtube.com"), "Not on YouTube homepage");

        String tab = "About";
        wrappers.navigateSidebar(tab);
        softAssert.assertTrue(driver.getCurrentUrl().contains("about"), "Not on the About page");

        wrappers.printPageContent();

        logCommand("End Testcase","testCase01");
    }

    @Test(description = "Testcase 02: Movies Tab Soft Assertions")
    public void testCase02() throws Exception {
        Reporter.log("testCase02: "
                    +"Go to the \"Films\" or \"Movies\" tab"
                    +"In the \"Top Selling\" section, scroll to the extreme right."
                    +"Apply a Soft Assert on whether the movie is marked \"A\" for Mature or not."
                    +"Apply a Soft assert on the movie category to check if it exists ex: \"Comedy\", \"Animation\", \"Drama\".");
        logCommand("Start Testcase", "testCase02");

        String url = "https://www.youtube.com";
        wrappers.navigate(url);
        String tab = "Films";
        wrappers.navigateSidebar(tab);

        softAssert.assertTrue(wrappers.checkFilmCertificate("Top selling", "last"), "Film certificate check failed");
        softAssert.assertTrue(wrappers.checkFilmCategory("Top selling", "last"), "Film category check failed");
        softAssert.assertAll();

        logCommand("End Testcase","testCase02");
    }
    
    @Test(description = "Testcase 03: Music Playlist Validation")
    public void testCase03() throws Exception {
        Reporter.log("testCase03: "
                    +"Go to the \"Music\" tab."
                    +"In the 1st section, print the name of the playlist on the most right."
                    +"Soft Assert on whether the number of tracks listed is less than or equal to 50.");
        logCommand("Start Testcase", "testCase03");

        String url = "https://www.youtube.com";
        wrappers.navigate(url);
        String tab = "Music";
        wrappers.navigateSidebar(tab);

        softAssert.assertTrue(wrappers.checkMusicTrackCount("1", "last") <= 50, "Music track count exceeds 50");
        softAssert.assertAll();

        logCommand("End Testcase","testCase03");
    }
    
    @Test(description = "Testcase 04: News Posts Likes Summary")
    public void testCase04() throws Exception {
        Reporter.log("testCase04: "
                    +"Go to the \"News\" tab."
                    +"Print the title and body of the 1st 3 \"Latest News Posts\","
                    +"along with the sum of the number of likes on all 3 of them. No likes given means 0.");
        logCommand("Start Testcase", "testCase04");

        String url = "https://www.youtube.com";
        wrappers.navigate(url);

        String tab = "News";
        wrappers.navigateSidebar(tab);

        int totalLikes = 0;
        for (int i = 1; i <= 3; i++) {
            Map<String, String> newsDetails = wrappers.getNewsPost("Latest news posts", String.valueOf(i));
            logCommand(String.format("News Details for Post %d", i), "\n\t"+newsDetails.getOrDefault("title", "Title not found"));
            totalLikes += Integer.parseInt(newsDetails.getOrDefault("likes", "0"));
        }
        logCommand("Total Likes", String.valueOf(totalLikes));

        logCommand("End Testcase","testCase04");
    }
    
    @Test(
        description = "Testcase 05: Search Items & Views Threshold",
        dataProvider = "excelData",
        dataProviderClass = ExcelDataProvider.class
    )
    public void testCase05(String searchKeyword) throws Exception {
        Reporter.log("testCase05: "
                    +"Search for each of the items given in the stubs: src/test/resources/data.xlsx. "
                    +"Keep scrolling till the sum of each video\'s views reach 10 Cr.");
        logCommand("Start Testcase", "testCase05");

        String url = "https://www.youtube.com";
        wrappers.navigate(url);

        logCommand("Search", searchKeyword);
        wrappers.search(searchKeyword);

        int totalViews = 0;
        int position = 1;
        while (totalViews < 100000000) {
            Map<String, String> result = wrappers.searchResult(searchKeyword, String.valueOf(position));
            String rawViews = result.getOrDefault("views", "0").trim().toLowerCase().split(" ")[0];
            long views;
            if (rawViews.endsWith("k")) {
                views = (long)(Double.parseDouble(rawViews.replace("k","")) * 1_000);
            } else if (rawViews.endsWith("m")) {
                views = (long)(Double.parseDouble(rawViews.replace("m","")) * 1_000_000);
            } else {
                views = Long.parseLong(rawViews.replaceAll("[^0-9]", ""));
            }
            totalViews += views;
            logCommand(String.format("Video %d", position), String.format("%d views", views));
            if (totalViews >= 100000000) {
                logCommand("Threshold reached", "Total views have reached 10 Cr with "+position+" videos.");
                break;
            }
            position++;
        }
        logCommand("End Testcase","testCase05");
    }
}

package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;

/**
 * ক্রস-ব্রাউজার সাপোর্ট: -Dbrowser=chrome|firefox|edge (ডিফল্ট: chrome)
 * টেস্ট টাইপ কভারেজ: Cross-browser Testing, Compatibility Testing
 *
 * উদাহরণ: mvn test -Dbrowser=firefox
 *         mvn test -Dbrowser=edge -Dheadless=true
 */
public class DriverFactory {
    private static WebDriver driver;

    public static WebDriver getDriver() {
        if (driver == null) {
            String browser = System.getProperty("browser", "chrome").toLowerCase();
            boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

            // গুরুত্বপূর্ণ ফিক্স (২০২৬-০৭-১০): আগে প্রতিটি স্ক্রিপ্ট Chrome-এর ডিফল্ট,
            // পার্সিস্টেন্ট ইউজার প্রোফাইল ব্যবহার করত। ফলে এক scenario-তে সফল লগইন
            // (বা রেজিস্ট্রেশন, যা auto-login করে) হওয়ার পর তৈরি হওয়া ParaBank সেশন
            // কুকি ডিস্কে থেকে যেত, এবং পরের scenario-তে (যদিও নতুন ChromeDriver
            // প্রসেস) সেই একই প্রোফাইল আবার লোড হয়ে আগের সেশন কুকি নিয়েই আসত -
            // ফলে negative login টেস্টগুলো "ইউজার আগে থেকেই লগইন করা আছে" অবস্থায়
            // শুরু হয়ে ভুলভাবে পাস/ফেল দেখাচ্ছিল (login.feature-এ ৬টা "Security
            // defect" ফলস-পজিটিভ ফেইলিওর এবং সম্ভবত register.feature-এর
            // duplicate-username টেস্টও এর শিকার)। প্রতিটি ব্রাউজারে
            // incognito/private/InPrivate মোড যোগ করে প্রতিটি scenario-কে সম্পূর্ণ
            // ফ্রেশ, কুকিহীন সেশনে শুরু করানো হলো।
            switch (browser) {
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions ffOptions = new FirefoxOptions();
                    ffOptions.addArguments("-private");
                    if (headless) ffOptions.addArguments("-headless");
                    driver = new FirefoxDriver(ffOptions);
                    break;

                case "edge":
                    WebDriverManager.edgedriver().setup();
                    EdgeOptions edgeOptions = new EdgeOptions();
                    edgeOptions.addArguments("--inprivate");
                    if (headless) edgeOptions.addArguments("--headless=new");
                    driver = new EdgeDriver(edgeOptions);
                    break;

                case "chrome":
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--remote-allow-origins=*");
                    options.addArguments("--disable-notifications");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");
                    options.addArguments("--incognito");
                    if (headless) options.addArguments("--headless=new");
                    // লগ কমানোর জন্য (CDP Warning লুকানোর চেষ্টা)
                    System.setProperty("webdriver.chrome.silentOutput", "true");
                    driver = new ChromeDriver(options);
                    break;
            }

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error closing driver: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }
}

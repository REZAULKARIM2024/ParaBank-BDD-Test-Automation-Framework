package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {

    WebDriver driver;
    WebDriverWait wait;

    // Locators
    private final By usernameField = By.name("username");
    private final By passwordField = By.name("password");
    private final By loginBtn = By.xpath("//input[@value='Log In']");
    private final By logoutLink = By.linkText("Log Out");
    private final By errorMessage = By.xpath("//div[contains(@class,'error')] | //p[contains(text(),'error')] | //div[@id='rightPanel']//p");
    private final By loginHeading = By.xpath("//h2[text()='Customer Login']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        // ওয়েট টাইম ১৫ সেকেন্ড করা হলো কারণ প্যারাব্যাংক মাঝে মাঝে স্লো থাকে
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void enterUsername(String user) {
        // এলিমেন্ট দৃশ্যমান হওয়ার পাশাপাশি সেটি এনাবলড আছে কিনা চেক করা
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
        userField.clear();
        if (user != null && !user.isEmpty()) {
            userField.sendKeys(user);
        }
    }

    public void enterPassword(String pass) {
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        passField.clear();
        if (pass != null && !pass.isEmpty()) {
            passField.sendKeys(pass);
        }
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    public boolean isLogoutDisplayed() {
        try {
            // লগইন সফল হওয়ার পর পেজ রিফ্রেশ হতে সময় নেয়, তাই ওয়েট জরুরি
            return wait.until(ExpectedConditions.visibilityOfElementLocated(logoutLink)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ===== Negative-path helpers =====

    /** নেগেটিভ টেস্টের জন্য: ভুল লগইনের পর error message দেখানো হচ্ছে কিনা */
    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fast, non-waiting variant of isErrorMessageDisplayed() - the page has
     * already loaded by the time clickLogin() returns (normal page-load
     * strategy), so there's nothing to gain by waiting the full timeout when
     * the element genuinely isn't there.
     */
    public boolean isErrorMessageDisplayedNow() {
        try {
            return !driver.findElements(errorMessage).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessageText() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * ভুল লগইনের পর ইউজার এখনো লগইন পেজেই আছে কিনা (redirect হয়নি)।
     * ParaBank submits the login form to /login.htm (not always back to
     * /index.htm), and a WAF/CDN challenge page in front of the shared demo
     * server can also intercept the request - both of those still count as
     * "did not reach an authenticated page", so they're accepted here too.
     */
    public boolean isStillOnLoginPage() {
        try {
            if (wait.until(ExpectedConditions.visibilityOfElementLocated(loginHeading)).isDisplayed()) {
                return true;
            }
        } catch (Exception ignored) {
            // fall through to URL/WAF based checks below
        }
        if (isWafOrChallengePage()) {
            return true;
        }
        String url = driver.getCurrentUrl();
        return url.contains("index.htm") || url.contains("login.htm");
    }

    /**
     * Detects whether the response is actually a Cloudflare (or similar CDN/WAF)
     * interstitial - e.g. "Just a moment..." (JS challenge) or "Attention
     * Required!" (blocked) - rather than ParaBank's own page. This was observed
     * in a live run: SQL-injection/XSS payloads submitted through the login form
     * sometimes get intercepted by the WAF in front of the public demo server
     * before ParaBank's own application code ever sees them. That is itself a
     * valid "malicious input was rejected" outcome for a security test, it just
     * doesn't look like ParaBank's own validation error.
     */
    public boolean isWafOrChallengePage() {
        try {
            String title = driver.getTitle();
            if (title != null) {
                String t = title.toLowerCase();
                if (t.contains("just a moment") || t.contains("attention required") || t.contains("cloudflare")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // ignore and fall through
        }
        return false;
    }

    /**
     * Fast, non-waiting check of whether the Logout link is currently present.
     * Unlike isLogoutDisplayed(), this does not block for the full explicit-wait
     * timeout when the element is absent - useful for negative-path assertions
     * where we already know login should NOT have succeeded, so there is nothing
     * to wait for.
     */
    public boolean isLogoutDisplayedNow() {
        try {
            return !driver.findElements(logoutLink).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

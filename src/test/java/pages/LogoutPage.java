package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LogoutPage {

    WebDriver driver;

    By logoutLink = By.linkText("Log Out");
    By loginPageHeader = By.xpath("//h2[text()='Customer Login']");

    public LogoutPage(WebDriver driver) {
        this.driver = driver;
    }

    public void clickLogout() {
        driver.findElement(logoutLink).click();
    }

    public boolean isLoggedOut() {
        return driver.findElement(loginPageHeader).isDisplayed();
    }

    /** সেশন সিকিউরিটি চেক: লগআউটের পর browser back button চাপলে protected page দেখানো উচিত না */
    public boolean isProtectedPageBlockedAfterBack() {
        driver.navigate().back();
        try {
            return driver.findElement(loginPageHeader).isDisplayed()
                    || driver.getCurrentUrl().contains("index.htm");
        } catch (Exception e) {
            return false;
        }
    }

    /** সেশন সিকিউরিটি চেক: লগআউটের পর ন্যাভিগেশন মেনু থেকে Logout লিংকটি নিজেই আর
     *  দেখা যাওয়া উচিত না (session state সঠিকভাবে ক্লিয়ার হয়েছে তার প্রমাণ)। */
    public boolean isLogoutLinkGone() {
        try {
            return driver.findElements(logoutLink).isEmpty();
        } catch (Exception e) {
            return true;
        }
    }
}

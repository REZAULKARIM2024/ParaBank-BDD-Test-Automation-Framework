package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Page Object for the "Customer Lookup" page ("Forgot login info?" link on the
 * login page). URL: https://parabank.parasoft.com/parabank/lookup.htm
 *
 * নোট (২য় সংশোধন - প্রথম লাইভ রানের ব্যর্থতার পর): প্রথম সংস্করণে
 * "customer.firstName" ইত্যাদি id অনুমান করে locator লেখা হয়েছিল (RegisterPage.java-র
 * কনভেনশন থেকে), কিন্তু লাইভ রানে TimeoutException দিয়ে প্রমাণিত হলো যে
 * lookup.htm ফর্মে এই id গুলো নেই - এটা register.htm থেকে ভিন্ন controller/form
 * bean, তাই id অনুমান ভুল ছিল।
 *
 * এই সেশনে Chrome/DevTools এক্সেস না থাকায় সরাসরি raw HTML/id দেখা সম্ভব হয়নি
 * (web_fetch টুল markdown-এ কনভার্ট করে, যা input ট্যাগের id/name attribute
 * স্ট্রিপ করে ফেলে)। তাই id-নির্ভর locator এর বদলে, fetch থেকে নিশ্চিত হওয়া
 * টেবিল স্ট্রাকচার (প্রতিটি ফিল্ডের লেবেল - "First Name:", "Last Name:",
 * "Address:", "City:", "State:", "Zip Code:", "SSN:" - প্রতিটি একটি টেবিল রো-তে
 * থাকে) ব্যবহার করে "লেবেল-রিলেটিভ" XPath locator লেখা হলো: প্রতিটি locator
 * লেবেল টেক্সটযুক্ত <tr> খুঁজে বের করে, তারপর সেই একই রো-এর মধ্যে থাকা <input>
 * এলিমেন্টটি নেয় - এটি প্রকৃত id/name attribute যাই হোক না কেন কাজ করবে, যতক্ষণ
 * পেজের row-based লেআউট (যা fetch-এ নিশ্চিত হয়েছে) অপরিবর্তিত থাকে।
 */
public class CustomerLookupPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By forgotLoginInfoLink = By.linkText("Forgot login info?");
    private final By heading = By.xpath("//h1[contains(text(),'Customer Lookup')]");

    // লেবেল-রিলেটিভ XPath: label টেক্সটযুক্ত <tr> এর মধ্যে যে <input> আছে সেটাকেই নেয় -
    // id/name attribute যাই হোক না কেন কাজ করবে (দেখুন উপরের ক্লাস-লেভেল নোট)।
    private final By firstName = By.xpath(rowInputXpath("First Name"));
    private final By lastName = By.xpath(rowInputXpath("Last Name"));
    private final By address = By.xpath(rowInputXpath("Address"));
    private final By city = By.xpath(rowInputXpath("City"));
    private final By state = By.xpath(rowInputXpath("State"));
    private final By zipCode = By.xpath(rowInputXpath("Zip Code"));
    private final By ssn = By.xpath(rowInputXpath("SSN"));
    private final By submitButton = By.cssSelector("input[type='submit']");

    private static String rowInputXpath(String labelText) {
        return "//tr[td[contains(normalize-space(.), '" + labelText + "')]]//input";
    }

    private final By fieldErrors = By.cssSelector("span.error, .error");
    private final By internalErrorText = By.xpath(
            "//*[contains(text(),'An internal error has occurred') or contains(text(),'Error!')]");
    // সফল lookup হলে ParaBank সাধারণত সরাসরি লগইন করিয়ে Accounts Overview-তে
    // নিয়ে যায় - তাই "logged in" অবস্থার প্রমাণ হিসেবে Logout লিংক ব্যবহার করা হলো
    private final By logoutLink = By.linkText("Log Out");

    public CustomerLookupPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openFromLoginPage() {
        wait.until(ExpectedConditions.elementToBeClickable(forgotLoginInfoLink)).click();
        wait.until(ExpectedConditions.urlContains("lookup.htm"));
    }

    public boolean isLookupHeadingDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(heading)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areAllExpectedFieldsDisplayed() {
        try {
            return isDisplayedNow(firstName) && isDisplayedNow(lastName) && isDisplayedNow(address)
                    && isDisplayedNow(city) && isDisplayedNow(state) && isDisplayedNow(zipCode)
                    && isDisplayedNow(ssn);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDisplayedNow(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void type(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        if (value != null && !value.isEmpty()) {
            el.sendKeys(value);
        }
    }

    public void fillLookupForm(String firstNameVal, String lastNameVal, String addressVal, String cityVal,
                                String stateVal, String zipVal, String ssnVal) {
        type(firstName, firstNameVal);
        type(lastName, lastNameVal);
        type(address, addressVal);
        type(city, cityVal);
        type(state, stateVal);
        type(zipCode, zipVal);
        type(ssn, ssnVal);
    }

    public void submitLookupForm() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    /** true হলে বোঝায় lookup সফল হয়নি - ব্যবহারকারীকে লগইন করানো হয়নি এবং কোনো
     *  matching customer পাওয়া যায়নি (URL এখনও lookup.htm-তে, বা কোনো এরর/ভ্যালিডেশন
     *  মেসেজ দেখা যাচ্ছে)। */
    public boolean isLookupUnsuccessful() {
        try {
            if (!driver.findElements(logoutLink).isEmpty()) {
                return false; // সফলভাবে লগইন হয়ে গেছে - lookup আসলে ম্যাচ পেয়েছে
            }
        } catch (Exception ignored) {
        }
        boolean stillOnLookupPage = driver.getCurrentUrl().contains("lookup.htm");
        boolean hasErrorMessage = !driver.findElements(fieldErrors).isEmpty();
        return stillOnLookupPage || hasErrorMessage;
    }

    public boolean isInternalServerErrorDisplayed() {
        try {
            return !driver.findElements(internalErrorText).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

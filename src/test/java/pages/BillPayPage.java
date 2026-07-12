package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class BillPayPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators: name-এ ডট (.) থাকলে অনেক সময় CSS Selector ব্যবহার করা বেশি নিরাপদ
    private final By billPayLink = By.linkText("Bill Pay");
    private final By payeeNameField = By.cssSelector("input[name='payee.name']");
    private final By addressField = By.cssSelector("input[name='payee.address.street']");
    private final By cityField = By.cssSelector("input[name='payee.address.city']");
    private final By stateField = By.cssSelector("input[name='payee.address.state']");
    private final By zipCodeField = By.cssSelector("input[name='payee.address.zipCode']");
    private final By phoneField = By.cssSelector("input[name='payee.phoneNumber']");
    private final By accountField = By.cssSelector("input[name='payee.accountNumber']");
    private final By verifyAccountField = By.cssSelector("input[name='verifyAccount']");
    private final By amountField = By.cssSelector("input[name='amount']");
    private final By sendPaymentBtn = By.xpath("//input[@value='Send Payment']");

    // সাকসেস মেসেজের জন্য আরও জেনেরিক XPath (id-তে ডাইনামিক ভ্যালু থাকতে পারে)
    private final By successMsg = By.xpath("//div[contains(@id, 'billpayResult')]//h1");
    // ফিল্ড ভ্যালিডেশন এরর (required field ফাঁকা থাকলে) - class="error"
    private final By fieldErrors = By.cssSelector("span.error, .error");

    public BillPayPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openBillPay() {
        // ১. মেনু লিংকে ক্লিক
        wait.until(ExpectedConditions.elementToBeClickable(billPayLink)).click();

        // ২. URL পরিবর্তন হওয়া পর্যন্ত অপেক্ষা করুন (এটি নিশ্চিত করে পেজ রিফ্রেশ শেষ হয়েছে)
        wait.until(ExpectedConditions.urlContains("billpay.htm"));

        // ৩. প্রথম ফিল্ডটি দৃশ্যমান এবং এনাবল হওয়া পর্যন্ত অপেক্ষা
        wait.until(ExpectedConditions.elementToBeClickable(payeeNameField));
    }

    private void sendKeys(By locator, String text) {
        // এলিমেন্টটি দৃশ্যমান হওয়া পর্যন্ত অপেক্ষা করে টেক্সট পাঠানো
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    public void enterPayeeDetails(String name, String addr, String cityName, String stateName,
                                  String zip, String phoneNo, String acc, String verifyAcc, String amt) {
        sendKeys(payeeNameField, name);
        sendKeys(addressField, addr);
        sendKeys(cityField, cityName);
        sendKeys(stateField, stateName);
        sendKeys(zipCodeField, zip);
        sendKeys(phoneField, phoneNo);
        sendKeys(accountField, acc);
        sendKeys(verifyAccountField, verifyAcc);
        sendKeys(amountField, amt);
    }

    public void submitPayment() {
        // বাটন ক্লিক করার আগে সেটি স্ক্রিনে আছে কি না নিশ্চিত করা
        wait.until(ExpectedConditions.elementToBeClickable(sendPaymentBtn)).click();
    }

    public boolean isPaymentSuccessful() {
        try {
            // "Bill Payment Complete" টেক্সটটি আসার জন্য অপেক্ষা
            WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(successMsg));
            return result.getText().trim().contains("Bill Payment Complete");
        } catch (Exception e) {
            return false;
        }
    }

    /** নেগেটিভ টেস্ট: required ফিল্ড ফাঁকা থাকলে ভ্যালিডেশন এরর দেখায় কিনা */
    public boolean isValidationErrorDisplayed() {
        try {
            List<WebElement> errors = driver.findElements(fieldErrors);
            return !errors.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

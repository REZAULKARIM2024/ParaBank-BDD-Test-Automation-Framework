package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Map;

/**
 * Page Object for the "Register" (Create New Customer) page.
 * URL: https://parabank.parasoft.com/parabank/register.htm
 */
public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By registerLink = By.linkText("Register");
    private final By firstName = By.id("customer.firstName");
    private final By lastName = By.id("customer.lastName");
    private final By address = By.id("customer.address.street");
    private final By city = By.id("customer.address.city");
    private final By state = By.id("customer.address.state");
    private final By zipCode = By.id("customer.address.zipCode");
    private final By phone = By.id("customer.phoneNumber");
    private final By ssn = By.id("customer.ssn");
    private final By username = By.id("customer.username");
    private final By password = By.id("customer.password");
    private final By confirmPassword = By.id("repeatedPassword");
    private final By registerButton = By.xpath("//input[@value='Register']");

    private final By successMessage = By.xpath("//p[contains(text(),'Your account was created successfully')]");
    // ফিল্ড-ভিত্তিক ভ্যালিডেশন এরর, যেমন "Username is required" / "Passwords did not match"
    private final By fieldErrors = By.cssSelector("span.error");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void openRegisterPage() {
        wait.until(ExpectedConditions.elementToBeClickable(registerLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName));
    }

    private void type(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        if (value != null && !value.isEmpty()) {
            el.sendKeys(value);
        }
    }

    public void fillRegistrationForm(Map<String, String> data) {
        type(firstName, data.get("firstName"));
        type(lastName, data.get("lastName"));
        type(address, data.get("address"));
        type(city, data.get("city"));
        type(state, data.get("state"));
        type(zipCode, data.get("zipCode"));
        type(phone, data.get("phone"));
        type(ssn, data.get("ssn"));
        type(username, data.get("username"));
        type(password, data.get("password"));
        type(confirmPassword, data.get("confirmPassword"));
    }

    public void submitRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(registerButton)).click();
    }

    public boolean isRegistrationSuccessful() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidationErrorDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(fieldErrors)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getFirstValidationErrorText() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(fieldErrors)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
}

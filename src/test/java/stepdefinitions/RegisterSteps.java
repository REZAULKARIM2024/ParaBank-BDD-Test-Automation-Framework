package stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.RegisterPage;
import utils.DriverFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RegisterSteps {

    private WebDriver driver;
    private RegisterPage registerPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            registerPage = new RegisterPage(driver);
        }
    }

    @Given("user is on Parabank registration page")
    public void user_is_on_registration_page() {
        driver = DriverFactory.getDriver();
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        registerPage = new RegisterPage(driver);
    }

    @When("user fills the registration form with unique username and:")
    public void user_fills_registration_form_unique(DataTable table) {
        setup();
        // table.asMap(...) returns an UNMODIFIABLE map - copy it into a mutable
        // HashMap first, otherwise data.put(...) below throws
        // UnsupportedOperationException (this was a real bug found via a live run).
        Map<String, String> data = new HashMap<>(table.asMap(String.class, String.class));
        // ইউনিক ইউজারনেম তৈরি করা হয় যাতে "duplicate username" এরর এড়ানো যায় (positive scenario)
        String uniqueUsername = data.get("username") + "_" + ThreadLocalRandom.current().nextInt(100000, 999999);
        data.put("username", uniqueUsername);
        registerPage.fillRegistrationForm(data);
    }

    @When("user fills the registration form with:")
    public void user_fills_registration_form(DataTable table) {
        setup();
        Map<String, String> data = table.asMap(String.class, String.class);
        registerPage.fillRegistrationForm(data);
    }

    @When("user submits the registration form")
    public void user_submits_registration_form() {
        setup();
        registerPage.submitRegistration();
    }

    @Then("the account should be registered successfully")
    public void account_registered_successfully() {
        setup();
        Assert.assertTrue(registerPage.isRegistrationSuccessful(),
                "FAILED: নতুন কাস্টমার রেজিস্ট্রেশন সফল হয়নি।");
    }

    @Then("registration should fail with a validation error")
    public void registration_should_fail_with_validation_error() {
        setup();
        Assert.assertTrue(registerPage.isValidationErrorDisplayed(),
                "FAILED: ইনভ্যালিড রেজিস্ট্রেশন ডেটা দেওয়ার পরেও কোনো ভ্যালিডেশন এরর দেখানো হয়নি (Negative Test)।");
        Assert.assertFalse(registerPage.isRegistrationSuccessful(),
                "FAILED (defect): অসম্পূর্ণ/ইনভ্যালিড ডেটা দিয়েও অ্যাকাউন্ট তৈরি হয়ে গেছে!");
    }
}

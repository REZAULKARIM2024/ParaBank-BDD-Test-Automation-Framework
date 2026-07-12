package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.OpenNewAccountPage;
import utils.DriverFactory;

public class OpenNewAccountSteps {

    WebDriver driver;
    OpenNewAccountPage openNewAccountPage;

    // এই স্ট্যাটিক ফিল্ডে সদ্য তৈরি হওয়া অ্যাকাউন্টের ID রাখা হয়, যাতে একই scenario-র
    // মধ্যে অন্য step definition class (যেমন RequestLoanSteps) থেকেও এটি পড়া যায় -
    // Cucumber প্রতি scenario-তে প্রতিটি step class-এর আলাদা instance বানায়, তাই সরাসরি
    // instance field শেয়ার করা যায় না।
    public static String lastCreatedAccountId;

    @When("user navigates to Open New Account page")
    public void user_navigates_to_open_new_account_page() {
        driver = DriverFactory.getDriver();
        openNewAccountPage = new OpenNewAccountPage(driver);
        openNewAccountPage.openOpenNewAccountPage();
    }

    // এই মেথডটি আপনার এরর লগের 'Undefined Step' ফিক্স করবে
    @When("user selects account type {string} and existing account {string}")
    public void user_selects_account_type_and_existing_account(String type, String fromAcc) {
        openNewAccountPage.selectAccountTypeAndExistingAccount(type, fromAcc);
    }

    @When("user clicks Open New Account")
    public void user_clicks_open_new_account() {
        openNewAccountPage.clickOpenNewAccount();
    }

    @Then("new account should be created successfully")
    public void new_account_should_be_created_successfully() {
        Assert.assertTrue(openNewAccountPage.isAccountCreated(), "New account creation failed!");
    }

    /** সদ্য তৈরি হওয়া অ্যাকাউন্টের ID ক্যাপচার করে static ফিল্ডে রাখে, যাতে অন্য
     *  feature/step class (যেমন Request Loan) থেকে এই ফ্রেশ, স্বাস্থ্যকর ব্যালেন্সের
     *  অ্যাকাউন্টটি ব্যবহার করা যায়। */
    @Then("the new account id is captured for later use")
    public void the_new_account_id_is_captured_for_later_use() {
        lastCreatedAccountId = openNewAccountPage.getNewAccountId();
        System.out.println("[DIAG] Captured newly created account id: " + lastCreatedAccountId);
        Assert.assertNotNull(lastCreatedAccountId,
                "FAILED: নতুন তৈরি হওয়া অ্যাকাউন্টের ID পেজ থেকে ক্যাপচার করা যায়নি।");
    }
}
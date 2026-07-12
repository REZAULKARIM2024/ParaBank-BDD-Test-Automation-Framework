package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.AccountsOverviewPage;
import pages.LoginPage;
import utils.DriverFactory;

public class AccountsOverviewSteps {

    private WebDriver driver;
    private AccountsOverviewPage accountsOverviewPage;

    // মেথডগুলো রান হওয়ার আগে ড্রাইভার এবং পেজ অবজেক্ট সেটআপ করার জন্য একটি হেল্পার মেথড
    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            accountsOverviewPage = new AccountsOverviewPage(driver);
        }
    }

    @When("user navigates to Accounts Overview page")
    public void user_navigates_to_accounts_overview_page() {
        setup(); // নিশ্চিত করে ড্রাইভার এবং পেজ অবজেক্ট রেডি
        accountsOverviewPage.openAccountsOverviewPage();
    }

    /** ডায়াগনস্টিক-ওনলি স্টেপ: কোনো অ্যাসারশন নেই, শুধু বর্তমান সব অ্যাকাউন্টের
     *  balance কনসোলে লগ করে - ব্যালেন্স-নির্ভর ফেইলিওর (যেমন loan/transfer টেস্ট)
     *  ডিবাগ করতে সাহায্য করে। */
    @Then("user logs the current account balances for debugging")
    public void user_logs_the_current_account_balances_for_debugging() {
        setup();
        System.out.println("[DIAG] Current account balances (Accounts Overview): "
                + accountsOverviewPage.getAccountRowsText());
    }

    @Then("user should see a list of all accounts with balances")
    public void user_should_see_a_list_of_all_accounts_with_balances() {
        setup();

        // ডাইনামিক ডাটা লোড হওয়ার জন্য এখানে আমরা পেজ অবজেক্টের বুলিয়ান চেকটি ব্যবহার করছি
        boolean isDisplayed = accountsOverviewPage.isAccountsListDisplayed();

        // Assertion: ব্যর্থ হলে সঠিক কারণ কনসোলে দেখাবে
        Assert.assertTrue(isDisplayed,
            "FAILED: Accounts overview table load হতে ব্যর্থ হয়েছে। সম্ভবত ডাটাবেজে কোনো অ্যাকাউন্ট নেই অথবা সার্ভার এরর (Internal Error) ঘটেছে।");
    }

    /** ডাটা-ইন্টিগ্রিটি: এই ডেমো ইউজারের একাধিক অ্যাকাউন্ট থাকা উচিত (আগের সেশনগুলোতে
     *  Open New Account দিয়ে তৈরি হওয়া অনেক অ্যাকাউন্ট সহ), তাই Accounts Overview
     *  পেজ সত্যিই একাধিক রো লিস্ট করছে কিনা যাচাই করা হয়। */
    @Then("accounts overview should list multiple accounts")
    public void accounts_overview_should_list_multiple_accounts() {
        setup();
        Assert.assertTrue(accountsOverviewPage.hasAtLeastAccountRows(2),
                "FAILED: Accounts Overview-এ একাধিক অ্যাকাউন্ট রো পাওয়া যায়নি। Rows: "
                        + accountsOverviewPage.getAccountRowsText());
    }

    /** ডাটা-ফরম্যাট: প্রতিটি অ্যাকাউন্ট রো-তে ব্যালেন্স সঠিক কারেন্সি ফরম্যাটে ($xxx.xx)
     *  দেখানো হচ্ছে কিনা যাচাই করা হয়। */
    @Then("every account balance should be displayed in a valid currency format")
    public void every_account_balance_should_be_displayed_in_valid_currency_format() {
        setup();
        Assert.assertTrue(accountsOverviewPage.areAllRowsValidCurrencyFormatted(),
                "FAILED: কিছু অ্যাকাউন্ট রো-তে ব্যালেন্স বৈধ কারেন্সি ফরম্যাটে নেই। Rows: "
                        + accountsOverviewPage.getAccountRowsText());
    }

    /** ইন্টিগ্রেশন টেস্ট: Open New Account থেকে সদ্য তৈরি হওয়া অ্যাকাউন্ট আইডি
     *  (OpenNewAccountSteps.lastCreatedAccountId-এ ক্যাপচার করা) Accounts Overview
     *  পেজে সত্যিই দেখা যাচ্ছে কিনা যাচাই করে - নতুন অ্যাকাউন্ট তৈরি হওয়ার পর
     *  ব্যাকএন্ডে সঠিকভাবে persist হয়েছে কিনা এটাই নিশ্চিত করে। */
    @Then("the newly created account should appear in the accounts list")
    public void the_newly_created_account_should_appear_in_the_accounts_list() {
        setup();
        String expectedId = OpenNewAccountSteps.lastCreatedAccountId;
        Assert.assertNotNull(expectedId,
                "FAILED: কোনো নতুন অ্যাকাউন্ট ID ক্যাপচার করা হয়নি - এই স্টেপের আগে "
                        + "\"the new account id is captured for later use\" স্টেপ চলেছে কিনা যাচাই করুন।");
        java.util.List<String> rows = accountsOverviewPage.getAccountRowsText();
        boolean found = rows.stream().anyMatch(r -> r.contains(expectedId));
        Assert.assertTrue(found, "FAILED: সদ্য তৈরি হওয়া অ্যাকাউন্ট #" + expectedId
                + " Accounts Overview-এ দেখা যায়নি। Rows: " + rows);
    }

    /** সদ্য নিবন্ধিত কাস্টমারের নিজস্ব (একমাত্র) অ্যাকাউন্ট ID ক্যাপচার করে
     *  RequestLoanSteps.freshCustomerAccountId-এ রাখে - Request Loan ফিচারে এটি
     *  ব্যবহার করা হয় যাতে "john" ইউজারের পুরনো, স্থায়ীভাবে নষ্ট হয়ে যাওয়া
     *  অ্যাকাউন্ট (#12345) সম্পূর্ণ এড়িয়ে একটি সম্পূর্ণ ফ্রেশ, দূষণ-মুক্ত কাস্টমার
     *  আইডেন্টিটি দিয়ে লোন টেস্ট চালানো যায়। */
    @Then("the customer's own account id is captured for later use")
    public void the_customers_own_account_id_is_captured_for_later_use() {
        setup();
        String id = accountsOverviewPage.getFirstAccountId();
        System.out.println("[DIAG] Captured fresh customer's own account id: " + id);
        Assert.assertNotNull(id,
                "FAILED: সদ্য নিবন্ধিত কাস্টমারের নিজস্ব অ্যাকাউন্ট ID Accounts Overview থেকে ক্যাপচার করা যায়নি।");
        RequestLoanSteps.freshCustomerAccountId = id;
    }

    // ==========================================================
    // Negative / Security (Broken Access Control) - সরাসরি URL
    // দিয়ে না-লগইন অবস্থায় প্রবেশের চেষ্টা
    // ==========================================================

    @Given("user is not logged into Parabank")
    public void user_is_not_logged_into_parabank() {
        driver = DriverFactory.getDriver();
        driver.manage().deleteAllCookies();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
    }

    @When("user directly navigates to the accounts overview URL without logging in")
    public void user_directly_navigates_to_overview_url() {
        setup();
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
    }

    @Then("user should be redirected to the login page")
    public void user_should_be_redirected_to_login() {
        LoginPage loginPage = new LoginPage(driver);
        Assert.assertTrue(loginPage.isStillOnLoginPage(),
                "FAILED (Security defect): লগইন ছাড়াই সরাসরি URL দিয়ে protected page-এ অ্যাক্সেস পাওয়া গেছে (Broken Access Control)।");
    }
}

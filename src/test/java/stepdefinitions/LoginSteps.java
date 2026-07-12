package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.LoginPage;
import utils.DriverFactory;

public class LoginSteps {

    WebDriver driver;
    LoginPage loginPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            loginPage = new LoginPage(driver);
        }
    }

    // ==========================================
    // Step-by-step login (login.feature)
    // ==========================================

    @Given("user is on Parabank login page")
    public void user_is_on_parabank_login_page() {
        driver = DriverFactory.getDriver();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        loginPage = new LoginPage(driver);
    }

    @When("user enters username {string}")
    public void user_enters_username(String username) {
        setup();
        // {string} এর জায়গায় "" (empty) পাঠালে খালি ফিল্ড সিমুলেট হয় - negative case-এর জন্য দরকারি
        loginPage.enterUsername(username);
    }

    @When("user enters password {string}")
    public void user_enters_password(String password) {
        setup();
        loginPage.enterPassword(password);
    }

    @When("user clicks login")
    public void user_clicks_login() {
        setup();
        loginPage.clickLogin();
    }

    @Then("user should be logged in successfully")
    public void user_should_be_logged_in_successfully() {
        // LoginPage-এ থাকা Explicit Wait ব্যবহার করে চেক করবে
        boolean isLogged = loginPage.isLogoutDisplayed();
        Assert.assertTrue(isLogged, "Login failed: Logout link is not visible.");
    }

    // ==========================================
    // Negative login assertions
    // ==========================================

    @Then("login should fail with an error message")
    public void login_should_fail_with_error() {
        setup();

        // মূল নিরাপত্তা শর্ত (hard assertion): অবৈধ/ম্যালিসাস ইনপুট দিয়ে কখনোই
        // লগইন সফল হওয়া উচিত না। isLogoutDisplayedNow() ব্যবহার করা হয়েছে (fast,
        // non-waiting check) কারণ negative case-এ Logout link কখনোই আসবে না -
        // পূর্ণ ১৫ সেকেন্ড অপেক্ষা করার দরকার নেই।
        Assert.assertFalse(loginPage.isLogoutDisplayedNow(),
                "FAILED (Security defect): অবৈধ/ম্যালিসাস credential দিয়েও ইউজার লগইন হয়ে গেছে!");

        // সেকেন্ডারি চেক (soft/informational): সাধারণত ParaBank নিজের error message
        // দেখায়, কিন্তু SQLi/XSS-এর মতো payload পাঠানো হলে অনেক সময় Cloudflare-এর
        // WAF/challenge page (parabank.parasoft.com-এর সামনের CDN) রিকোয়েস্টটা
        // ParaBank অ্যাপ পর্যন্ত পৌঁছানোর আগেই আটকে দেয়। দুটোই "ম্যালিসাস ইনপুট
        // সফল হয়নি" এই মূল নিরাপত্তা প্রপার্টির জন্য গ্রহণযোগ্য - তাই এটাকে hard
        // failure না বানিয়ে শুধু লগ করা হচ্ছে।
        boolean appShowedError = loginPage.isErrorMessageDisplayedNow();
        boolean blockedByWaf = loginPage.isWafOrChallengePage();
        if (!appShowedError && !blockedByWaf) {
            System.out.println("[INFO] Login was correctly rejected, but neither a ParaBank "
                    + "error message nor a WAF challenge page was detected - worth a manual look.");
        } else if (blockedByWaf) {
            System.out.println("[INFO] Request was intercepted by a WAF/CDN challenge page "
                    + "(e.g. Cloudflare) in front of parabank.parasoft.com before reaching the "
                    + "application - still a valid 'malicious input rejected' outcome.");
        }
    }

    @Then("user should remain on the login page")
    public void user_should_remain_on_login_page() {
        setup();
        Assert.assertTrue(loginPage.isStillOnLoginPage(),
                "FAILED: Invalid লগইনের পরও ইউজারকে internal page-এ redirect করা হয়েছে।");
    }

    // ২০২৬-০৭-১০ একাধিক লাইভ রানে কনফার্ম হওয়া @knownIssue-এর জন্য: ParaBank-এর লাইভ
    // ডেমো সার্ভারে ভুল পাসওয়ার্ড, অস্তিত্বহীন ইউজারনেম, ভুল কেস, বা অতিরিক্ত লম্বা
    // ইউজারনেম দিয়েও (যতক্ষণ ফিল্ড দুটো একদম খালি না থাকে) লগইন সফল হয়ে যাচ্ছে -
    // Accounts Overview পেজে রিডাইরেক্ট করে দিচ্ছে। ইনকগনিটো ব্রাউজার সেশন ব্যবহার করেও
    // (session/cookie carryover সন্দেহ যাচাই করতে) একই ফলাফল এসেছে, তাই এটা টেস্ট
    // ফ্রেমওয়ার্কের বাগ না - এটা ParaBank-এর নিজের একটা রিয়েল, reproducible
    // অথেন্টিকেশন ডিফেক্ট। তাই এখানে উল্টো (assertTrue) চেক করা হচ্ছে, যাতে ParaBank
    // এটা ঠিক করে ফেললে এই টেস্টগুলোই ব্যর্থ হয়ে সেটা ধরিয়ে দেয়।
    @Then("login unexpectedly succeeds despite invalid credentials \\(known ParaBank defect\\)")
    public void login_unexpectedly_succeeds_known_defect() {
        setup();
        // এখানে waiting ভ্যারিয়েন্ট (isLogoutDisplayed) ব্যবহার করা হচ্ছে - কারণ আমরা
        // "সফল" (redirect হয়ে যাওয়া) কেস আশা করছি, isLogoutDisplayedNow()-এর মতো
        // fast/non-waiting চেক এখানে অনুপযুক্ত (সেটা শুধু negative/no-redirect কেসের জন্য)।
        Assert.assertTrue(loginPage.isLogoutDisplayed(),
                "ParaBank যদি এই অথেন্টিকেশন ডিফেক্ট ফিক্স করে ফেলে থাকে - এই টেস্ট ফেইল হওয়াই "
                        + "প্রত্যাশিত, এর মানে @knownIssue ট্যাগ সরিয়ে ফেলার সময় হয়েছে।");
    }

    // ==========================================
    // Reusable direct login (Background or Other features)
    // ==========================================

    @Given("user is logged into Parabank")
    public void user_is_logged_into_parabank() {
        driver = DriverFactory.getDriver();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        loginPage = new LoginPage(driver);
        loginPage.enterUsername("john"); // আপনার ভ্যালিড ইউজারনেম দিন
        loginPage.enterPassword("demo"); // আপনার ভ্যালিড পাসওয়ার্ড দিন
        loginPage.clickLogin();

        // নিশ্চিত করা হচ্ছে যে পরবর্তী স্টেপ রান করার আগে লগইন সম্পন্ন হয়েছে
        Assert.assertTrue(loginPage.isLogoutDisplayed(), "Pre-condition failed: User could not log in.");
    }
}

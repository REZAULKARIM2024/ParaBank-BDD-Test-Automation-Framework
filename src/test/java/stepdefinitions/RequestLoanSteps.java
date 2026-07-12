package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.RequestLoanPage;
import utils.DriverFactory;

public class RequestLoanSteps {

    WebDriver driver;
    RequestLoanPage requestLoanPage;

    // সপ্তম রাউন্ড (2026-07-09): বারবার "insufficient funds" ফেইলিওরের পর প্রমাণ
    // মিলেছে যে সমস্যাটা নির্দিষ্ট ফান্ডিং অ্যাকাউন্টের ব্যালেন্স না - "john" ইউজারের
    // অ্যাকাউন্ট #12345 স্থায়ীভাবে ~-$1 বিলিয়ন ব্যালেন্সে থাকায় (এবং সেটা মেরামতযোগ্য
    // না), সম্ভবত ParaBank-এর লোন-অ্যাপ্রুভাল লজিক নির্দিষ্ট ফান্ডিং অ্যাকাউন্টের বদলে
    // পুরো কাস্টমারের এগ্রিগেট ব্যালেন্স/ইকুইটি চেক করে - যা ব্যাখ্যা করে কেন $100
    // ব্যালেন্সের ফ্রেশ অ্যাকাউন্ট দিয়েও ছোট ডাউন পেমেন্ট বারবার ফেল করছিল। সমাধান:
    // অ্যাকাউন্ট-লেভেলে আর trick না করে সম্পূর্ণ ফ্রেশ, নতুন রেজিস্টার করা কাস্টমার
    // ব্যবহার করা হচ্ছে - যার নিজস্ব অ্যাকাউন্ট/ইকুইটি "john"-এর দূষিত ইতিহাস দ্বারা
    // একদমই প্রভাবিত না (AccountsOverviewSteps.the_customers_own_account_id_is_captured_for_later_use()-এ ক্যাপচার করা)।
    public static String freshCustomerAccountId;

    // ড্রাইভার এবং পেজ অবজেক্ট ইনিশিয়ালাইজ করার হেল্পার মেথড
    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            requestLoanPage = new RequestLoanPage(driver);
        }
    }

    @When("user navigates to Request Loan page")
    public void user_navigates_to_request_loan_page() {
        setup();
        requestLoanPage.openRequestLoanPage();
    }

    @And("user selects funding account {string}")
    public void user_selects_funding_account(String account) {
        setup();
        requestLoanPage.selectFromAccount(account);
    }

    /** শেয়ার্ড ডেমো অ্যাকাউন্ট #12345/#54321 আগের ভুল টেস্ট রানের কারণে বিলিয়ন-ডলার
     *  নেগেটিভ ব্যালেন্সে চলে গেছে (confirmed via diagnostics), তাই সেগুলো top-up করে
     *  কাজ চালানো আর সম্ভব না। এর বদলে Open New Account ফিচার ব্যবহার করে তৈরি করা
     *  সদ্য-ফ্রেশ, স্বাস্থ্যকর ব্যালেন্সের অ্যাকাউন্টটি এখানে ফান্ডিং অ্যাকাউন্ট হিসেবে
     *  ব্যবহার করা হয় (OpenNewAccountSteps.lastCreatedAccountId-এ ক্যাপচার করা)। */
    @And("user selects the freshly created account as funding account")
    public void user_selects_the_freshly_created_account_as_funding_account() {
        setup();
        String accountId = OpenNewAccountSteps.lastCreatedAccountId;
        Assert.assertNotNull(accountId,
                "FAILED: কোনো নতুন অ্যাকাউন্ট ID ক্যাপচার করা হয়নি - loan টেস্টের আগে "
                        + "\"the new account id is captured for later use\" স্টেপ চলেছে কিনা যাচাই করুন।");
        requestLoanPage.selectFromAccount(accountId);
    }

    /** সপ্তম রাউন্ডের ফিক্স: "john"-এর দূষিত অ্যাকাউন্ট এড়িয়ে সম্পূর্ণ ফ্রেশ, নতুন
     *  রেজিস্টার করা কাস্টমারের নিজস্ব অ্যাকাউন্ট ফান্ডিং সোর্স হিসেবে ব্যবহার করা হয়। */
    @And("user selects the newly registered customer's own account as funding account")
    public void user_selects_the_newly_registered_customers_own_account_as_funding_account() {
        setup();
        Assert.assertNotNull(freshCustomerAccountId,
                "FAILED: সদ্য রেজিস্টার্ড কাস্টমারের নিজস্ব অ্যাকাউন্ট ID ক্যাপচার করা হয়নি।");
        requestLoanPage.selectFromAccount(freshCustomerAccountId);
    }

    // ফিক্সড: পেজ ক্লাসের enterLoanDetails মেথডটি কল করা হয়েছে
    @When("user enters loan amount {string} and down payment {string}")
    public void user_enters_loan_amount_and_down_payment(String amount, String downPayment) {
        setup();
        requestLoanPage.enterLoanDetails(amount, downPayment);
    }

    @And("user clicks Apply Now")
    public void user_clicks_apply_now() {
        setup();
        requestLoanPage.clickApply();
    }

    @Then("loan request should be approved")
    public void loan_request_should_be_approved() {
        setup();
        boolean isApproved = requestLoanPage.isLoanApproved();
        Assert.assertTrue(isApproved, "FAILED: Loan request approved হয়নি। এটি Denied হতে পারে অথবা সার্ভার রেসপন্স দেয়নি।");
    }

    @Then("loan request should be denied")
    public void loan_request_should_be_denied() {
        setup();
        boolean isDenied = requestLoanPage.isLoanDenied();
        Assert.assertTrue(isDenied, "FAILED: কম down payment (<10%) দেওয়ার পরেও লোন Approved হয়ে গেছে (business rule ভঙ্গ)।");
    }

    /** $0 loan amount কেসে ParaBank denial না দেখিয়ে নিজের internal server error পেজ
     *  দেখায় (confirmed reproducible, 2026-07-08 এবং 2026-07-09 উভয় লাইভ রানে)। */
    @Then("the loan request should crash with an internal server error")
    public void the_loan_request_should_crash_with_an_internal_server_error() {
        setup();
        boolean crashed = requestLoanPage.isInternalServerErrorDisplayed();
        Assert.assertTrue(crashed,
                "FAILED: $0 loan amount সাবমিট করার পরেও প্রত্যাশিত internal server error পেজ দেখা যায়নি।");
    }
}

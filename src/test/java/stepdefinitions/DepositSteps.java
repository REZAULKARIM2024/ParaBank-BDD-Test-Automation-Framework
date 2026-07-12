package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.DepositPage;
import utils.DriverFactory;

/**
 * টেস্ট হাইজিন স্টেপ: শেয়ার্ড ডেমো অ্যাকাউন্টের ব্যালেন্স আগের রানের
 * negative/boundary টেস্টের কারণে কমে গেলে, এই স্টেপ ব্যবহার করে ব্যালেন্স
 * top-up করা হয় - যাতে পরবর্তী পজিটিভ টেস্ট (Request Loan ইত্যাদি)
 * "insufficient funds" নিয়ে ফ্লেকি না হয়।
 */
public class DepositSteps {

    private WebDriver driver;
    private DepositPage depositPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            depositPage = new DepositPage(driver);
        }
    }

    @When("user deposits {string} into account {string} to top up test balance")
    public void user_deposits_into_account(String amount, String account) {
        setup();
        depositPage.openDepositFunds();
        depositPage.depositToAccount(account, amount);
    }

    @Then("the top-up deposit should be successful")
    public void the_top_up_deposit_should_be_successful() {
        setup();
        Assert.assertTrue(depositPage.isDepositSuccessful(),
                "FAILED: টেস্ট ব্যালেন্স top-up করার জন্য deposit সফল হয়নি - পরবর্তী "
                        + "loan/transfer টেস্টগুলো insufficient funds এ ফেইল করতে পারে।");
    }
}

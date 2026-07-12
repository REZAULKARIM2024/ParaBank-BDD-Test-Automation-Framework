package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.FundTransferPage;
import utils.DriverFactory;

public class FundTransferSteps {

    WebDriver driver;
    FundTransferPage fundTransferPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            fundTransferPage = new FundTransferPage(driver);
        }
    }

    @When("user navigates to Fund Transfer page")
    public void navigate() {
        setup();
        fundTransferPage.openFundTransfer();
    }

    @And("user transfers amount {string} from account {string} to account {string}")
    public void performTransfer(String amount, String from, String to) {
        setup();
        fundTransferPage.transferFunds(from, to, amount);
    }

    @Then("transfer should be successful")
    public void verifyTransfer() {
        Assert.assertTrue(fundTransferPage.isTransferSuccessful(), "Fund transfer failed or success message not found!");
    }

    @Then("transfer should fail with a validation error")
    public void transfer_should_fail_with_validation_error() {
        Assert.assertTrue(fundTransferPage.isValidationErrorDisplayed(),
                "FAILED: ইনভ্যালিড ট্রান্সফার ডেটার জন্য কোনো এরর/ব্লক দেখানো হয়নি।");
        Assert.assertFalse(fundTransferPage.isTransferSuccessful(),
                "FAILED (defect): ইনভ্যালিড ডেটা দিয়েও ট্রান্সফার সম্পন্ন হয়ে গেছে!");
    }
}

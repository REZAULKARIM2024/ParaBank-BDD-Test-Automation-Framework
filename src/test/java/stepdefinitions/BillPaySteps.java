package stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.BillPayPage;
import utils.DriverFactory;

import java.util.Map;

public class BillPaySteps {

    WebDriver driver;
    BillPayPage billPayPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            billPayPage = new BillPayPage(driver);
        }
    }

    @When("user pays bill with following details")
    public void user_pays_bill(DataTable dataTable) {
        setup();
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        billPayPage.openBillPay();

        billPayPage.enterPayeeDetails(
                data.get("payeeName"),
                data.get("address"),
                data.get("city"),
                data.get("state"),
                data.get("zipCode"),
                data.get("phone"),
                data.get("account"),
                data.get("verifyAcc"),
                data.get("amount")
        );

        billPayPage.submitPayment();
    }

    @Then("bill payment should be successful")
    public void bill_payment_should_be_successful() {
        Assert.assertTrue(billPayPage.isPaymentSuccessful());
    }

    @Then("bill payment should fail with a validation error")
    public void bill_payment_should_fail_with_validation_error() {
        Assert.assertTrue(billPayPage.isValidationErrorDisplayed(),
                "FAILED: ইনভ্যালিড/অসম্পূর্ণ বিল পে ডেটার জন্য কোনো ভ্যালিডেশন এরর দেখানো হয়নি।");
        Assert.assertFalse(billPayPage.isPaymentSuccessful(),
                "FAILED (defect): অসম্পূর্ণ ডেটা দিয়েও পেমেন্ট সম্পন্ন হয়ে গেছে!");
    }
}

package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.CustomerLookupPage;
import utils.DriverFactory;

public class CustomerLookupSteps {

    WebDriver driver;
    CustomerLookupPage customerLookupPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            customerLookupPage = new CustomerLookupPage(driver);
        }
    }

    @When("user clicks the {string} link")
    public void user_clicks_the_link(String linkText) {
        setup();
        if ("Forgot login info?".equals(linkText)) {
            customerLookupPage.openFromLoginPage();
        } else {
            throw new UnsupportedOperationException("No step wiring for link text: " + linkText);
        }
    }

    @Then("the Customer Lookup page should be displayed")
    public void the_customer_lookup_page_should_be_displayed() {
        Assert.assertTrue(customerLookupPage.isLookupHeadingDisplayed(),
                "FAILED: Customer Lookup পেজের heading দেখা যায়নি।");
    }

    @Then("the lookup form should display all expected fields")
    public void the_lookup_form_should_display_all_expected_fields() {
        Assert.assertTrue(customerLookupPage.areAllExpectedFieldsDisplayed(),
                "FAILED: Customer Lookup ফর্মের সব প্রত্যাশিত ফিল্ড (First Name, Last Name, Address, "
                        + "City, State, Zip Code, SSN) দেখা যাচ্ছে না।");
    }

    @When("user submits the lookup form with clearly non-existent customer details")
    public void user_submits_the_lookup_form_with_clearly_non_existent_customer_details() {
        setup();
        customerLookupPage.fillLookupForm("Zzzznonexistent", "Zzzznonexistent", "000 Nowhere Street",
                "Nowhere", "ZZ", "00000", "000000000");
        customerLookupPage.submitLookupForm();
    }

    @When("user submits the lookup form with all fields empty")
    public void user_submits_the_lookup_form_with_all_fields_empty() {
        setup();
        customerLookupPage.fillLookupForm("", "", "", "", "", "", "");
        customerLookupPage.submitLookupForm();
    }

    @When("user submits the lookup form with an extremely long last name value")
    public void user_submits_the_lookup_form_with_an_extremely_long_last_name_value() {
        setup();
        String longLastName = "A".repeat(300);
        customerLookupPage.fillLookupForm("Rezaul", longLastName, "123 Street", "Dhaka", "DH", "1207", "123456789");
        customerLookupPage.submitLookupForm();
    }

    @Then("the lookup should not succeed")
    public void the_lookup_should_not_succeed() {
        Assert.assertTrue(customerLookupPage.isLookupUnsuccessful(),
                "FAILED: স্পষ্টতই ভুয়া/অস্তিত্বহীন কাস্টমার তথ্য দিয়েও lookup সফল হয়ে গেছে (ইউজার লগইন হয়ে গেছে)।");
    }

    @Then("the customer lookup page should not show an internal server error")
    public void the_customer_lookup_page_should_not_show_an_internal_server_error() {
        Assert.assertFalse(customerLookupPage.isInternalServerErrorDisplayed(),
                "FAILED: Customer Lookup পেজে একটি internal server error দেখা যাচ্ছে।");
    }

    // ২০২৬-০৭-১০ লাইভ রানে কনফার্ম হওয়া @knownIssue-এর জন্য: empty ফর্ম বা অতিরিক্ত
    // লম্বা Last Name সাবমিট করলে ParaBank সত্যিই "An internal error has occurred"
    // ক্র্যাশ পেজ দেখায় (গ্রেসফুল ভ্যালিডেশন হয় না) - তাই এখানে উল্টো (assertTrue)
    // চেক করা হচ্ছে, যাতে ParaBank এটা ঠিক করে ফেললে এই টেস্টটাই ব্যর্থ হয়ে সেটা
    // ধরিয়ে দেয় (দেখুন request_loan.feature-এর একই প্যাটার্ন)।
    @Then("the customer lookup page shows an internal server error")
    public void the_customer_lookup_page_shows_an_internal_server_error() {
        Assert.assertTrue(customerLookupPage.isInternalServerErrorDisplayed(),
                "ParaBank যদি এই defect ফিক্স করে ফেলে থাকে - এই টেস্ট ফেইল হওয়াই প্রত্যাশিত, "
                        + "এর মানে @knownIssue ট্যাগ সরিয়ে ফেলার সময় হয়েছে।");
    }
}

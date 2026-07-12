package stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.UpdateContactInfoPage;
import utils.DriverFactory;
import java.util.Map;

public class UpdateContactInfoSteps {

    WebDriver driver;
    UpdateContactInfoPage updateContactInfoPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            updateContactInfoPage = new UpdateContactInfoPage(driver);
        }
    }

    @When("user navigates to Update Contact Info page")
    public void user_navigates_to_update_contact_info_page() {
        setup();
        updateContactInfoPage.openUpdateContactInfoPage();
    }

    @When("user updates contact info with:")
    public void user_updates_contact_info_with(DataTable dataTable) {
        setup();
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        updateContactInfoPage.enterContactInfo(data);
    }

    // ২০২৬-০৭-১০ অনুসন্ধান: "Update user contact information successfully" scenario-টা
    // পরপর অন্তত ৩-৪টা লাইভ রানে একই "An internal error has occurred" ক্র্যাশ দিয়ে
    // ব্যর্থ হচ্ছে - অথচ একই শেয়ার্ড "john" অ্যাকাউন্ট দিয়ে চালানো অন্য (empty-field)
    // স্ক্রিপ্টগুলো ঠিকই কাজ করছে। সন্দেহজনক প্যাটার্ন: এই scenario প্রতিটা রানে
    // হুবহু একই হার্ডকোডেড ডেটা (John/Doe/123 St/...) সাবমিট করে - অর্থাৎ "john"-এর
    // প্রোফাইল আগের রান থেকেই এই একই ভ্যালুতে সেট হয়ে আছে, তাই এটি কার্যত একটি
    // no-op (কোনো প্রকৃত পরিবর্তন নেই এমন) আপডেট। এটাই সন্দেহভাজন ট্রিগার হতে পারে
    // (যেমন stale ORM/optimistic-locking বাগ)। এই হাইপোথিসিস যাচাই করতে "address"
    // ফিল্ডে প্রতি রানে একটি ইউনিক (timestamp-ভিত্তিক) সাফিক্স যোগ করা হচ্ছে, ঠিক
    // RegisterSteps-এর "unique username" কনভেনশনের মতো - পরবর্তী লাইভ রানে যদি এটি
    // ঠিক হয়ে যায়, তাহলে হাইপোথিসিসটাই কনফার্ম হবে।
    @When("user updates contact info with unique details:")
    public void user_updates_contact_info_with_unique_details(DataTable dataTable) {
        setup();
        Map<String, String> data = new java.util.HashMap<>(dataTable.asMap(String.class, String.class));
        String uniqueSuffix = " Apt " + java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999);
        data.put("address", data.getOrDefault("address", "") + uniqueSuffix);
        updateContactInfoPage.enterContactInfo(data);
    }

    @When("user clicks Update")
    public void user_clicks_update() {
        setup();
        updateContactInfoPage.clickUpdate();
    }

    @Then("contact info should be updated successfully")
    public void contact_info_should_be_updated_successfully() {
        // Assertion ফেইল করলে মেসেজ দেখাবে যা ডিবাগিং সহজ করবে
        boolean success = updateContactInfoPage.isUpdateSuccessful();
        Assert.assertTrue(success, "Update Profile success message was not displayed!");
    }

    @Then("contact info update should fail with a validation error")
    public void contact_info_update_should_fail() {
        Assert.assertTrue(updateContactInfoPage.isValidationErrorDisplayed(),
                "FAILED: ফাঁকা required ফিল্ডের জন্য কোনো ভ্যালিডেশন এরর দেখানো হয়নি।");
        Assert.assertFalse(updateContactInfoPage.isUpdateSuccessful(),
                "FAILED (defect): অসম্পূর্ণ তথ্য দিয়েও প্রোফাইল আপডেট হয়ে গেছে!");
    }

    /** Confirmed via a live run (2026-07-09): a very long address value crashes
     *  ParaBank with an unhandled internal server error instead of a graceful
     *  validation error or truncation. Documented as @knownIssue. */
    @Then("the profile update should crash with an internal server error")
    public void the_profile_update_should_crash_with_an_internal_server_error() {
        Assert.assertTrue(updateContactInfoPage.isInternalServerErrorDisplayed(),
                "FAILED: বড় সাইজের ইনপুটের জন্য প্রত্যাশিত internal server error page দেখা যায়নি।");
    }
}

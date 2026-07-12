package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.LogoutPage;
import utils.DriverFactory;

public class LogoutSteps {

    WebDriver driver;
    LogoutPage logoutPage;

    private void setup() {
        if (driver == null) {
            driver = DriverFactory.getDriver();
            logoutPage = new LogoutPage(driver);
        }
    }

    @When("user clicks logout")
    public void user_clicks_logout() {
        setup();
        logoutPage.clickLogout();
    }

    @Then("user should be logged out successfully")
    public void user_should_be_logged_out_successfully() {
        Assert.assertTrue(logoutPage.isLoggedOut());
    }

    @Then("navigating back after logout should not show a protected page")
    public void navigating_back_after_logout_should_not_show_protected_page() {
        Assert.assertTrue(logoutPage.isProtectedPageBlockedAfterBack(),
                "FAILED (Security defect): লগআউটের পর Back বাটনে protected page ক্যাশ থেকে দেখা যাচ্ছে।");
    }

    // ২০২৬-০৭-১০ কনফার্ম হওয়া @knownIssue-এর জন্য (bfcache defect - ParaBank
    // authenticated পেজে Cache-Control: no-store পাঠায় না): উল্টো (assertFalse)
    // চেক করা হচ্ছে, যাতে ParaBank এই bfcache defect ফিক্স করে ফেললে এই টেস্টটাই
    // ব্যর্থ হয়ে সেটা ধরিয়ে দেয়।
    @Then("navigating back after logout unexpectedly shows the protected page \\(known defect\\)")
    public void navigating_back_after_logout_unexpectedly_shows_protected_page() {
        Assert.assertFalse(logoutPage.isProtectedPageBlockedAfterBack(),
                "ParaBank যদি এই bfcache ডিফেক্ট ফিক্স করে ফেলে থাকে (Cache-Control: no-store যোগ করে) - "
                        + "এই টেস্ট ফেইল হওয়াই প্রত্যাশিত, এর মানে @knownIssue ট্যাগ সরিয়ে ফেলার সময় হয়েছে।");
    }

    @Then("the Logout link should no longer be visible")
    public void the_logout_link_should_no_longer_be_visible() {
        Assert.assertTrue(logoutPage.isLogoutLinkGone(),
                "FAILED: লগআউটের পরও ন্যাভিগেশন মেনুতে Logout লিংক এখনো দেখা যাচ্ছে।");
    }
}

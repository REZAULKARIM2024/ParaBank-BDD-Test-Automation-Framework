package performance;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import pages.BillPayPage;
import pages.FundTransferPage;
import pages.LoginPage;
import pages.RequestLoanPage;
import utils.DriverFactory;
import utils.PerformanceUtils;

/**
 * Performance Testing (client-side smoke level)
 * টেস্ট টাইপ কভারেজ: Performance Testing (basic)
 *
 * নোট: প্রকৃত Load/Stress/Spike/Endurance টেস্টের জন্য JMeter ব্যবহার করা হয়েছে
 * (দেখুন: performance/jmeter/ParaBank_Load_Test_Plan.jmx)। এই ক্লাসটি শুধু একজন
 * ইউজারের পেজ-লোড সময় একটি acceptable threshold-এর মধ্যে আছে কিনা তা যাচাই করে
 * (regression-এর সময় দ্রুত ধরার জন্য - CI friendly)।
 */
public class PerformanceSmokeTests {

    private static final long MAX_ACCEPTABLE_LOAD_TIME_MS = 8000;

    @Test(groups = {"performance", "smoke"})
    public void loginPage_shouldLoadWithinAcceptableThreshold() {
        WebDriver driver = DriverFactory.getDriver();
        long start = System.currentTimeMillis();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        long clientTiming = PerformanceUtils.getPageLoadTimeMillis(driver);
        long wallClock = System.currentTimeMillis() - start;

        System.out.println("[PERF] Login page - Navigation Timing API: " + clientTiming
                + "ms, Wall-clock: " + wallClock + "ms");

        Assert.assertTrue(wallClock < MAX_ACCEPTABLE_LOAD_TIME_MS,
                "FAILED: Login page load time (" + wallClock + "ms) exceeded threshold of "
                        + MAX_ACCEPTABLE_LOAD_TIME_MS + "ms.");
    }

    /** সব "logged-in page" টেস্টের জন্য শেয়ার্ড লগইন হেল্পার - কোড ডুপ্লিকেশন এড়াতে। */
    private void loginAsJohn(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterUsername("john");
        loginPage.enterPassword("demo");
        loginPage.clickLogin();
        Assert.assertTrue(loginPage.isLogoutDisplayed(), "Pre-condition failed: could not log in for performance check.");
    }

    private void assertLoadTimeWithinThreshold(long wallClock, String pageName) {
        Assert.assertTrue(wallClock < MAX_ACCEPTABLE_LOAD_TIME_MS,
                "FAILED: " + pageName + " page load time (" + wallClock + "ms) exceeded threshold of "
                        + MAX_ACCEPTABLE_LOAD_TIME_MS + "ms.");
    }

    @Test(groups = {"performance", "smoke"})
    public void registerPage_shouldLoadWithinAcceptableThreshold() {
        WebDriver driver = DriverFactory.getDriver();
        long start = System.currentTimeMillis();
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        long wallClock = System.currentTimeMillis() - start;
        System.out.println("[PERF] Register page - Wall-clock: " + wallClock + "ms");
        assertLoadTimeWithinThreshold(wallClock, "Register");
    }

    @Test(groups = {"performance", "regression"})
    public void accountsOverviewPage_shouldLoadWithinAcceptableThreshold() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        long start = System.currentTimeMillis();
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        long wallClock = System.currentTimeMillis() - start;
        System.out.println("[PERF] Accounts Overview page - Wall-clock: " + wallClock + "ms");
        assertLoadTimeWithinThreshold(wallClock, "Accounts Overview");
    }

    @Test(groups = {"performance", "regression"})
    public void fundTransferPage_shouldLoadWithinAcceptableThreshold() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        long start = System.currentTimeMillis();
        new FundTransferPage(driver).openFundTransfer();
        long wallClock = System.currentTimeMillis() - start;
        System.out.println("[PERF] Transfer Funds page - Wall-clock: " + wallClock + "ms");
        assertLoadTimeWithinThreshold(wallClock, "Transfer Funds");
    }

    @Test(groups = {"performance", "regression"})
    public void billPayPage_shouldLoadWithinAcceptableThreshold() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        long start = System.currentTimeMillis();
        new BillPayPage(driver).openBillPay();
        long wallClock = System.currentTimeMillis() - start;
        System.out.println("[PERF] Bill Pay page - Wall-clock: " + wallClock + "ms");
        assertLoadTimeWithinThreshold(wallClock, "Bill Pay");
    }

    @Test(groups = {"performance", "regression"})
    public void requestLoanPage_shouldLoadWithinAcceptableThreshold() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        long start = System.currentTimeMillis();
        new RequestLoanPage(driver).openRequestLoanPage();
        long wallClock = System.currentTimeMillis() - start;
        System.out.println("[PERF] Request Loan page - Wall-clock: " + wallClock + "ms");
        assertLoadTimeWithinThreshold(wallClock, "Request Loan");
    }

    @AfterClass
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}

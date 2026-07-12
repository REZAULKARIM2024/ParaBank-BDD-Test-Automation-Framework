package accessibility;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import pages.BillPayPage;
import pages.FundTransferPage;
import pages.LoginPage;
import pages.OpenNewAccountPage;
import pages.RequestLoanPage;
import utils.DriverFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Accessibility Testing (WCAG 2.2 AA) - axe-core ব্যবহার করে key page গুলোতে
 * critical/serious accessibility violation স্ক্যান করা হয়।
 * টেস্ট টাইপ কভারেজ: Accessibility Testing
 */
public class AccessibilityTests {

    @Test(groups = {"accessibility", "regression"})
    public void loginPage_shouldHaveNoCriticalAccessibilityViolations() {
        WebDriver driver = DriverFactory.getDriver();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        Results results = new AxeBuilder().analyze(driver);
        List<Rule> criticalViolations = results.getViolations().stream()
                .filter(v -> "critical".equalsIgnoreCase(v.getImpact()) || "serious".equalsIgnoreCase(v.getImpact()))
                .collect(Collectors.toList());

        if (!criticalViolations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Accessibility violations found on Login page:\n");
            criticalViolations.forEach(v -> sb.append(" - [").append(v.getImpact()).append("] ")
                    .append(v.getId()).append(": ").append(v.getDescription()).append("\n"));
            System.out.println(sb);
        }

        Assert.assertTrue(criticalViolations.isEmpty(),
                "FAILED: Login page-এ " + criticalViolations.size()
                        + "টি critical/serious accessibility violation পাওয়া গেছে (WCAG 2.2 AA)।");
    }

    @Test(groups = {"accessibility", "regression"})
    public void accountsOverviewPage_shouldHaveNoCriticalAccessibilityViolations() {
        WebDriver driver = DriverFactory.getDriver();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterUsername("john");
        loginPage.enterPassword("demo");
        loginPage.clickLogin();
        Assert.assertTrue(loginPage.isLogoutDisplayed(), "Pre-condition failed: could not log in for accessibility scan.");

        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        Results results = new AxeBuilder().analyze(driver);
        List<Rule> criticalViolations = results.getViolations().stream()
                .filter(v -> "critical".equalsIgnoreCase(v.getImpact()) || "serious".equalsIgnoreCase(v.getImpact()))
                .collect(Collectors.toList());

        Assert.assertTrue(criticalViolations.isEmpty(),
                "FAILED: Accounts Overview page-এ " + criticalViolations.size()
                        + "টি critical/serious accessibility violation পাওয়া গেছে (WCAG 2.2 AA)।");
    }

    /** সব "logged-in page" টেস্টের জন্য শেয়ার্ড লগইন হেল্পার - কোড ডুপ্লিকেশন এড়াতে। */
    private LoginPage loginAsJohn(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterUsername("john");
        loginPage.enterPassword("demo");
        loginPage.clickLogin();
        Assert.assertTrue(loginPage.isLogoutDisplayed(), "Pre-condition failed: could not log in for accessibility scan.");
        return loginPage;
    }

    private void assertNoCriticalViolations(WebDriver driver, String pageName) {
        Results results = new AxeBuilder().analyze(driver);
        List<Rule> criticalViolations = results.getViolations().stream()
                .filter(v -> "critical".equalsIgnoreCase(v.getImpact()) || "serious".equalsIgnoreCase(v.getImpact()))
                .collect(Collectors.toList());

        if (!criticalViolations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Accessibility violations found on " + pageName + " page:\n");
            criticalViolations.forEach(v -> sb.append(" - [").append(v.getImpact()).append("] ")
                    .append(v.getId()).append(": ").append(v.getDescription()).append("\n"));
            System.out.println(sb);
        }

        Assert.assertTrue(criticalViolations.isEmpty(),
                "FAILED: " + pageName + " page-এ " + criticalViolations.size()
                        + "টি critical/serious accessibility violation পাওয়া গেছে (WCAG 2.2 AA)।");
    }

    @Test(groups = {"accessibility", "regression"})
    public void registerPage_shouldHaveNoCriticalAccessibilityViolations() {
        WebDriver driver = DriverFactory.getDriver();
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        assertNoCriticalViolations(driver, "Register");
    }

    @Test(groups = {"accessibility", "regression"})
    public void fundTransferPage_shouldHaveNoCriticalAccessibilityViolations() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        new FundTransferPage(driver).openFundTransfer();
        assertNoCriticalViolations(driver, "Transfer Funds");
    }

    @Test(groups = {"accessibility", "regression"})
    public void billPayPage_shouldHaveNoCriticalAccessibilityViolations() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        new BillPayPage(driver).openBillPay();
        assertNoCriticalViolations(driver, "Bill Pay");
    }

    @Test(groups = {"accessibility", "regression"})
    public void requestLoanPage_shouldHaveNoCriticalAccessibilityViolations() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        new RequestLoanPage(driver).openRequestLoanPage();
        assertNoCriticalViolations(driver, "Request Loan");
    }

    @Test(groups = {"accessibility", "regression"})
    public void openNewAccountPage_shouldHaveNoCriticalAccessibilityViolations() {
        WebDriver driver = DriverFactory.getDriver();
        loginAsJohn(driver);
        new OpenNewAccountPage(driver).openOpenNewAccountPage();
        assertNoCriticalViolations(driver, "Open New Account");
    }

    @AfterClass
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}

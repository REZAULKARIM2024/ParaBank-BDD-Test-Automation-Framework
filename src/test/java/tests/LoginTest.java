package tests;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.DriverFactory;
import utils.ExcelUtils;

public class LoginTest {

    @DataProvider
    public Object[][] loginData() throws Exception {
        return ExcelUtils.getTestData("testdata/LoginData.xlsx", "Sheet1");
    }

    @Test(dataProvider = "loginData")
    public void loginTest(String username, String password) {
        WebDriver driver = DriverFactory.getDriver();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        LoginPage page = new LoginPage(driver);
        page.enterUsername(username);
        page.enterPassword(password);
        page.clickLogin();

        Assert.assertTrue(page.isLogoutDisplayed());
        DriverFactory.quitDriver();
    }
}

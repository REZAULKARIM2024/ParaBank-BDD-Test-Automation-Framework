package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * ParaBank-এর "Deposit Funds" পেজ - টেস্ট স্যুইটে এটি মূলত হাইজিন/সাপোর্ট পারপাসে
 * ব্যবহৃত হয়: শেয়ার্ড ডেমো অ্যাকাউন্টের ব্যালেন্স আগের রানের নেগেটিভ/বাউন্ডারি
 * টেস্টের কারণে কমে গেলে, পরবর্তী পজিটিভ টেস্ট (যেমন Request Loan) যাতে
 * "insufficient funds"-এ ফেইল না করে তার জন্য ব্যালেন্স top-up করা হয়। এটি ParaBank-এর
 * নিজস্ব সিমুলেটেড ডেমো ফিচার - বাস্তব কোনো আর্থিক লেনদেন নয়।
 */
public class DepositPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By depositFundsLink = By.linkText("Deposit Funds");
    private final By amountField = By.id("amount");
    private final By accountDropdown = By.id("accountId");
    private final By depositBtn = By.xpath("//input[@value='Deposit']");
    private final By resultPanel = By.id("depositResult");

    public DepositPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public void openDepositFunds() {
        wait.until(ExpectedConditions.elementToBeClickable(depositFundsLink)).click();
        wait.until(ExpectedConditions.urlContains("depositfunds.htm"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(amountField));
    }

    public void depositToAccount(String account, String amount) {
        WebElement amtInput = wait.until(ExpectedConditions.visibilityOfElementLocated(amountField));
        amtInput.clear();
        if (amount != null && !amount.isEmpty()) {
            amtInput.sendKeys(amount);
        }

        // AJAX ড্রপডাউন সিঙ্ক্রোনাইজেশন: অন্তত একটি অপশন আসা পর্যন্ত অপেক্ষা
        wait.until(d -> new Select(d.findElement(accountDropdown)).getOptions().size() > 0);
        try {
            new Select(driver.findElement(accountDropdown)).selectByVisibleText(account);
        } catch (Exception e) {
            new Select(driver.findElement(accountDropdown)).selectByIndex(0);
        }

        wait.until(ExpectedConditions.elementToBeClickable(depositBtn)).click();
    }

    public boolean isDepositSuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(resultPanel));
            String text = driver.findElement(resultPanel).getText().toLowerCase();
            return text.contains("deposit");
        } catch (Exception e) {
            // ফলব্যাক: রেজাল্ট প্যানেলের এক্সাক্ট স্ট্রাকচার ভিন্ন হলে, পুরো বডি টেক্সটে
            // "deposited" শব্দ খোঁজা হয়
            try {
                return driver.findElement(By.tagName("body")).getText().toLowerCase().contains("deposited");
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}

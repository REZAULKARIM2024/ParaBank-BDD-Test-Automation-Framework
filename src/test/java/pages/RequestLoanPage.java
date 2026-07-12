package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.stream.Collectors;

public class RequestLoanPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By requestLoanLink = By.linkText("Request Loan");
    private final By loanAmountInput = By.id("amount");
    private final By downPaymentInput = By.id("downPayment");
    // ডাউন পেমেন্ট কোন অ্যাকাউন্ট থেকে নেওয়া হবে তা এই ড্রপডাউন থেকে সিলেক্ট করা হয়।
    // আগে এটি টাচ করা হতো না, ফলে সাইটে যত বেশি অ্যাকাউন্ট (Open New Account থেকে) তৈরি
    // হতো, ততই ব্রাউজার ডিফল্ট-সিলেক্টেড অ্যাকাউন্ট বদলে যেত - এবং আমাদের টেস্ট ব্যালেন্স
    // top-up account #12345-এ করলেও, লোন রিকোয়েস্ট আসলে অন্য কোনো (কম ব্যালেন্সের)
    // অ্যাকাউন্ট থেকে টাকা তোলার চেষ্টা করত। এখন সবসময় এক্সপ্লিসিটলি অ্যাকাউন্ট সিলেক্ট
    // করা হয়, যাতে top-up করা ব্যালেন্স আসলেই ব্যবহার হয়।
    private final By fromAccountDropdown = By.id("fromAccountId");
    private final By applyButton = By.xpath("//input[@value='Apply Now']");
    // সাকসেস মেসেজ অনেক সময় dynamic হয়, তাই contains ব্যবহার করা নিরাপদ
    private final By approvalMessage = By.xpath("//div[@id='requestLoanResult']//h1[contains(text(),'Loan Request Processed')]");
    private final By statusText = By.id("loanStatus");

    public RequestLoanPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public void openRequestLoanPage() {
        wait.until(ExpectedConditions.elementToBeClickable(requestLoanLink)).click();
        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(loanAmountInput));
    }

    private void typeData(By locator, String data) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.clear();
        // অনেক সময় value clear হয় না, তাই সরাসরি কি-বোর্ড অ্যাকশন ব্যবহার করা ভালো
        String current = element.getAttribute("value");
        if (current != null && !current.isEmpty()) {
            element.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
            element.sendKeys(org.openqa.selenium.Keys.BACK_SPACE);
        }
        if (data != null && !data.isEmpty()) {
            element.sendKeys(data);
        }
    }

    public void enterLoanDetails(String amount, String downPayment) {
        typeData(loanAmountInput, amount);
        typeData(downPaymentInput, downPayment);
    }

    /** ডাউন পেমেন্টের জন্য ফান্ডিং অ্যাকাউন্ট এক্সপ্লিসিটলি সিলেক্ট করা হয়, যাতে
     *  ব্রাউজারের ডিফল্ট সিলেকশনের উপর নির্ভর করতে না হয় (একাধিক অ্যাকাউন্ট থাকলে
     *  ডিফল্ট বদলে যেতে পারে)। */
    public void selectFromAccount(String account) {
        wait.until(d -> new Select(d.findElement(fromAccountDropdown)).getOptions().size() > 0);
        Select select = new Select(driver.findElement(fromAccountDropdown));
        try {
            select.selectByVisibleText(account);
            System.out.println("[DIAG] Request Loan fromAccountId dropdown: selected \"" + account
                    + "\" successfully. Options were: " + select.getOptions().stream()
                    .map(WebElement::getText).collect(Collectors.toList()));
        } catch (Exception e) {
            // এই fallback আগে চুপচাপ ঘটত (silent) - এখন লগ করা হচ্ছে, কারণ এটাই সন্দেহভাজন
            // root cause: যদি ড্রপডাউনে "account"-এর ভিজিবল টেক্সট হুবহু না মেলে (যেমন
            // balance/নাম যোগ করা থাকতে পারে), তাহলে ভুল অ্যাকাউন্ট (index 0) সিলেক্ট হয়ে
            // যাচ্ছে - যেটা top-up করা ব্যালেন্স ব্যবহার না করার আসল কারণ হতে পারে।
            System.out.println("[DIAG] WARNING: could not find \"" + account + "\" as exact visible "
                    + "text in Request Loan fromAccountId dropdown. Available options were: "
                    + select.getOptions().stream().map(WebElement::getText).collect(Collectors.toList())
                    + " - falling back to index 0, which may NOT be the intended account.");
            select.selectByIndex(0);
        }
    }

    public void clickApply() {
        wait.until(ExpectedConditions.elementToBeClickable(applyButton)).click();
    }

    public boolean isLoanApproved() {
        try {
            // লোন প্রসেস হতে ৫-৭ সেকেন্ড সময় নিতে পারে
            wait.until(ExpectedConditions.visibilityOfElementLocated(approvalMessage));
            String status = driver.findElement(statusText).getText().trim();
            return status.equalsIgnoreCase("Approved");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoanDenied() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(approvalMessage));
            String status = driver.findElement(statusText).getText().trim();
            return status.equalsIgnoreCase("Denied");
        } catch (Exception e) {
            return false;
        }
    }

    // $0 loan amount কনফার্মড (2026-07-08, পুনরায় 2026-07-09): ParaBank গ্রেসফুলি Denied
    // দেখায় না - বরং নিজের "An internal error has occurred and has been logged." পেজ
    // দেখায় (unhandled server-side exception)। isLoanDenied() approvalMessage/statusText
    // এলিমেন্ট খোঁজে যা এই ক্র্যাশ কেসে কখনো আসে না, তাই এই আলাদা চেক যোগ করা হলো
    // (UpdateContactInfoPage.isInternalServerErrorDisplayed()-এর একই প্যাটার্নে)।
    private final By internalErrorText = By.xpath(
            "//*[contains(text(),'An internal error has occurred') or contains(text(),'Error!')]");

    public boolean isInternalServerErrorDisplayed() {
        try {
            return !driver.findElements(internalErrorText).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}

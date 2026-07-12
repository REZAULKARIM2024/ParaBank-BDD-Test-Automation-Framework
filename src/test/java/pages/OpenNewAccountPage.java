package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class OpenNewAccountPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By openAccountLink = By.linkText("Open New Account");
    private final By accountTypeDropdown = By.id("type");
    private final By existingAccountDropdown = By.id("fromAccountId");
    private final By openAccountButton = By.xpath("//input[@value='Open New Account']");
    private final By newAccountMessage = By.xpath("//h1[text()='Account Opened!']");

    public OpenNewAccountPage(WebDriver driver) {
        this.driver = driver;
        // "john"-এর অ্যাকাউন্ট লিস্ট এই সেশনে ৬০+ এ পৌঁছানোর পর একটি লাইভ রানে
        // (2026-07-09) existingAccountDropdown পপুলেট হতে 20 সেকেন্ডের বেশি সময়
        // লেগেছে (শেয়ার্ড ডেমো সার্ভারের স্লোনেস) - ৪৫ সেকেন্ডে বাড়ানো হলো।
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
    }

    public void openOpenNewAccountPage() {
        wait.until(ExpectedConditions.elementToBeClickable(openAccountLink)).click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
    }

    public void selectAccountTypeAndExistingAccount(String accountType, String existingAccount) {
        // ১. একাউন্ট টাইপ সিলেক্ট (লুপ ব্যবহার করে কেস-সেনসিটিভিটি হ্যান্ডেল করা হয়েছে)
        WebElement typeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(accountTypeDropdown));
        Select typeSelect = new Select(typeElement);
        
        boolean found = false;
        for (WebElement option : typeSelect.getOptions()) {
            if (option.getText().equalsIgnoreCase(accountType)) {
                typeSelect.selectByVisibleText(option.getText());
                found = true;
                break;
            }
        }
        // যদি টেক্সট না মেলে, তবে ইনডেক্স দিয়ে সিলেক্ট করার ব্যাকআপ লজিক
        if(!found) typeSelect.selectByIndex(accountType.equalsIgnoreCase("SAVINGS") ? 1 : 0);

        // ২. ড্রপডাউন অপশন লোড হওয়ার জন্য অপেক্ষা (Parabank AJAX Fix)
        wait.until(d -> new Select(d.findElement(existingAccountDropdown)).getOptions().size() > 0);

        // ৩. এক্সিস্টিং একাউন্ট সিলেক্ট
        Select existingSelect = new Select(driver.findElement(existingAccountDropdown));
        try {
            existingSelect.selectByVisibleText(existingAccount);
        } catch (Exception e) {
            // যদি নির্দিষ্ট একাউন্ট না পায়, প্রথম একাউন্টটি সিলেক্ট করবে যাতে টেস্ট ক্রাশ না করে
            existingSelect.selectByIndex(0);
        }
    }

    public void clickOpenNewAccount() {
        // বাটন এনাবেল হওয়ার জন্য সামান্য হার্ড ওয়েট (AngularJS অ্যাপের জন্য কার্যকর)
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        wait.until(ExpectedConditions.elementToBeClickable(openAccountButton)).click();
    }

    public boolean isAccountCreated() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(newAccountMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** নতুন তৈরি হওয়া অ্যাকাউন্টের ID রিটার্ন করে - এটি ব্যবহার করা হয় যখন আমাদের একটি
     *  গ্যারান্টিড-ফ্রেশ, ভালো ব্যালেন্সের অ্যাকাউন্ট দরকার হয় (যেমন loan টেস্টের জন্য),
     *  কারণ শেয়ার্ড ডেমো অ্যাকাউন্ট #12345/#54321 আগের ভুল টেস্ট রানের কারণে স্থায়ীভাবে
     *  বিলিয়ন ডলার নেগেটিভ ব্যালেন্সে চলে গেছে (confirmed via diagnostics) - top-up করার
     *  চেষ্টা করা আর বাস্তবসম্মত না। */
    public String getNewAccountId() {
        try {
            WebElement idLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newAccountId")));
            return idLink.getText().trim();
        } catch (Exception e) {
            // ফলব্যাক: id="newAccountId" না থাকলে confirmation প্যানেলের পুরো টেক্সট থেকে
            // প্রথম নাম্বার বের করার চেষ্টা করা হয়
            try {
                String panelText = driver.findElement(By.xpath("//*[contains(text(),'Account Opened')]/..")).getText();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{4,}").matcher(panelText);
                if (m.find()) {
                    return m.group();
                }
            } catch (Exception ignored) { }
            return null;
        }
    }
}
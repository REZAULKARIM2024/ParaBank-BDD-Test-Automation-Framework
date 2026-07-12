package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class AccountsOverviewPage {
    WebDriver driver;
    WebDriverWait wait;

    private final By accountsOverviewLink = By.linkText("Accounts Overview");
    private final By accountTable = By.id("accountTable");
    // ব্যালেন্স বা অন্তত একটি ডেটা সেল উপস্থিত হওয়া পর্যন্ত অপেক্ষা করার জন্য
    private final By tableData = By.xpath("//table[@id='accountTable']//td");

    public AccountsOverviewPage(WebDriver driver) {
        this.driver = driver;
        // এই সেশনে "john"-এর অ্যাকাউন্ট লিস্ট ৬০+ এ পৌঁছেছে (এই পুরো সেশন জুড়ে তৈরি
        // হওয়া টেস্ট অ্যাকাউন্টগুলোর কারণে), এবং একটি লাইভ রানে (2026-07-09) দেখা
        // গেছে অন্য বিভিন্ন পেজেও (Open New Account, Fund Transfer) ড্রপডাউন/টেবিল
        // পপুলেট হতে সময় বেশি লাগছে - সম্ভবত শেয়ার্ড ডেমো সার্ভারের সাধারণ স্লোনেস।
        // ওয়েট টাইম ২০ থেকে ৪৫ সেকেন্ডে বাড়ানো হলো এই ফ্লেকিনেস কমানোর জন্য।
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
    }

    public void openAccountsOverviewPage() {
        wait.until(ExpectedConditions.elementToBeClickable(accountsOverviewLink)).click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }

    public boolean isAccountsListDisplayed() {
        try {
            // টেবিল দৃশ্যমান হওয়া পর্যন্ত অপেক্ষা
            wait.until(ExpectedConditions.visibilityOfElementLocated(accountTable));
            // অন্তত একটি ডেটা সেল (Data Cell) লোড হওয়া পর্যন্ত অপেক্ষা (Thread.sleep এর পরিবর্তে)
            wait.until(ExpectedConditions.presenceOfElementLocated(tableData));
            
            List<WebElement> rows = driver.findElements(By.xpath("//table[@id='accountTable']/tbody/tr"));
            // Total ব্যালেন্সের রো বাদ দিয়ে অন্তত ১টি অ্যাকাউন্ট রো থাকতে হবে
            return rows.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** ডায়াগনস্টিক-ওনলি হেল্পার: প্রতিটি অ্যাকাউন্ট রো-এর raw টেক্সট রিটার্ন করে
     *  (account id, balance, available amount ইত্যাদি) - কোনো অ্যাসারশন নেই, শুধু
     *  লগ করার জন্য ব্যবহার করা হয় যাতে ব্যালেন্স-সম্পর্কিত ফেইলিওর ডিবাগ করা সহজ হয়। */
    public java.util.List<String> getAccountRowsText() {
        java.util.List<String> result = new java.util.ArrayList<>();
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(accountTable));
            wait.until(ExpectedConditions.presenceOfElementLocated(tableData));
            List<WebElement> rows = driver.findElements(By.xpath("//table[@id='accountTable']/tbody/tr"));
            for (WebElement row : rows) {
                result.add(row.getText().replaceAll("\\s+", " ").trim());
            }
        } catch (Exception e) {
            result.add("(could not read accounts table: " + e.getMessage() + ")");
        }
        return result;
    }

    /** ডাটা-ইন্টিগ্রিটি টেস্ট: টেবিলে অন্তত minCount সংখ্যক রো (Total রো সহ) আছে কিনা।
     *  এই ডেমো ইউজারের একাধিক অ্যাকাউন্ট থাকা উচিত (আগের রান থেকে তৈরি হওয়া অনেক
     *  অ্যাকাউন্ট সহ), তাই এটি Accounts Overview পেজ সত্যিই একাধিক অ্যাকাউন্ট
     *  লিস্ট করছে কিনা যাচাই করে। */
    public boolean hasAtLeastAccountRows(int minCount) {
        return getAccountRowsText().size() >= minCount;
    }

    /** একটি সদ্য নিবন্ধিত (freshly registered) কাস্টমারের নিজস্ব (একমাত্র) অ্যাকাউন্ট ID
     *  বের করে আনে - রো টেক্সটের প্রথম টোকেনটিই সাধারণত অ্যাকাউন্ট ID। "Total" রো
     *  বাদ দেওয়া হয়। Request Loan-এর মতো ফিচারে ব্যবহৃত হয় যেখানে "john"-এর
     *  পুরনো, নষ্ট হয়ে যাওয়া অ্যাকাউন্টের বদলে একটি সম্পূর্ণ ফ্রেশ কাস্টমারের
     *  নিজস্ব, দূষণ-মুক্ত অ্যাকাউন্ট ফান্ডিং সোর্স হিসেবে ব্যবহার করা হয়। */
    public String getFirstAccountId() {
        for (String row : getAccountRowsText()) {
            if (row.startsWith("(could not read") || row.toLowerCase().startsWith("total")) {
                continue;
            }
            String[] parts = row.split("\\s+");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                return parts[0];
            }
        }
        return null;
    }

    /** ডাটা-ফরম্যাট টেস্ট: প্রতিটি (non-error) রো-তে অন্তত একটি বৈধ কারেন্সি ফরম্যাটেড
     *  ভ্যালু ($১২৩.৪৫ বা -$১২৩.৪৫ প্যাটার্ন) আছে কিনা যাচাই করে। */
    public boolean areAllRowsValidCurrencyFormatted() {
        java.util.regex.Pattern currencyPattern = java.util.regex.Pattern.compile("-?\\$[\\d,]+\\.\\d{2}");
        java.util.List<String> rows = getAccountRowsText();
        if (rows.isEmpty()) {
            return false;
        }
        for (String row : rows) {
            if (row.startsWith("(could not read")) {
                return false;
            }
            if (!currencyPattern.matcher(row).find()) {
                return false;
            }
        }
        return true;
    }
}
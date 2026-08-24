package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Browser-এর Navigation Timing API ব্যবহার করে client-side page load time বের করার utility।
 * Wall-clock (System.currentTimeMillis) টাইমিং-এ Selenium command overhead যোগ হয়ে যায়;
 * এটা তার বদলে ব্রাউজার নিজে যা মেপেছে সেটা রিপোর্ট করে।
 */
public class PerformanceUtils {

    /**
     * বর্তমান পেজের load time (মিলিসেকেন্ডে) রিটার্ন করে, modern Navigation Timing Level 2 API
     * (performance.getEntriesByType("navigation")) ব্যবহার করে। এই API না থাকলে (পুরনো ব্রাউজার)
     * legacy performance.timing API-তে fallback করে।
     */
    public static long getPageLoadTimeMillis(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Object result = js.executeScript(
            "var nav = performance.getEntriesByType('navigation')[0];" +
            "if (nav) { return Math.round(nav.duration); }" +
            "if (performance.timing && performance.timing.loadEventEnd > 0) {" +
            "  return performance.timing.loadEventEnd - performance.timing.navigationStart;" +
            "}" +
            "return -1;"
        );

        if (result instanceof Number) {
            return ((Number) result).longValue();
        }
        return -1;
    }
}
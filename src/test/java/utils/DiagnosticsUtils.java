package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Captures lightweight diagnostic context (current URL, page title, ready-state,
 * and any visible error/alert text) whenever a scenario fails.
 *
 * This exists because a failed assertion like "Logout link is not visible" only
 * tells you WHAT didn't happen, not WHY. Especially against a shared public demo
 * server (parabank.parasoft.com), failures are often caused by the site itself
 * (maintenance banner, "Internal Error" page, slow/incomplete page load) rather
 * than a defect in the test or the locators. Capturing this text alongside the
 * screenshot makes that distinction obvious at a glance in the Cucumber report,
 * without having to re-run the scenario with a debugger attached.
 */
public class DiagnosticsUtils {

    // Common places ParaBank surfaces error/validation text.
    private static final By[] ERROR_LOCATORS = {
        By.cssSelector("span.error, .error"),
        By.xpath("//div[@id='rightPanel']//p"),
        By.xpath("//*[contains(text(),'Error') or contains(text(),'error')]"),
        By.xpath("//*[contains(text(),'not available') or contains(text(),'maintenance')]"),
    };

    public static String captureDiagnostics(WebDriver driver) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Failure Diagnostics ===\n");

        try {
            sb.append("Current URL   : ").append(driver.getCurrentUrl()).append("\n");
        } catch (Exception e) {
            sb.append("Current URL   : <unavailable: ").append(e.getMessage()).append(">\n");
        }

        try {
            sb.append("Page Title    : ").append(driver.getTitle()).append("\n");
        } catch (Exception e) {
            sb.append("Page Title    : <unavailable: ").append(e.getMessage()).append(">\n");
        }

        try {
            Object readyState = ((JavascriptExecutor) driver).executeScript("return document.readyState");
            sb.append("Document State: ").append(readyState).append("\n");
        } catch (Exception e) {
            sb.append("Document State: <unavailable>\n");
        }

        String visibleErrorText = findVisibleErrorText(driver);
        sb.append("Visible Error/Status Text:\n")
          .append(visibleErrorText.isEmpty() ? "  (none found on page)\n" : visibleErrorText);

        return sb.toString();
    }

    private static String findVisibleErrorText(WebDriver driver) {
        StringBuilder found = new StringBuilder();
        for (By locator : ERROR_LOCATORS) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                String text = elements.stream()
                        .map(WebElement::getText)
                        .filter(t -> t != null && !t.trim().isEmpty())
                        .distinct()
                        .limit(5)
                        .collect(Collectors.joining("\n  - ", "  - ", ""));
                if (!text.trim().equals("-")) {
                    found.append(text).append("\n");
                }
            } catch (Exception ignored) {
                // locator not applicable on this page - skip
            }
        }
        return found.toString();
    }
}

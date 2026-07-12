package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * ParaBank REST API - Login সংক্রান্ত টেস্ট
 * টেস্ট টাইপ কভারেজ: API Testing, Security Testing (Authentication)
 */
public class LoginApiTests extends ApiTestBase {

    @Test(groups = {"api", "smoke", "positive"})
    public void login_withValidCredentials_positive() {
        Response response = given(requestSpec)
                .pathParam("username", prop("valid.username"))
                .pathParam("password", prop("valid.password"))
                .when()
                .get("/login/{username}/{password}");

        Assert.assertEquals(response.getStatusCode(), 200,
                "FAILED: বৈধ credential দিয়েও login API সফল status ফেরত দেয়নি।");
    }

    @Test(groups = {"api", "regression", "negative", "security"})
    public void login_withInvalidCredentials_negative() {
        Response response = given(requestSpec)
                .pathParam("username", prop("valid.username"))
                .pathParam("password", "wrongPassword!!")
                .when()
                .get("/login/{username}/{password}");

        String body = response.getBody().asString();
        boolean looksAuthenticated = response.getStatusCode() == 200
                && body != null
                && body.toLowerCase().contains("\"id\"");

        Assert.assertFalse(looksAuthenticated,
                "FAILED (Security defect): ভুল পাসওয়ার্ড দিয়েও login API একটি ভ্যালিড customer object ফেরত দিয়েছে।");
    }

    @Test(groups = {"api", "regression", "negative", "security"})
    public void login_withSqlInjectionAttempt_negative() {
        Response response = given(requestSpec)
                .pathParam("username", "john")
                .pathParam("password", "' OR '1'='1")
                .when()
                .get("/login/{username}/{password}");

        String body = response.getBody().asString();
        boolean looksAuthenticated = response.getStatusCode() == 200
                && body != null
                && body.toLowerCase().contains("\"id\"");

        Assert.assertFalse(looksAuthenticated,
                "FAILED (Security defect): SQL injection payload দিয়ে authentication bypass সম্ভব হয়েছে!");
    }

    @Test(groups = {"api", "security", "regression"})
    public void response_shouldNotExposeSensitiveHeaders() {
        Response response = given(requestSpec)
                .pathParam("username", prop("valid.username"))
                .pathParam("password", prop("valid.password"))
                .when()
                .get("/login/{username}/{password}");

        // OWASP-সংক্রান্ত বেসিক চেক: সার্ভার হেডারে ভার্সন তথ্য ফাঁস হচ্ছে কিনা তা নোট করা হয় (informational)
        String serverHeader = response.getHeader("Server");
        if (serverHeader != null) {
            System.out.println("[SECURITY-INFO] Server header exposed: " + serverHeader
                    + " -- consider suppressing detailed version info in production.");
        }
    }
}

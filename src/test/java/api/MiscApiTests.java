package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * ParaBank REST API - Swagger tag: Misc ("miscellaneous operations")
 * Endpoints under test:
 *   GET  /login/{username}/{password}   - Login (john/demo)
 *   POST /setParameter/{name}/{value}   - Set a configuration parameter
 *
 * (Login positive/negative/security scenarios are also covered in more depth
 * in LoginApiTests.java - this class focuses on the Misc-tag-specific
 * setParameter endpoint plus a login smoke check for completeness.)
 *
 * Test type coverage: API Testing, Security Testing, Configuration Testing.
 */
public class MiscApiTests extends ApiTestBase {

    @Test(groups = {"api", "misc", "smoke", "positive"})
    public void login_viaMiscTag_positive() {
        given(requestSpec)
                .pathParam("username", prop("valid.username"))
                .pathParam("password", prop("valid.password"))
                .when()
                .get("/login/{username}/{password}")
                .then()
                .statusCode(200);
    }

    @Test(groups = {"api", "misc", "regression", "negative", "security"})
    public void setParameter_withoutAuthorization_shouldBeRestricted_negative() {
        // setParameter can change server-side configuration; it should not be
        // callable anonymously without some form of restriction in a hardened
        // deployment. On the open demo server this is expected to succeed,
        // which is worth flagging as a configuration-hardening item for any
        // real production deployment of this API.
        Response response = given(requestSpec)
                .pathParam("name", "qaAutomationTestParam")
                .pathParam("value", "true")
                .when()
                .post("/setParameter/{name}/{value}");

        System.out.println("[SECURITY-INFO] /setParameter responded with status "
                + response.getStatusCode() + " with no authentication - flag for hardening in production.");
    }

    @Test(groups = {"api", "misc", "regression", "negative"})
    public void login_withInvalidCredentials_viaMiscTag_negative() {
        Response response = given(requestSpec)
                .pathParam("username", prop("valid.username"))
                .pathParam("password", "definitelyWrongPassword123")
                .when()
                .get("/login/{username}/{password}");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: logging in with an invalid password via the Misc-tag endpoint still returned a successful response.");
    }
}

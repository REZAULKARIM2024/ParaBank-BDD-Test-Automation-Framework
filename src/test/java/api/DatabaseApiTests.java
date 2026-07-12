package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * ParaBank REST API - Swagger tag: Database ("data management operations")
 * Endpoints under test:
 *   POST /cleanDB        - Reset database contents to a minimal state
 *   POST /initializeDB   - Reset database contents to a populated state
 *
 * ******************************************************************************
 * WARNING: These endpoints wipe/reseed ALL data on the target ParaBank instance.
 * Against the shared public demo (parabank.parasoft.com) this would affect every
 * other user's accounts and transactions at the same time. These tests are
 * therefore SKIPPED unless destructive.tests.enabled=true is explicitly set
 * (config/api.properties or -Ddestructive.tests.enabled=true), and even then
 * should only ever be pointed at a local/private/disposable ParaBank instance.
 * ******************************************************************************
 *
 * Test type coverage: API Testing, Backend/Data Management Testing, Reliability
 * Testing (verifying the app returns to a known-good state).
 */
public class DatabaseApiTests extends ApiTestBase {

    private void skipUnlessDestructiveEnabled() {
        if (!destructiveTestsEnabled()) {
            throw new SkipException("Skipped: Database endpoints are destructive on a shared server. "
                    + "Set destructive.tests.enabled=true against a local/private ParaBank instance to run this test.");
        }
    }

    @Test(groups = {"api", "database", "destructive"})
    public void initializeDB_resetsToPopulatedState() {
        skipUnlessDestructiveEnabled();

        Response response = given(requestSpec)
                .when()
                .post("/initializeDB");

        Assert.assertEquals(response.getStatusCode(), 200,
                "FAILED: /initializeDB did not return a successful status code.");
    }

    @Test(groups = {"api", "database", "destructive"})
    public void cleanDB_resetsToMinimalState() {
        skipUnlessDestructiveEnabled();

        Response response = given(requestSpec)
                .when()
                .post("/cleanDB");

        Assert.assertEquals(response.getStatusCode(), 200,
                "FAILED: /cleanDB did not return a successful status code.");
    }
}

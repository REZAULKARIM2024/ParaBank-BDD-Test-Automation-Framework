package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * ParaBank REST API - Swagger tag: JMS ("message service operations")
 * Endpoints under test:
 *   POST /shutdownJmsListener - Disable the JMS message listener
 *   POST /startupJmsListener  - Enable the JMS message listener
 *
 * ParaBank processes loan requests asynchronously through a JMS queue
 * (see the Loans tag / /requestLoan). Disabling the listener here would break
 * loan-processing for every other user of a shared server, so - like the
 * Database endpoints - these are SKIPPED unless explicitly opted in.
 *
 * Test type coverage: API Testing, Integration Testing (async messaging),
 * Reliability Testing.
 */
public class JmsApiTests extends ApiTestBase {

    private void skipUnlessDestructiveEnabled() {
        if (!destructiveTestsEnabled()) {
            throw new SkipException("Skipped: JMS listener endpoints affect asynchronous loan processing "
                    + "for the whole shared server. Set destructive.tests.enabled=true against a "
                    + "local/private ParaBank instance to run this test.");
        }
    }

    @Test(groups = {"api", "jms", "destructive"})
    public void startupJmsListener_enablesListener() {
        skipUnlessDestructiveEnabled();

        Response response = given(requestSpec)
                .when()
                .post("/startupJmsListener");

        Assert.assertEquals(response.getStatusCode(), 200,
                "FAILED: /startupJmsListener did not return a successful status code.");
    }

    @Test(groups = {"api", "jms", "destructive"}, dependsOnMethods = "startupJmsListener_enablesListener")
    public void shutdownJmsListener_disablesListener_thenRestart() {
        skipUnlessDestructiveEnabled();

        Response shutdownResponse = given(requestSpec)
                .when()
                .post("/shutdownJmsListener");

        Assert.assertEquals(shutdownResponse.getStatusCode(), 200,
                "FAILED: /shutdownJmsListener did not return a successful status code.");

        // Always restore the listener so subsequent loan-request tests keep working.
        given(requestSpec).when().post("/startupJmsListener");
    }

    @Test(groups = {"api", "jms", "regression", "negative"})
    public void loanRequest_stillRespondsWhenListenerStateUnknown_negative() {
        // Non-destructive sanity check: even without toggling the listener ourselves,
        // a loan request should never hang indefinitely or return a 5xx due to
        // JMS being unavailable - it should degrade gracefully.
        Response response = given(requestSpec)
                .queryParam("customerId", prop("valid.customer.id"))
                .queryParam("amount", prop("loan.amount.approved"))
                .queryParam("downPayment", prop("loan.downpayment.approved"))
                .queryParam("fromAccountId", prop("valid.account.id"))
                .when()
                .post("/requestLoan");

        Assert.assertTrue(response.getStatusCode() < 500,
                "FAILED: loan request returned a server error, possibly due to JMS listener being down.");
    }
}

package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ParaBank REST API - Swagger tag: Loans ("request loan operations")
 * Endpoint under test:
 *   POST /requestLoan?customerId=&amount=&downPayment=&fromAccountId=
 *
 * Business rule under test: a loan is Approved when the down payment is at
 * least ~10% of the requested amount, otherwise Denied.
 *
 * Test type coverage: API Testing, Backend Testing, Boundary Value Analysis,
 * Business-rule / Negative Testing.
 */
public class LoansApiTests extends ApiTestBase {

    @Test(groups = {"api", "loans", "smoke", "positive"})
    public void requestLoan_withSufficientDownPayment_isApproved() {
        given(requestSpec)
                .queryParam("customerId", prop("valid.customer.id"))
                .queryParam("amount", prop("loan.amount.approved"))
                .queryParam("downPayment", prop("loan.downpayment.approved"))
                .queryParam("fromAccountId", prop("valid.account.id"))
                .when()
                .post("/requestLoan")
                .then()
                .statusCode(200)
                .body("responseDate", notNullValue())
                .body("accountId", notNullValue());
    }

    @Test(groups = {"api", "loans", "regression", "negative", "businessrule"})
    public void requestLoan_withInsufficientDownPayment_isDenied() {
        Response response = given(requestSpec)
                .queryParam("customerId", prop("valid.customer.id"))
                .queryParam("amount", prop("loan.amount.denied"))
                .queryParam("downPayment", prop("loan.downpayment.denied"))
                .queryParam("fromAccountId", prop("valid.account.id"))
                .when()
                .post("/requestLoan");

        response.then().statusCode(200);
        String status = response.jsonPath().getString("approved");
        Assert.assertTrue("false".equalsIgnoreCase(status) || response.getBody().asString().toLowerCase().contains("denied"),
                "FAILED: a loan with a down payment below the 10% threshold should be Denied.");
    }

    @Test(groups = {"api", "loans", "regression", "negative"})
    public void requestLoan_withInvalidAccount_negative() {
        Response response = given(requestSpec)
                .queryParam("customerId", prop("valid.customer.id"))
                .queryParam("amount", prop("loan.amount.approved"))
                .queryParam("downPayment", prop("loan.downpayment.approved"))
                .queryParam("fromAccountId", prop("invalid.account.id"))
                .when()
                .post("/requestLoan");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: requesting a loan funded from a non-existent account should be rejected.");
    }

    @Test(groups = {"api", "loans", "regression", "negative", "boundary"})
    public void requestLoan_withZeroAmount_negative() {
        Response response = given(requestSpec)
                .queryParam("customerId", prop("valid.customer.id"))
                .queryParam("amount", "0")
                .queryParam("downPayment", "0")
                .queryParam("fromAccountId", prop("valid.account.id"))
                .when()
                .post("/requestLoan");

        // A zero-amount loan should not be silently approved.
        String body = response.getBody().asString();
        boolean approved = body != null && body.toLowerCase().contains("\"approved\":true");
        Assert.assertFalse(approved,
                "FAILED (defect): a zero-amount loan request was approved.");
    }

    @Test(groups = {"api", "loans", "regression", "negative", "boundary"})
    public void requestLoan_withNegativeAmount_negative() {
        Response response = given(requestSpec)
                .queryParam("customerId", prop("valid.customer.id"))
                .queryParam("amount", "-500")
                .queryParam("downPayment", "0")
                .queryParam("fromAccountId", prop("valid.account.id"))
                .when()
                .post("/requestLoan");

        // A negative loan amount should not be silently approved.
        String body = response.getBody().asString();
        boolean approved = body != null && body.toLowerCase().contains("\"approved\":true");
        Assert.assertFalse(approved,
                "FAILED (defect): a negative-amount loan request was approved.");
    }
}

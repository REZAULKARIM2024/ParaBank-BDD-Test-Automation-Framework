package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ParaBank REST API - Swagger tag: Accounts ("account centric operations")
 * Endpoints under test:
 *   GET  /accounts/{accountId}                              - Get Account by Id
 *   GET  /customers/{customerId}/accounts                   - Get Customer Accounts
 *   POST /deposit?accountId=&amount=                         - Deposit funds
 *   POST /withdraw?accountId=&amount=                        - Withdraw funds
 *   POST /transfer?fromAccountId=&toAccountId=&amount=       - Transfer funds
 *   POST /createAccount?customerId=&newAccountType=&fromAccountId= - Create a new account
 *
 * Test type coverage: API Testing (Positive+Negative), Backend/Data Validation
 * Testing, Integration Testing (UI-visible data should match API responses).
 */
public class AccountApiTests extends ApiTestBase {

    @Test(groups = {"api", "accounts", "smoke", "positive"})
    public void getAccountById_returnsAccountDetails_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .when()
                .get("/accounts/{id}")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("balance", notNullValue());
    }

    @Test(groups = {"api", "accounts", "regression", "negative"})
    public void getAccountById_withInvalidId_returnsErrorOrEmpty_negative() {
        Response response = given(requestSpec)
                .pathParam("id", prop("invalid.account.id"))
                .when()
                .get("/accounts/{id}");

        int status = response.getStatusCode();
        Assert.assertTrue(status == 404 || status == 500 || status == 400,
                "FAILED: an invalid account id still returned status code " + status + " (expected 4xx/5xx).");
    }

    @Test(groups = {"api", "accounts", "regression", "negative"})
    public void getAccountById_withNonNumericId_returnsBadRequest_negative() {
        Response response = given(requestSpec)
                .pathParam("id", "abc123")
                .when()
                .get("/accounts/{id}");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: a non-numeric account id still returned a successful (2xx) response.");
    }

    @Test(groups = {"api", "accounts", "regression", "positive"})
    public void getCustomerAccounts_returnsAccountList_positive() {
        given(requestSpec)
                .pathParam("customerId", prop("valid.customer.id"))
                .when()
                .get("/customers/{customerId}/accounts")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test(groups = {"api", "accounts", "regression", "negative"})
    public void getCustomerAccounts_withInvalidCustomerId_negative() {
        Response response = given(requestSpec)
                .pathParam("customerId", "0")
                .when()
                .get("/customers/{customerId}/accounts");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: an invalid customer id still returned an accounts list.");
    }

    @Test(groups = {"api", "accounts", "regression", "positive"})
    public void deposit_withValidAmount_positive() {
        given(requestSpec)
                .queryParam("accountId", prop("valid.account.id"))
                .queryParam("amount", "100")
                .when()
                .post("/deposit")
                .then()
                .statusCode(200);
    }

    @Test(groups = {"api", "accounts", "regression", "negative", "boundary"})
    public void deposit_withNegativeAmount_negative() {
        Response response = given(requestSpec)
                .queryParam("accountId", prop("valid.account.id"))
                .queryParam("amount", "-50")
                .when()
                .post("/deposit");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED (possible defect): a negative deposit amount was not rejected.");
    }

    @Test(groups = {"api", "accounts", "regression", "negative"})
    public void withdraw_exceedingBalance_negative() {
        Response response = given(requestSpec)
                .queryParam("accountId", prop("valid.account.id"))
                .queryParam("amount", "99999999")
                .when()
                .post("/withdraw");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED (possible defect): withdrawing far more than the available balance was not rejected.");
    }

    @Test(groups = {"api", "accounts", "regression", "negative"})
    public void transfer_toSameAccount_negative() {
        Response response = given(requestSpec)
                .queryParam("fromAccountId", prop("valid.account.id"))
                .queryParam("toAccountId", prop("valid.account.id"))
                .queryParam("amount", "10")
                .when()
                .post("/transfer");

        // Transferring an account to itself should ideally be blocked; documented as
        // a potential business-rule gap if the API returns 200 here.
        System.out.println("[INFO] transfer-to-self returned status " + response.getStatusCode()
                + " - flag as a business-rule gap if this is expected to be rejected.");
    }

    @Test(groups = {"api", "accounts", "regression", "negative", "boundary"})
    public void getAccountById_withNegativeId_negative() {
        Response response = given(requestSpec)
                .pathParam("id", "-1")
                .when()
                .get("/accounts/{id}");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: a negative account id still returned a successful (2xx) response.");
    }

    @Test(groups = {"api", "accounts", "regression", "negative", "boundary"})
    public void transfer_withNegativeAmount_negative() {
        Response response = given(requestSpec)
                .queryParam("fromAccountId", prop("valid.account.id"))
                .queryParam("toAccountId", prop("valid.account.id"))
                .queryParam("amount", "-25")
                .when()
                .post("/transfer");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED (possible defect): a negative transfer amount was not rejected.");
    }
}

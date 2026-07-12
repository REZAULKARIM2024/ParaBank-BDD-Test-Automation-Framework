package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ParaBank REST API - Swagger tag: Transactions ("banking transactions centric operations")
 * Endpoints under test:
 *   GET /transactions/{transactionId}                                   - Get transaction by id
 *   GET /accounts/{accountId}/transactions                              - Get transactions for account
 *   GET /accounts/{accountId}/transactions/amount/{amount}              - Find by amount
 *   GET /accounts/{accountId}/transactions/month/{month}/type/{type}    - Find by month & type
 *   GET /accounts/{accountId}/transactions/fromDate/{fromDate}/toDate/{toDate} - Find by date range
 *   GET /accounts/{accountId}/transactions/onDate/{onDate}              - Find by a specific date
 *
 * Test type coverage: API Testing, Data Validation Testing, Boundary Value Analysis.
 */
public class TransactionApiTests extends ApiTestBase {

    @Test(groups = {"api", "transactions", "smoke", "positive"})
    public void getTransactionsByAccountId_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .when()
                .get("/accounts/{id}/transactions")
                .then()
                .statusCode(200);
    }

    @Test(groups = {"api", "transactions", "regression", "negative"})
    public void getTransactionsByAccountId_withInvalidAccount_negative() {
        Response response = given(requestSpec)
                .pathParam("id", prop("invalid.account.id"))
                .when()
                .get("/accounts/{id}/transactions");

        Assert.assertTrue(response.getStatusCode() >= 400 || response.jsonPath().getList("$").isEmpty(),
                "FAILED: an invalid account id returned transaction data.");
    }

    @Test(groups = {"api", "transactions", "regression", "negative", "boundary"})
    public void searchTransactionsByAmount_withNegativeAmount_negative() {
        Response response = given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .pathParam("amount", "-50")
                .when()
                .get("/accounts/{id}/transactions/amount/{amount}");

        Assert.assertTrue(response.getStatusCode() >= 400 ||
                        response.jsonPath().getList("$") == null ||
                        response.jsonPath().getList("$").isEmpty(),
                "FAILED: a negative-amount search unexpectedly returned data.");
    }

    @Test(groups = {"api", "transactions", "regression", "positive"})
    public void getTransactionById_positive() {
        // First discover a real transaction id from the account's transaction list.
        Response listResponse = given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .when()
                .get("/accounts/{id}/transactions");

        listResponse.then().statusCode(200);
        Integer transactionId = listResponse.jsonPath().getInt("[0].id");

        if (transactionId == null) {
            throw new org.testng.SkipException("No existing transactions found for the configured test account/environment.");
        }

        given(requestSpec)
                .pathParam("id", transactionId)
                .when()
                .get("/transactions/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(transactionId));
    }

    @Test(groups = {"api", "transactions", "regression", "negative"})
    public void getTransactionById_withInvalidId_negative() {
        Response response = given(requestSpec)
                .pathParam("id", "999999999")
                .when()
                .get("/transactions/{id}");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: an invalid transaction id should not return a successful response.");
    }

    @Test(groups = {"api", "transactions", "regression", "positive"})
    public void getTransactionsByMonthAndType_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .pathParam("month", prop("transaction.month"))
                .pathParam("type", prop("transaction.type"))
                .when()
                .get("/accounts/{id}/transactions/month/{month}/type/{type}")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }

    @Test(groups = {"api", "transactions", "regression", "negative"})
    public void getTransactionsByMonthAndType_withInvalidMonth_negative() {
        Response response = given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .pathParam("month", "NotAMonth")
                .pathParam("type", prop("transaction.type"))
                .when()
                .get("/accounts/{id}/transactions/month/{month}/type/{type}");

        Assert.assertTrue(response.getStatusCode() >= 400 ||
                        (response.jsonPath().getList("$") != null && response.jsonPath().getList("$").isEmpty()),
                "FAILED: an invalid month value should not silently succeed with data.");
    }

    @Test(groups = {"api", "transactions", "regression", "positive"})
    public void getTransactionsByDateRange_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .pathParam("fromDate", prop("transaction.from.date"))
                .pathParam("toDate", prop("transaction.to.date"))
                .when()
                .get("/accounts/{id}/transactions/fromDate/{fromDate}/toDate/{toDate}")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }

    @Test(groups = {"api", "transactions", "regression", "negative", "boundary"})
    public void getTransactionsByDateRange_withFromAfterTo_negative() {
        Response response = given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .pathParam("fromDate", prop("transaction.to.date"))
                .pathParam("toDate", prop("transaction.from.date"))
                .when()
                .get("/accounts/{id}/transactions/fromDate/{fromDate}/toDate/{toDate}");

        Assert.assertTrue(response.getStatusCode() < 500,
                "FAILED: an inverted date range caused a server error instead of a graceful (empty/4xx) response.");
    }

    @Test(groups = {"api", "transactions", "regression", "positive"})
    public void getTransactionsOnSpecificDate_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .pathParam("onDate", prop("transaction.on.date"))
                .when()
                .get("/accounts/{id}/transactions/onDate/{onDate}")
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }
}

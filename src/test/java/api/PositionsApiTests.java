package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ParaBank REST API - Swagger tag: Positions ("stock centric operations")
 * Endpoints under test:
 *   GET  /positions/{positionId}                                - Get Position by id
 *   GET  /positions/{positionId}/{startDate}/{endDate}          - Get Position history in a date range
 *   GET  /customers/{customerId}/positions                      - Get Positions for Customer
 *   POST /customers/{customerId}/buyPosition?accountId=&name=&symbol=&shares=&pricePerShare=
 *   POST /customers/{customerId}/sellPosition?accountId=&positionId=&shares=&pricePerShare=
 *
 * Test type coverage: API Testing (Positive+Negative), Data Validation Testing,
 * Boundary Value Analysis.
 */
public class PositionsApiTests extends ApiTestBase {

    @Test(groups = {"api", "positions", "regression", "positive"})
    public void getPositionsForCustomer_positive() {
        given(requestSpec)
                .pathParam("customerId", prop("valid.customer.id"))
                .when()
                .get("/customers/{customerId}/positions")
                .then()
                .statusCode(200);
    }

    @Test(groups = {"api", "positions", "regression", "negative"})
    public void getPositionById_withInvalidId_negative() {
        Response response = given(requestSpec)
                .pathParam("id", "999999999")
                .when()
                .get("/positions/{id}");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: an invalid position id should not return a successful response.");
    }

    @Test(groups = {"api", "positions", "regression", "positive"})
    public void getPositionHistory_forValidDateRange_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.position.id"))
                .pathParam("startDate", prop("position.start.date"))
                .pathParam("endDate", prop("position.end.date"))
                .when()
                .get("/positions/{id}/{startDate}/{endDate}")
                .then()
                .statusCode(anyOf(is(200), is(404))); // 404 acceptable if the seeded position id doesn't exist in this environment
    }

    @Test(groups = {"api", "positions", "regression", "negative", "boundary"})
    public void getPositionHistory_withFromDateAfterToDate_negative() {
        Response response = given(requestSpec)
                .pathParam("id", prop("valid.position.id"))
                .pathParam("startDate", prop("position.end.date"))
                .pathParam("endDate", prop("position.start.date"))
                .when()
                .get("/positions/{id}/{startDate}/{endDate}");

        // An inverted date range should ideally return an empty list or an error,
        // never a server error.
        Assert.assertTrue(response.getStatusCode() < 500,
                "FAILED: an inverted (from > to) date range caused a server error instead of a graceful response.");
    }

    @Test(groups = {"api", "positions", "regression", "negative"})
    public void buyPosition_withNegativeShares_negative() {
        Response response = given(requestSpec)
                .pathParam("customerId", prop("valid.customer.id"))
                .queryParam("accountId", prop("valid.account.id"))
                .queryParam("name", prop("position.stock.name"))
                .queryParam("symbol", prop("position.stock.symbol"))
                .queryParam("shares", "-5")
                .queryParam("pricePerShare", prop("position.price.per.share"))
                .when()
                .post("/customers/{customerId}/buyPosition");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED (possible defect): buying a negative number of shares was not rejected.");
    }

    @Test(groups = {"api", "positions", "regression", "negative"})
    public void sellPosition_withInvalidPositionId_negative() {
        Response response = given(requestSpec)
                .pathParam("customerId", prop("valid.customer.id"))
                .queryParam("accountId", prop("valid.account.id"))
                .queryParam("positionId", "999999999")
                .queryParam("shares", prop("position.shares"))
                .queryParam("pricePerShare", prop("position.price.per.share"))
                .when()
                .post("/customers/{customerId}/sellPosition");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: selling a non-existent position should be rejected.");
    }
}

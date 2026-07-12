package api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ParaBank REST API - Swagger tag: Customers ("customer centric operations")
 * Endpoints under test (from com.parasoft.parabank.service.ParaBankService):
 *   GET  /customers/{customerId}                 - Get Customer Details
 *   GET  /customers/{customerId}/accounts         - Get Customer Accounts
 *   POST /customers/update/{customerId}           - Update customer information
 *
 * Test type coverage: API Testing (Positive+Negative), Integration Testing,
 * Data Validation Testing.
 */
public class CustomersApiTests extends ApiTestBase {

    @Test(groups = {"api", "customers", "smoke", "positive"})
    public void getCustomerById_returnsCustomerDetails_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.customer.id"))
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("username", notNullValue());
    }

    @Test(groups = {"api", "customers", "regression", "negative"})
    public void getCustomerById_withInvalidId_negative() {
        Response response = given(requestSpec)
                .pathParam("id", prop("invalid.customer.id"))
                .when()
                .get("/customers/{id}");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: an invalid customer id should not return a successful (2xx) response.");
    }

    @Test(groups = {"api", "customers", "regression", "positive"})
    public void getCustomerAccounts_returnsAccountList_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.customer.id"))
                .when()
                .get("/customers/{id}/accounts")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test(groups = {"api", "customers", "regression", "negative"})
    public void getCustomerAccounts_withNonNumericId_negative() {
        Response response = given(requestSpec)
                .pathParam("id", "not-a-number")
                .when()
                .get("/customers/{id}/accounts");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED: a non-numeric customer id should be rejected, not silently accepted.");
    }

    @Test(groups = {"api", "customers", "regression", "negative"})
    public void updateCustomer_withMissingRequiredFields_negative() {
        // Deliberately omit required query params (firstName, lastName, etc.)
        Response response = given(requestSpec)
                .pathParam("id", prop("valid.customer.id"))
                .queryParam("firstName", "") // required field left empty
                .when()
                .post("/customers/update/{id}");

        Assert.assertTrue(response.getStatusCode() >= 400,
                "FAILED (possible defect): updating a customer with an empty required field "
                        + "(firstName) did not return an error status.");
    }

    /** Contract check: response Content-Type header should be application/json (not text/html
     *  or another type), so consumers can safely rely on JSON parsing. */
    @Test(groups = {"api", "customers", "regression", "contract"})
    public void getCustomerById_hasJsonContentType_positive() {
        given(requestSpec)
                .pathParam("id", prop("valid.customer.id"))
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(200)
                .contentType("application/json");
    }
}

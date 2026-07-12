package api;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Contract Testing: API রেসপন্সের স্ট্রাকচার (schema) প্রত্যাশিত ফরম্যাটের সাথে মেলে কিনা।
 * এটি নিশ্চিত করে যে ব্যাকএন্ডে পরিবর্তন হলেও ফ্রন্টএন্ড/consumer ব্রেক করবে না।
 */
public class ContractValidationTests extends ApiTestBase {

    @Test(groups = {"api", "contract", "regression"})
    public void accountResponse_matchesExpectedSchema() {
        given(requestSpec)
                .pathParam("id", prop("valid.account.id"))
                .when()
                .get("/accounts/{id}")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/account-schema.json"));
    }
}

package api;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Common base class for all API test classes.
 * Test type coverage: API Testing, Integration Testing, Contract Testing, Backend Testing.
 */
public class ApiTestBase {

    protected static Properties config = new Properties();
    protected RequestSpecification requestSpec;

    @BeforeClass
    public void setUpApi() throws IOException {
        if (config.isEmpty()) {
            try (InputStream in = ApiTestBase.class.getClassLoader()
                    .getResourceAsStream("config/api.properties")) {
                config.load(in);
            }
        }
        RestAssured.baseURI = config.getProperty("base.url");
        requestSpec = RestAssured.given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
    }

    protected String prop(String key) {
        return config.getProperty(key);
    }

    /**
     * Guards destructive endpoints (Database /cleanDB, /initializeDB and JMS
     * listener start/stop) that affect the SHARED PUBLIC ParaBank demo server.
     * Returns true only when explicitly opted in via config/api.properties or
     * -Ddestructive.tests.enabled=true, so these never run by accident against
     * a shared environment.
     */
    protected boolean destructiveTestsEnabled() {
        String sysProp = System.getProperty("destructive.tests.enabled");
        if (sysProp != null) {
            return Boolean.parseBoolean(sysProp);
        }
        return Boolean.parseBoolean(prop("destructive.tests.enabled"));
    }
}

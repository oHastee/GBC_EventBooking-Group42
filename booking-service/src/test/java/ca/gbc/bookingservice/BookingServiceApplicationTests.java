package ca.gbc.bookingservice;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingServiceApplicationTests {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        mongoDBContainer.start();
    }
    @Test
    @Order(1)
    void createBookingTest() {
        String requestBody = """
                {
                    "ownerId": 1,
                    "startTime": "2025-01-07T15:33",
                    "endTime": "2025-02-07T14:33",
                    "purpose": "Wine Tasting",
                    "roomId": 1
                }
                """;

        RestAssured.baseURI = "http://localhost";
        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/booking")
                .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("ownerId", Matchers.equalTo(1))
                .body("roomId", Matchers.equalTo(1))
                .body("startTime", Matchers.equalTo(1736263980))
                .body("endTime", Matchers.equalTo(1738938780))
                .body("purpose", Matchers.equalTo("Wine Tasting"));

    }

    @Test
    @Order(2)
    void getAllBookingsTest() {
        String requestBody = """
                {
                    "ownerId": 5,
                    "startTime": "2025-01-07T15:33",
                    "endTime": "2025-02-07T14:33",
                    "purpose": "Wine Tasting",
                    "roomId": 5
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/booking")
                .then()
                .statusCode(201);

        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/booking")
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("[0].ownerId", Matchers.equalTo(5))
                .body("[0].roomId", Matchers.equalTo(5))
                .body("[0].startTime", Matchers.equalTo(1736263980))
                .body("[0].endTime", Matchers.equalTo(1738938780))
                .body("[0].purpose", Matchers.equalTo("Wine Tasting"));
    }



    @Test
    @Order(3)
    void getBookingById() {

        String requestBody = """
                {
                    "ownerId": 2,
                    "startTime": "2025-01-07T15:33",
                    "endTime": "2025-02-07T14:33",
                    "purpose": "Wine Tasting",
                    "roomId": 2
                }
                """;

//        long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/booking")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String approval_id = response.path("id");

        RestAssured.given()
                .pathParam("id", approval_id)
                .when()
                .get("/api/booking/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", Matchers.notNullValue())
                .body("ownerId", Matchers.equalTo(2))
                .body("roomId", Matchers.equalTo(2))
                .body("startTime", Matchers.equalTo(1736263980))
                .body("endTime", Matchers.equalTo(1738938780))
                .body("purpose", Matchers.equalTo("Wine Tasting"));
    }

    @Test
    @Order(4)
    void deleteBooking() {

        String requestBody = """
                {
                    "ownerId": 3,
                    "startTime": "2025-01-07T15:33",
                    "endTime": "2025-02-07T14:33",
                    "purpose": "Wine Tasting",
                    "roomId": 3
                }
                """;

//        long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/booking")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String approval_id = response.path("id");

        RestAssured.given()
                .pathParam("id", approval_id)
                .when()
                .delete("/api/booking/{id}")
                .then()
                .log().all()
                .statusCode(200);
    }
    @Test
    @Order(5)
    void updateBooking() {

        String requestBody = """
                {
                    "ownerId": 4,
                    "startTime": "2025-01-07T15:33",
                    "endTime": "2025-02-07T14:33",
                    "purpose": "Wine Tasting",
                    "roomId": 4
                }
                """;

        String requestBodyUpdate = """
                {
                    "userId": 4,
                    "startTime": "2025-01-07T15:33",
                    "endTime": "2025-02-07T14:33",
                    "purpose": "Meeting",
                    "roomId": 4
                }
                """;

//        long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/booking")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String approval_id = response.path("id");

        RestAssured.given()
                .contentType("application/json")
                .body(requestBodyUpdate)
                .when()
                .put("/api/booking/{id}", approval_id)
                .then()
                .log().all()
                .statusCode(200)
                .body("id", Matchers.notNullValue())
                .body("ownerId", Matchers.equalTo(4))
                .body("roomId", Matchers.equalTo(4))
                .body("startTime", Matchers.equalTo(1736263980))
                .body("endTime", Matchers.equalTo(1738938780))
                .body("purpose", Matchers.equalTo("Meeting"));
    }

}

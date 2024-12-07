package ca.gbc.roomservice;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.MatcherAssert.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomServiceApplicationTests {

    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("admin")
            .withPassword("password");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        postgreSQLContainer.start();
    }

    @Test
    void createRoomTest() {
        // Request body for placing an order
        String requestBody = """
                {
                   "roomName": "Conference Room A",
                   "capacity": 20,
                   "features": "Projector, Whiteboard"
                 }
                """;

        // Execute POST request and validate response
        var responseBodyString =  RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/room")
                .then()
                .log().all()
                .statusCode(201)  // Ensure room creation returns a 201 Created status
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Conference Room A"))
                .body("capacity", Matchers.equalTo(20))
                .body("features", Matchers.equalTo("Projector, Whiteboard"));
    }


    @Test
    void getAllRoomsTest() {

        // First, create a room to make sure there's data to retrieve.
        String createRequestBody = """
            {
                "roomName" : "Deluxe Suite",
                "capacity" : 4,
                "features" : "Wi-Fi, TV, Mini Bar"
            }
            """;

        RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/room")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Deluxe Suite"))
                .body("capacity", Matchers.equalTo(4))
                .body("features", Matchers.equalTo("Wi-Fi, TV, Mini Bar"));

        // Now, test the GET all rooms endpoint.
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/room")
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // Ensure at least one room exists
                .body("[0].roomName", Matchers.equalTo("Deluxe Suite"))
                .body("[0].capacity", Matchers.equalTo(4))
                .body("[0].features", Matchers.equalTo("Wi-Fi, TV, Mini Bar"));
    }

    @Test
    void getRoomByIdTest() {
        // First, create a room to make sure there's data to retrieve.
        String createRequestBody = """
            {
                "roomName" : "Standard Room",
                "capacity" : 2,
                "features" : "Wi-Fi, Air Conditioning"
            }
            """;

        String roomId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/room")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Standard Room"))
                .body("capacity", Matchers.equalTo(2))
                .body("features", Matchers.equalTo("Wi-Fi, Air Conditioning"))
                .extract()
                .jsonPath()
                .getString("id"); // Extract room ID from the response

        // Now, test the GET room by ID endpoint.
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/room/{id}", roomId)
                .then()
                .log().all()
                .statusCode(200)
                .body("id", Matchers.equalTo(roomId)) // Ensure the ID matches
                .body("roomName", Matchers.equalTo("Standard Room"))
                .body("capacity", Matchers.equalTo(2))
                .body("features", Matchers.equalTo("Wi-Fi, Air Conditioning"));
    }

    @Test
    void getRoomByIdNotFoundTest() {
        // Test for a room ID that doesn't exist
        String nonExistentRoomId = "999";  // Assuming this ID doesn't exist in the database

        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/room/{id}", nonExistentRoomId)
                .then()
                .log().all()
                .statusCode(404);  // Expecting a 404 Not Found response
    }

    @Test
    void updateRoomTest() {
        // First, create a room to make sure there's data to update.
        String createRequestBody = """
            {
                "roomName" : "Standard Room",
                "capacity" : 2,
                "features" : "Wi-Fi, Air Conditioning"
            }
            """;

        String roomId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/room")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Standard Room"))
                .body("capacity", Matchers.equalTo(2))
                .body("features", Matchers.equalTo("Wi-Fi, Air Conditioning"))
                .extract()
                .jsonPath()
                .getString("id"); // Extract room ID from the response

        // Now, update the room with new details
        String updateRequestBody = """
            {
                "roomName" : "Updated Room",
                "capacity" : 5,
                "features" : "Wi-Fi, TV, Mini Bar"
            }
            """;

        // Send PUT request to update the room
        RestAssured.given()
                .contentType("application/json")
                .body(updateRequestBody)
                .when()
                .put("/api/room/{id}", roomId)
                .then()
                .log().all()
                .statusCode(200)  // Expecting a 200 OK response
                .body("id", Matchers.equalTo(roomId)) // Ensure the ID remains the same
                .body("roomName", Matchers.equalTo("Updated Room"))
                .body("capacity", Matchers.equalTo(5))
                .body("features", Matchers.equalTo("Wi-Fi, TV, Mini Bar"));

        // Verify the update by fetching the room by ID
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/room/{id}", roomId)
                .then()
                .log().all()
                .statusCode(200)
                .body("id", Matchers.equalTo(roomId)) // Ensure the ID is the same
                .body("roomName", Matchers.equalTo("Updated Room"))
                .body("capacity", Matchers.equalTo(5))
                .body("features", Matchers.equalTo("Wi-Fi, TV, Mini Bar"));
    }

    @Test
    void updateRoomNotFoundTest() {
        // Test for a room ID that doesn't exist
        String nonExistentRoomId = "999";  // Assuming this ID doesn't exist in the database

        String updateRequestBody = """
            {
                "roomName" : "Non-existent Room",
                "capacity" : 10,
                "features" : "Wi-Fi, Projector"
            }
            """;

        RestAssured.given()
                .contentType("application/json")
                .body(updateRequestBody)
                .when()
                .put("/api/room/{id}", nonExistentRoomId)
                .then()
                .log().all()
                .statusCode(404);  // Expecting a 404 Not Found response
    }


    @Test
    void deleteRoomTest() {
        // First, create a room to make sure there's a room to delete
        String createRequestBody = """
        {
            "roomName" : "Luxury Suite",
            "capacity" : 3,
            "features" : "Wi-Fi, TV, Balcony"
        }
        """;

        String roomId = RestAssured.given()
                .contentType("application/json")
                .body(createRequestBody)
                .when()
                .post("/api/room")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("roomName", Matchers.equalTo("Luxury Suite"))
                .body("capacity", Matchers.equalTo(3))
                .body("features", Matchers.equalTo("Wi-Fi, TV, Balcony"))
                .extract()
                .jsonPath()
                .getString("id"); // Extract room ID from the response

        // Now, delete the room using the room ID
        RestAssured.given()
                .contentType("application/json")
                .when()
                .delete("/api/room/{id}", roomId)
                .then()
                .log().all()
                .statusCode(200); // Expecting a 200 OK response for successful deletion

        // Verify the room has been deleted by trying to fetch it and expecting a 404 response
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/room/{id}", roomId)
                .then()
                .log().all()
                .statusCode(404); // Expecting a 404 Not Found response since the room should be deleted
    }


}
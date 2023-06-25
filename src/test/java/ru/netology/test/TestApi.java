package ru.netology.test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.netology.data.Data;

import java.sql.DriverManager;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestApi {

    private static String token;

    private static final RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(9999)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    @BeforeAll
    public static void getToken() {
        given()
                .spec(requestSpec)
                .body(Data.getAuthInfo())
                .post("/api/auth")
                .then()
                .statusCode(200);
        Response response =
                given()
                        .spec(requestSpec)
                        .body(Data.getVerificationCode(Data.getAuthInfo()))
                        .post("/api/auth/verification")
                        .then()
                        .statusCode(200)
                        .extract().response();
        String jsonString = response.getBody().asString();
        token = JsonPath.from(jsonString).get("token");
    }

    @SneakyThrows
    @AfterAll
    public static void clearDB() {
        QueryRunner runner = new QueryRunner();
        var con = DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "app", "pass");
        runner.update(con, "delete from auth_codes");
        runner.update(con, "delete from card_transactions");
        runner.update(con, "delete from cards");
        runner.update(con, "delete from users");
    }

    @Test
    public void getCardsTestV1() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body("")
                .get("/api/cards")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void getCardsTestV2() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body("")
                .get("/api/cards")
                .then()
                .statusCode(200)
                .body("", hasSize(2));
    }

    @Test
    public void getCardsTestV3() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body("")
                .get("/api/cards")
                .then()
                .statusCode(200)
                .body("every{ it.balance >= 0 }", is(true));
    }

    @Test
    public void transferTestV1() {
        int amount = 5000;
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(Data.getTransferInfo1(amount))
                .post("/api/transfer")
                .then()
                .statusCode(200);

        assertEquals(initBalance1 - amount, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2 + amount, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferTestV2() {
        int amount = 5000;
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(Data.getTransferInfo2(amount))
                .post("/api/transfer")
                .then()
                .statusCode(200);

        assertEquals(initBalance1 + amount, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2 - amount, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfAmountIsZeroTest() {
        int amount = 0;
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(Data.getTransferInfo1(amount))
                .post("/api/transfer")
                .then()
                .statusCode(400);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfAmountIsNegativeTest() {
        int amount = -1000;
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(Data.getTransferInfo1(amount))
                .post("/api/transfer")
                .then()
                .statusCode(400);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfFromAndToEquals() {
        int amount = 5000;
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(Data.getTransferInfo(amount))
                .post("/api/transfer")
                .then()
                .statusCode(400);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfAmountMoreThanBalance() {
        int amount = 10001;
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(Data.getTransferInfo(amount))
                .post("/api/transfer")
                .then()
                .statusCode(400);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }
}

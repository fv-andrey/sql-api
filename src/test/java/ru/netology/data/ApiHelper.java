package ru.netology.data;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

public class ApiHelper {


    private static final RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(9999)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    private ApiHelper() {

    }

    public static String getTokenInfo() {
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
        return response.jsonPath().getString("token");
    }

    public static void transferRequest(String token, Data.TransferInfo transferInfo, String path, int statusCode) {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(transferInfo)
                .post(path)
                .then()
                .statusCode(statusCode);
    }

    public static Response cardsRequest(String token, int statusCode, ContentType contentType, int size) {
        return given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body("")
                .get("/api/cards")
                .then()
                .statusCode(statusCode)
                .contentType(contentType)
                .body("", hasSize(size))
                .extract().response();
    }
}


package ru.netology.test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.netology.data.ApiHelper;
import ru.netology.data.Data;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.netology.data.Data.clearDB;

public class TestApi {

    private static String token;

    @BeforeAll
    public static void set() {
        token = ApiHelper.getTokenInfo();
    }

    @SneakyThrows
    @AfterAll
    public static void clear() {
        clearDB();
    }

    @Test
    public void getCardsTest() {
        int statusCode = 200;
        int size = 2;
        ContentType contentType = ContentType.JSON;

        Response response = ApiHelper.cardsRequest(token, statusCode, contentType, size);

        List<Integer> balances = response.jsonPath().getList("balance");
        int balance1 = balances.get(1);
        int balance2 = balances.get(0);

        assertTrue(balance1 > 0);
        assertTrue(balance2 > 0);
    }

    @Test
    public void assertBalanceInBodyAnswerAndDB() {
        int statusCode = 200;
        int size = 2;
        ContentType contentType = ContentType.JSON;

        Response response = ApiHelper.cardsRequest(token, statusCode, contentType, size);

        List<Integer> balances = response.jsonPath().getList("balance");
        int balance1 = balances.get(1);
        int balance2 = balances.get(0);

        assertEquals(balance1, Data.getCard1Balance().getBalance());
        assertEquals(balance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferTestV1() {
        int amount = 5000;
        int statusCode = 200;
        var transferInfo = Data.getTransferInfo1(amount);
        String path = "/api/transfer";
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();

        ApiHelper.transferRequest(token, transferInfo, path, statusCode);

        assertEquals(initBalance1 - amount, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2 + amount, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferTestV2() {
        int amount = 5000;
        int statusCode = 200;
        var transferInfo = Data.getTransferInfo2(amount);
        String path = "/api/transfer";
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();

        ApiHelper.transferRequest(token, transferInfo, path, statusCode);

        assertEquals(initBalance1 + amount, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2 - amount, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfAmountIsZeroTest() {
        int amount = 0;
        int statusCode = 400;
        var transferInfo = Data.getTransferInfo1(amount);
        String path = "/api/transfer";
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();

        ApiHelper.transferRequest(token, transferInfo, path, statusCode);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfAmountIsNegativeTest() {
        int amount = -1000;
        int statusCode = 400;
        var transferInfo = Data.getTransferInfo1(amount);
        String path = "/api/transfer";
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();

        ApiHelper.transferRequest(token, transferInfo, path, statusCode);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfFromAndToEquals() {
        int amount = 5000;
        int statusCode = 400;
        var transferInfo = Data.getTransferInfo(amount);
        String path = "/api/transfer";
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();

        ApiHelper.transferRequest(token, transferInfo, path, statusCode);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferIfAmountMoreThanBalance() {
        int amount = 10001;
        int statusCode = 400;
        var transferInfo = Data.getTransferInfo1(amount);
        String path = "/api/transfer";
        int initBalance1 = Data.getCard1Balance().getBalance();
        int initBalance2 = Data.getCard2Balance().getBalance();

        ApiHelper.transferRequest(token, transferInfo, path, statusCode);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
        assertEquals(initBalance2, Data.getCard2Balance().getBalance());
    }

    @Test
    public void transferToRandomCard() {
        int amount = 5000;
        int statusCode = 400;
        var transferInfo = Data.getRandomTransferInfo(amount);
        String path = "/api/transfer";
        int initBalance1 = Data.getCard1Balance().getBalance();

        ApiHelper.transferRequest(token, transferInfo, path, statusCode);

        assertEquals(initBalance1, Data.getCard1Balance().getBalance());
    }
}

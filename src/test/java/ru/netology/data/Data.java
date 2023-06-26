package ru.netology.data;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.dbutils.QueryRunner;;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.DriverManager;
import java.util.Locale;

public class Data {

    private static Faker faker = new Faker(new Locale("ru"));

    private Data() {
    }

    @Value
    public static class AuthInfo {
        String login;
        String password;
    }

    public static AuthInfo getAuthInfo() {
        return new AuthInfo("vasya", "qwerty123");
    }

    @Value
    public static class VerificationCode {
        String code;
        String login;
    }

    @SneakyThrows
    public static VerificationCode getVerificationCode(AuthInfo info) {
        var code = "select code from auth_codes order by created desc limit 1";
        var conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "app", "pass");
        var verCode = new QueryRunner().query(conn, code, new ScalarHandler<String>());
        return new VerificationCode(verCode, info.getLogin());
    }

    @Value
    public static class TransferInfo {
        String from;
        String to;
        int amount;
    }

    public static TransferInfo getTransferInfo(int amount) {
        return new TransferInfo("5559 0000 0000 0001", "5559 0000 0000 0001", amount);
    }

    public static TransferInfo getTransferInfo1(int amount) {
        return new TransferInfo("5559 0000 0000 0001", "5559 0000 0000 0002", amount);
    }

    public static TransferInfo getTransferInfo2(int amount) {
        return new TransferInfo("5559 0000 0000 0002", "5559 0000 0000 0001", amount);
    }

    public static TransferInfo getRandomTransferInfo(int amount) {
        return new TransferInfo("5559 0000 0000 0001", faker.numerify("#### #### #### ####"), amount);
    }

    @Value
    public static class CardBalance {
        int balance;
    }

    @SneakyThrows
    public static int getCardBalance(String select) {
        var conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "app", "pass");
        return new QueryRunner().query(conn, select, new ScalarHandler<>());
    }

    @SneakyThrows
    public static CardBalance getCard1Balance() {
        var balance1 = getCardBalance("select balance_in_kopecks from cards where number = '5559 0000 0000 0001'");
        return new CardBalance(balance1);
    }

    @SneakyThrows
    public static CardBalance getCard2Balance() {
        var balance2 = getCardBalance("select balance_in_kopecks from cards where number = '5559 0000 0000 0002'");
        return new CardBalance(balance2);
    }

    @SneakyThrows
    public static void clearDB() {
        QueryRunner runner = new QueryRunner();
        var con = DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "app", "pass");
        runner.update(con, "delete from auth_codes");
        runner.update(con, "delete from card_transactions");
        runner.update(con, "delete from cards");
        runner.update(con, "delete from users");
    }
}


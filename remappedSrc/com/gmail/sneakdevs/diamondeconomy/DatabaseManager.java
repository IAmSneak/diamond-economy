package com.gmail.sneakdevs.diamondeconomy;

import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.UUID;

public class DatabaseManager {
    private static final JsonParser PARSER = new JsonParser();
    public static String url;

    public static void createNewDatabase(File file) {

        url = "jdbc:sqlite:" + file.getPath().replace('\\', '/');

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        createNewTable();
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewTable() {

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS diamonds (\n"
                + "	uuid text PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	money integer DEFAULT 0\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addPlayer(String uuid, String name) {
        String sql = "INSERT INTO diamonds(uuid,name,money) VALUES(?,?,?)";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, uuid);
            pstmt.setString(2, name);
            pstmt.setInt(3, 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            updateName(uuid, name, getBalanceFromUUID(uuid));
        }
    }

    public int getBalanceFromUUID(String uuid){
        String sql = "SELECT uuid, money FROM diamonds";

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                if (rs.getString("uuid").equals(uuid)) {
                    return rs.getInt("money");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public int getBalanceFromName(String name){
        String sql = "SELECT name, money FROM diamonds";

        try (Connection conn = this.connect(); Statement stmt  = conn.createStatement(); ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                if (rs.getString("name").equals(name)) {
                    return rs.getInt("money");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public void setBalance(String uuid, String name, int money) {
        String sql = "UPDATE diamonds SET name = ? , "
                + "money = ? "
                + "WHERE uuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(3, uuid);
            pstmt.setInt(2, money);
            pstmt.setString(1, name);
            // update
            pstmt.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public String top(String uuid, int topAmount){
        String sql = "SELECT uuid, name, money FROM diamonds ORDER BY money DESC";
        String rankings = "";
        int i = 0;
        int playerRank = 0;

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next() && (i < topAmount || playerRank == 0)) {
                i++;
                if (uuid.equals(rs.getString("uuid"))) {
                    playerRank = i;
                }
                if (i <= topAmount) {
                    rankings = rankings.concat(rs.getString("name") + "  " + rs.getInt("money") + "\n");
                }
            }
        } catch (SQLException ignored) {
        }
        return rankings.concat("Your rank is: " + playerRank);
    }

    public void updateName(String uuid, String name, int money) {
        String sql = "UPDATE diamonds SET name = ? "
                + "WHERE uuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(2, uuid);
            pstmt.setString(1, name);
            // update
            pstmt.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public void setName(String uuid, String name) {
        String sql = "UPDATE diamonds SET name = ? "
                + "WHERE uuid != ? "
                + "AND name = ?";

        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(3, name);
            pstmt.setString(2, uuid);
            pstmt.setString(1, "a");
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

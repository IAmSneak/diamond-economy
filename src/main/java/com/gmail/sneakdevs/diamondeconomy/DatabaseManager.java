package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import me.shedaniel.autoconfig.AutoConfig;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
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
        String sql = "CREATE TABLE IF NOT EXISTS diamonds (uuid text PRIMARY KEY, name text NOT NULL, money integer DEFAULT 0);";
        String sql2 = "CREATE TABLE IF NOT EXISTS transactions (transactionid integer PRIMARY KEY AUTOINCREMENT, time integer, type integer, executer text, victim text, amount integer, oldval integer);";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
            if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().transactionHistory) {
                stmt.execute(sql2);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTransaction(String type, String executerUUID, String victimUUID, int amount, int oldVal) {
        String sql = "INSERT INTO transactions(time,type,executer,victim,amount,oldval) VALUES(?,?,?,?,?,?)";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setString(2, type);
            pstmt.setString(3, executerUUID);
            pstmt.setString(4, victimUUID);
            pstmt.setInt(5, amount);
            pstmt.setInt(6, oldVal);
            pstmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void addPlayer(String uuid, String name) {
        String sql = "INSERT INTO diamonds(uuid,name,money) VALUES(?,?,?)";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, uuid);
            pstmt.setString(2, name);
            pstmt.setInt(3, 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            updateName(uuid, name);
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

    public void setBalance(String uuid, int money) {
        String sql = "UPDATE diamonds SET money = ? WHERE uuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setInt(1, money);
            pstmt.setString(2, uuid);
            // update
            pstmt.executeUpdate();
        } catch (SQLException ignored) {}
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

    public void updateName(String uuid, String name) {
        String sql = "UPDATE diamonds SET name = ? "
                + "WHERE uuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, name);
            pstmt.setString(2, uuid);
            // update
            pstmt.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public void setName(String uuid, String name) {
        String sql = "UPDATE diamonds SET name = ? "
                + "WHERE uuid != ?"
                + "AND name = ?";

        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, "a");
            pstmt.setString(2, uuid);
            pstmt.setString(3, name);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

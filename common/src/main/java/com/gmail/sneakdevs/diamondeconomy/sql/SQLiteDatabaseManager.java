package com.gmail.sneakdevs.diamondeconomy.sql;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;

import java.io.File;
import java.sql.*;

public class SQLiteDatabaseManager implements DatabaseManager {
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

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static void createNewTable() {
        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            for (String query : DiamondEconomy.tableRegistry) {
                stmt.execute(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            updateName(uuid, name);
        }
    }

    public void updateName(String uuid, String name) {
        String sql = "UPDATE diamonds SET name = ? WHERE uuid = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setName(String uuid, String name) {
        String sql = "UPDATE diamonds SET name = ? WHERE uuid != ? AND name = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "a");
            pstmt.setString(2, uuid);
            pstmt.setString(3, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getBalanceFromUUID(String uuid){
        String sql = "SELECT uuid, money FROM diamonds WHERE uuid = '" + uuid + "'";

        try (Connection conn = this.connect(); Statement stmt  = conn.createStatement(); ResultSet rs    = stmt.executeQuery(sql)){
            rs.next();
            return rs.getInt("money");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getNameFromUUID(String uuid){
        String sql = "SELECT uuid, name FROM diamonds WHERE uuid = '" + uuid + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getString("name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getBalanceFromName(String name){
        String sql = "SELECT name, money FROM diamonds WHERE name = '" + name + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getInt("money");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean setBalance(String uuid, int money) {
        String sql = "UPDATE diamonds SET money = ? WHERE uuid = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (money >= 0 && money < Integer.MAX_VALUE) {
                pstmt.setInt(1, money);
                pstmt.setString(2, uuid);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changeBalance(String uuid, int money) {
        String sql = "UPDATE diamonds SET money = ? WHERE uuid = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int bal = getBalanceFromUUID(uuid);
            if (bal + money >= 0 && bal + money < Integer.MAX_VALUE) {
                pstmt.setInt(1, bal + money);
                pstmt.setString(2, uuid);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String top(String uuid, int page){
        String sql = "SELECT uuid, name, money FROM diamonds ORDER BY money DESC";
        String rankings = "";
        int i = 0;
        int playerRank = 0;
        int repeats = 0;

        try (Connection conn = this.connect(); Statement stmt  = conn.createStatement(); ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next() && (repeats < 10 || playerRank == 0)) {
                if (repeats / 10 + 1 == page) {
                    rankings = rankings.concat(rs.getRow() + ") " + rs.getString("name") + ": $" + rs.getInt("money") + "\n");
                    i++;
                }
                repeats++;
                if (uuid.equals(rs.getString("uuid"))) {
                    playerRank = repeats;
                }
            }
            if (i < 10) {
                rankings = rankings.concat("---End--- \n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rankings.concat("Your rank is: " + playerRank);
    }
}

package com.gmail.sneakdevs.diamondeconomy.sql;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;

public class SQLiteDatabaseManager implements DatabaseManager {
    public static String url;

    @SuppressWarnings("all")
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

    private static void createNewTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS diamonds (uuid text PRIMARY KEY, name text NOT NULL, money integer DEFAULT 0);";
        String sql2 = "CREATE TABLE IF NOT EXISTS transactions (transactionid integer PRIMARY KEY AUTOINCREMENT, time integer, type integer, executer text, victim text, amount integer, oldval integer);";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
            stmt.execute(sql2);
        } catch (SQLException e) {
            e.printStackTrace();
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
        String sql = "UPDATE diamonds SET name = ? "
                + "WHERE uuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setName(String uuid, String name) {
        String sql = "UPDATE diamonds SET name = ? "
                + "WHERE uuid != ?"
                + "AND name = ?";

        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "a");
            pstmt.setString(2, uuid);
            pstmt.setString(3, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return -1;
    }

    public String getNameFromUUID(String uuid){
        String sql = "SELECT uuid, name FROM diamonds";

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                if (rs.getString("uuid").equals(uuid)) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getBalanceFromName(String name){
        String sql = "SELECT name, money FROM diamonds";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                if (rs.getString("name").equals(name)) {
                    return rs.getInt("money");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean setBalance(String uuid, int money) {
        String sql = "UPDATE diamonds SET money = ? WHERE uuid = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (pstmt.getResultSet().getInt(money) + money >= 0 && pstmt.getResultSet().getInt(money) + money < Integer.MAX_VALUE) {
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
            int bal = pstmt.getResultSet().getInt(money);
            if (bal + money >= 0) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rankings.concat("Your rank is: " + playerRank);
    }

    public String history(int page){
        String sql = "SELECT time,type,executer,victim,amount,oldval FROM transactions ORDER BY transactionid DESC";
        StringBuilder history = new StringBuilder();
        int i = 0;
        int repeats = 0;

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            while (rs.next() && i < 8) {
                if (repeats / 8 + 1 == page) {
                    history.append("\n").append(getMessage(rs.getString("type"), getNameFromUUID(rs.getString("executer")), getNameFromUUID(rs.getString("victim")), rs.getInt("amount"), rs.getLong("time"), rs.getInt("oldval")));
                    i++;
                }
                repeats++;
            }
            if (i < 8) {
                history.append("\n").append("End of Transaction History");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history.toString();
    }

    public String getMessage(String type, String executer, String victim, int amount, long time, int oldVal){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date resultTime = new Date(time);
        return switch (type) {
            case "take" -> executer + " took " + amount + " from " + victim + " on " + sdf.format(resultTime);
            case "send" -> executer + " sent " + amount + " to " + victim + " on " + sdf.format(resultTime);
            case "give" -> executer + " gave " + amount + " to " + victim + " on " + sdf.format(resultTime);
            case "set" -> executer + " set " + victim + "'s balance to " + amount + " from " + oldVal + " on " + sdf.format(resultTime);
            default -> "Unknown Transaction";
        };
    }
}

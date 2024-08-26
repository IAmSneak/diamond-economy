package com.gmail.sneakdevs.diamondeconomy.sql;

import com.gmail.sneakdevs.diamondeconomy.CurrencyType;
import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;

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
            pstmt.setInt(3, DiamondEconomyConfig.getInstance().startingMoney);
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

    public void setAllBalance(int money) {
        String sql = "UPDATE diamonds SET money = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (money >= 0 && money < Integer.MAX_VALUE) {
                pstmt.setInt(1, money);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public void changeAllBalance(int money) {
        String sql = "UPDATE diamonds SET money = money + " + money + " WHERE " + Integer.MAX_VALUE + " > money + " + money + " AND 0 <= money + " + money;

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public String rank(int rank){
        int repeats = 0;
        String sql = "SELECT name FROM diamonds ORDER BY money DESC";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next() ) {
                repeats++;
                if (repeats == rank) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No Player";
    }

    public int playerRank(String uuid){
        String sql = "SELECT uuid FROM diamonds ORDER BY money DESC";
        int repeats = 1;

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            while (!rs.getString("uuid").equals(uuid)) {
                rs.next();
                repeats++;
            }
            return repeats;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addCurrency(String item, int sellValue, int buyvalue, boolean incurrencylist, boolean canbuy, boolean cansell, boolean init) {
        if (init || !item.equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
            String sql = "INSERT INTO currencies(item,sellvalue,buyvalue,incurrencylist,canbuy,cansell) VALUES(?,?,?,?,?,?)";
            try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item);
                pstmt.setInt(2, sellValue);
                pstmt.setInt(3, buyvalue);
                pstmt.setBoolean(4, incurrencylist);
                pstmt.setBoolean(5, canbuy);
                pstmt.setBoolean(6, cansell);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    public boolean setSellValue(String item, int sellValue) {
        if (!item.equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
            String sql = "UPDATE currencies SET sellvalue = ? WHERE item = ?";
            try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sellValue);
                pstmt.setString(2, item);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean setBuyValue(String item, int buyValue) {
        if (!item.equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
            String sql = "UPDATE currencies SET buyvalue = ? WHERE item = ?";
            try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, buyValue);
                pstmt.setString(2, item);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean setInCurrencyList(String item, boolean inCurrencyList) {
        if (!item.equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
            String sql = "UPDATE currencies SET incurrencylist = ? WHERE item = ?";
            try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, inCurrencyList);
                pstmt.setString(2, item);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean setCanBuy(String item, boolean canBuy) {
        if (!item.equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
            String sql = "UPDATE currencies SET canbuy = ? WHERE item = ?";
            try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, canBuy);
                pstmt.setString(2, item);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean setCanSell(String item, boolean canSell) {
        if (!item.equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
            String sql = "UPDATE currencies SET cansell = ? WHERE item = ?";
            try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, canSell);
                pstmt.setString(2, item);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean isCurrency(String item) {
        String sql = "SELECT item FROM currencies WHERE item = '" + item + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return (rs.getString("item")).equals(item);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int getSellValue(String item) {
        String sql = "SELECT sellvalue FROM currencies WHERE item = '" + item + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getInt("sellvalue");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getBuyValue(String item) {
        String sql = "SELECT buyvalue FROM currencies WHERE item = '" + item + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getInt("buyvalue");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean getInCurrencyList(String item) {
        String sql = "SELECT incurrencylist FROM currencies WHERE item = '" + item + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getBoolean("incurrencylist");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean getCanBuy(String item) {
        String sql = "SELECT canbuy FROM currencies WHERE item = '" + item + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getBoolean("canbuy");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean getCanSell(String item) {
        String sql = "SELECT cansell FROM currencies WHERE item = '" + item + "'";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getBoolean("cansell");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeCurrency(String item) {
        if (!item.equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
            String sql = "DELETE FROM currencies WHERE item = ?";
            try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public CurrencyType getCurrency(String item) {
        String sql = "SELECT item, sellvalue, buyvalue, incurrencylist, canbuy, cansell FROM currencies WHERE item = '" + item + "'";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            int sellValue = rs.getInt("sellvalue");
            int buyValue = rs.getInt("buyvalue");
            boolean inCurrencyList = rs.getBoolean("incurrencylist");
            boolean canBuy = rs.getBoolean("canbuy");
            boolean canSell = rs.getBoolean("cansell");
            //System.out.println("item: " + item + ", depositVal: " + sellValue + ", withdrawVal: " + buyValue + ", inCurrencyList: " + inCurrencyList + ", canBuy: " + canBuy + ", canSell: " + canSell);
            return new CurrencyType(item, sellValue, buyValue, inCurrencyList, canBuy, canSell);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public CurrencyType getCurrency(int i) {
        String sql = "SELECT item, sellvalue, buyvalue, incurrencylist, canbuy, cansell FROM currencies ORDER BY buyvalue DESC";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            for (int j = -1; j < i; j++) {
                rs.next();
            }
            String item = rs.getString("item");
            int sellValue = rs.getInt("sellvalue");
            int buyValue = rs.getInt("buyvalue");
            boolean inCurrencyList = rs.getBoolean("incurrencylist");
            boolean canBuy = rs.getBoolean("canbuy");
            boolean canSell = rs.getBoolean("cansell");
            //System.out.println("item: " + item + ", depositVal: " + sellValue + ", withdrawVal: " + buyValue + ", inCurrencyList: " + inCurrencyList + ", canBuy: " + canBuy + ", canSell: " + canSell);
            return new CurrencyType(item, sellValue, buyValue, inCurrencyList, canBuy, canSell);
        } catch (SQLException e) {
            return null;
        }
    }
}

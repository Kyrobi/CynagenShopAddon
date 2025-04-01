package me.kyrobi.cynagenshopsearch.logging;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class SQLHelper {
    private static String DB_PATH;

    public static void initialize() {
        File file = new File("");
        DB_PATH = String.valueOf(new File(file.getAbsolutePath() + File.separator + "plugins" + File.separator + "CynagenShopAddon" + File.separator + "priceHistory.db"));

        File dbFile = new File(DB_PATH);

        String createListingTable = """
            CREATE TABLE IF NOT EXISTS listings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                time INTEGER NOT NULL,
                uuid TEXT NOT NULL,
                username TEXT NOT NULL,
                item TEXT NOT NULL,
                price REAL NOT NULL,
                amount INTEGER NOT NULL,
                type TEXT NOT NULL
            );
            """;

        String createPurchasesTable = """
            CREATE TABLE IF NOT EXISTS purchases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                time INTEGER NOT NULL,
                uuid TEXT NOT NULL,
                username TEXT NOT NULL,
                item TEXT NOT NULL,
                price REAL NOT NULL,
                amount INTEGER NOT NULL,
                type TEXT NOT NULL
            );
            """;

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            if (dbFile.exists()) {
                System.out.println("Database already exists. No action taken.");
            } else {
                System.out.println("A new database has been created.");
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createListingTable);
                stmt.execute(createPurchasesTable);
                System.out.println("Table 'warps' has been created (or already exists).");
            } catch (SQLException e) {
                System.out.println("Failed to create the table: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    public static void insert(String uuid, String username, String item, double price, int amount, String type){

        String sqlcommand = "INSERT INTO purchases (time, uuid, username, item, price, amount, type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";

        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)){
            PreparedStatement stmt = conn.prepareStatement(sqlcommand);
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, uuid);
            stmt.setString(3, username);
            stmt.setString(4, item);
            stmt.setDouble(5, price);
            stmt.setInt(6, amount);
            stmt.setString(7, type);
            stmt.executeUpdate();
            conn.close();
        }
        catch(SQLException error){
            System.out.println(error.getMessage());
        }
    }

    public static void bulkInsert(List<String> uuid, List<String> username, List<String> item, List<Double> price, List<Integer> amount, List<String> type){
        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)){
            // PreparedStatement update = conn.prepareStatement("UPDATE stats SET time = ? WHERE userID = ? AND serverID = ?");

            PreparedStatement updateOrInsertStatement = conn.prepareStatement(
                    "INSERT INTO listings (time, uuid, username, item, price, amount, type) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);"
            );

            // Disable auto-commit to enable batch processing
            conn.setAutoCommit(false);


            for (int i = 0; i < uuid.size(); i++){

                String _uuid = uuid.get(i);
                String _username = username.get(i);
                String _item = item.get(i);
                double _price = price.get(i);
                int _amount = amount.get(i);
                String _type = type.get(i);

                updateOrInsertStatement.setLong(1, System.currentTimeMillis());
                updateOrInsertStatement.setString(2, _uuid);
                updateOrInsertStatement.setString(3, _username);
                updateOrInsertStatement.setString(4, _item);
                updateOrInsertStatement.setDouble(5, _price);
                updateOrInsertStatement.setInt(6, _amount);
                updateOrInsertStatement.setString(7, _type);
                updateOrInsertStatement.addBatch();
            }

            // Execute the batch
            updateOrInsertStatement.executeBatch();


            // Commit the changes
            conn.commit();


            // Enable auto-commit again
            conn.setAutoCommit(true);

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

}

package com.example.inventory_system_java;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTable {
    public static void main(String[] args) {
        try(Connection connection = MySQLConnection.getConnection();
            Statement statement = connection.createStatement()){
            String createTableQuery = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(50) NOT NULL,"
                    + "password VARCHAR(100) NOT NULL)";
            statement.execute(createTableQuery);
            System.out.println("Table Create Successfully!");
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }
}

package com.example.inventory_system_java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        try(Connection connection = MySQLConnection.getConnection();
            Statement statement = connection.createStatement()){
            String createTableQuery = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(50) NOT NULL,"
                    + "password VARCHAR(100) NOT NULL)";
            statement.execute(createTableQuery);

            String InventoryQuery = "CREATE TABLE IF NOT EXISTS inventory ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "prodname VARCHAR(100) NOT NULL,"
                    + "prodqty INT(100) NOT NULL)";
            statement.executeUpdate(InventoryQuery);


            System.out.println("Tables Create Successfully!");
        } catch (SQLException e) {
            e.getStackTrace();
        }




        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
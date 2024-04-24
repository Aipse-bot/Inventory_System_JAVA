package com.example.inventory_system_java;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class HelloController {
    @FXML
    private Label MessageText;

    @FXML
    private TextField Username;

    @FXML
    private TextField Password;


    @FXML
    protected void onLogInButtonClick() throws IOException {
        try(Connection connection = MySQLConnection.getConnection();
            Statement statement = connection.createStatement()){
            int ctr = 0;

            String selectQuery = "SELECT * FROM users";
            ResultSet resultSet = statement.executeQuery(selectQuery);

            String name = Username.getText();
            String password = Password.getText();
            if(name.isEmpty() || password.isEmpty()){
                MessageText.setText("Please enter your username and password!");
            }else {

                while (resultSet.next()) {
                    String checkusername = resultSet.getString("name");
                    String checkpassword = resultSet.getString("password");
                    if (checkusername.equals(Username.getText()) && checkpassword.equals(Password.getText())) {
                        System.out.println("Login Successful!");
                        MessageText.setText("Login Successful!");
                        ctr++;

                    }
                }
                if (ctr == 0) {
                    MessageText.setText("Login Failed!");
                    System.out.println("Login Failed!");
                } else {

                    openInventory();
                }
            }
        } catch (SQLException e) {
            e.getStackTrace();
        }


    }

    @FXML
    protected void onRegisterButtonClick() {
        try(Connection connection = MySQLConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (name, password) VALUES (?, ?)")){
            String name = Username.getText();
            String password = Password.getText();
            if(name.isEmpty() || password.isEmpty()){
                MessageText.setText("Please enter your username and password!");
            }else{
                statement.setString(1,name);
                statement.setString(2, password);
                int rowsInserted = statement.executeUpdate();
                if(rowsInserted > 0){
                    System.out.println("Inserted Data Successfully!");
                }
                MessageText.setText("Registered Successfully!");
            }
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    private void openInventory() throws IOException {


        FXMLLoader InventoryLoader = new FXMLLoader(getClass().getResource("Inventory-view.fxml"));
        Parent root = (Parent) InventoryLoader.load();
        Stage INVstage = new Stage();
        INVstage.setTitle("Inventory System");
        INVstage.setScene(new Scene(root));
        INVstage.show();


    }
}
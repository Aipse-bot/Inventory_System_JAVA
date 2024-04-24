package com.example.inventory_system_java;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;


public class InventoryView {

    public TextField ProductName;
    public Label INVMessageText;

    @FXML
    TableView<Product> table;

    @FXML
    TableColumn<Product, String> ProdNameCol;

    @FXML
    TableColumn<Product, Integer> ProdQtyCol;

    @FXML
    private Label QTYNum;

    @FXML
    public void initialize() {
        getInventory();
        ProdNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        ProdQtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
        table.setItems(getInventory());

    }

    @FXML
    protected void OnplusClickBTN(){
        int prodqty = Integer.parseInt(QTYNum.getText());
        prodqty++;
        QTYNum.setText(String.valueOf(prodqty));
    }


    public void OnminusClickBTN(ActionEvent actionEvent) {
        int prodqty = Integer.parseInt(QTYNum.getText());
        if(prodqty == 0){
            QTYNum.setText(String.valueOf(prodqty));
        }else{
            prodqty--;
            QTYNum.setText(String.valueOf(prodqty));
        }


    }

    public void OnINVAddBTN(ActionEvent actionEvent) {
        try(Connection connection = MySQLConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO inventory (prodname, prodqty) VALUES  (?,?)")) {
            String Pname = ProductName.getText();
            int Qty = Integer.parseInt(QTYNum.getText());

            if(Pname.equals("")){
                INVMessageText.setText("Add Products to Inventory");
            } else if (Qty == 0) {
                INVMessageText.setText("You added none to Inventory");
            }
            else{
            String selectQuery = "SELECT * FROM inventory";
            ResultSet resultSet = statement.executeQuery(selectQuery);
            int ctr = 0;


            while (resultSet.next()) {
                String checkpname = resultSet.getString("prodname");
                if(checkpname.equals(Pname)){
                    ctr++;
                }
            }

            if(ctr == 0){
                INVMessageText.setText("Product added to Inventory");
                statement.setString(1, Pname);
                statement.setInt(2, Qty);
                statement.executeUpdate();

                getInventory();
                ProdNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
                ProdQtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
                table.setItems(getInventory());

            } else{
                INVMessageText.setText("Product Already Exists");
            }
            }
            } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void OnINVDelBTN(ActionEvent actionEvent) {
        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM inventory WHERE prodname = (?)")) {

            String Pname = ProductName.getText();


            if (Pname.isEmpty()) {
                INVMessageText.setText("Please enter a product name to delete.");
                return;
            }

            String checkQuery = "SELECT COUNT(*) FROM inventory WHERE prodname = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, Pname);
                ResultSet checkResultSet = checkStatement.executeQuery();
                if (checkResultSet.next() && checkResultSet.getInt(1) == 0) {
                    INVMessageText.setText("Product '" + Pname + "' does not exist.");
                    return;
                }
            }

            statement.setString(1, Pname);
            int rowsDeleted = statement.executeUpdate();
            getInventory();
            ProdNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            ProdQtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
            table.setItems(getInventory());

            if (rowsDeleted == 0) {
                INVMessageText.setText("Did not delete anything.");

            } else {
                INVMessageText.setText("Deleted " + rowsDeleted + " product(s).");
            }
        } catch (SQLException e) {
            INVMessageText.setText("Deletion failed: " + e.getMessage());

        }
    }


    public ObservableList<Product> getInventory(){
        ObservableList<Product> list = FXCollections.observableArrayList();

        try(Connection connection = MySQLConnection.getConnection();
            Statement statement = connection.createStatement()) {
            String selectQuery = "SELECT * FROM inventory";
            ResultSet resultSet = statement.executeQuery(selectQuery);
            while (resultSet.next()) {
                String Pname = resultSet.getString("prodname");
                int Qty = Integer.parseInt(resultSet.getString("prodqty"));
                list.add(new Product(Pname, Qty));

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }


}

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
        try (Connection connection = MySQLConnection.getConnection()) {
            String Pname = ProductName.getText();
            int Qty = Integer.parseInt(QTYNum.getText());

            if (Pname.isEmpty()) {
                INVMessageText.setText("Please enter a product name to add.");
                return;
            } else if (Qty == 0) {
                INVMessageText.setText("Please enter a valid quantity to add.");
                return;
            }

            String checkQuery = "SELECT COUNT(*) FROM inventory WHERE prodname = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, Pname);
                ResultSet checkResultSet = checkStatement.executeQuery();
                if (checkResultSet.next() && checkResultSet.getInt(1) == 0) {
                    INVMessageText.setText("Product '" + Pname + "' added to Inventory.");
                    insertProduct(connection, Pname, Qty);
                } else {
                    updateQuantity(connection, Pname, Qty);
                }
            }

            getInventory();
            ProdNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            ProdQtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
            table.setItems(getInventory());
        } catch (SQLException e) {
            INVMessageText.setText("Adding failed: " + e.getMessage());
        }
    }

    private void insertProduct(Connection connection, String Pname, int Qty) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO inventory (prodname, prodqty) VALUES (?,?)");
        insertStatement.setString(1, Pname);
        insertStatement.setInt(2, Qty);
        insertStatement.executeUpdate();
    }

    private void updateQuantity(Connection connection, String Pname, int Qty) throws SQLException {
        String updateQuery = "UPDATE inventory SET prodqty = prodqty + ? WHERE prodname = ?";
        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
        updateStatement.setInt(1, Qty);
        updateStatement.setString(2, Pname);
        int rowsUpdated = updateStatement.executeUpdate();

        if (rowsUpdated == 0) {
            INVMessageText.setText("An error while updating quantity for '" + Pname + "'.");
        } else {
            INVMessageText.setText("Quantity updated for '" + Pname + "'.");
        }
    }

    public void OnINVDelBTN(ActionEvent actionEvent) {
        try (Connection connection = MySQLConnection.getConnection()) {
            String Pname = ProductName.getText();
            int Qty = Integer.parseInt(QTYNum.getText());
            if (Pname.isEmpty()) {
                INVMessageText.setText("Please enter a product name to delete.");
                return;
            } else if (Qty <= 0) {
                INVMessageText.setText("Please enter a valid quantity to delete.");
                return;
            }
            String checkQuery = "SELECT prodqty FROM inventory WHERE prodname = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, Pname);
                ResultSet checkResultSet = checkStatement.executeQuery();
                if (!checkResultSet.next()) {
                    INVMessageText.setText("Product '" + Pname + "' does not exist.");
                    return;
                }
                int existingQty = checkResultSet.getInt("prodqty");

                if (existingQty >= Qty) {
                    String updateQuery = "UPDATE inventory SET prodqty = prodqty - ? WHERE prodname = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setInt(1, Qty);
                        updateStatement.setString(2, Pname);
                        int rowsUpdated = updateStatement.executeUpdate();

                        if (rowsUpdated == 1) {
                            int newQty = existingQty - Qty;
                            if (newQty <= 0) {
                                String deleteQuery = "DELETE FROM inventory WHERE prodname = ?";
                                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                                    deleteStatement.setString(1, Pname);
                                    int rowsDeleted = deleteStatement.executeUpdate();
                                    if (rowsDeleted == 1) {
                                        INVMessageText.setText("Product '" + Pname + "' deleted.");
                                    } else {
                                        INVMessageText.setText("An error during deletion.");
                                    }
                                }
                            } else {
                                INVMessageText.setText("Updated quantity for '" + Pname + "'.");
                            }
                        } else {
                            INVMessageText.setText("An error while updating quantity.");
                        }
                    }
                } else {
                    String deleteQuery = "DELETE FROM inventory WHERE prodname = ?";
                    try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                        deleteStatement.setString(1, Pname);
                        int rowsDeleted = deleteStatement.executeUpdate();
                        if (rowsDeleted == 1) {
                            INVMessageText.setText("Product '" + Pname + "' deleted.");
                        }
                    }
                }

                getInventory();
                ProdNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
                ProdQtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
                table.setItems(getInventory());
            } catch (SQLException e) {
                INVMessageText.setText("Deletion failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

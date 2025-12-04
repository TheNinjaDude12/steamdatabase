package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class GenerateCustomerReport {
    @FXML
    RadioButton yearRadio;
    @FXML
    RadioButton monthRadio;
    @FXML
    Spinner<Integer> yearSpinner;
    @FXML
    Spinner<Integer> monthSpinner;
    @FXML
    TableView<CustomerEngagement> reportTable;

    SpinnerValueFactory<Integer> yearValueFactory;
    SpinnerValueFactory<Integer> monthValueFactory;
    int year = 0;
    int month = 0;

    public void initialize() {
        // Setup year spinner
        yearValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2100, 2024);
        yearSpinner.setValueFactory(yearValueFactory);
        yearSpinner.setDisable(true);

        // Setup month spinner
        monthValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1);
        monthSpinner.setValueFactory(monthValueFactory);
        monthSpinner.setDisable(true);

        // Setup table columns
        setupTableColumns();

        // Year radio handler
        yearRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // Enable only year spinner
                yearSpinner.setDisable(false);
                yearSpinner.setEditable(true);
                monthSpinner.setDisable(true);  // Disable month when year is selected
            }
        });

        // Month radio handler
        monthRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // Enable both year AND month spinners
                yearSpinner.setDisable(false);
                yearSpinner.setEditable(true);
                if(monthSpinner.isDisable()) {
                    monthSpinner.setDisable(false);
                }
                else {
                    monthSpinner.setDisable(true);
                    monthSpinner.setValueFactory(monthValueFactory);
                }
                monthSpinner.setEditable(true);
            }
        });
    }
    private void setupTableColumns() {
        reportTable.getColumns().clear();

        TableColumn<CustomerEngagement, Integer> idCol = new TableColumn<>("Customer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        idCol.setPrefWidth(100);

        TableColumn<CustomerEngagement, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        nameCol.setPrefWidth(200);

        TableColumn<CustomerEngagement, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<CustomerEngagement, Integer> purchasesCol = new TableColumn<>("Total Purchases");
        purchasesCol.setCellValueFactory(new PropertyValueFactory<>("totalPurchases"));
        purchasesCol.setPrefWidth(130);

        TableColumn<CustomerEngagement, Double> spentCol = new TableColumn<>("Total Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        spentCol.setPrefWidth(120);

        reportTable.getColumns().addAll(idCol, nameCol, emailCol, purchasesCol, spentCol);
    }

    public void setYearSpinner() {
        yearSpinner.setDisable(false);
        yearSpinner.setEditable(true);
        yearSpinner.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                year = yearSpinner.getValue();
            }
        });
    }

    public void generateReport() {
        // Validate selection
        if (!yearRadio.isSelected() && !monthRadio.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "No Filter Selected",
                    "Please select either Year or Month filter");
            return;
        }

        // Get current spinner values
        year = yearSpinner.getValue();
        month = monthSpinner.getValue();

        ObservableList<CustomerEngagement> data;

        if (yearRadio.isSelected() && !monthRadio.isSelected()) {
            // Year filter only
            data = generateYearReport(year);
        } else {
            // Month filter (needs both year and month)
            data = generateMonthReport(year, month);
        }

        reportTable.setItems(data);

        if (data.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data",
                    "No transactions found for the selected period.");
        } else {
            String period = "";
            if(yearRadio.isSelected() && !monthRadio.isSelected()) {
                period = "Year " + year;

            }
            else {
                period = "Year " + year + ", month " + month;
            }
            showAlert(Alert.AlertType.INFORMATION, "Report Generated",
                    "Found " + data.size() + " customers in " + period);
        }
    }

    private ObservableList<CustomerEngagement> generateYearReport(int year) {
        ObservableList<CustomerEngagement> data = FXCollections.observableArrayList();

        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT customer_id, first_name, last_name, email, total_purchases, total_spent FROM customerEngagementReportYear WHERE YEAR = ? ");

                    pstmt.setInt(1, year);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                data.add(new CustomerEngagement(
                        rs.getInt("customer_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getInt("total_purchases"),
                        rs.getDouble("total_spent")
                ));
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error generating report: " + e.getMessage());
        }

        return data;
    }

    private ObservableList<CustomerEngagement> generateMonthReport(int year, int month) {
        ObservableList<CustomerEngagement> data = FXCollections.observableArrayList();

        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT customer_id, first_name, last_name, email, total_purchases, total_spent FROM customerEngagementReportMonth WHERE YEAR = ? AND MONTH = ?");

            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                data.add(new CustomerEngagement(
                        rs.getInt("customer_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getInt("total_purchases"),
                        rs.getDouble("total_spent")
                ));
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error generating report: " + e.getMessage());
        }

        return data;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for table data
    public static class CustomerEngagement {
        private int customerId;
        private String customerName;
        private String email;
        private int totalPurchases;
        private double totalSpent;

        public CustomerEngagement(int customerId, String customerName, String email,
                                  int totalPurchases, double totalSpent) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.email = email;
            this.totalPurchases = totalPurchases;
            this.totalSpent = totalSpent;
        }

        public int getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public String getEmail() { return email; }
        public int getTotalPurchases() { return totalPurchases; }
        public double getTotalSpent() { return totalSpent; }
    }

    public void back(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("customerView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
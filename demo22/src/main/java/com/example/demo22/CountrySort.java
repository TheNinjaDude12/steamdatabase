package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class CountrySort {

    @FXML
    TableView<countrySpent> reportTable;
    public void initialize() {

        setupTableColumns();
        reportTable.setEditable(false);

    }

    private void setupTableColumns() {
        reportTable.getColumns().clear();

        TableColumn<CountrySort.countrySpent, String> idCol = new TableColumn<>("Country");
        idCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        idCol.setPrefWidth(100);

        TableColumn<CountrySort.countrySpent, Double> nameCol = new TableColumn<>("Total Spent");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("spent"));
        nameCol.setPrefWidth(200);



        reportTable.getColumns().addAll(idCol,nameCol);
    }


    private ObservableList<CountrySort.countrySpent> generateCountryReport() {
        ObservableList<CountrySort.countrySpent> data = FXCollections.observableArrayList();

        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT country, SUM(total_spent) AS total_spent FROM customer_record GROUP BY country ORDER BY country ASC");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                data.add(new CountrySort.countrySpent(
                       rs.getString("country"),
                       rs.getDouble("total_spent")

                ));
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();

        }

        return data.sorted();
    }

    public void generateReport() {
        ObservableList<CountrySort.countrySpent> data;

        data = generateCountryReport();

        // Use SortedList and bind it
        SortedList<countrySpent> sortedData = new SortedList<>(data);
        sortedData.comparatorProperty().bind(reportTable.comparatorProperty());

        reportTable.setItems(sortedData);

        // Set initial sort
        TableColumn<countrySpent, ?> countryColumn = reportTable.getColumns().get(0);
        countryColumn.setSortType(TableColumn.SortType.ASCENDING);
        reportTable.getSortOrder().clear();
        reportTable.getSortOrder().add(countryColumn);

        if (data.isEmpty()) {
            System.out.println("No data found");
        }
    }
    public void back(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("generateCustomerReportView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void exportReport() {
        // Check if there's data to export
        if (reportTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data",
                    "Please generate a report first before exporting.");
            return;
        }

        // Create file chooser
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Country Spending Report");

        // Set initial filename
        String filename = "CountrySpendingReport_" +
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        fileChooser.setInitialFileName(filename);

        // Set file extension filter
        javafx.stage.FileChooser.ExtensionFilter txtFilter =
                new javafx.stage.FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(txtFilter);

        // Show save dialog
        java.io.File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                // Write header
                writer.println("COUNTRY SPENDING REPORT");
                writer.println("=".repeat(60));
                writer.println("Generated: " + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println("Total Countries: " + reportTable.getItems().size());
                writer.println("=".repeat(60));
                writer.println();

                // Write column headers
                writer.printf("%-30s %20s%n", "Country", "Total Spent");
                writer.println("-".repeat(60));

                // Write data rows
                double grandTotal = 0;

                for (countrySpent country : reportTable.getItems()) {
                    writer.printf("%-30s $%19.2f%n",
                            country.getCountry(),
                            country.getSpent());

                    grandTotal += country.getSpent();
                }

                // Write summary
                writer.println("-".repeat(60));
                writer.printf("%-30s $%19.2f%n", "GRAND TOTAL:", grandTotal);
                writer.println("=".repeat(60));

                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                        "Report exported to:\n" + file.getAbsolutePath());

            } catch (java.io.IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Failed to export report: " + e.getMessage());
            }
        }
    }

    // Helper method for showing alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static class countrySpent {
        private String country;
        private double total_spent;

        public countrySpent(String country, double total_spent) {
            this.country = country;
            this.total_spent = total_spent;

        }
        public String getCountry() {

            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }
        public double getSpent() {
            return total_spent;
        }
        public void setSpent(double spent) {
            this.total_spent = spent;
        }
    }

}



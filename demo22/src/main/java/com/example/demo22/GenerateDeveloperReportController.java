package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class GenerateDeveloperReportController {
    @FXML
    private RadioButton yearRadio;
    @FXML
    private RadioButton monthRadio;
    @FXML
    private Spinner<Integer> yearSpinner;
    @FXML
    private Spinner<Integer> monthSpinner;
    @FXML
    private TableView<DeveloperStats> reportTable;
    @FXML
    private ToggleGroup filterGroup;

    @FXML
    public void initialize() {
        // Initialize spinners
        SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2100, 2024);
        yearSpinner.setValueFactory(yearFactory);
        yearSpinner.setEditable(true);
        yearSpinner.setDisable(true);

        SpinnerValueFactory<Integer> monthFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1);
        monthSpinner.setValueFactory(monthFactory);
        monthSpinner.setEditable(true);
        monthSpinner.setDisable(true);

        setupTableColumns();

        // Radio button logic
        yearRadio.setOnAction(e -> {
            yearSpinner.setDisable(false);
            monthSpinner.setDisable(true);
        });

        monthRadio.setOnAction(e -> {
            yearSpinner.setDisable(false);
            monthSpinner.setDisable(false);
        });
    }

    private void setupTableColumns() {
        reportTable.getColumns().clear();

        TableColumn<DeveloperStats, Integer> idCol = new TableColumn<>("Dev ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("devId"));
        idCol.setPrefWidth(80);

        TableColumn<DeveloperStats, String> nameCol = new TableColumn<>("Developer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("devName"));
        nameCol.setPrefWidth(200);

        TableColumn<DeveloperStats, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        countryCol.setPrefWidth(120);

        TableColumn<DeveloperStats, Integer> unitsCol = new TableColumn<>("Units Sold");
        unitsCol.setCellValueFactory(new PropertyValueFactory<>("unitsSold"));
        unitsCol.setPrefWidth(100);

        TableColumn<DeveloperStats, Double> revenueCol = new TableColumn<>("Total Revenue");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        revenueCol.setPrefWidth(150);

        reportTable.getColumns().addAll(idCol, nameCol, countryCol, unitsCol, revenueCol);
    }

    @FXML
    public void generateReport(ActionEvent event) {
        commitSpinner(yearSpinner);
        commitSpinner(monthSpinner);

        if (!yearRadio.isSelected() && !monthRadio.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a filter (Year or Month).");
            return;
        }

        int year = yearSpinner.getValue();
        int month = monthSpinner.getValue();
        ObservableList<DeveloperStats> data;

        if (yearRadio.isSelected()) {
            data = getReportData(year, -1); // -1 indicates year only
        } else {
            data = getReportData(year, month);
        }

        reportTable.setItems(data);

        if (data.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data", "No sales found for the selected period.");
        }
    }

    private ObservableList<DeveloperStats> getReportData(int year, int month) {
        ObservableList<DeveloperStats> data = FXCollections.observableArrayList();
        String sql;

        if (month == -1) {
            sql = "SELECT d.developer_id, d.name, d.country, COUNT(t.transaction_id) as units_sold, SUM(t.amount) as total_revenue " +
                  "FROM developer_record d " +
                  "JOIN game_record g ON d.developer_id = g.developer_id " +
                  "JOIN transaction_log t ON g.game_id = t.game_id " +
                  "WHERE YEAR(t.purchase_date) = ? " +
                  "GROUP BY d.developer_id, d.name, d.country ORDER BY total_revenue DESC";
        } else {
            sql = "SELECT d.developer_id, d.name, d.country, COUNT(t.transaction_id) as units_sold, SUM(t.amount) as total_revenue " +
                  "FROM developer_record d " +
                  "JOIN game_record g ON d.developer_id = g.developer_id " +
                  "JOIN transaction_log t ON g.game_id = t.game_id " +
                  "WHERE YEAR(t.purchase_date) = ? AND MONTH(t.purchase_date) = ? " +
                  "GROUP BY d.developer_id, d.name, d.country ORDER BY total_revenue DESC";
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase", "root", "thunder1515");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            if (month != -1) {
                pstmt.setInt(2, month);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.add(new DeveloperStats(
                    rs.getInt("developer_id"), 
                    rs.getString("name"), 
                    rs.getString("country"),
                    rs.getInt("units_sold"), 
                    rs.getDouble("total_revenue")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
        return data;
    }

    private void commitSpinner(Spinner<Integer> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<Integer> valueFactory = spinner.getValueFactory();
        if (valueFactory != null && valueFactory.getConverter() != null) {
            try {
                Integer value = valueFactory.getConverter().fromString(text);
                valueFactory.setValue(value);
            } catch (Exception e) { }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("developerView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static class DeveloperStats {
        private final int devId;
        private final String devName;
        private final String country;
        private final int unitsSold;
        private final double totalRevenue;

        public DeveloperStats(int devId, String devName, String country, int unitsSold, double totalRevenue) {
            this.devId = devId;
            this.devName = devName;
            this.country = country;
            this.unitsSold = unitsSold;
            this.totalRevenue = totalRevenue;
        }

        public int getDevId() { return devId; }
        public String getDevName() { return devName; }
        public String getCountry() { return country; }
        public int getUnitsSold() { return unitsSold; }
        public double getTotalRevenue() { return totalRevenue; }
    }
}
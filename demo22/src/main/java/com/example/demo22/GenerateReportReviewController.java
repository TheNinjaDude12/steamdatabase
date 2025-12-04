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

public class GenerateReportReviewController {

    public TableView reportTable;
    public TableColumn customerNameColumn;
    public TableColumn gameNameColumn;
    public TableColumn reviewColumn;
    public TableColumn commentColumn;
    public RadioButton radio2024;
    public RadioButton radio2025;
    public ChoiceBox monthComboBox;

    private ToggleGroup yearGroup;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";

    @FXML
    public void initialize(){
        yearGroup = new ToggleGroup();
        radio2024.setToggleGroup(yearGroup);
        radio2025.setToggleGroup(yearGroup);
        radio2024.setSelected(true);

        ObservableList<String> months = FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
        monthComboBox.setItems(months);

        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        gameNameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("review"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
    }

    @FXML
    private void buttonGenerate(ActionEvent event){
        String selectedMonth = monthComboBox.getValue().toString();

        if (selectedMonth == null) {
            showAlert("Error", "Please select a month");
            return;
        }

        int year = radio2024.isSelected() ? 2024 : 2025;
        int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;

        loadReportData(year, month);
    }

    private void loadReportData(int year, int month) {
        String query = "SELECT c.first_name, c.last_name, g.title, r.rating, r.comment " +
                "FROM review_record r " +
                "JOIN customer_record c ON r.customer_id = c.customer_id " +
                "JOIN game_record g ON r.game_id = g.game_id " +
                "WHERE YEAR(r.date_posted) = ? AND MONTH(r.date_posted) = ? " +
                "ORDER BY r.date_posted DESC";

        ObservableList<ReviewReport> reportData = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, year);
            pstmt.setInt(2, month);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String customerName = lastName + " " + firstName;
                String gameName = rs.getString("title");
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");

                reportData.add(new ReviewReport(customerName, gameName, rating, comment));
            }

            reportTable.setItems(reportData);

            if (reportData.isEmpty()) {
                showAlert("Information", "No reviews found for the selected period");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading report data: " + e.getMessage());
        }
    }

    public void backButton (ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("game-review.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ReviewReport {
        private String customerName;
        private String gameName;
        private int review;
        private String comment;

        public ReviewReport(String customerName, String gameName, int review, String comment) {
            this.customerName = customerName;
            this.gameName = gameName;
            this.review = review;
            this.comment = comment;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getGameName() {
            return gameName;
        }

        public int getReview() {
            return review;
        }

        public String getComment() {
            return comment;
        }
    }
}

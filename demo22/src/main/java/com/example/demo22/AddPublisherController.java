package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

public class AddPublisherController {

    public TextField nameField;
    public TextField countryField;
    public DatePicker establishedDatePicker;
    public TextField websiteField;
    public TextField emailField;
    public TextField sizeField;
    public TextField specializationField;
    public CheckBox isActiveCheckBox;
    public TextField gamesPublishedField;
    public TextField shareField;
    public TextField budgetField;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";

    private boolean validateInputsAddPublisher(){
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name is required");
            return false;
        }

        String name = nameField.getText().trim();
        if (isPublisherNameExists(name)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Name",
                    name + "' already exists!\nPlease use a different name.");
            return false;
        }

        if (countryField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Country is required");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email is required");
            return false;
        }

        try {
            Integer.parseInt(sizeField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Size must be a valid number");
            return false;
        }

        if (!gamesPublishedField.getText().trim().isEmpty()){
            try {
                Integer.parseInt(gamesPublishedField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Games Published must be a valid number");
                return false;
            }
        }

        try {
            Double.parseDouble(shareField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Share must be a valid number");
            return false;
        }

        try {
            Float.parseFloat(budgetField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Budget must be a valid number");
            return false;
        }

        return true;
    }

    private boolean isPublisherNameExists(String name) {
        String sql = "SELECT COUNT(*) FROM publisher_record WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }

        } catch (Exception e) {
            System.err.println("Error checking publisher name: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public void addPublisher(ActionEvent event) {
        if (!validateInputsAddPublisher()) {
            return;
        }

        String name = nameField.getText().trim();
        String country = countryField.getText().trim();
        LocalDate localDate;
        if (establishedDatePicker.getValue() == null) {
            localDate = LocalDate.now();
        } else {
            localDate = establishedDatePicker.getValue();
        }
        Date establishedDate = Date.valueOf(localDate);
        String website = websiteField.getText().trim();
        String email = emailField.getText().trim();
        int size = Integer.parseInt(sizeField.getText().trim());
        String specialization = specializationField.getText().trim();
        boolean isActive = isActiveCheckBox.isSelected();
        int gamesPublished;
        if (gamesPublishedField.getText().trim().isEmpty()){
            gamesPublished = 0;
        } else {
            gamesPublished = Integer.parseInt(gamesPublishedField.getText().trim());
        }
        double share = Double.parseDouble(shareField.getText().trim());
        float budget = Float.parseFloat(budgetField.getText().trim());

        String sql = "INSERT INTO publisher_record (name, country, established_date, website, contact_email, " +
                "company_size, specialization, is_active, total_games_published, revenue_share_percentage, publisher_budget_range) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, country);
            pstmt.setDate(3, establishedDate);
            pstmt.setString(4, website);
            pstmt.setString(5, email);
            pstmt.setInt(6, size);
            pstmt.setString(7, specialization);
            pstmt.setBoolean(8, isActive);
            pstmt.setInt(9, gamesPublished);
            pstmt.setDouble(10, share);
            pstmt.setFloat(11, budget);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Publisher '" + name + "' added successfully!\nEstablished Date: " + localDate);
                clearTextAddPublisher();

            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add publisher");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error adding publisher: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void backButton (ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("publisher-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        clearTextAddPublisher();
    }

    private void clearTextAddPublisher(){
        nameField.clear();
        countryField.clear();
        establishedDatePicker.setValue(null);
        websiteField.clear();
        emailField.clear();
        sizeField.clear();
        specializationField.clear();
        isActiveCheckBox.setSelected(true);
        gamesPublishedField.clear();
        shareField.clear();
        budgetField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

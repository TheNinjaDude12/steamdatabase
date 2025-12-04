package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class DeveloperAdd {

    @FXML private TextField nameField;
    @FXML private TextField countryField;
    @FXML private TextField emailField;
    @FXML private TextField websiteField;
    @FXML private Text nameError;
    @FXML private Text countryError;
    @FXML private Text emailError;

    private String name;
    private String country;
    private String email;
    private String website;

    public boolean canAddDeveloper() {
        boolean isValid = true;
        nameError.setVisible(false);
        countryError.setVisible(false);
        emailError.setVisible(false);

        // Validate Name
        if (nameField.getText().isEmpty()) {
            nameError.setText("Name must not be empty");
            nameError.setVisible(true);
            isValid = false;
        } else if (isInteger(nameField.getText())) {
            nameError.setText("Name cannot be just a number");
            nameError.setVisible(true);
            isValid = false;
        } else {
            name = nameField.getText();
        }

        // Validate Country
        if (countryField.getText().isEmpty()) {
            countryError.setText("Country must not be empty");
            countryError.setVisible(true);
            isValid = false;
        } else if (isInteger(countryField.getText())) {
            countryError.setText("Country cannot be just a number");
            countryError.setVisible(true);
            isValid = false;
        } else {
            country = countryField.getText();
        }

        // Validate Email
        if (emailField.getText().isEmpty()) {
            emailError.setText("Email must not be empty");
            emailError.setVisible(true);
            isValid = false;
        } else if (!emailField.getText().contains("@") || !emailField.getText().contains(".")) {
            emailError.setText("Invalid email format");
            emailError.setVisible(true);
            isValid = false;
        } else {
            email = emailField.getText();
        }

        // Website is optional, no validation needed unless you want to check format
        website = websiteField.getText();

        return isValid;
    }

    @FXML
    public void addDeveloperData() {
        if (!canAddDeveloper()) {
            return;
        }

        String sql = "INSERT INTO developer_record(name, country, email, website) VALUES(?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement insertDeveloper = connection.prepareStatement(sql)) {

            insertDeveloper.setString(1, name);
            insertDeveloper.setString(2, country);
            insertDeveloper.setString(3, email);
            insertDeveloper.setString(4, website);
            insertDeveloper.executeUpdate();

            showSuccessAlert();
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add developer: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        countryField.clear();
        emailField.clear();
        websiteField.clear();
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Developer added successfully!");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void back(ActionEvent event) throws IOException {
        System.out.println("Returning to Developer Menu");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("developerView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
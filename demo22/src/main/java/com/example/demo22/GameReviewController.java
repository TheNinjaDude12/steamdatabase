package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class GameReviewController {

    public TextField emailField;
    public PasswordField passwordField;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";

    public void loginButton(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required",
                    "Please enter both email and password");
            return;
        }

        checkCustomer(email, password, event);
    }

    private void checkCustomer(String email, String password, ActionEvent event){
        String sql = "SELECT customer_id, first_name, last_name, email, password " +
                "FROM customer_record WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                if (password.equals(storedPassword)) {
                    int customerId = rs.getInt("customer_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String fullName = firstName + " " + lastName;

                    CurrentCustomer.getInstance().login(customerId, email, fullName);

                    switchToAddGameReview(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed",
                            "Incorrect password. Please try again.");
                    passwordField.clear();
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "No account found with this email address.");
            }

            rs.close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void switchToAddGameReview(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("customer-add-review.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void backButton(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToLogIn(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("log-in-review.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void backToGameReview(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("game-review.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        CurrentCustomer.getInstance().logout();
    }

    public void switchGenerateReport (ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("generate-report-review.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

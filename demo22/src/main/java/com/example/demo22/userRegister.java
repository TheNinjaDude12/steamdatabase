package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class userRegister {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField countryField;
    @FXML
    private TextField platformField;

    @FXML
    private Text passwordError;
    @FXML
    private Text emailError;
    @FXML
    private Text firstNameError;
    @FXML
    private Text lastNameError;
    @FXML
    private Text countryError;
    @FXML
    private RadioButton showPasswordRadio;
    @FXML
    private TextField passwordTextField;

    String firstName;
    String lastName;
    String email;
    String platform;
    String password;
    int year;
    int month;
    int day;
    String registerDate;
    String country;


    public void initialize() {
        passwordError.setVisible(false);
        emailError.setVisible(false);
        firstNameError.setVisible(false);
        lastNameError.setVisible(false);
        countryError.setVisible(false);
        showPasswordRadio.setSelected(false);
        passwordTextField.setDisable(true);


    }

    public boolean canAddCustomer() {
        if(firstNameError.isVisible()) {
            firstNameError.setVisible(false);
        }
        if(lastNameError.isVisible()) {
            lastNameError.setVisible(false);
        }
        if(emailError.isVisible()) {
            emailError.setVisible(false);
        }
        if(countryError.isVisible()) {
            countryError.setVisible(false);
        }



        boolean isValid = true;
        if(firstNameField.getText().isEmpty()) {
            firstNameError.setText("First name must not be empty");
            firstNameError.setVisible(true);
            isValid = false;
        }
        else if(isInteger(firstNameField.getText())) {
            firstNameError.setText("First name cannot be an integer");
            firstNameError.setVisible(true);
            isValid = false;
        }
        else {
            firstName = firstNameField.getText();

        }
        if(lastNameField.getText().isEmpty()) {
            lastNameError.setText("Last name must not be empty");
            lastNameError.setVisible(true);
            isValid = false;
        }
        else if(isInteger(lastNameField.getText())) {
            lastNameError.setText("Last name cannot be an integer");
            lastNameError.setVisible(true);
            isValid = false;
        }
        else {
            lastName = lastNameField.getText();
        }

        if(passwordField.getText().isEmpty() && passwordTextField.getText().isEmpty()) {
            passwordError.setVisible(true);
            passwordError.setText("Password must not be empty");
        }
        else {
            password = passwordField.getText();
        }
        if(emailField.getText().isEmpty()) {
            emailError.setText("Email must not be empty");
            emailError.setVisible(true);
            isValid = false;
        }
        else if(!isValidEmail(emailField.getText())) {  // ← Add email format check
            emailError.setText("Invalid email format");
            emailError.setVisible(true);
            isValid = false;
        }
        else if(emailExists(emailField.getText().toLowerCase())) {  // ← Add duplicate check
            emailError.setText("Email already registered");
            emailError.setVisible(true);
            isValid = false;
        }
        else {
            email = emailField.getText();
        }

        if(countryField.getText().isEmpty()) {
            countryError.setText("Country must not be empty");
            countryError.setVisible(true);
            isValid = false;

        } else if (isInteger(countryField.getText())) {
            countryError.setText("Country cannot be an integer");
            countryError.setVisible(true);
            isValid = false;

        } else {
            country = countryField.getText();
            String firstChar = country.substring(0,1).toUpperCase();
            country = firstChar + country.substring(1).toLowerCase();
        }
        platform = platformField.getText();


        return isValid;
    }
    public void addCustomer(ActionEvent event) throws IOException {
        boolean canAdd = canAddCustomer();
        if(!canAdd) {
            return;
        }



        System.out.println("TESTING");

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            PreparedStatement insertCustomer = connection.prepareStatement("INSERT INTO customer_record(last_name, first_name, email, registration_date, country, preferred_platform, password) " +
                    "VALUES(?,?,?,CURDATE(),?,?,?)");
            insertCustomer.setString(1, lastName);
            insertCustomer.setString(2, firstName);
            insertCustomer.setString(3, email.toLowerCase());
            insertCustomer.setString(4, country);
            if(!platform.isEmpty()) {
                insertCustomer.setString(5, platform);
            }
            else {
                insertCustomer.setString(5, "None");
            }

            String password;
            if(showPasswordRadio.isSelected()) {
                password = passwordTextField.getText();
            }
            else {
                password = passwordField.getText();
            }
            insertCustomer.setString(6, password);
            insertCustomer.executeUpdate();
            showSuccessAlert();
            System.out.println("works");
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userView.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();



        } catch(SQLException e) {
            e.printStackTrace();

        }


    }
    public void showPassword() {


        if(showPasswordRadio.isSelected()) {
            passwordField.setVisible(false);
            passwordTextField.setVisible(true);
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setDisable(false);
        }
        else {
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setDisable(true);

        }
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false; // An empty or null string cannot be an integer
        }
        try {
            Integer.parseInt(str); // Attempt to parse the string as an integer
            return true; // If successful, it's an integer
        } catch (NumberFormatException e) {
            return false; // If a NumberFormatException is caught, it's not a valid integer
        }
    }


    private boolean isValidEmail(String email) {
        // Simple email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean emailExists(String email) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            PreparedStatement checkEmail = connection.prepareStatement(
                    "SELECT COUNT(*) FROM customer_record WHERE email = ?");
            checkEmail.setString(1, email);
            ResultSet rs = checkEmail.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                connection.close();
                return true; // Email exists
            }

            connection.close();
            return false; // Email doesn't exist

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Customer added successfully!");
        alert.showAndWait();
    }
    public void back (ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }



}
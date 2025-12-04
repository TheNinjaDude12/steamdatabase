package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class UserLogIn {
    public static int user_id;
    @FXML
    private TextField userTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private RadioButton passwordRadioButton;
    @FXML
    private Button backButton;
    @FXML
    private Button loginButton;
    @FXML
    private Text invalidEmailText;
    @FXML
    private Text invalidPasswordText;
    String email;
    public void initialize() {
        System.out.println("Test");
        passwordTextField.setVisible(false);
        passwordTextField.setDisable(true);
        invalidPasswordText.setVisible(false);
        invalidEmailText.setVisible(false);



    }
    public void showPassword(ActionEvent event) {
        if(passwordRadioButton.isSelected()) {
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
    public void login(ActionEvent event) {
        invalidEmailText.setVisible(false);
        invalidPasswordText.setVisible(false);
        if(!userTextField.getText().isEmpty() && (!passwordTextField.getText().isEmpty() || !passwordField.getText().isEmpty())) {
            email = "'" + userTextField.getText() + "'";

            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                        "root", "thunder1515");
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM customer_record WHERE email = " +email );
                if(resultSet.next()) {
                    System.out.println(resultSet.getInt("customer_id"));
                    if(resultSet.getString("password").equals(passwordTextField.getText()) ||
                            resultSet.getString("password").equals(passwordField.getText())) {
                            System.out.println(resultSet.getString("password"));
                            System.out.println("works");
                            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userMenuOptions.fxml")));
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            Scene scene = new Scene(root);
                            user_id = resultSet.getInt("customer_id");
                            stage.setScene(scene);
                            stage.show();

                    }
                    else {
                        invalidPasswordText.setVisible(true);
                        invalidPasswordText.setText("Wrong password!");
                    }
                }
                else {
                    showAlert();
                    invalidEmailText.setVisible(true);
                    invalidEmailText.setText("Invalid email!");
                }


            }
            catch (SQLException | IOException e) {
                e.printStackTrace();
            }


        }
        else if((passwordTextField.getText().isEmpty() || passwordField.getText().isEmpty()) && !userTextField.getText().isEmpty()) {
            invalidPasswordText.setVisible(true);
            invalidPasswordText.setText("Password is empty!");
        }
        else if((!passwordTextField.getText().isEmpty() || !passwordField.getText().isEmpty()) && userTextField.getText().isEmpty()) {
            invalidEmailText.setVisible(true);
            invalidEmailText.setText("Email is empty!");
        }
        else {
            showEmptyAlert();
            return;

        }

    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Failed");
        alert.setHeaderText(null);
        alert.setContentText("User does not exist, please register");
        alert.showAndWait();
    }
    private void showEmptyAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Failed");
        alert.setHeaderText(null);
        alert.setContentText("Both fields must not be empty!");
        alert.showAndWait();
    }

    public void back(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}

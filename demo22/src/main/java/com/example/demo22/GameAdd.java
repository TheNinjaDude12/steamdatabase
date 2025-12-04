package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

public class GameAdd {

    @FXML
    private ComboBox<String> comboBox1;

    @FXML
    private ComboBox<String> comboBox2;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField textField1;

    @FXML
    private TextField textField2;

    @FXML
    private TextField textField3;

    @FXML
    private TextField textField4;

    @FXML
    private Text titleError;

    @FXML
    private Text statusError;

    @FXML
    private Text pubIDError;

    String title;
    String genre;
    LocalDate release_date;
    String platform;
    int price;
    String status;
    int publisherId;

    public boolean canAddGame(){
        boolean isValid = true;

        titleError.setVisible(false);
        statusError.setVisible(false);
        pubIDError.setVisible(false);

        if(textField1.getText().isEmpty()){
            titleError.setText("Title must not be empty");
            titleError.setVisible(true);
            isValid = false;
        }
        else{
            title = textField1.getText();
        }

        if(comboBox2.getValue().equals(null)){
            statusError.setText("Status must not be empty");
            statusError.setVisible(true);
            isValid = false;
        }

        if((textField4.getText().isEmpty())){
            pubIDError.setText("Publisher ID must not be empty");
            pubIDError.setVisible(true);
            isValid = false;
        }
        else if(!isString(textField4.getText())){
            pubIDError.setText("Publisher ID must be an integer");
            pubIDError.setVisible(true);
            isValid = false;
        }
        else{
            publisherId = Integer.parseInt(textField4.getText().toString());
        }

        genre = textField2.getText();
        release_date = datePicker.getValue();
        platform = comboBox1.getValue().toString();
        price = Integer.parseInt(textField3.getText().toString());

        return isValid;
    }

    @FXML
    public void initialize(){
        comboBox1.getItems().addAll("Console", "PC", "Mobile", "Switch", "VR");
        comboBox2.getItems().addAll("Beta", "Under Development", "Released");
    }

    @FXML
    public void addGameData(){
        if(!canAddGame()){
            return;
        }

        String sql = "INSERT INTO game_record(title, genre, release_date, platform, price, status, publisher_id) VALUES (?,?,?,?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement insertGame = connection.prepareStatement(sql)) {

            insertGame.setString(1, title);
            insertGame.setString(2, genre);
            insertGame.setDate(3, Date.valueOf(release_date));
            insertGame.setString(4, platform);
            insertGame.setInt(5, price);
            insertGame.setString(6, status);
            insertGame.setInt(7, publisherId);
            insertGame.executeUpdate();

            showSuccessAlert();
            clearFields();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add customer: " + e.getMessage());
        }
    }

    private void clearFields() {
        textField1.clear();
        textField2.clear();
        textField3.clear();
        textField4.clear();
        datePicker.setValue(null);
        comboBox1.setValue(null);
        comboBox2.setValue(null);
    }

    public boolean isString(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return true;
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Game added successfully!");
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
        System.out.println("Returning to Game Menu");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

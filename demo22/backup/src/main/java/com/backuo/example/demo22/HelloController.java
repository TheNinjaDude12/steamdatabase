package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloController {
    @FXML
    private Label welcomeText;


    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void switchToCustomerView(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("customerSelector.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void switchToBuyView(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("buyGameView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToDeveloperView(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("developerView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}
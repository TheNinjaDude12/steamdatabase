package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class UserView {
    public void switchToUserRegister(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userRegister.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void switchToUserLogIn(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userLogIn.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

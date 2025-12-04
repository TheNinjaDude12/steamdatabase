package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class DeveloperRecordController {

    public void viewDeveloperTable(ActionEvent event) throws IOException {
        System.out.println("Switching to Developer Table View");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("developerTable.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void addDeveloper(ActionEvent event) throws IOException {
        System.out.println("Switching to Add Developer View");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("developerAdd.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Add generateReport method later if needed
    // public void generateReport(ActionEvent event) throws IOException { ... }

    public void back(ActionEvent event) throws IOException {
        System.out.println("Returning to Main Menu");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
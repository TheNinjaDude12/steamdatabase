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
import java.sql.*;
import java.util.Objects;

public class HelloController {
    @FXML
    private Label welcomeText;


    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";

    public void switchToCustomerView(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("customerSelector.fxml")));
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
    public void switchToGameView(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("GameTable.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void switchToGameAddView(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("GameAdd.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToPubView(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("publisher-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "UPDATE publisher_record p " +
                    "SET p.total_games_published = (" +
                    "    SELECT COUNT(*) " +
                    "    FROM game_record g " +
                    "    WHERE g.publisher_id = p.publisher_id" +
                    ")";

            Statement stmt = conn.createStatement();
            int rowsUpdated = stmt.executeUpdate(sql);

            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void switchGameReview(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("game-review.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        String query = "UPDATE game_record g SET " +
                "g.review_average = (SELECT COALESCE(AVG(r.rating), 0) FROM review_record r WHERE r.game_id = g.game_id), " +
                "g.reviews = (SELECT COUNT(*) FROM review_record r WHERE r.game_id = g.game_id)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            int rowsAffected = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
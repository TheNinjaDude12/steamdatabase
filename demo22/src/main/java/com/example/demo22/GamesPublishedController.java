package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class GamesPublishedController {

    public TableView gamesTable;
    public TableColumn gameId;
    public TableColumn gameName;
    public TableColumn gameGenre;
    public TableColumn gameDate;
    public TableColumn gamePlatform;
    public TextField publisherIdField;
    public Label publisherNameLabel;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";

    public void initialize() {
        gameId.setCellValueFactory(new PropertyValueFactory<>("gameId"));
        gameName.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        gameGenre.setCellValueFactory(new PropertyValueFactory<>("gameGenre"));
        gameDate.setCellValueFactory(new PropertyValueFactory<>("gameDate"));
        gamePlatform.setCellValueFactory(new PropertyValueFactory<>("gamePlatform"));
    }

    public void viewGamesPublished(ActionEvent event){
        String idText = publisherIdField.getText().trim();

        if (idText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Publisher ID");
            return;
        }

        int publisherId;
        try {
            publisherId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Publisher ID must be a valid number");
            return;
        }
        if (publisherId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID", "Publisher ID must be a positive number");
            return;
        }

        String publisherName = getPublisherName(publisherId);
        if (publisherName == null) {
            showAlert(Alert.AlertType.WARNING, "Not Found",
                    "No publisher found with ID: " + publisherId);
            publisherNameLabel.setText("Publisher Name");
            gamesTable.getItems().clear();
            return;
        }
        publisherNameLabel.setText(publisherName);
        loadGamesForPublisher(publisherId);
    }

    private String getPublisherName(int publisherId) {
        String sql = "SELECT name FROM publisher_record WHERE publisher_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, publisherId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error retrieving publisher name: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void loadGamesForPublisher(int publisherId) {
        gamesTable.getItems().clear();

        String sql = "SELECT game_id, title, genre, release_date, platform " +
                "FROM game_record WHERE publisher_id = ? ORDER BY game_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, publisherId);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    int gameId = rs.getInt("game_id");
                    String gameName = rs.getString("title");
                    String genre = rs.getString("genre");
                    Date releaseDate = rs.getDate("release_date");
                    String platform = rs.getString("platform");

                    Game game = new Game(gameId, gameName, genre, releaseDate, platform);
                    gamesTable.getItems().add(game);
                    count++;
                }

                if (count > 0) {

                } else {
                    showAlert(Alert.AlertType.INFORMATION, "No Games Found",
                            "This publisher hasn't published any games yet");
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error loading games: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void backButton (ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("publisher-view.fxml")));
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

    public static class Game {
        private int gameId;
        private String gameName;
        private String gameGenre;
        private Date gameDate;
        private String gamePlatform;

        public Game(int gameId, String gameName, String gameGenre, Date gameDate, String gamePlatform) {
            this.gameId = gameId;
            this.gameName = gameName;
            this.gameGenre = gameGenre;
            this.gameDate = gameDate;
            this.gamePlatform = gamePlatform;
        }
        public int getGameId() {
            return gameId;
        }
        public String getGameName() {
            return gameName;
        }

        public String getGameGenre() {
            return gameGenre;
        }

        public Date getGameDate() {
            return gameDate;
        }

        public String getGamePlatform() {
            return gamePlatform;
        }

    }
}

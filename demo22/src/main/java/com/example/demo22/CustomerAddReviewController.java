package com.example.demo22;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

public class CustomerAddReviewController {

    public TableView gamesTable;
    public TableColumn gameIdColumn;
    public TableColumn gameNameColumn;
    public TableColumn purchasedDateColumn;
    public TextArea reviewTextArea;
    public TextField ratingField;
    public TextField gameIdField;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";

    @FXML
    public void initialize() {
        gameIdColumn.setCellValueFactory(new PropertyValueFactory<>("gameId"));
        gameNameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        purchasedDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));

        loadReviewableGames();
    }

    private void loadReviewableGames() {
        gamesTable.getItems().clear();

        int customerId = CurrentCustomer.getInstance().getCustomerId();

        String sql = "SELECT DISTINCT g.game_id, g.title, t.purchase_date " +
                "FROM game_record g " +
                "INNER JOIN transaction_log t ON g.game_id = t.game_id " +
                "WHERE t.customer_id = ? " +
                "AND NOT EXISTS (" +
                "    SELECT 1 FROM review_record r " +
                "    WHERE r.game_id = g.game_id " +
                "    AND r.customer_id = t.customer_id" +
                ") " +
                "ORDER BY g.game_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int gameId = rs.getInt("game_id");
                String gameName = rs.getString("title");
                Date purchaseDate = rs.getDate("purchase_date");

                ReviewableGame game = new ReviewableGame(gameId, gameName, purchaseDate);
                gamesTable.getItems().add(game);
                count++;
            }

            if (count == 0) {
                showAlert(Alert.AlertType.INFORMATION, "No Games",
                        "You have no games available for review.\n\n" +
                                "Either you haven't purchased any games, or you've already reviewed all your games!");
            }

            rs.close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error loading games: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void submitReview(ActionEvent event) {
        int customerId = CurrentCustomer.getInstance().getCustomerId();

        String gameIdText = gameIdField.getText().trim();
        if (gameIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required",
                    "Please enter a Game ID from the table");
            return;
        }

        int gameId;
        try {
            gameId = Integer.parseInt(gameIdText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Game ID must be a valid number");
            return;
        }

        String ratingText = ratingField.getText().trim();
        if (ratingText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Rating Required",
                    "Please enter a rating (1-5)");
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Rating",
                    "Rating must be a number");
            return;
        }

        if (rating < 1 || rating > 5) {
            showAlert(Alert.AlertType.ERROR, "Invalid Rating",
                    "Rating must be between 1 and 5 stars.\n\n" +
                            "You entered: " + rating);
            return;
        }

        String reviewText = reviewTextArea.getText().trim();
        if (reviewText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Review Required",
                    "Please write a review");
            return;
        }

        if (reviewText.length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Review Too Short",
                    "Review must be at least 10 characters long.\n\n" +
                            "Current length: " + reviewText.length() + " characters");
            return;
        }

        if (!canReviewGame(customerId, gameId)) {
            return;
        }

        insertReview(customerId, gameId, rating, reviewText);
    }

    private boolean canReviewGame(int customerId, int gameId) {
        String ownsSql = "SELECT COUNT(*) as count FROM transaction_log " +
                "WHERE customer_id = ? AND game_id = ?";

        String reviewedSql = "SELECT COUNT(*) as count FROM review_record " +
                "WHERE customer_id = ? AND game_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ownsStmt = conn.prepareStatement(ownsSql);
             PreparedStatement reviewedStmt = conn.prepareStatement(reviewedSql)) {

            ownsStmt.setInt(1, customerId);
            ownsStmt.setInt(2, gameId);
            ResultSet ownsRs = ownsStmt.executeQuery();

            if (ownsRs.next() && ownsRs.getInt("count") == 0) {
                showAlert(Alert.AlertType.ERROR, "Cannot Review",
                        "You don't own this game!\n\n" +
                                "Game ID " + gameId + " is not in your purchase history.");
                ownsRs.close();
                return false;
            }
            ownsRs.close();

            reviewedStmt.setInt(1, customerId);
            reviewedStmt.setInt(2, gameId);
            ResultSet reviewedRs = reviewedStmt.executeQuery();

            if (reviewedRs.next() && reviewedRs.getInt("count") > 0) {
                showAlert(Alert.AlertType.ERROR, "Already Reviewed",
                        "You have already reviewed this game!\n\n" +
                                "Game ID " + gameId + " cannot be reviewed again.");
                reviewedRs.close();
                return false;
            }
            reviewedRs.close();

            boolean gameInTable = false;
            for (Object obj : gamesTable.getItems()) {
                if (obj instanceof ReviewableGame) {
                    ReviewableGame game = (ReviewableGame) obj;
                    if (game.getGameId() == gameId) {
                        gameInTable = true;
                        break;
                    }
                }
            }

            if (!gameInTable) {
                showAlert(Alert.AlertType.WARNING, "Game Not Found",
                        "Game ID " + gameId + " is not in your reviewable games list.\n\n" +
                                "Please select a Game ID from the table.");
                return false;
            }

            return true;

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error checking game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void insertReview(int customerId, int gameId, int rating, String reviewText) {
        String sql = "INSERT INTO review_record (customer_id, game_id, rating, comment, date_posted) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, gameId);
            pstmt.setInt(3, rating);
            pstmt.setString(4, reviewText);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                gameIdField.clear();
                ratingField.clear();
                reviewTextArea.clear();
                loadReviewableGames();
                updateGameReviews();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit review");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error submitting review: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateGameReviews() {
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

    public void switchToDeleteReview(ActionEvent event) throws IOException {
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("delete-review.fxml")));
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

    public static class ReviewableGame {
        private int gameId;
        private String gameName;
        private Date purchaseDate;

        public ReviewableGame(int gameId, String gameName, Date purchaseDate) {
            this.gameId = gameId;
            this.gameName = gameName;
            this.purchaseDate = purchaseDate;
        }

        public int getGameId() {
            return gameId;
        }

        public String getGameName() {
            return gameName;
        }

        public Date getPurchaseDate() {
            return purchaseDate;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

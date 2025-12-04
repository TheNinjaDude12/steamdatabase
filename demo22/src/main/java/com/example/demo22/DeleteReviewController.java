package com.example.demo22;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class DeleteReviewController {

    public TableView reviewsTable;
    public TableColumn reviewIdColumn;
    public TableColumn customerNameColumn;
    public TableColumn gameNameColumn;
    public TableColumn ratingColumn;
    public TableColumn commentColumn;
    public TextField reviewIdField;
    public Button deleteButton;
    public Button backButton;

    public int customerId;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";

    @FXML
    public void initialize() {
        if (!CurrentCustomer.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Not Logged In",
                    "Please login first to access this page");
            return;
        }

        reviewIdColumn.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        gameNameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        loadCustomerReviews();
    }

    private void loadCustomerReviews() {
        reviewsTable.getItems().clear();

        customerId = CurrentCustomer.getInstance().getCustomerId();

        // SQL query to get customer's reviews with customer name and game name
        String sql = "SELECT r.review_id, " +
                "CONCAT(c.last_name, ', ', c.first_name) as customer_name, " +
                "g.title, " +
                "r.rating, " +
                "r.comment " +
                "FROM review_record r " +
                "INNER JOIN customer_record c ON r.customer_id = c.customer_id " +
                "INNER JOIN game_record g ON r.game_id = g.game_id " +
                "WHERE r.customer_id = ? " +
                "ORDER BY r.date_posted DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int reviewId = rs.getInt("review_id");
                String customerName = rs.getString("customer_name");
                String gameName = rs.getString("title");
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");

                CustomerReview review = new CustomerReview(reviewId, customerName, gameName, rating, comment);
                reviewsTable.getItems().add(review);
                count++;
            }

            if (count == 0) {
                showAlert(Alert.AlertType.INFORMATION, "No Reviews",
                        "You haven't written any reviews yet!");
            }

            rs.close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error loading reviews: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteReviewButton(javafx.event.ActionEvent event){

        String reviewIdText = reviewIdField.getText().trim();
        if (reviewIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required",
                    "Please enter a Review ID from the table");
            return;
        }

        int reviewId;
        try {
            reviewId = Integer.parseInt(reviewIdText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Review ID must be a valid number");
            return;
        }

        if (reviewId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID",
                    "Review ID must be a positive number");
            return;
        }

        if (!canDeleteReview(customerId, reviewId)) {
            return;
        }

        CustomerReview reviewToDelete = getReviewById(reviewId);
        if (reviewToDelete == null) {
            showAlert(Alert.AlertType.ERROR, "Not Found",
                    "Review ID " + reviewId + " not found in your reviews");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Review?");
        confirmAlert.setContentText("Are you sure you want to delete this review?\n\n" +
                "Review ID: " + reviewId + "\n" +
                "Game: " + reviewToDelete.getGameName() + "\n" +
                "Rating: " + reviewToDelete.getRating() + " stars\n\n" +
                "This action cannot be undone!");

        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        deleteReviewFromDatabase(reviewId, reviewToDelete.getGameName());
    }

    private boolean canDeleteReview(int customerId, int reviewId) {
        String sql = "SELECT customer_id FROM review_record WHERE review_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // Review doesn't exist
                showAlert(Alert.AlertType.ERROR, "Not Found",
                        "Review ID " + reviewId + " does not exist");
                rs.close();
                return false;
            }

            int reviewCustomerId = rs.getInt("customer_id");
            rs.close();

            if (reviewCustomerId != customerId) {
                // Review belongs to another customer
                showAlert(Alert.AlertType.ERROR, "Cannot Delete",
                        "You can only delete your own reviews!\n\n" +
                                "Review ID " + reviewId + " belongs to another customer.");
                return false;
            }

            // Check if review is in the table
            boolean reviewInTable = false;
            for (Object obj : reviewsTable.getItems()) {
                if (obj instanceof CustomerReview) {
                    CustomerReview review = (CustomerReview) obj;
                    if (review.getReviewId() == reviewId) {
                        reviewInTable = true;
                        break;
                    }
                }
            }

            if (!reviewInTable) {
                showAlert(Alert.AlertType.WARNING, "Not Found",
                        "Review ID " + reviewId + " is not in your reviews list.\n\n" +
                                "Please select a Review ID from the table.");
                return false;
            }

            return true;

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error checking review: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private CustomerReview getReviewById(int reviewId) {
        for (Object obj : reviewsTable.getItems()) {
            if (obj instanceof CustomerReview) {
                CustomerReview review = (CustomerReview) obj;
                if (review.getReviewId() == reviewId) {
                    return review;
                }
            }
        }
        return null;
    }

    private void deleteReviewFromDatabase(int reviewId, String gameName) {
        String sql = "DELETE FROM review_record WHERE review_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reviewId);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Review Deleted",
                        "Your review has been deleted successfully!\n\n" +
                                "Review ID: " + reviewId + "\n" +
                                "Game: " + gameName);

                // Clear the text field
                reviewIdField.clear();

                // Refresh the table
                loadCustomerReviews();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete review");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error deleting review: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void backButton (javafx.event.ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("customer-add-review.fxml")));
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

    public static class CustomerReview {
        private int reviewId;
        private String customerName;
        private String gameName;
        private int rating;
        private String comment;

        public CustomerReview(int reviewId, String customerName, String gameName, int rating, String comment) {
            this.reviewId = reviewId;
            this.customerName = customerName;
            this.gameName = gameName;
            this.rating = rating;
            this.comment = comment;
        }

        // Getters
        public int getReviewId() {
            return reviewId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getGameName() {
            return gameName;
        }

        public int getRating() {
            return rating;
        }

        public String getComment() {
            return comment;
        }
    }
}

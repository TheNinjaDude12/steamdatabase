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
import java.util.Date;
import java.util.Objects;

public class PublisherController {

    public TableView<publisherTable.Publisher> publisher_table;
    public TableColumn<publisherTable.Publisher, Integer> publisher_id;
    public TableColumn<publisherTable.Publisher, String> publisher_name;
    public TableColumn<publisherTable.Publisher, String> publisher_country;
    public TableColumn<publisherTable.Publisher, Date> publisher_esdate;
    public TableColumn<publisherTable.Publisher, String> publisher_website;
    public TableColumn<publisherTable.Publisher, String> publisher_email;
    public TableColumn<publisherTable.Publisher, Integer> publisher_size;
    public TableColumn<publisherTable.Publisher, String> publisher_specialization;
    public TableColumn<publisherTable.Publisher, Boolean> publisher_active;
    public TableColumn<publisherTable.Publisher, Integer> publisher_gamespublished;
    public TableColumn<publisherTable.Publisher, Double> publisher_share;
    public TableColumn<publisherTable.Publisher, Float> publisher_budget;

    public TextField searchIDField;
    public TextField deleteIDField;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "thunder1515";


    @FXML
    public void initialize(){
        publisher_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        publisher_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        publisher_country.setCellValueFactory(new PropertyValueFactory<>("country"));
        publisher_esdate.setCellValueFactory(new PropertyValueFactory<>("establishedDate"));
        publisher_website.setCellValueFactory(new PropertyValueFactory<>("website"));
        publisher_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        publisher_size.setCellValueFactory(new PropertyValueFactory<>("size"));
        publisher_specialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        publisher_active.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        publisher_gamespublished.setCellValueFactory(new PropertyValueFactory<>("gamesPublished"));
        publisher_share.setCellValueFactory(new PropertyValueFactory<>("share"));
        publisher_budget.setCellValueFactory(new PropertyValueFactory<>("budget"));
    }

    @FXML
    public void searchPublisherButton(ActionEvent event){
        String idText = searchIDField.getText().trim();
        int publisherId;

        if(idText.isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Publisher ID");
            return;
        }
        try {
            publisherId = Integer.parseInt(idText);
        } catch (NumberFormatException e){
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Publisher ID must be a valid number");
            return;
        }
        if (publisherId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID", "Publisher ID must be a positive number");
            return;
        }

        searchPublisherById(publisherId);
    }

    private void searchPublisherById(int id){
        publisher_table.getItems().clear();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Prepare SQL query with parameter
            String sql = "SELECT * FROM publisher_record WHERE publisher_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int publisherId = rs.getInt("publisher_id");
                String name = rs.getString("name");
                String country = rs.getString("country");
                Date establishedDate = rs.getDate("established_date");
                String website = rs.getString("website");
                String email = rs.getString("contact_email");
                int size = rs.getInt("company_size");
                String specialization = rs.getString("specialization");
                boolean isActive = rs.getBoolean("is_active");
                int gamesPublished = rs.getInt("total_games_published");
                double share = rs.getDouble("revenue_share_percentage");
                float budget = rs.getFloat("publisher_budget_range");

                publisherTable.Publisher publisher = new publisherTable.Publisher(
                        publisherId, name, country, establishedDate, website, email,
                        size, specialization, isActive, gamesPublished, share, budget
                );

                publisher_table.getItems().add(publisher);

            } else {
                // Publisher not found
                showAlert(Alert.AlertType.WARNING, "Not Found",
                        "No publisher found with ID: " + id);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error searching for publisher: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadPublishersFromDatabase(){
        publisher_table.getItems().clear();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            stmt = conn.createStatement();
            String sql = "SELECT * FROM publisher_record";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("publisher_id");
                String name = rs.getString("name");
                String country = rs.getString("country");
                Date establishedDate = rs.getDate("established_date");
                String website = rs.getString("website");
                String email = rs.getString("contact_email");
                int size = rs.getInt("company_size");
                String specialization = rs.getString("specialization");
                boolean isActive = rs.getBoolean("is_active");
                int gamesPublished = rs.getInt("total_games_published");
                double share = rs.getDouble("revenue_share_percentage");
                float budget = rs.getFloat("publisher_budget_range");

                publisherTable.Publisher publisher = new publisherTable.Publisher(
                        id, name, country, establishedDate, website, email,
                        size, specialization, isActive, gamesPublished, share, budget
                );
                publisher_table.getItems().add(publisher);
            }

        } catch (Exception e) {
            System.err.println("Error loading data from database:");
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deletePublisherIfNoGames(int publisherId) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement deleteStmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String checkSql = "SELECT name, total_games_published FROM publisher_record WHERE publisher_id = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, publisherId);
            rs = checkStmt.executeQuery();

            if (!rs.next()) {
                showAlert(Alert.AlertType.WARNING, "Not Found",
                        "No publisher found with ID: " + publisherId);
                return;
            }

            String publisherName = rs.getString("name");
            int gamesPublished = rs.getInt("total_games_published");
            if (gamesPublished > 0) {
                showAlert(Alert.AlertType.ERROR, "Cannot Delete",
                        "Cannot delete publisher '" + publisherName + "'!\n\n" +
                                "This publisher has " + gamesPublished + " game(s) published.\n" +
                                "Only publishers with 0 games can be deleted.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Publisher?");
            confirmAlert.setContentText("Are you sure you want to delete:\n\n" +
                    "ID: " + publisherId + "\n" +
                    "Name: " + publisherName + "\n" +
                    "Games Published: 0\n\n" +
                    "This action cannot be undone!");


            if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                return;
            }

            String deleteSql = "DELETE FROM publisher_record WHERE publisher_id = ?";
            deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, publisherId);

            int rowsDeleted = deleteStmt.executeUpdate();

            if (rowsDeleted > 0) {
                deleteIDField.clear();
                loadPublishersFromDatabase();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete publisher");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error deleting publisher: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (deleteStmt != null) deleteStmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void deletePublisherButton(ActionEvent event) {
        String idText = deleteIDField.getText().trim();

        if (idText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required",
                    "Please enter a Publisher ID to delete");
            return;
        }

        int publisherId;
        try {
            publisherId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Publisher ID must be a valid number");
            return;
        }

        if (publisherId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID",
                    "Publisher ID must be a positive number");
            return;
        }

        deletePublisherIfNoGames(publisherId);
    }

    public void viewAllDetails(ActionEvent event) throws IOException {
        loadPublishersFromDatabase();
    }

    public void backButton (ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    public void  switchGamesPublished(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("games-published-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchAddPublisher(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("add-publisher.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}

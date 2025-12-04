package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;


public class GameTable {

    @FXML
    private TableView<Game> table;

    public static class Game{
        private int gameId;
        private String title;
        private String genre;
        private String releaseDate;
        private String platform;
        private float price;
        private int totalBought;
        private String status;
        private float avgReview;
        private int reviews;
        private int publisherId;

        Game(int gameId, String title, String genre, String releaseDate, String platform, float price,
             int totalBought, String status, float avgReview, int reviews, int publisherId){
            this.gameId = gameId;
            this.title = title;
            this.genre = genre;
            this.releaseDate = releaseDate;
            this.platform = platform;
            this.price = price;
            this.totalBought = totalBought;
            this.status = status;
            this.avgReview = avgReview;
            this.reviews = reviews;
            this.publisherId = publisherId;
        }

        public int getGameId() { return gameId; }
        public String getTitle() { return title; }
        public String getGenre() { return genre; }
        public String getReleaseDate() { return releaseDate; }
        public String getPlatform() { return platform; }
        public float getPrice() { return price; }
        public int getTotalBought() { return totalBought; }
        public String getStatus() { return status; }
        public float getAvgReview() { return avgReview; }
        public int getReviews() { return reviews; }
        public int getPublisherId() { return publisherId; }

        public void setGameId(int gameId) { this.gameId = gameId; }
        public void setTitle(String title) { this.title = title; }
        public void setGenre(String genre) { this.genre = genre; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
        public void setPlatform(String platform) { this.platform = platform; }
        public void setPrice(float price) { this.price = price; }
        public void setTotalBought(int totalBought) { this.totalBought = totalBought; }
        public void setStatus(String status) { this.status = status; }
        public void setAvgReview(float avgReview) { this.avgReview = avgReview; }
        public void setReviews(int reviews) { this.reviews = reviews; }
        public void setPublisherId(int publisherId) { this.publisherId = publisherId;}
    }

    public void initialize(){
        TableColumn<Game, Integer> gameIdCol = new TableColumn<>("GAME ID");
        TableColumn<Game, String> titleCol = new TableColumn<>("TITLE");
        TableColumn<Game, String> genreCol = new TableColumn<>("GENRE");
        TableColumn<Game, String> releaseDateCol = new TableColumn<>("RELEASE DATE");
        TableColumn<Game, String> platformCol = new TableColumn<>("PLATFORM");
        TableColumn<Game, Float> priceCol = new TableColumn<>("PRICE");
        TableColumn<Game, Integer> totalBoughtCol = new TableColumn<>("TOTAL BOUGHT");
        TableColumn<Game, String> statusCol = new TableColumn<>("STATUS");
        TableColumn<Game, Float> avgReviewCol = new TableColumn<>("RATINGS");
        TableColumn<Game, Integer> reviewsCol = new TableColumn<>("REVIEWS");
        TableColumn<Game, Integer> publisherIdCol = new TableColumn<>("PUBLISHER ID");

        table.getColumns().addAll(gameIdCol, titleCol, genreCol, releaseDateCol, platformCol, priceCol,
                                  totalBoughtCol, statusCol, avgReviewCol, reviewsCol, publisherIdCol);

        gameIdCol.setCellValueFactory(new PropertyValueFactory<>("gameId"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        releaseDateCol.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        platformCol.setCellValueFactory(new PropertyValueFactory<>("platform"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalBoughtCol.setCellValueFactory(new PropertyValueFactory<>("totalBought"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        avgReviewCol.setCellValueFactory(new PropertyValueFactory<>("avgReview"));
        reviewsCol.setCellValueFactory(new PropertyValueFactory<>("reviews"));
        publisherIdCol.setCellValueFactory(new PropertyValueFactory<>("publisherId"));

        loadGames();

        table.setEditable(true);
        setupEditableColumns();
        setupActionButtonColumn();
    }

    private void setupEditableColumns(){
        //Title
        TableColumn<Game, String> titleCol = (TableColumn<Game, String>) table.getColumns().get(1);
        titleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        titleCol.setOnEditCommit(t -> {
            if(t.getNewValue() != null && !t.getNewValue().isEmpty()){
                Game game = t.getRowValue();
                game.setTitle(t.getNewValue());
                alterGameData(game.getTitle(), game.getGameId(), "title");
            }
            else{
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Title cannot be empty.");
                t.getTableView().getItems().set(t.getTablePosition().getRow(), t.getRowValue());
            }
        });
        //Genre
        TableColumn<Game, String> genreCol = (TableColumn<Game, String>) table.getColumns().get(2);
        genreCol.setCellFactory(TextFieldTableCell.forTableColumn());
        genreCol.setOnEditCommit(t -> {
            Game game = t.getRowValue();
            game.setGenre(t.getNewValue()); //Genre can be empty
            alterGameData(game.getGenre(), game.getGameId(), "genre");
        });

        //Release Date
        TableColumn<Game, String> releaseDateCol = (TableColumn<Game, String>) table.getColumns().get(3);
        releaseDateCol.setCellFactory(column -> new TableCell<>() {
            private final DatePicker datePicker = new DatePicker();
            {
                datePicker.setOnAction(event -> {
                    if (datePicker.getValue() != null) {
                        commitEdit(datePicker.getValue().toString());
                    }
                });
                datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused && datePicker.getValue() != null) {
                        commitEdit(datePicker.getValue().toString());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        datePicker.setValue(LocalDate.parse(item));
                        setText(null);
                        setGraphic(datePicker);
                    } else {
                        setText(item);
                        setGraphic(null);
                    }
                }
            }
            @Override
            public void startEdit() {
                super.startEdit();
                String currentValue = getItem();
                if (currentValue != null) {
                    datePicker.setValue(LocalDate.parse(currentValue));
                }
                setText(null);
                setGraphic(datePicker);
                datePicker.requestFocus();
            }
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }
        });
        releaseDateCol.setOnEditCommit(event -> {
            Game game = event.getRowValue();
            String newDate = event.getNewValue();
            game.setReleaseDate(newDate);
            alterGameData(game.getReleaseDate(), game.getGameId(), "release_date");
        });

        //Platform
        TableColumn<Game, String> platformCol = (TableColumn<Game, String>) table.getColumns().get(4);
        platformCol.setCellFactory(TextFieldTableCell.forTableColumn());
        platformCol.setOnEditCommit(t -> {
            if(checkIfValidString(t.getNewValue())){
                Game game = t.getRowValue();
                game.setPlatform(t.getNewValue());
                alterGameData(game.getPlatform(), game.getGameId(), "platform");
            }
            else{
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Platform cannot be empty or a number.");
                t.getTableView().getItems().set(t.getTablePosition().getRow(), t.getRowValue());
            }
        });

        //Status
        TableColumn<Game, String> statusCol = (TableColumn<Game, String>) table.getColumns().get(5);
        statusCol.setCellFactory(TextFieldTableCell.forTableColumn());
        statusCol.setOnEditCommit(t -> {
            if(checkIfValidString(t.getNewValue())){
                Game game = t.getRowValue();
                game.setStatus(t.getNewValue());
                alterGameData(game.getStatus(), game.getGameId(), "status");
            }
            else{
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Status cannot be empty or a number.");
                t.getTableView().getItems().set(t.getTablePosition().getRow(), t.getRowValue());
            }
        });
    }

    public void alterGameData(String text, int id, String field) {
        String sql = String.format("UPDATE game_record SET %s = ? WHERE game_id = ?", field);

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, text);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update game: " + e.getMessage());
        }
    }

    private void setupActionButtonColumn() {
        TableColumn<Game, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);

        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("👁️ View");
            private final Button deleteButton = new Button("🗑️");
            private final HBox buttons = new HBox(5, viewButton, deleteButton);

            {
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);

                viewButton.setOnAction(event -> {
                    Game game = getTableView().getItems().get(getIndex());
                    showGameDetails(game);
                });

                deleteButton.setOnAction(event -> {
                    Game game = getTableView().getItems().get(getIndex());
                    handleDeleteGame(game);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        table.getColumns().add(actionCol);
    }

    private void handleDeleteGame(Game game) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Game?");
        confirmAlert.setContentText(
                "Are you sure you want to delete:\n\n" +
                        "Game ID: " + game.getGameId() + "\n" +
                        "Title: " + game.getTitle() + "\n" +
                        "Genre: " + game.getGenre() + "\n" +
                        "Release Date: " + game.getReleaseDate() + "\n" +
                        "Platform: " + game.getPlatform() + "\n" +
                        "Price: " + game.getPrice() + "\n" +
                        "Status: " + game.getStatus() + "\n" +
                        "Publisher ID: " + game.getPublisherId() + "\n\n" +
                        "This action cannot be undone!"
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteFromDatabase(game.getGameId());
            loadGames(); // Refresh table
        }
    }

    private void deleteFromDatabase(int gameId) {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515")) {

            // Check if game is active
            try (PreparedStatement checkStatus = connection.prepareStatement(
                    "SELECT status FROM game_record WHERE game_id = ?")) {
                checkStatus.setInt(1, gameId);
                try (ResultSet rs = checkStatus.executeQuery()) {
                    if (rs.next() && rs.getString("status").equals("Released")) {
                        showAlert(Alert.AlertType.WARNING, "Cannot Delete",
                                "Game is currently active.\n" +
                                        "Cannot delete active games");
                        return;
                    }
                }
            }

            // If game status is Under Development or Beta
            try (PreparedStatement deleteStmt = connection.prepareStatement(
                    "DELETE FROM game_record WHERE game_id = ?")) {
                deleteStmt.setInt(1, gameId);
                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Game deleted successfully!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete game: " + e.getMessage());
        }
    }

    private void loadGames() {
        ObservableList<Game> gameList = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM game_record ORDER BY game_id");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                gameList.add(new Game(
                        rs.getInt("game_id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("release_date"),
                        rs.getString("platform"),
                        rs.getInt("price"),
                        rs.getInt("total_bought"),
                        rs.getString("status"),
                        rs.getFloat("review_average"),
                        rs.getInt("reviews"),
                        rs.getInt("publisher_id")
                ));
            }
            table.setItems(gameList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showGameDetails(Game game) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Game Details");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(650);

        // ═══ GAME INFORMATION ═══
        Label customerInfoHeader = new Label("═══ GAME INFORMATION ═══");
        customerInfoHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane gameInfo = new GridPane();
        gameInfo.setHgap(10);
        gameInfo.setVgap(8);

        gameInfo.add(new Label("Game ID:"), 0, 0);
        gameInfo.add(new Label(String.valueOf(game.getGameId())), 1, 0);
        gameInfo.add(new Label("Title:"), 0, 1);
        gameInfo.add(new Label(game.getTitle()), 1, 1);
        gameInfo.add(new Label("Genre:"), 0, 2);
        gameInfo.add(new Label(game.getGenre() != null ? game.getGenre() : "N/A"), 1, 2);
        gameInfo.add(new Label("Release Date:"), 0, 3);
        gameInfo.add(new Label(game.getReleaseDate()), 1, 3);
        gameInfo.add(new Label("Platform:"), 0, 4);
        gameInfo.add(new Label(game.getPlatform()), 1, 4);
        gameInfo.add(new Label("Price:"), 0, 5);
        gameInfo.add(new Label(String.format("$%.2f", game.getPrice())), 1, 5);
        gameInfo.add(new Label("Total Bought:"), 0, 6);
        gameInfo.add(new Label(String.valueOf(game.getTotalBought())), 1, 6);
        gameInfo.add(new Label("Status:"), 0, 7);
        gameInfo.add(new Label(game.getStatus()), 1, 7);
        gameInfo.add(new Label("Ratings:"), 0, 8);
        gameInfo.add(new Label(String.format("$%.2f", game.getAvgReview())), 1, 8);
        gameInfo.add(new Label("Reviews:"), 0, 9);
        gameInfo.add(new Label(String.valueOf(game.getReviews())), 1, 9);
        gameInfo.add(new Label("Publisher ID:"), 0, 10);
        gameInfo.add(new Label(String.valueOf(game.getPublisherId())), 1, 10);

        Label customersHeader = new Label("═══ GAME CUSTOMERS ═══");
        customersHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<CustomerDetail> gamesTable = new TableView<>();
        gamesTable.setPrefHeight(250);

        TableColumn<CustomerDetail, String> fNameCol = new TableColumn<>("First Name");
        fNameCol.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        fNameCol.setPrefWidth(300);

        TableColumn<CustomerDetail, String> lNameCol = new TableColumn<>("Last Name");
        lNameCol.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        lNameCol.setPrefWidth(100);

        TableColumn<CustomerDetail, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        countryCol.setPrefWidth(120);

        TableColumn<CustomerDetail, String> prefPlatformCol = new TableColumn<>("Preferred Platform");
        prefPlatformCol.setCellValueFactory(new PropertyValueFactory<>("preferredPlatform"));
        prefPlatformCol.setPrefWidth(120);

        gamesTable.getColumns().addAll(fNameCol, lNameCol, countryCol, prefPlatformCol);

        ObservableList<CustomerDetail> games = getGameCustomers(game.getGameId());
        gamesTable.setItems(games);

        if (games.isEmpty()) {
            Label noGames = new Label("Game has no customers.");
            noGames.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            content.getChildren().addAll(customerInfoHeader, gameInfo, customersHeader, noGames);
        } else {
            content.getChildren().addAll(customerInfoHeader, gameInfo, customersHeader, gamesTable);
        }


        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    //CustomerDetail Class
    public static class CustomerDetail{
        private String last_name;
        private String first_name;
        private String country;
        private String preferredPlatform;

        CustomerDetail(String last_name, String first_name, String country, String preferredPlatform){
            this.last_name = last_name;
            this.first_name = first_name;
            this.country = country;
            this.preferredPlatform = preferredPlatform;
        }

        public String getLast_name() { return last_name; }
        public String getFirst_name() { return first_name; }
        public String getCountry() { return country; }
        public String getPreferredPlatform() { return preferredPlatform; }
    }

    private ObservableList<CustomerDetail> getGameCustomers(int gameId) {
        ObservableList<CustomerDetail> customers = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT c.last_name, c.first_name, c.country, c.preferred_platform, g.game_id " +
                             "FROM customer_record c " +
                             "JOIN transaction_log t " +
                             "ON c.customer_id = t.customer_id " +
                             "JOIN game_record g " +
                             "ON t.game_id = g.game_id " +
                             "WHERE g.game_id = ? " +
                             "ORDER BY last_name")) {

            pstmt.setInt(1, gameId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new CustomerDetail(
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("country"),
                            rs.getString("preferred_platform")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public boolean checkIfValidString(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return !isInteger(text);
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

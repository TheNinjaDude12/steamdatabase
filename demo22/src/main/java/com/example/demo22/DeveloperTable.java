package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class DeveloperTable {

    @FXML
    private TableView<Developer> developerTable;

    public static class Developer {
        private int id;
        private String name;
        private String country;
        private String email;
        private String website;
        private int gamesDeveloped;

        public Developer(int id, String name, String country, String email, String website, int gamesDeveloped) {
            this.id = id;
            this.name = name;
            this.country = country;
            this.email = email;
            this.website = website;
            this.gamesDeveloped = gamesDeveloped;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCountry() { return country; }
        public String getEmail() { return email; }
        public String getWebsite() { return website; }
        public int getGamesDeveloped() { return gamesDeveloped; }

        public void setId(int id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setCountry(String country) { this.country = country; }
        public void setEmail(String email) { this.email = email; }
        public void setWebsite(String website) { this.website = website; }
        public void setGamesDeveloped(int gamesDeveloped) { this.gamesDeveloped = gamesDeveloped; }
    }

    public void initialize() {
        TableColumn<Developer, Integer> devIdCol = new TableColumn<>("ID");
        TableColumn<Developer, String> nameCol = new TableColumn<>("Name");
        TableColumn<Developer, String> countryCol = new TableColumn<>("Country");
        TableColumn<Developer, String> emailCol = new TableColumn<>("Email");
        TableColumn<Developer, String> websiteCol = new TableColumn<>("Website");
        TableColumn<Developer, Integer> gamesDevCol = new TableColumn<>("Games Developed");

        developerTable.getColumns().clear();
        developerTable.getColumns().addAll(devIdCol, nameCol, countryCol, emailCol, websiteCol, gamesDevCol);

        devIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        websiteCol.setCellValueFactory(new PropertyValueFactory<>("website"));
        gamesDevCol.setCellValueFactory(new PropertyValueFactory<>("gamesDeveloped"));

        loadDevelopers();

        developerTable.setEditable(true);
        setupEditableColumns();
        setupActionButtonColumn();
    }

    private void setupEditableColumns() {
        TableColumn<Developer, String> nameCol = (TableColumn<Developer, String>) developerTable.getColumns().get(1);
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(t -> {
            if (checkIfValidString(t.getNewValue())) {
                Developer dev = t.getRowValue();
                dev.setName(t.getNewValue());
                alterDeveloperData(dev.getName(), dev.getId(), "name");
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Name cannot be empty or a number.");
                developerTable.refresh();
            }
        });

        TableColumn<Developer, String> countryCol = (TableColumn<Developer, String>) developerTable.getColumns().get(2);
        countryCol.setCellFactory(TextFieldTableCell.forTableColumn());
        countryCol.setOnEditCommit(t -> {
            if (checkIfValidString(t.getNewValue())) {
                Developer dev = t.getRowValue();
                dev.setCountry(t.getNewValue());
                alterDeveloperData(dev.getCountry(), dev.getId(), "country");
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Country cannot be empty or a number.");
                developerTable.refresh();
            }
        });

        TableColumn<Developer, String> emailCol = (TableColumn<Developer, String>) developerTable.getColumns().get(3);
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        emailCol.setOnEditCommit(t -> {
            if (t.getNewValue() != null && !t.getNewValue().isEmpty() && t.getNewValue().contains("@")) {
                Developer dev = t.getRowValue();
                dev.setEmail(t.getNewValue());
                alterDeveloperData(dev.getEmail(), dev.getId(), "email");
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Email cannot be empty or invalid.");
                developerTable.refresh();
            }
        });

        TableColumn<Developer, String> websiteCol = (TableColumn<Developer, String>) developerTable.getColumns().get(4);
        websiteCol.setCellFactory(TextFieldTableCell.forTableColumn());
        websiteCol.setOnEditCommit(t -> {
            Developer dev = t.getRowValue();
            dev.setWebsite(t.getNewValue());
            alterDeveloperData(dev.getWebsite(), dev.getId(), "website");
        });
    }

    public void alterDeveloperData(String text, int id, String field) {
        String sql = String.format("UPDATE developer_record SET %s = ? WHERE developer_id = ?", field);
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, text);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update developer: " + e.getMessage());
        }
    }

    private void setupActionButtonColumn() {
        TableColumn<Developer, Void> actionCol = new TableColumn<>("Actions");
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
                    Developer dev = getTableView().getItems().get(getIndex());
                    showDeveloperDetails(dev);
                });

                deleteButton.setOnAction(event -> {
                    Developer dev = getTableView().getItems().get(getIndex());
                    handleDeleteDeveloper(dev);
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

        developerTable.getColumns().add(actionCol);
    }

    private void handleDeleteDeveloper(Developer dev) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Developer?");
        confirmAlert.setContentText("Are you sure you want to delete: " + dev.getName() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteFromDatabase(dev.getId());
            loadDevelopers();
        }
    }

    private void deleteFromDatabase(int developerId) {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515")) {

            try (PreparedStatement checkGames = connection.prepareStatement(
                    "SELECT COUNT(*) FROM game_record WHERE developer_id = ?")) {
                checkGames.setInt(1, developerId);
                try (ResultSet rs = checkGames.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.WARNING, "Cannot Delete",
                                "This developer has associated games. Cannot delete.");
                        return;
                    }
                }
            }

            try (PreparedStatement deleteStmt = connection.prepareStatement(
                    "DELETE FROM developer_record WHERE developer_id = ?")) {
                deleteStmt.setInt(1, developerId);
                deleteStmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Developer deleted successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete developer: " + e.getMessage());
        }
    }

    private void loadDevelopers() {
        ObservableList<Developer> devList = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT d.*, (SELECT COUNT(*) FROM game_record g WHERE g.developer_id = d.developer_id) as real_games_count FROM developer_record d ORDER BY d.developer_id");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                devList.add(new Developer(
                        rs.getInt("developer_id"),
                        rs.getString("name"),
                        rs.getString("country"),
                        rs.getString("email"),
                        rs.getString("website"),
                        rs.getInt("real_games_count")
                ));
            }
            developerTable.setItems(devList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDeveloperDetails(Developer dev) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Developer Details");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(650);

        Label devInfoHeader = new Label("═══ DEVELOPER INFORMATION ═══");
        devInfoHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane devInfo = new GridPane();
        devInfo.setHgap(10);
        devInfo.setVgap(8);

        devInfo.add(new Label("ID:"), 0, 0);
        devInfo.add(new Label(String.valueOf(dev.getId())), 1, 0);
        devInfo.add(new Label("Name:"), 0, 1);
        devInfo.add(new Label(dev.getName()), 1, 1);
        devInfo.add(new Label("Email:"), 0, 2);
        devInfo.add(new Label(dev.getEmail()), 1, 2);
        devInfo.add(new Label("Country:"), 0, 3);
        devInfo.add(new Label(dev.getCountry()), 1, 3);
        devInfo.add(new Label("Website:"), 0, 4);
        devInfo.add(new Label(dev.getWebsite() != null ? dev.getWebsite() : "N/A"), 1, 4);

        Label gamesHeader = new Label("═══ GAMES DEVELOPED ═══");
        gamesHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<GameRecord> gamesTable = new TableView<>();
        gamesTable.setPrefHeight(250);

        TableColumn<GameRecord, String> titleCol = new TableColumn<>("Game Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(300);

        TableColumn<GameRecord, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<GameRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        gamesTable.getColumns().addAll(titleCol, priceCol, statusCol);
        ObservableList<GameRecord> games = getDeveloperGames(dev.getId());
        gamesTable.setItems(games);

        content.getChildren().addAll(devInfoHeader, devInfo, gamesHeader, gamesTable);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public static class GameRecord {
        private String title;
        private double price;
        private String status;

        public GameRecord(String title, double price, String status) {
            this.title = title;
            this.price = price;
            this.status = status;
        }

        public String getTitle() { return title; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
    }

    private ObservableList<GameRecord> getDeveloperGames(int developerId) {
        ObservableList<GameRecord> games = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                "root", "thunder1515");
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT title, price, status FROM game_record WHERE developer_id = ?")) {

            pstmt.setInt(1, developerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    games.add(new GameRecord(
                            rs.getString("title"),
                            rs.getDouble("price"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return games;
    }

    public boolean checkIfValidString(String text) {
        if (text == null || text.isEmpty()) return false;
        return !isInteger(text);
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
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
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("developerView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
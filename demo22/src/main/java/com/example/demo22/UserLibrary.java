package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class UserLibrary {
    @FXML private Text gameRelease;
    @FXML private Text gameGenre;
    @FXML private Text gameTitle;
    @FXML private Text welcomeText;
    @FXML private TableView<Game> gamesTable;
    @FXML public TextField filterText;

    public class Game {
        private String title;

        Game(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public void initialize() {
        // ✅ FIX: Get user_id fresh from UserLogIn each time
        int currentUserId = UserLogIn.user_id;

        // ✅ FIX: Add validation
        if (currentUserId <= 0) {
            System.err.println("Invalid user_id: " + currentUserId);
            return;
        }

        TableColumn<Game, String> gameName = new TableColumn<>("Games");
        gamesTable.getColumns().add(gameName);

        ObservableList<Game> games = FXCollections.observableArrayList();

        gamesTable.setEditable(false);
        gamesTable.setStyle(
                "-fx-background-color: #1b2838;" +
                        "-fx-control-inner-background: #16202d;" +
                        "-fx-table-cell-border-color: transparent;" +
                        "-fx-text-fill: white;"
        );

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            // ✅ FIX: Use PreparedStatement with currentUserId
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT DISTINCT gr.title FROM game_record gr " +
                            "JOIN transaction_log tl ON gr.game_id = tl.game_id " +
                            "WHERE tl.customer_id = ? AND tl.status = 'Paid'");
            pstmt.setInt(1, currentUserId);

            ResultSet resultSet = pstmt.executeQuery();

            while(resultSet.next()) {
                games.add(new Game(resultSet.getString("title")));
            }

            // Get customer name
            PreparedStatement pstmt2 = connection.prepareStatement(
                    "SELECT first_name FROM customer_record WHERE customer_id = ?");
            pstmt2.setInt(1, currentUserId);

            ResultSet resultSet2 = pstmt2.executeQuery();

            if (resultSet2.next()) {
                welcomeText.setText("Welcome " + resultSet2.getString("first_name"));
            }

            resultSet.close();
            resultSet2.close();
            pstmt.close();
            pstmt2.close();
            connection.close();

        } catch(SQLException e) {
            e.printStackTrace();
        }

        gameName.setCellValueFactory(new PropertyValueFactory<>("title"));
        gameName.prefWidthProperty().bind(gamesTable.widthProperty());

        // Setup filtered and sorted list
        FilteredList<Game> filteredData = new FilteredList<>(games, b -> true);

        filterText.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(game -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String searchKeyword = newValue.toLowerCase();
                return game.getTitle().toLowerCase().contains(searchKeyword);
            });
        });

        SortedList<Game> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(gamesTable.comparatorProperty());
        gamesTable.setItems(sortedData);

        gameName.setCellFactory(TextFieldTableCell.forTableColumn());

        gamesTable.setOnMouseClicked(event -> {
            Game selectedGame = gamesTable.getSelectionModel().getSelectedItem();
            if (selectedGame != null) {
                gameTitle.setText(selectedGame.getTitle());

                try {
                    Connection connection = DriverManager.getConnection(
                            "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                            "root", "thunder1515");

                    PreparedStatement pstmt = connection.prepareStatement(
                            "SELECT genre, release_date FROM game_record WHERE title = ?");
                    pstmt.setString(1, selectedGame.getTitle());

                    ResultSet resultSet = pstmt.executeQuery();

                    if (resultSet.next()) {
                        gameGenre.setText(resultSet.getString("genre"));
                        gameRelease.setText(resultSet.getString("release_date"));
                    }

                    resultSet.close();
                    pstmt.close();
                    connection.close();

                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("userMenuOptions.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
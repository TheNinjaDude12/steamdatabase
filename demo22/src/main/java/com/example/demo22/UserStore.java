package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class UserStore {

    @FXML private Text genreText;
    @FXML private Text releaseDateText;
    @FXML private Text reviewAVGtext;
    @FXML private Text statusText;
    @FXML private Text platformText;
    @FXML private Text priceText;
    @FXML private Text reviewText;
    @FXML private ComboBox<String> customerComboBox;
    @FXML private ComboBox<String> gameComboBox;
    @FXML private Button buyGameButton;
    @FXML private ChoiceBox<String> paymentSelector;
    private ArrayList<String> paymentMethods;
    private int game_id;

    public void initialize() {
        setupPaymentSelector();
        setupGameComboBox();
        buyGameButton.setDisable(true);
    }

    private void setupGameComboBox() {
        ArrayList<String> gameList = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            // ✅ GET release_date
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT title, release_date FROM game_record WHERE status = 'Released' ORDER BY title");

            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String releaseDate = resultSet.getString("release_date");
                // ✅ ADD RELEASE DATE TO DISPLAY
                String display = title + " (" + releaseDate + ")";
                gameList.add(display);
            }

            resultSet.close();
            pstmt.close();
            connection.close();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load games: " + e.getMessage());
        }

        ObservableList<String> allGames = FXCollections.observableArrayList(gameList);
        FilteredList<String> filteredGames = new FilteredList<>(allGames, p -> true);

        gameComboBox.setItems(filteredGames);
        gameComboBox.setEditable(true);
        gameComboBox.setPromptText("Type to search game...");

        final boolean[] isUpdatingGame = {false};

        // Text change listener - filters as user types
        gameComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (isUpdatingGame[0]) {
                return;
            }

            final String searchText = newValue == null ? "" : newValue.toLowerCase();

            // Only filter if nothing is selected
            if (gameComboBox.getSelectionModel().getSelectedItem() == null) {
                filteredGames.setPredicate(game -> {
                    if (searchText.isEmpty()) {
                        return true;
                    }
                    return game.toLowerCase().contains(searchText);
                });

                if (!searchText.isEmpty() && !gameComboBox.isShowing()) {
                    gameComboBox.show();
                }
            }
        });

        gameComboBox.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                event.consume();

                String currentText = gameComboBox.getEditor().getText();

                boolean exactMatch = false;
                for (String game : allGames) {
                    if (game.equalsIgnoreCase(currentText)) {
                        isUpdatingGame[0] = true;
                        gameComboBox.setValue(game);
                        gameComboBox.getSelectionModel().select(game);
                        loadGameDetails(game);
                        gameComboBox.hide();
                        isUpdatingGame[0] = false;
                        exactMatch = true;
                        break;
                    }
                }

                if (!exactMatch) {
                    isUpdatingGame[0] = true;
                    gameComboBox.getEditor().clear();
                    gameComboBox.getSelectionModel().clearSelection();
                    filteredGames.setPredicate(p -> true);
                    gameComboBox.hide();
                    isUpdatingGame[0] = false;
                }
            }
            else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                event.consume();
                isUpdatingGame[0] = true;
                gameComboBox.getEditor().clear();
                gameComboBox.getSelectionModel().clearSelection();
                filteredGames.setPredicate(p -> true);
                gameComboBox.hide();
                isUpdatingGame[0] = false;
            }
            else if (event.getCode() == javafx.scene.input.KeyCode.DOWN) {
                if (!gameComboBox.isShowing()) {
                    gameComboBox.show();
                }
            }
        });

        gameComboBox.setOnAction(event -> {
            if (isUpdatingGame[0]) {
                return;
            }

            String selected = gameComboBox.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                isUpdatingGame[0] = true;
                try {
                    gameComboBox.getEditor().setText(selected);
                    loadGameDetails(selected);
                    gameComboBox.hide();
                } finally {
                    isUpdatingGame[0] = false;
                }
            }
        });
    }

    private void loadGameDetails(String gameDisplay) {
        // gameDisplay format: "Persona 3 Reload (2024-02-02)"
        try {
            // ✅ EXTRACT TITLE AND RELEASE DATE FROM DISPLAY
            int dateStart = gameDisplay.lastIndexOf("(");
            int dateEnd = gameDisplay.lastIndexOf(")");

            if (dateStart == -1 || dateEnd == -1) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid game format");
                return;
            }

            String title = gameDisplay.substring(0, dateStart).trim();
            String releaseDate = gameDisplay.substring(dateStart + 1, dateEnd).trim();

            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            // ✅ QUERY BY BOTH TITLE AND RELEASE_DATE
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT * FROM game_record WHERE title = ? AND release_date = ?");
            pstmt.setString(1, title);
            pstmt.setString(2, releaseDate);

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                genreText.setText(resultSet.getString("genre"));
                releaseDateText.setText(resultSet.getString("release_date"));
                reviewAVGtext.setText(String.valueOf(resultSet.getDouble("review_average")));
                statusText.setText(resultSet.getString("status"));
                platformText.setText(resultSet.getString("platform"));
                priceText.setText(String.valueOf(resultSet.getDouble("price")));
                reviewText.setText(String.valueOf(resultSet.getInt("reviews")));
                game_id = resultSet.getInt("game_id");

                buyGameButton.setDisable(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Game not found");
            }

            resultSet.close();
            pstmt.close();
            connection.close();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load game details");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupPaymentSelector() {
        paymentMethods = new ArrayList<>();
        paymentMethods.add("Cash");
        paymentMethods.add("Debit Card");
        paymentMethods.add("Credit Card");
        paymentMethods.add("Gcash");
        paymentMethods.add("Shoppee PayLater");
        paymentMethods.add("PayPal");

        ObservableList<String> payment = FXCollections.observableArrayList(paymentMethods);
        paymentSelector.setItems(payment);
        paymentSelector.setValue("Cash");
    }

    public void buyGame() {
        int customerId = UserLogIn.user_id;
        int gameId = game_id;
        double price = Double.parseDouble(priceText.getText());

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            // Check ownership
            PreparedStatement checkOwnership = connection.prepareStatement(
                    "SELECT COUNT(*) FROM transaction_log " +
                            "WHERE customer_id = ? AND game_id = ? AND status = 'Paid'");
            checkOwnership.setInt(1, customerId);
            checkOwnership.setInt(2, gameId);
            ResultSet rsOwnership = checkOwnership.executeQuery();

            if (rsOwnership.next() && rsOwnership.getInt(1) > 0) {
                showAlert(Alert.AlertType.WARNING, "Already Owned",
                        "You already own this game!");
                connection.close();
                return;
            }

            // Insert transaction
            PreparedStatement insertTx = connection.prepareStatement(
                    "INSERT INTO transaction_log(customer_id, game_id, purchase_date, " +
                            "payment_method, amount, status) VALUES (?, ?, NOW(), ?, ?, 'Paid')");
            insertTx.setInt(1, customerId);
            insertTx.setInt(2, gameId);
            insertTx.setString(3, paymentSelector.getValue());
            insertTx.setDouble(4, price);
            insertTx.executeUpdate();

            // Update customer totals
            PreparedStatement updateCustomer = connection.prepareStatement(
                    "UPDATE customer_record SET games_owned = games_owned + 1, " +
                            "total_spent = total_spent + ? WHERE customer_id = ?");
            updateCustomer.setDouble(1, price);
            updateCustomer.setInt(2, customerId);
            updateCustomer.executeUpdate();

            // Update game total_bought
            PreparedStatement updateGame = connection.prepareStatement(
                    "UPDATE game_record SET total_bought = total_bought + 1 WHERE game_id = ?");
            updateGame.setInt(1, gameId);
            updateGame.executeUpdate();

            connection.close();

            showAlert(Alert.AlertType.INFORMATION, "Purchase Successful",
                    "Game purchased successfully!\n\n" +
                            "Game: " + gameComboBox.getValue() + "\n" +
                            "Amount: ₱" + String.format("%.2f", price) + "\n" +
                            "Payment: " + paymentSelector.getValue());

            clearForm();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Purchase Failed",
                    "Failed to complete purchase: " + e.getMessage());
        } catch(NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Invalid game ID or price");
        }
    }

    private void clearForm() {
        gameComboBox.setValue(null);
        gameComboBox.getEditor().clear();
        buyGameButton.setDisable(true);

        genreText.setText("");
        releaseDateText.setText("");
        reviewAVGtext.setText("");
        statusText.setText("");
        platformText.setText("");
        priceText.setText("");
        reviewText.setText("");
    }

    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("/com/example/demo22/userMenuOptions.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
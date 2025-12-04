package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class BuyGameView {
    @FXML private Text customerID;  // NEW: Customer ID display
    @FXML private Text firstNameText;
    @FXML private Text lastNameText;
    @FXML private Text emailText;
    @FXML private Text countryText;
    @FXML private Text gameIdText;
    @FXML private Text genreText;
    @FXML private Text releaseDateText;
    @FXML private Text reviewAVGtext;
    @FXML private Text statusText;
    @FXML private Text platformText;
    @FXML private Text priceText;
    @FXML private Text reviewText;
    @FXML private ComboBox<String> customerComboBox;
    @FXML private ComboBox<String> gameComboBox;  // Changed from ChoiceBox
    @FXML private Button buyGameButton;
    @FXML private ChoiceBox<String> paymentSelector;

    public static ArrayList<String> paymentMethods;

    public void initialize() {
        setupPaymentSelector();
        setupCustomerComboBox();
        setupGameComboBox();

        buyGameButton.setDisable(true);
        gameComboBox.setDisable(true);
    }

    private void setupPaymentSelector() {
        paymentMethods = new ArrayList<>();
        paymentMethods.add("Cash");
        paymentMethods.add("Debit Card");
        paymentMethods.add("Credit Card");
        paymentMethods.add("Gcash");
        paymentMethods.add("Shoppee PayLater");
        paymentMethods.add("PayPal");
        paymentMethods.add("Add Payment");

        ObservableList<String> payment = FXCollections.observableArrayList(paymentMethods);
        paymentSelector.setItems(payment);
        paymentSelector.setValue("Cash");

        paymentSelector.setOnAction(event -> {
            if (paymentSelector.getValue() != null &&
                    paymentSelector.getValue().equals("Add Payment")) {
                showAddPaymentDialog();
            }
        });
    }

    private void showAddPaymentDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Payment Method");
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        TextField paymentField = new TextField();
        paymentField.setPromptText("Enter payment method");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            String newPayment = paymentField.getText().trim();

            if (newPayment.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Input",
                        "Please enter a payment method");
                return;
            }

            if (paymentMethods.contains(newPayment)) {
                showAlert(Alert.AlertType.WARNING, "Duplicate",
                        "This payment method already exists");
                return;
            }

            paymentMethods.remove("Add Payment");
            paymentMethods.add(newPayment);
            paymentMethods.add("Add Payment");

            paymentSelector.setItems(FXCollections.observableArrayList(paymentMethods));
            paymentSelector.setValue(newPayment);

            dialog.close();
        });

        content.getChildren().addAll(
                new Label("Payment Method:"),
                paymentField,
                addButton
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.showAndWait();
    }

    private void setupCustomerComboBox() {
        ArrayList<String> customerList = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT customer_id, first_name, last_name, email, country " +
                            "FROM customer_record ORDER BY last_name, first_name");

            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                String lastName = resultSet.getString("last_name");
                String firstName = resultSet.getString("first_name");
                String email = resultSet.getString("email");  // ✅ GET EMAIL
                // ✅ ADD EMAIL TO DISPLAY
                String display = lastName + " " + firstName + " (" + email + ")";
                customerList.add(display);
            }

            resultSet.close();
            pstmt.close();
            connection.close();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load customers: " + e.getMessage());
        }

        ObservableList<String> allCustomers = FXCollections.observableArrayList(customerList);
        FilteredList<String> filteredCustomers = new FilteredList<>(allCustomers, p -> true);

        customerComboBox.setItems(filteredCustomers);
        customerComboBox.setEditable(true);
        customerComboBox.setPromptText("Type to search customer...");

        final boolean[] isUpdating = {false};

        // Text change listener
        customerComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (isUpdating[0]) {
                return;
            }

            final String searchText = newValue == null ? "" : newValue.toLowerCase();

            if (customerComboBox.getSelectionModel().getSelectedItem() == null) {
                filteredCustomers.setPredicate(customer -> {
                    if (searchText.isEmpty()) {
                        return true;
                    }
                    return customer.toLowerCase().contains(searchText);
                });

                if (!searchText.isEmpty() && !customerComboBox.isShowing()) {
                    customerComboBox.show();
                }
            }
        });

        customerComboBox.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                event.consume();

                String currentText = customerComboBox.getEditor().getText();

                boolean exactMatch = false;
                for (String customer : allCustomers) {
                    if (customer.equalsIgnoreCase(currentText)) {
                        isUpdating[0] = true;
                        customerComboBox.setValue(customer);
                        customerComboBox.getSelectionModel().select(customer);
                        loadCustomerDetails(customer);
                        gameComboBox.setDisable(false);
                        customerComboBox.hide();
                        isUpdating[0] = false;
                        exactMatch = true;
                        break;
                    }
                }

                if (!exactMatch) {
                    isUpdating[0] = true;
                    customerComboBox.getEditor().clear();
                    customerComboBox.getSelectionModel().clearSelection();
                    filteredCustomers.setPredicate(p -> true);
                    customerComboBox.hide();
                    isUpdating[0] = false;
                }
            }
            else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                event.consume();
                isUpdating[0] = true;
                customerComboBox.getEditor().clear();
                customerComboBox.getSelectionModel().clearSelection();
                filteredCustomers.setPredicate(p -> true);
                customerComboBox.hide();
                isUpdating[0] = false;
            }
            else if (event.getCode() == javafx.scene.input.KeyCode.DOWN) {
                if (!customerComboBox.isShowing()) {
                    customerComboBox.show();
                }
            }
        });

        customerComboBox.setOnAction(event -> {
            if (isUpdating[0]) {
                return;
            }

            String selected = customerComboBox.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                isUpdating[0] = true;
                try {
                    customerComboBox.getEditor().setText(selected);
                    loadCustomerDetails(selected);
                    gameComboBox.setDisable(false);
                    customerComboBox.hide();
                } finally {
                    isUpdating[0] = false;
                }
            }
        });
    }

    private void loadCustomerDetails(String customerName) {
        // customerName format: "Garcia Derick (derick@email.com)"
        try {
            // ✅ EXTRACT EMAIL FROM PARENTHESES
            int emailStart = customerName.indexOf("(");
            int emailEnd = customerName.indexOf(")");

            if (emailStart == -1 || emailEnd == -1) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid customer format");
                return;
            }

            String email = customerName.substring(emailStart + 1, emailEnd).trim();

            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            // ✅ QUERY BY EMAIL (UNIQUE)
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT * FROM customer_record WHERE email = ?");
            pstmt.setString(1, email);

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                customerID.setText(String.valueOf(resultSet.getInt("customer_id")));
                firstNameText.setText(resultSet.getString("first_name"));
                lastNameText.setText(resultSet.getString("last_name"));
                emailText.setText(resultSet.getString("email"));
                countryText.setText(resultSet.getString("country"));
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Customer not found");
            }

            resultSet.close();
            pstmt.close();
            connection.close();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load customer details");
        }
    }

    private void setupGameComboBox() {
        ArrayList<String> gameList = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            // ✅ GET game_id
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT game_id, title FROM game_record WHERE status = 'Released' ORDER BY title");

            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                int gameId = resultSet.getInt("game_id");
                // ✅ ADD GAME ID TO DISPLAY
                String display = title + " (ID: " + gameId + ")";
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

        gameComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (isUpdatingGame[0]) {
                return;
            }

            final String searchText = newValue == null ? "" : newValue.toLowerCase();

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
        // gameDisplay format: "Persona 3 Reload (ID: 1)"
        try {
            // ✅ EXTRACT GAME ID FROM DISPLAY
            int idStart = gameDisplay.lastIndexOf("(ID: ");
            int idEnd = gameDisplay.lastIndexOf(")");

            if (idStart == -1 || idEnd == -1) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid game format");
                return;
            }

            String gameIdStr = gameDisplay.substring(idStart + 5, idEnd).trim();
            int gameId = Integer.parseInt(gameIdStr);

            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            // ✅ QUERY BY GAME_ID (UNIQUE)
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT * FROM game_record WHERE game_id = ?");
            pstmt.setInt(1, gameId);

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                gameIdText.setText(String.valueOf(resultSet.getInt("game_id")));
                genreText.setText(resultSet.getString("genre"));
                releaseDateText.setText(resultSet.getString("release_date"));
                reviewAVGtext.setText(String.valueOf(resultSet.getDouble("review_average")));
                statusText.setText(resultSet.getString("status"));
                platformText.setText(resultSet.getString("platform"));
                priceText.setText(String.valueOf(resultSet.getDouble("price")));
                reviewText.setText(String.valueOf(resultSet.getInt("reviews")));

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
        } catch(NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Invalid game ID format");
        }
    }
    public void buyGame() {
        // Validation
        if (customerComboBox.getValue() == null || gameComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Selection",
                    "Please select both customer and game");
            return;
        }

        // Get customer ID from the Text field (already loaded)
        if (customerID.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Customer ID not loaded");
            return;
        }

        int customerId = Integer.parseInt(customerID.getText());
        int gameId = Integer.parseInt(gameIdText.getText());
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
                        "This customer already owns this game!");
                connection.close();
                return;
            }

            // Insert transaction
            PreparedStatement insertTx = connection.prepareStatement(
                    "INSERT INTO transaction_log(customer_id, game_id, purchase_date, " +
                            "payment_method, amount, status) VALUES (?, ?, CURDATE(), ?, ?, 'Paid')");
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
                            "Customer: " + firstNameText.getText() + " " + lastNameText.getText() + "\n" +
                            "Customer ID: " + customerId + "\n" +
                            "Game: " + gameComboBox.getValue() + "\n" +
                            "Amount: $" + priceText.getText() + "\n" +
                            "Payment: " + paymentSelector.getValue());

            clearForm();

        } catch(SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Purchase Failed",
                    "Failed to complete purchase: " + e.getMessage());
        } catch(NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Invalid customer ID or game ID");
        }
    }

    private void clearForm() {
        customerComboBox.setValue(null);
        customerComboBox.getEditor().clear();
        gameComboBox.setValue(null);
        gameComboBox.setDisable(true);
        buyGameButton.setDisable(true);

        customerID.setText("");
        firstNameText.setText("");
        lastNameText.setText("");
        emailText.setText("");
        countryText.setText("");
        gameIdText.setText("");
        genreText.setText("");
        releaseDateText.setText("");
        reviewAVGtext.setText("");
        statusText.setText("");
        platformText.setText("");
        priceText.setText("");
        reviewText.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("customerView.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
package com.example.demo22;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class BuyGameView {
    @FXML
    private Text firstNameText;
    @FXML
    private Text lastNameText;
    @FXML
    private Text emailText;
    @FXML
    private Text countryText;
    @FXML
    private Text gameIdText;
    @FXML
    private Text genreText;
    @FXML
    private Text releaseDateText;
    @FXML
    private Text reviewAVGtext;
    @FXML
    private Text statusText;
    @FXML
    private Text platformText;
    @FXML
    private Text priceText;
    @FXML
    private Text reviewText;

    @FXML
    private ChoiceBox<Integer> customerIdSelector;
    @FXML
    private ChoiceBox<String> gameSelector;
    @FXML
    private Button buyGameButton;
    @FXML
    private ChoiceBox<String> paymentSelector;
    public void initialize() {
        ArrayList<String> payArr = new ArrayList<>();
        payArr.add("Cash");
        payArr.add("Debit Card");
        payArr.add("Credit Card");
        payArr.add("Gcash");
        payArr.add("Shoppee PayLater");
        payArr.add("PayPal");
        payArr.add("Add Payment");

        ObservableList<String> payment = FXCollections.observableArrayList(payArr);
        paymentSelector.setItems(payment);
        paymentSelector.setOnAction(event -> {
           if(paymentSelector.getValue() !=null && paymentSelector.getValue().equals("Add Payment")) {
               Dialog<Void> dialog = new Dialog<>();
               dialog.setTitle("Customer Details");
               dialog.setHeaderText(null);
               VBox content = new VBox(15);
               content.setPadding(new Insets(20));
               content.setPrefWidth(400);
               Text paymentMethod = new Text("Payment Method");
               TextField paymentField = new TextField();
               paymentField.setPromptText("Add Payment Method");
               Button addPaymentButton = new Button("Add");
               addPaymentButton.setOnAction(adder -> {
                   String newPayment = paymentField.getText().trim();

                   // Validation
                   if (newPayment.isEmpty()) {
                       showAlert(Alert.AlertType.WARNING, "Empty Input",
                               "Please enter a payment method");
                       return;
                   }

                   if (payArr.contains(newPayment)) {
                       showAlert(Alert.AlertType.WARNING, "Duplicate",
                               "This payment method already exists");
                       return;
                   }
                   System.out.println("Test");
                   payArr.add(paymentField.getText());
                   payArr.add("Add Payment");
                   paymentSelector.setItems(FXCollections.observableArrayList(payArr));
                   dialog.close();


                       });
               dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
               content.getChildren().addAll(paymentMethod, paymentField, addPaymentButton);
               dialog.getDialogPane().setContent(content);

               dialog.showAndWait();
           }
        });
        paymentSelector.setItems(payment);
        paymentSelector.setValue("Cash");
        buyGameButton.setDisable(true);
        gameSelector.setDisable(true);
        ArrayList<Integer> intArray = new ArrayList<>();
        ArrayList<String> stringArray = new ArrayList<>();
        //For the customer ID
        try {
            System.out.println("ID");
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");
           ResultSet resultSet = connection.createStatement().executeQuery("SELECT customer_id FROM customer_record");
           while (resultSet.next()) {
               intArray.add(resultSet.getInt("customer_id"));
           }



        } catch(SQLException e) {
            e.printStackTrace();
        }
        Integer[] arr = new Integer[intArray.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = intArray.get(i);
        }
        Arrays.sort(arr);
        ObservableList<Integer> observableIntList = FXCollections.observableArrayList(arr);
        customerIdSelector.setItems(observableIntList);
        customerIdSelector.setOnAction(event -> {
            try {
                String firstName = "";
                String lastName = "";
                String email = "";
                String country = "";
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                        "root", "thunder1515");
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM customer_record WHERE customer_id = " + customerIdSelector.getValue());
               while (resultSet.next()) {
                   firstName = (resultSet.getString("first_name"));
                   lastName = (resultSet.getString("last_name"));
                   email = (resultSet.getString("email"));
                   country = resultSet.getString("country");
               }
                firstNameText.setText(firstName);
                lastNameText.setText(lastName);
                emailText.setText(email);
                countryText.setText(country);



            gameSelector.setDisable(false);
            } catch(SQLException e) {
                e.printStackTrace();
            }




        });

        try {
            System.out.println("Games");
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT title FROM game_record WHERE status = 'active'");
            while (resultSet.next()) {
                stringArray.add(resultSet.getString("title"));
            }



        } catch(SQLException e) {
            e.printStackTrace();
        }
        String[] arr2 = new String[stringArray.size()];
        for (int i = 0; i < arr2.length; i++) {
            arr2[i] = stringArray.get(i);
        }
        Arrays.sort(arr2);
        ObservableList<String> observableStringList = FXCollections.observableArrayList(stringArray);
        gameSelector.setItems(observableStringList);
        gameSelector.setOnAction(event -> {

            try {
                int gameId = 0;
                String genre = "";
                String release_date = "";
                double review_avg = 0.0;
                int review_count = 0;
                String status = "";
                String platform = "";
                double price = 0.0;
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                        "root", "thunder1515");
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM game_record WHERE title = " + "'" + gameSelector.getValue() + "'");
                while (resultSet.next()) {
                    gameId = (resultSet.getInt("game_id"));
                    genre = (resultSet.getString("genre"));
                    release_date = (resultSet.getString("release_date"));
                    review_avg = resultSet.getFloat("review_average");
                    review_count = resultSet.getInt("reviews");
                    status = (resultSet.getString("status"));
                    platform = (resultSet.getString("platform"));
                    price = resultSet.getFloat("price");


                }
                gameIdText.setText(String.valueOf(gameId));
                genreText.setText(genre);
                releaseDateText.setText(release_date);
                reviewAVGtext.setText(String.valueOf(review_avg));
                statusText.setText(status);
                platformText.setText(platform);
                priceText.setText(String.valueOf(price));
                reviewText.setText(String.valueOf(review_count));
                buyGameButton.setDisable(false);




            } catch(SQLException e) {
                e.printStackTrace();
            }

        });
    }

    public void buyGame() {

        try {

            System.out.println("Games");
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase",
                    "root", "thunder1515");

            PreparedStatement checkOwnership = connection.prepareStatement(
                    "SELECT COUNT(*) FROM transaction_log WHERE customer_id = ? AND game_id = ?");
            checkOwnership.setInt(1, customerIdSelector.getValue());
            checkOwnership.setInt(2, Integer.parseInt(gameIdText.getText()));
            ResultSet rsOwnership = checkOwnership.executeQuery();

            if (rsOwnership.next() && rsOwnership.getInt(1) > 0) {
                showAlert(Alert.AlertType.WARNING, "Already Owned",
                        "This customer already owns this game!");
                connection.close();
                return;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO transaction_log(customer_id, game_id, purchase_date, payment_method, amount) VALUES (?,?,CURDATE(),?,?) "
            );

            preparedStatement.setInt(1, customerIdSelector.getValue());
            preparedStatement.setInt(2, Integer.parseInt(gameIdText.getText()));
            preparedStatement.setString(3, paymentSelector.getValue());
            preparedStatement.setFloat(4, Float.parseFloat(priceText.getText()));
            preparedStatement.execute();

            PreparedStatement preparedStatement2 = connection.prepareStatement(
                    "UPDATE customer_record SET games_owned = games_owned + 1 WHERE customer_id= " + customerIdSelector.getValue()
            );
            preparedStatement2.execute();
            PreparedStatement preparedStatement3 = connection.prepareStatement(
                    "UPDATE customer_record SET total_spent = total_spent + " +priceText.getText() + " WHERE customer_id= " + customerIdSelector.getValue()
            );
            preparedStatement3.execute();
            PreparedStatement preparedStatement4 = connection.prepareStatement( "UPDATE game_record SET total_bought = total_bought + 1 WHERE game_id =" + gameIdText.getText()
            );
            preparedStatement4.execute();

            showAlert(Alert.AlertType.INFORMATION, "Purchase Successful",
                    "Game purchased successfully!\n\n" +
                            "Customer: " + firstNameText.getText() + " " + lastNameText.getText() + "\n" +
                            "Game: " + gameSelector.getValue() + "\n" +
                            "Amount: $" + priceText.getText() + "\n" +
                            "Payment: " + paymentSelector.getValue());





        } catch(SQLException e) {
            e.printStackTrace();
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
        System.out.println("works");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }






}


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo22;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class GameManagementApp extends Application {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/gamemanagementdatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "thunder1515";

    private final TableView<Customer> customerTable = new TableView<>();
    private final TableView<Developer> developerTable = new TableView<>();
    private final TableView<CustomerEngagement> customerReportTable = new TableView<>();
    private final TableView<ContractReport> contractReportTable = new TableView<>();

    private final ObservableList<Customer> customerData = FXCollections.observableArrayList();
    private final ObservableList<Developer> developerData = FXCollections.observableArrayList();
    private final ObservableList<CustomerEngagement> customerReportData = FXCollections.observableArrayList();
    private final ObservableList<ContractReport> contractReportData = FXCollections.observableArrayList();

    private final ComboBox<Integer> gameIdComboBox = new ComboBox<>();

    public static void main(String[] args) {
        launch(args);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Game Management System");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab customerTab = new Tab("Customer Management", createCustomerModule());
        Tab developerTab = new Tab("Developer Management", createDeveloperModule());
        Tab reportTab = new Tab("Reports", createReportModule());

        tabPane.getTabs().addAll(customerTab, developerTab, reportTab);

        Scene scene = new Scene(tabPane, 1280, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadCustomers();
        loadDevelopers();
        loadGameIds();
    }

    private Node createCustomerModule() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        VBox topForm = createCustomerAddForm();
        layout.setTop(topForm);

        setupCustomerTable();
        customerTable.setItems(customerData);
        VBox tableContainer = new VBox(10, new Label("All Customers"), customerTable);
        tableContainer.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        layout.setCenter(tableContainer);

        return layout;
    }

    private VBox createCustomerAddForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        Label title = new Label("Add New Customer");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField countryField = new TextField();
        countryField.setPromptText("Country");
        DatePicker registerField = new DatePicker(LocalDate.now());
        TextField platformField = new TextField();
        platformField.setPromptText("Preferred Platform");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Country:"), 2, 0);
        grid.add(countryField, 3, 0);
        grid.add(new Label("Register Date:"), 2, 1);
        grid.add(registerField, 3, 1);
        grid.add(new Label("Platform:"), 2, 2);
        grid.add(platformField, 3, 2);

        Button addButton = new Button("Add Customer");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> {
            handleAddCustomer(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    countryField.getText(),
                    registerField.getValue(),
                    platformField.getText()
            );
            firstNameField.clear();
            lastNameField.clear();
            emailField.clear();
            countryField.clear();
            platformField.clear();
            registerField.setValue(LocalDate.now());
        });

        HBox buttonBox = new HBox(addButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        formBox.getChildren().addAll(title, grid, buttonBox);
        return formBox;
    }

    private void setupCustomerTable() {
        customerTable.setEditable(true);

        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Customer, String> firstCol = new TableColumn<>("First Name");
        firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstCol.setCellFactory(TextFieldTableCell.forTableColumn());
        firstCol.setOnEditCommit(e -> {
            Customer customer = e.getRowValue();
            customer.setFirstName(e.getNewValue());
            updateCustomerField(customer.getId(), "first_name", e.getNewValue());
        });

        TableColumn<Customer, String> lastCol = new TableColumn<>("Last Name");
        lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastCol.setOnEditCommit(e -> {
            Customer customer = e.getRowValue();
            customer.setLastName(e.getNewValue());
            updateCustomerField(customer.getId(), "last_name", e.getNewValue());
        });

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        emailCol.setPrefWidth(200);
        emailCol.setOnEditCommit(e -> {
            Customer customer = e.getRowValue();
            customer.setEmail(e.getNewValue());
            updateCustomerField(customer.getId(), "email", e.getNewValue());
        });

        TableColumn<Customer, String> dateCol = new TableColumn<>("Reg. Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Customer, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        countryCol.setCellFactory(TextFieldTableCell.forTableColumn());
        countryCol.setOnEditCommit(e -> {
            Customer customer = e.getRowValue();
            customer.setCountry(e.getNewValue());
            updateCustomerField(customer.getId(), "country", e.getNewValue());
        });

        TableColumn<Customer, String> platformCol = new TableColumn<>("Platform");
        platformCol.setCellValueFactory(new PropertyValueFactory<>("platform"));
        platformCol.setCellFactory(TextFieldTableCell.forTableColumn());
        platformCol.setOnEditCommit(e -> {
            Customer customer = e.getRowValue();
            customer.setPlatform(e.getNewValue());
            updateCustomerField(customer.getId(), "preferred_platform", e.getNewValue());
        });

        TableColumn<Customer, Double> spentCol = new TableColumn<>("Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("spent"));

        TableColumn<Customer, Integer> gamesCol = new TableColumn<>("Games");
        gamesCol.setCellValueFactory(new PropertyValueFactory<>("games"));

        TableColumn<Customer, Void> actionCol = createCustomerActionColumn();

        customerTable.getColumns().addAll(idCol, firstCol, lastCol, emailCol, dateCol, countryCol, platformCol, spentCol, gamesCol, actionCol);
    }

    private TableColumn<Customer, Void> createCustomerActionColumn() {
        TableColumn<Customer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, viewButton, deleteButton);

            {
                viewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);

                viewButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    showCustomerDetails(customer);
                });

                deleteButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    handleDeleteCustomer(customer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        return actionCol;
    }

    private Node createDeveloperModule() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        VBox topForm = createDeveloperAddForm();
        layout.setTop(topForm);

        setupDeveloperTable();
        developerTable.setItems(developerData);
        VBox tableContainer = new VBox(10, new Label("All Developers"), developerTable);
        tableContainer.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(developerTable, Priority.ALWAYS);
        layout.setCenter(tableContainer);

        return layout;
    }

    private VBox createDeveloperAddForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        Label title = new Label("Add New Developer");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField roleField = new TextField();
        roleField.setPromptText("e.g., Programmer");
        TextField departmentField = new TextField();
        departmentField.setPromptText("e.g., Engineering");
        DatePicker hireDateField = new DatePicker(LocalDate.now());
        TextField rateField = new TextField();
        rateField.setPromptText("Hourly Rate (e.g., 50.00)");

        loadGameIds();
        gameIdComboBox.setPromptText("Assign Game");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleField, 1, 2);
        grid.add(new Label("Department:"), 2, 0);
        grid.add(departmentField, 3, 0);
        grid.add(new Label("Hire Date:"), 2, 1);
        grid.add(hireDateField, 3, 1);
        grid.add(new Label("Hourly Rate:"), 2, 2);
        grid.add(rateField, 3, 2);
        grid.add(new Label("Assigned Game:"), 0, 3);
        grid.add(gameIdComboBox, 1, 3);

        Button addButton = new Button("Add Developer");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> {
            handleAddDeveloper(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    roleField.getText(),
                    departmentField.getText(),
                    hireDateField.getValue(),
                    rateField.getText(),
                    gameIdComboBox.getValue()
            );
            firstNameField.clear();
            lastNameField.clear();
            roleField.clear();
            departmentField.clear();
            rateField.clear();
            hireDateField.setValue(LocalDate.now());
            gameIdComboBox.setValue(null);
        });

        HBox buttonBox = new HBox(addButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        formBox.getChildren().addAll(title, grid, buttonBox);
        return formBox;
    }

    private void setupDeveloperTable() {
        developerTable.setEditable(true);

        TableColumn<Developer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Developer, String> firstCol = new TableColumn<>("First Name");
        firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstCol.setCellFactory(TextFieldTableCell.forTableColumn());
        firstCol.setOnEditCommit(e -> {
            Developer dev = e.getRowValue();
            dev.setFirstName(e.getNewValue());
            updateDeveloperField(dev.getId(), "first_name", e.getNewValue());
        });

        TableColumn<Developer, String> lastCol = new TableColumn<>("Last Name");
        lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastCol.setOnEditCommit(e -> {
            Developer dev = e.getRowValue();
            dev.setLastName(e.getNewValue());
            updateDeveloperField(dev.getId(), "last_name", e.getNewValue());
        });

        TableColumn<Developer, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        roleCol.setOnEditCommit(e -> {
            Developer dev = e.getRowValue();
            dev.setRole(e.getNewValue());
            updateDeveloperField(dev.getId(), "role", e.getNewValue());
        });

        TableColumn<Developer, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        deptCol.setCellFactory(TextFieldTableCell.forTableColumn());
        deptCol.setOnEditCommit(e -> {
            Developer dev = e.getRowValue();
            dev.setDepartment(e.getNewValue());
            updateDeveloperField(dev.getId(), "department", e.getNewValue());
        });

        TableColumn<Developer, String> hireDateCol = new TableColumn<>("Hire Date");
        hireDateCol.setCellValueFactory(new PropertyValueFactory<>("hireDate"));

        TableColumn<Developer, Integer> gameIdCol = new TableColumn<>("Game ID");
        gameIdCol.setCellValueFactory(new PropertyValueFactory<>("assignedGameId"));
        gameIdCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        gameIdCol.setOnEditCommit(e -> {
            Developer dev = e.getRowValue();
            dev.setAssignedGameId(e.getNewValue());
            updateDeveloperField(dev.getId(), "assigned_game_id", e.getNewValue());
        });

        TableColumn<Developer, Double> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("developerRate"));
        rateCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        rateCol.setOnEditCommit(e -> {
            Developer dev = e.getRowValue();
            dev.setDeveloperRate(e.getNewValue());
            updateDeveloperField(dev.getId(), "developer_rate", e.getNewValue());
        });

        TableColumn<Developer, Void> actionCol = createDeveloperActionColumn();

        developerTable.getColumns().addAll(idCol, firstCol, lastCol, roleCol, deptCol, hireDateCol, gameIdCol, rateCol, actionCol);
    }

    private TableColumn<Developer, Void> createDeveloperActionColumn() {
        TableColumn<Developer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(100);

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(deleteButton);

            {
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);

                deleteButton.setOnAction(event -> {
                    Developer dev = getTableView().getItems().get(getIndex());
                    handleDeleteDeveloper(dev);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        return actionCol;
    }

    private Node createReportModule() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(10));

        VBox customerReportBox = createCustomerReportGenerator();
        VBox developerReportBox = createDeveloperReportGenerator();

        layout.getChildren().addAll(customerReportBox, new Separator(), developerReportBox);
        VBox.setVgrow(customerReportBox, Priority.ALWAYS);
        VBox.setVgrow(developerReportBox, Priority.ALWAYS);

        return layout;
    }

    private VBox createCustomerReportGenerator() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        Label title = new Label("Customer Engagement Report");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup filterGroup = new ToggleGroup();
        RadioButton yearRadio = new RadioButton("By Year");
        yearRadio.setToggleGroup(filterGroup);
        RadioButton monthRadio = new RadioButton("By Month");
        monthRadio.setToggleGroup(filterGroup);

        Spinner<Integer> yearSpinner = new Spinner<>(2000, 2100, LocalDate.now().getYear());
        Spinner<Integer> monthSpinner = new Spinner<>(1, 12, LocalDate.now().getMonthValue());
        yearSpinner.setDisable(true);
        monthSpinner.setDisable(true);

        yearRadio.setOnAction(e -> {
            yearSpinner.setDisable(false);
            monthSpinner.setDisable(true);
        });
        monthRadio.setOnAction(e -> {
            yearSpinner.setDisable(false);
            monthSpinner.setDisable(false);
        });

        Button generateButton = new Button("Generate");
        generateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        generateButton.setOnAction(e -> handleGenerateCustomerReport(
                filterGroup.getSelectedToggle(),
                yearRadio,
                yearSpinner.getValue(),
                monthSpinner.getValue()
        ));

        controls.getChildren().addAll(yearRadio, monthRadio, new Label("Year:"), yearSpinner, new Label("Month:"), monthSpinner, generateButton);

        setupCustomerReportTable();
        customerReportTable.setItems(customerReportData);

        box.getChildren().addAll(title, controls, customerReportTable);
        VBox.setVgrow(customerReportTable, Priority.ALWAYS);
        return box;
    }

    private void setupCustomerReportTable() {
        TableColumn<CustomerEngagement, Integer> idCol = new TableColumn<>("Customer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<CustomerEngagement, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        TableColumn<CustomerEngagement, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<CustomerEngagement, Integer> purchasesCol = new TableColumn<>("Total Purchases");
        purchasesCol.setCellValueFactory(new PropertyValueFactory<>("totalPurchases"));
        TableColumn<CustomerEngagement, Double> spentCol = new TableColumn<>("Total Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));

        customerReportTable.getColumns().setAll(idCol, nameCol, emailCol, purchasesCol, spentCol);
        customerReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private VBox createDeveloperReportGenerator() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        Label title = new Label("Developer-Publisher Contract Report");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button generateButton = new Button("Generate Report");
        generateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        generateButton.setOnAction(e -> handleGenerateContractReport());

        setupContractReportTable();
        contractReportTable.setItems(contractReportData);

        box.getChildren().addAll(title, generateButton, contractReportTable);
        VBox.setVgrow(contractReportTable, Priority.ALWAYS);
        return box;
    }

    private void setupContractReportTable() {
        TableColumn<ContractReport, String> devCol = new TableColumn<>("Developer");
        devCol.setCellValueFactory(new PropertyValueFactory<>("developerName"));
        TableColumn<ContractReport, Double> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("developerRate"));
        TableColumn<ContractReport, String> gameCol = new TableColumn<>("Assigned Game");
        gameCol.setCellValueFactory(new PropertyValueFactory<>("gameTitle"));
        TableColumn<ContractReport, String> pubCol = new TableColumn<>("Publisher");
        pubCol.setCellValueFactory(new PropertyValueFactory<>("publisherName"));
        TableColumn<ContractReport, String> contractDateCol = new TableColumn<>("Contract Date");
        contractDateCol.setCellValueFactory(new PropertyValueFactory<>("contractDate"));
        TableColumn<ContractReport, Double> royaltyCol = new TableColumn<>("Royalty %");
        royaltyCol.setCellValueFactory(new PropertyValueFactory<>("royaltyPercent"));

        contractReportTable.getColumns().setAll(devCol, rateCol, gameCol, pubCol, contractDateCol, royaltyCol);
        contractReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadCustomers() {
        customerData.clear();
        String sql = "SELECT * FROM customer_record ORDER BY customer_id";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                customerData.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("registration_date"),
                        rs.getString("country"),
                        rs.getString("preferred_platform"),
                        rs.getDouble("total_spent"),
                        rs.getInt("games_owned")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load customers: " + e.getMessage());
        }
    }

    private void loadDevelopers() {
        developerData.clear();
        String sql = "SELECT * FROM developer_record ORDER BY developer_id";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                developerData.add(new Developer(
                        rs.getInt("developer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getString("department"),
                        rs.getString("hire_date"),
                        rs.getInt("assigned_game_id"),
                        rs.getDouble("developer_rate")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load developers: " + e.getMessage());
        }
    }

    private void loadGameIds() {
        ObservableList<Integer> gameIds = FXCollections.observableArrayList();
        String sql = "SELECT game_id FROM game_record WHERE status = 'active' OR status = 'under_dev'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                gameIds.add(rs.getInt("game_id"));
            }
            gameIdComboBox.setItems(gameIds);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load game IDs: " + e.getMessage());
        }
    }

    private void handleAddCustomer(String first, String last, String email, String country, LocalDate date, String platform) {
        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || country.isEmpty() || date == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields except platform are required.");
            return;
        }

        String sql = "INSERT INTO customer_record(last_name, first_name, email, registration_date, country, preferred_platform) VALUES(?,?,?,?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, last);
            pstmt.setString(2, first);
            pstmt.setString(3, email);
            pstmt.setDate(4, Date.valueOf(date));
            pstmt.setString(5, country);
            pstmt.setString(6, platform.isEmpty() ? "None" : platform);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");
                loadCustomers();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add customer: " + e.getMessage());
        }
    }

    private void handleAddDeveloper(String first, String last, String role, String dept, LocalDate date, String rate, Integer gameId) {
        if (first.isEmpty() || last.isEmpty() || role.isEmpty() || dept.isEmpty() || date == null || rate.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields except Assigned Game are required.");
            return;
        }

        double devRate;
        try {
            devRate = Double.parseDouble(rate);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Developer rate must be a valid number.");
            return;
        }

        String sql = "INSERT INTO developer_record(first_name, last_name, role, department, hire_date, assigned_game_id, developer_rate) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, first);
            pstmt.setString(2, last);
            pstmt.setString(3, role);
            pstmt.setString(4, dept);
            pstmt.setDate(5, Date.valueOf(date));

            if (gameId == null) {
                pstmt.setNull(6, Types.INTEGER);
            } else {
                pstmt.setInt(6, gameId);
            }

            pstmt.setDouble(7, devRate);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Developer added successfully!");
                loadDevelopers();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add developer: " + e.getMessage());
        }
    }

    private void updateCustomerField(int id, String dbField, Object value) {
        String sql = "UPDATE customer_record SET " + dbField + " = ? WHERE customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, value);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update customer: " + e.getMessage());
            loadCustomers();
        }
    }

    private void updateDeveloperField(int id, String dbField, Object value) {
        String sql = "UPDATE developer_record SET " + dbField + " = ? WHERE developer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, value);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update developer: " + e.getMessage());
            loadDevelopers();
        }
    }

    private void handleDeleteCustomer(Customer customer) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Customer?");
        confirm.setContentText("Are you sure you want to delete " + customer.getFirstName() + " " + customer.getLastName() + "?\nThis action cannot be undone!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String checkSql = "SELECT COUNT(*) FROM transaction_log WHERE customer_id = ?";
            String deleteSql = "DELETE FROM customer_record WHERE customer_id = ?";

            try (Connection conn = getConnection()) {
                boolean hasTransactions = false;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, customer.getId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            hasTransactions = true;
                        }
                    }
                }

                if (hasTransactions) {
                    showAlert(Alert.AlertType.WARNING, "Deletion Failed", "Cannot delete customer. This customer has existing transactions.");
                    return;
                }

                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, customer.getId());
                    int rowsAffected = deleteStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Customer deleted successfully.");
                        customerData.remove(customer);
                    }
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete customer: " + e.getMessage());
            }
        }
    }

    private void handleDeleteDeveloper(Developer dev) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Developer?");
        confirm.setContentText("Are you sure you want to delete " + dev.getFirstName() + " " + dev.getLastName() + "?\nThis may affect contract records.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deleteSql = "DELETE FROM developer_record WHERE developer_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

                deleteStmt.setInt(1, dev.getId());
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Developer deleted successfully.");
                    developerData.remove(dev);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete developer. They may be linked to active contracts: " + e.getMessage());
            }
        }
    }

    private void showCustomerDetails(Customer customer) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Customer Details: " + customer.getFirstName() + " " + customer.getLastName());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(650);

        GridPane customerInfo = new GridPane();
        customerInfo.setHgap(10);
        customerInfo.setVgap(8);
        customerInfo.add(new Label("ID:"), 0, 0);
        customerInfo.add(new Label(String.valueOf(customer.getId())), 1, 0);
        customerInfo.add(new Label("Name:"), 0, 1);
        customerInfo.add(new Label(customer.getFirstName() + " " + customer.getLastName()), 1, 1);
        customerInfo.add(new Label("Email:"), 0, 2);
        customerInfo.add(new Label(customer.getEmail()), 1, 2);
        customerInfo.add(new Label("Registration Date:"), 0, 3);
        customerInfo.add(new Label(customer.getDate()), 1, 3);
        customerInfo.add(new Label("Country:"), 0, 4);
        customerInfo.add(new Label(customer.getCountry()), 1, 4);
        customerInfo.add(new Label("Preferred Platform:"), 0, 5);
        customerInfo.add(new Label(customer.getPlatform()), 1, 5);
        customerInfo.add(new Label("Total Spent:"), 0, 6);
        customerInfo.add(new Label(String.format("$%.2f", customer.getSpent())), 1, 6);
        customerInfo.add(new Label("Games Owned:"), 0, 7);
        customerInfo.add(new Label(String.valueOf(customer.getGames())), 1, 7);

        Label gamesHeader = new Label("Games Purchased");
        gamesHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<GamePurchase> gamesTable = new TableView<>();
        gamesTable.setPrefHeight(250);

        TableColumn<GamePurchase, String> titleCol = new TableColumn<>("Game Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(300);
        TableColumn<GamePurchase, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        TableColumn<GamePurchase, String> dateCol = new TableColumn<>("Purchase Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        TableColumn<GamePurchase, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        gamesTable.getColumns().addAll(titleCol, priceCol, dateCol, paymentCol);

        ObservableList<GamePurchase> games = getCustomerGamesDetailed(customer.getId());
        gamesTable.setItems(games);

        if (games.isEmpty()) {
            content.getChildren().addAll(customerInfo, gamesHeader, new Label("No games purchased yet."));
        } else {
            content.getChildren().addAll(customerInfo, gamesHeader, gamesTable);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private ObservableList<GamePurchase> getCustomerGamesDetailed(int customerId) {
        ObservableList<GamePurchase> games = FXCollections.observableArrayList();
        String sql = "SELECT gr.title, tl.amount, tl.purchase_date, tl.payment_method " +
                "FROM game_record gr " +
                "JOIN transaction_log tl ON gr.game_id = tl.game_id " +
                "WHERE tl.customer_id = ? " +
                "ORDER BY tl.purchase_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    games.add(new GamePurchase(
                            rs.getString("title"),
                            rs.getDouble("amount"),
                            rs.getString("purchase_date"),
                            rs.getString("payment_method")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load game purchase history.");
        }
        return games;
    }

    private void handleGenerateCustomerReport(Toggle selected, RadioButton yearRadio, int year, int month) {
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Filter Selected", "Please select either By Year or By Month.");
            return;
        }

        customerReportData.clear();
        boolean byYearOnly = (selected == yearRadio);

        String sql = "SELECT c.customer_id, c.first_name, c.last_name, c.email, " +
                "COUNT(t.transaction_id) as total_purchases, SUM(t.amount) as total_spent " +
                "FROM customer_record c " +
                "JOIN transaction_log t ON c.customer_id = t.customer_id " +
                "WHERE YEAR(t.purchase_date) = ? " +
                (byYearOnly ? "" : "AND MONTH(t.purchase_date) = ? ") +
                "GROUP BY c.customer_id, c.first_name, c.last_name, c.email " +
                "ORDER BY total_spent DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            if (!byYearOnly) {
                pstmt.setInt(2, month);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customerReportData.add(new CustomerEngagement(
                            rs.getInt("customer_id"),
                            rs.getString("first_name") + " " + rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getInt("total_purchases"),
                            rs.getDouble("total_spent")
                    ));
                }
            }

            if (customerReportData.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Data", "No transactions found for the selected period.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error generating report: " + e.getMessage());
        }
    }

    private void handleGenerateContractReport() {
        contractReportData.clear();
        String sql = "SELECT " +
                "d.first_name, d.last_name, d.developer_rate, " +
                "g.title AS game_title, " +
                "p.publisher_name, " +
                "cr.contract_date, cr.royalty_percent " +
                "FROM developer_record d " +
                "LEFT JOIN game_record g ON d.assigned_game_id = g.game_id " +
                "LEFT JOIN contract_record cr ON d.developer_id = cr.developer_id AND g.game_id = cr.game_id " +
                "LEFT JOIN publisher_record p ON cr.publisher_id = p.publisher_id " +
                "ORDER BY d.last_name, d.first_name";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                contractReportData.add(new ContractReport(
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getDouble("developer_rate"),
                        Objects.toString(rs.getString("game_title"), "N/A"),
                        Objects.toString(rs.getString("publisher_name"), "N/A"),
                        Objects.toString(rs.getString("contract_date"), "N/A"),
                        rs.getDouble("royalty_percent")
                ));
            }

            if (contractReportData.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Data", "No developer or contract data found.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error generating report: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Customer {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty email;
        private final SimpleStringProperty date;
        private final SimpleStringProperty country;
        private final SimpleStringProperty platform;
        private final SimpleDoubleProperty spent;
        private final SimpleIntegerProperty games;

        public Customer(int id, String firstName, String lastName, String email, String date, String country, String platform, double spent, int games) {
            this.id = new SimpleIntegerProperty(id);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.email = new SimpleStringProperty(email);
            this.date = new SimpleStringProperty(date);
            this.country = new SimpleStringProperty(country);
            this.platform = new SimpleStringProperty(platform);
            this.spent = new SimpleDoubleProperty(spent);
            this.games = new SimpleIntegerProperty(games);
        }

        public int getId() { return id.get(); }
        public String getFirstName() { return firstName.get(); }
        public String getLastName() { return lastName.get(); }
        public String getEmail() { return email.get(); }
        public String getDate() { return date.get(); }
        public String getCountry() { return country.get(); }
        public String getPlatform() { return platform.get(); }
        public double getSpent() { return spent.get(); }
        public int getGames() { return games.get(); }

        public void setFirstName(String value) { firstName.set(value); }
        public void setLastName(String value) { lastName.set(value); }
        public void setEmail(String value) { email.set(value); }
        public void setCountry(String value) { country.set(value); }
        public void setPlatform(String value) { platform.set(value); }
    }

    public static class GamePurchase {
        private final SimpleStringProperty title;
        private final SimpleDoubleProperty price;
        private final SimpleStringProperty purchaseDate;
        private final SimpleStringProperty paymentMethod;

        public GamePurchase(String title, double price, String purchaseDate, String paymentMethod) {
            this.title = new SimpleStringProperty(title);
            this.price = new SimpleDoubleProperty(price);
            this.purchaseDate = new SimpleStringProperty(purchaseDate);
            this.paymentMethod = new SimpleStringProperty(paymentMethod);
        }

        public String getTitle() { return title.get(); }
        public double getPrice() { return price.get(); }
        public String getPurchaseDate() { return purchaseDate.get(); }
        public String getPaymentMethod() { return paymentMethod.get(); }
    }

    public static class CustomerEngagement {
        private final SimpleIntegerProperty customerId;
        private final SimpleStringProperty customerName;
        private final SimpleStringProperty email;
        private final SimpleIntegerProperty totalPurchases;
        private final SimpleDoubleProperty totalSpent;

        public CustomerEngagement(int customerId, String customerName, String email, int totalPurchases, double totalSpent) {
            this.customerId = new SimpleIntegerProperty(customerId);
            this.customerName = new SimpleStringProperty(customerName);
            this.email = new SimpleStringProperty(email);
            this.totalPurchases = new SimpleIntegerProperty(totalPurchases);
            this.totalSpent = new SimpleDoubleProperty(totalSpent);
        }

        public int getCustomerId() { return customerId.get(); }
        public String getCustomerName() { return customerName.get(); }
        public String getEmail() { return email.get(); }
        public int getTotalPurchases() { return totalPurchases.get(); }
        public double getTotalSpent() { return totalSpent.get(); }
    }

    public static class Developer {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty role;
        private final SimpleStringProperty department;
        private final SimpleStringProperty hireDate;
        private final SimpleIntegerProperty assignedGameId;
        private final SimpleDoubleProperty developerRate;

        public Developer(int id, String firstName, String lastName, String role, String department, String hireDate, int assignedGameId, double developerRate) {
            this.id = new SimpleIntegerProperty(id);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.role = new SimpleStringProperty(role);
            this.department = new SimpleStringProperty(department);
            this.hireDate = new SimpleStringProperty(hireDate);
            this.assignedGameId = new SimpleIntegerProperty(assignedGameId);
            this.developerRate = new SimpleDoubleProperty(developerRate);

            if (assignedGameId == 0) {
                this.assignedGameId.set(0);
            }
        }

        public int getId() { return id.get(); }
        public String getFirstName() { return firstName.get(); }
        public String getLastName() { return lastName.get(); }
        public String getRole() { return role.get(); }
        public String getDepartment() { return department.get(); }
        public String getHireDate() { return hireDate.get(); }
        public int getAssignedGameId() { return assignedGameId.get(); }
        public double getDeveloperRate() { return developerRate.get(); }

        public void setFirstName(String value) { firstName.set(value); }
        public void setLastName(String value) { lastName.set(value); }
        public void setRole(String value) { role.set(value); }
        public void setDepartment(String value) { department.set(value); }
        public void setAssignedGameId(int value) { assignedGameId.set(value); }
        public void setDeveloperRate(double value) { developerRate.set(value); }
    }

    public static class ContractReport {
        private final SimpleStringProperty developerName;
        private final SimpleDoubleProperty developerRate;
        private final SimpleStringProperty gameTitle;
        private final SimpleStringProperty publisherName;
        private final SimpleStringProperty contractDate;
        private final SimpleDoubleProperty royaltyPercent;

        public ContractReport(String developerName, double developerRate, String gameTitle, String publisherName, String contractDate, double royaltyPercent) {
            this.developerName = new SimpleStringProperty(developerName);
            this.developerRate = new SimpleDoubleProperty(developerRate);
            this.gameTitle = new SimpleStringProperty(gameTitle);
            this.publisherName = new SimpleStringProperty(publisherName);
            this.contractDate = new SimpleStringProperty(contractDate);
            this.royaltyPercent = new SimpleDoubleProperty(royaltyPercent);
        }

        public String getDeveloperName() { return developerName.get(); }
        public double getDeveloperRate() { return developerRate.get(); }
        public String getGameTitle() { return gameTitle.get(); }
        public String getPublisherName() { return publisherName.get(); }
        public String getContractDate() { return contractDate.get(); }
        public double getRoyaltyPercent() { return royaltyPercent.get(); }
    }
}
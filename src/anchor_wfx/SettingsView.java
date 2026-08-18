package anchor_wfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * SettingsView — Admin settings and employee management.
 */
public class SettingsView {

    private static final String NAVY_BLUE = Dashboard.NAVY_BLUE;
    private static final String LIGHT_BLUE = Dashboard.LIGHT_BLUE;
    private static final String DARK_NAVY = Dashboard.DARK_NAVY;

    // Admin credentials placeholder
    private static String currentAdminUsername = Session.getUser();
    private static String currentAdminEmployeeId; // loaded from DB
    private static String currentAdminPassword;   // loaded from DB

    // Employee data
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private static int nextEmployeeId = 100;

    private final BorderPane innerContent;
    private final Button[] tabButtons = new Button[2];
    private static final String[] TAB_LABELS = {"Admin Account", "Employee Management"};

    private final Dashboard dashboard;

    public SettingsView(Dashboard dashboard) {
        this.dashboard = dashboard;
        this.innerContent = new BorderPane();

        OptionQueries queries = new OptionQueries();
        employeeList.addAll(queries.getUserListFromDatabase());

        Employee currentUser = queries.getUserByUsername(Session.getUser());
        if (currentUser != null) {
            currentAdminUsername = currentUser.getUsername();
            currentAdminPassword = currentUser.getPassword();
            currentAdminEmployeeId = currentUser.getEmployeeId();
        }
    }

    private String generateEmployeeId() {
        return "EMP-" + String.format("%04d", nextEmployeeId++);
    }

    // Any employee with SUPER_ADMIN role cannot be deleted
    private boolean isSystemAdmin(Employee emp) {
        return emp.getRole() == EmployeeRole.SUPER_ADMIN;
    }

    public VBox build() {
        VBox shell = new VBox(0);
        shell.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");
        shell.setMaxHeight(Double.MAX_VALUE);
        shell.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(innerContent, Priority.ALWAYS);
        shell.getChildren().addAll(buildTabBar(), innerContent);
        return shell;
    }

    private HBox buildTabBar() {
        HBox bar = new HBox(0);
        bar.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        for (int i = 0; i < TAB_LABELS.length; i++) {
            Button btn = new Button(TAB_LABELS[i]);
            btn.setPrefHeight(46);
            btn.setPrefWidth(190);
            btn.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            final int idx = i;
            btn.setOnAction(e -> navigateTo(idx));
            tabButtons[i] = btn;
            bar.getChildren().add(btn);
        }
        if (Session.isAccountManager()) {
            tabButtons[0].setVisible(false);
            tabButtons[0].setManaged(false);
            navigateTo(1); // go straight to Employee Management
        } else {
            navigateTo(0); // default to Admin Account
        }
        return bar;
    }

    private void navigateTo(int tabIndex) {
        updateTabStyles(tabIndex);
        switch (tabIndex) {
            case 0 ->
                innerContent.setCenter(buildAdminSettingsTab());
            case 1 ->
                innerContent.setCenter(buildEmployeeManagementTab());
        }
    }

    private void updateTabStyles(int active) {
        for (int i = 0; i < tabButtons.length; i++) {
            boolean on = (i == active);
            tabButtons[i].setStyle(
                    "-fx-background-color: " + (on ? AppStyles.NAVY_BLUE : "white") + "; "
                    + "-fx-text-fill: " + (on ? "white" : AppStyles.NAVY_BLUE) + "; "
                    + "-fx-font-weight: " + (on ? "bold" : "normal") + "; "
                    + "-fx-border-color: " + AppStyles.NAVY_BLUE + "; "
                    + "-fx-border-width: 0 1 " + (on ? "3" : "0") + " 0; "
                    + "-fx-cursor: hand;"
            );
        }
    }

    // =============================================================
    // TAB 1: ADMIN ACCOUNT
    // =============================================================
    private ScrollPane buildAdminSettingsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");
        content.setMaxWidth(550);
        content.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 35;");

        Label title = new Label("Administrator Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(NAVY_BLUE));

        // Current info
        VBox infoBox = new VBox(12);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #F5F7FA; -fx-background-radius: 10;");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);

        Label empIdLabel = new Label("Employee ID:");
        empIdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Label empIdValue = new Label(currentAdminEmployeeId);
        empIdValue.setFont(Font.font("Arial", 13));
        empIdValue.setTextFill(Color.web(NAVY_BLUE));

        Label userLabel = new Label("Username:");
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Label userValue = new Label(currentAdminUsername);
        userValue.setFont(Font.font("Arial", 13));
        userValue.setTextFill(Color.web(NAVY_BLUE));

        infoGrid.add(empIdLabel, 0, 0);
        infoGrid.add(empIdValue, 1, 0);
        infoGrid.add(userLabel, 0, 1);
        infoGrid.add(userValue, 1, 1);
        infoBox.getChildren().add(infoGrid);

        Separator separator = new Separator();

        // Change credentials form
        VBox formBox = new VBox(15);

        Label changeLabel = new Label("Change Credentials");
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        changeLabel.setTextFill(Color.web(NAVY_BLUE));

        Label currentPassLabel = new Label("Current Password");
        currentPassLabel.setFont(Font.font("Arial", 12));
        currentPassLabel.setTextFill(Color.web("#666666"));

        PasswordField currentPassField = new PasswordField();
        currentPassField.setPromptText("Enter current password");
        currentPassField.setPrefHeight(44);
        currentPassField.setStyle(fieldStyleLarge());

        Label newUsernameLabel = new Label("New Username (optional)");
        newUsernameLabel.setFont(Font.font("Arial", 12));
        newUsernameLabel.setTextFill(Color.web("#666666"));

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Enter new username");
        newUsernameField.setPrefHeight(44);
        newUsernameField.setStyle(fieldStyleLarge());

        Label newPasswordLabel = new Label("New Password (optional)");
        newPasswordLabel.setFont(Font.font("Arial", 12));
        newPasswordLabel.setTextFill(Color.web("#666666"));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password (min. 6 characters)");
        newPasswordField.setPrefHeight(44);
        newPasswordField.setStyle(fieldStyleLarge());

        Label confirmLabel = new Label("Confirm New Password");
        confirmLabel.setFont(Font.font("Arial", 12));
        confirmLabel.setTextFill(Color.web("#666666"));

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Re-enter new password");
        confirmField.setPrefHeight(44);
        confirmField.setStyle(fieldStyleLarge());

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#C62828"));
        errorLabel.setFont(Font.font("Arial", 12));

        Button updateBtn = new Button("Update Credentials");
        updateBtn.setPrefHeight(44);
        updateBtn.setPrefWidth(200);
        updateBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        updateBtn.setStyle(primaryButtonStyle());
        updateBtn.setOnMouseEntered(e -> updateBtn.setStyle(primaryButtonHoverStyle()));
        updateBtn.setOnMouseExited(e -> updateBtn.setStyle(primaryButtonStyle()));

        updateBtn.setOnAction(e -> {
            String currentPass = currentPassField.getText();
            String newUsername = newUsernameField.getText().trim();
            String newPassword = newPasswordField.getText();
            String confirm = confirmField.getText();

            if (!currentPass.equals(currentAdminPassword)) {
                errorLabel.setText("Current password is incorrect!");
                return;
            }
            if (newUsername.isEmpty() && newPassword.isEmpty()) {
                errorLabel.setText("Enter new username or password to update.");
                return;
            }
            if (!newPassword.isEmpty() && !newPassword.equals(confirm)) {
                errorLabel.setText("New passwords do not match!");
                return;
            }
            if (!newPassword.isEmpty() && newPassword.length() < 6) {
                errorLabel.setText("Password must be at least 6 characters.");
                return;
            }

            if (!newUsername.isEmpty()) {
                currentAdminUsername = newUsername;
            }
            if (!newPassword.isEmpty()) {
                currentAdminPassword = newPassword;
            }

            OptionQueries queries = new OptionQueries();
            boolean saved = queries.updateAdminCredentials(
                    currentAdminEmployeeId,
                    currentAdminUsername,
                    currentAdminPassword
            );

            if (saved) {
                showAlert("Success", "Credentials Updated", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to update credentials in database.", Alert.AlertType.ERROR);
            }

            currentPassField.clear();
            newUsernameField.clear();
            newPasswordField.clear();
            confirmField.clear();
            errorLabel.setText("");
            userValue.setText(currentAdminUsername);
        });

        formBox.getChildren().addAll(
                changeLabel,
                currentPassLabel, currentPassField,
                newUsernameLabel, newUsernameField,
                newPasswordLabel, newPasswordField,
                confirmLabel, confirmField,
                errorLabel, updateBtn
        );

        card.getChildren().addAll(title, infoBox, separator, formBox);
        content.getChildren().add(card);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + LIGHT_BLUE + "; -fx-background-color: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    // =============================================================
    // TAB 2: EMPLOYEE MANAGEMENT
    // =============================================================
    private VBox buildEmployeeManagementTab() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        // Header
        Label title = new Label("System Employees");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(NAVY_BLUE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add New Employee");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        addBtn.setStyle(blueButtonStyle());
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(blueButtonHoverStyle()));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(blueButtonStyle()));
        addBtn.setOnAction(e -> showAddEmployeeDialog());

        HBox header = new HBox(12, title, spacer, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // ===== FIXED: LARGER SEARCH FIELD - FORCED HEIGHT =====
        TextField searchField = new TextField();
        searchField.setPromptText("Search by employee ID or username...");
        searchField.setPrefHeight(48);        // Taller - explicitly set
        searchField.setMinHeight(48);         // Force minimum height
        searchField.setMaxHeight(48);         // Force maximum height
        searchField.setPrefWidth(500);        // Wider
        searchField.setStyle(
                "-fx-font-size: 14px; "
                + "-fx-padding: 10 15 10 15; "
                + "-fx-border-color: #E0E0E0; "
                + "-fx-border-radius: 8; "
                + "-fx-background-radius: 8;"
        );
        // Table
        TableView<Employee> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Employee, String> idCol = new TableColumn<>("Employee ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        idCol.setPrefWidth(120);

        TableColumn<Employee, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<Employee, EmployeeRole> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(200);
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(EmployeeRole role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(role.getDisplayName());
                    badge.setStyle("-fx-background-color: " + role.getColor() + "; -fx-text-fill: white; -fx-padding: 5 14 5 14; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Employee, Boolean> activeCol = new TableColumn<>("Status");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(90);
        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                } else {
                    Label status = new Label(active ? "Active" : "Inactive");
                    status.setTextFill(active ? Color.web("#2E7D32") : Color.web("#C62828"));
                    status.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                    setGraphic(status);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Employee, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(130);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button resetBtn = new Button("Reset");
            private final HBox buttons = new HBox(6, editBtn, deleteBtn, resetBtn);

            {
                editBtn.setStyle(outlineButtonStyle());
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(outlineButtonHoverStyle()));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(outlineButtonStyle()));
                editBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    innerContent.setCenter(buildEditEmployeePage(emp));
                });

                deleteBtn.setStyle(dangerButtonStyle());
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(dangerButtonHoverStyle()));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(dangerButtonStyle()));
                deleteBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    confirmDelete(emp);
                });

                resetBtn.setStyle(warningButtonStyle());
                resetBtn.setOnMouseEntered(e -> resetBtn.setStyle(warningButtonHoverStyle()));
                resetBtn.setOnMouseExited(e -> resetBtn.setStyle(warningButtonStyle()));
                resetBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    resetPassword(emp);
                });

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Employee emp = getTableRow().getItem();

                // Hide delete button for ANY SUPER_ADMIN
                if (isSystemAdmin(emp)) {
                    deleteBtn.setVisible(false);
                    deleteBtn.setManaged(false);
                } else {
                    deleteBtn.setVisible(true);
                    deleteBtn.setManaged(true);
                }

                setGraphic(buttons);
            }
        });

        table.getColumns().addAll(idCol, usernameCol, roleCol, activeCol, actionsCol);

        // Filter
        ObservableList<Employee> filtered = FXCollections.observableArrayList(employeeList);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, old, val) -> {
            String search = val.toLowerCase();
            filtered.setAll(employeeList.filtered(e
                    -> e.getEmployeeId().toLowerCase().contains(search)
                    || e.getUsername().toLowerCase().contains(search)
            ));
        });

        // Stats bar
        HBox statsBar = new HBox(15);
        statsBar.setPadding(new Insets(10, 0, 0, 0));

        long total = employeeList.size();
        long active = employeeList.stream().filter(Employee::isActive).count();
        long inactive = total - active;

        Label stats = new Label(String.format("Total: %d  |  Active: %d  |  Inactive: %d", total, active, inactive));
        stats.setFont(Font.font("Arial", 12));
        stats.setTextFill(Color.web("#666666"));
        statsBar.getChildren().add(stats);

        content.getChildren().addAll(header, searchField, table, statsBar);
        VBox.setVgrow(table, Priority.ALWAYS);

        return content;
    }

    // =============================================================
    // EDIT EMPLOYEE - FULL PAGE
    // =============================================================
    private ScrollPane buildEditEmployeePage(Employee emp) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");
        content.setMaxWidth(700);
        content.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 35;");

        // Header with back button
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back to Employee List");
        backBtn.setStyle(outlineButtonStyle());
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(outlineButtonHoverStyle()));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(outlineButtonStyle()));
        backBtn.setOnAction(e -> innerContent.setCenter(buildEmployeeManagementTab()));

        Label pageTitle = new Label("Edit Employee");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(NAVY_BLUE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backBtn, spacer, pageTitle);

        Separator topSep = new Separator();

        // Form fields
        VBox formBox = new VBox(18);
        formBox.setPadding(new Insets(10, 0, 0, 0));

        // Employee ID (read-only)
        Label empIdLabel = new Label("Employee ID");
        empIdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        TextField empIdField = new TextField(emp.getEmployeeId());
        empIdField.setEditable(false);
        empIdField.setPrefHeight(44);
        empIdField.setStyle(fieldStyleLarge() + " -fx-background-color: #f5f5f5;");

        // Username (read-only)
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        TextField usernameField = new TextField(emp.getUsername());
        usernameField.setEditable(false);
        usernameField.setPrefHeight(44);
        usernameField.setStyle(fieldStyleLarge() + " -fx-background-color: #f5f5f5;");

        // Full Name (editable - only visible in edit mode)
        Label fullNameLabel = new Label("Full Name *");
        fullNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        TextField fullNameField = new TextField(emp.getFullName());
        fullNameField.setPromptText("Enter full name");
        fullNameField.setPrefHeight(44);
        fullNameField.setStyle(fieldStyleLarge());

        // Email
        Label emailLabel = new Label("Email Address *");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        TextField emailField = new TextField(emp.getEmail());
        emailField.setPromptText("Enter email address");
        emailField.setPrefHeight(44);
        emailField.setStyle(fieldStyleLarge());

        // Role
        Label roleLabel = new Label("Role *");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<EmployeeRole> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(
                EmployeeRole.ACCOUNT_MANAGER,
                EmployeeRole.OPERATIONS_OFFICER,
                EmployeeRole.BILLING_OFFICER,
                EmployeeRole.FLEET_MANAGER,
                EmployeeRole.CUSTOMER_OFFICER,
                EmployeeRole.AUDITOR
        );
        if (Session.isSuperAdmin()) {
            roleCombo.getItems().add(0, EmployeeRole.SUPER_ADMIN);
        }
        roleCombo.setValue(emp.getRole());
        roleCombo.setPrefHeight(44);
        roleCombo.setStyle(comboBoxStyleLarge());

        if (emp.getUsername().equals(Session.getUser())) {
            Label selfEditWarning = new Label(
                    "⚠ You are editing your own account. Changing your role will log you out immediately."
            );
            selfEditWarning.setTextFill(Color.web("#C62828"));
            selfEditWarning.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            selfEditWarning.setWrapText(true);
            formBox.getChildren().add(selfEditWarning);
        }

        // Role description
        Label roleDesc = new Label();
        roleDesc.setFont(Font.font("Arial", 11));
        roleDesc.setTextFill(Color.web("#888888"));
        roleDesc.setWrapText(true);
        updateRoleDescription(roleCombo.getValue(), roleDesc);
        roleCombo.valueProperty().addListener((obs, old, val) -> updateRoleDescription(val, roleDesc));

        // Active status
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(emp.isActive());
        activeCheck.setFont(Font.font("Arial", 13));

        if (emp.getUsername().equals(Session.getUser())) {
            roleCombo.setDisable(true);
            activeCheck.setDisable(true); // ← add this

            Label selfEditWarning = new Label(
                    "⚠ You cannot change your own role or status."
            );
            selfEditWarning.setTextFill(Color.web("#C62828"));
            selfEditWarning.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            selfEditWarning.setWrapText(true);
            formBox.getChildren().add(selfEditWarning);
        }

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#C62828"));
        errorLabel.setFont(Font.font("Arial", 12));

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefHeight(42);
        cancelBtn.setPrefWidth(110);
        cancelBtn.setStyle(outlineButtonStyle());
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(outlineButtonHoverStyle()));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(outlineButtonStyle()));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildEmployeeManagementTab()));

        Button saveBtn = new Button("Save Changes");
        saveBtn.setPrefHeight(42);
        saveBtn.setPrefWidth(140);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        saveBtn.setStyle(primaryButtonStyle());
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(primaryButtonHoverStyle()));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(primaryButtonStyle()));

        saveBtn.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            EmployeeRole role = roleCombo.getValue();
            boolean active = activeCheck.isSelected();

            if (fullName.isEmpty() || email.isEmpty() || role == null) {
                errorLabel.setText("Please fill in all required fields.");
                return;
            }

            emp.setFullName(fullName);
            emp.setEmail(email);
            emp.setRole(role);
            emp.setActive(active);

            OptionQueries queries = new OptionQueries();
            boolean success = queries.updateUser(emp);

            if (emp.getUsername().equals(Session.getUser())) {
                if (role != emp.getRole()) {
                    errorLabel.setText("You cannot change your own role.");
                    return;
                }
                if (active != emp.isActive()) {
                    errorLabel.setText("You cannot change your own status.");
                    return;
                }
            }

// Prevent non-super-admin from assigning super_admin role
            if (role == EmployeeRole.SUPER_ADMIN && !Session.isSuperAdmin()) {
                errorLabel.setText("Only a Super Admin can assign the Super Admin role.");
                return;
            }

            if (success) {
                // Check if the edited employee is the currently logged-in user
                if (emp.getUsername().equals(Session.getUser())) {
                    Session.setRole(role.name().toLowerCase());

                    showAlert("Success",
                            "Your role has been changed. You will be logged out.",
                            Alert.AlertType.INFORMATION);

                    dashboard.forceLogout(); // ← uses your existing stage
                    return;
                }

                showAlert("Success", "Employee has been updated successfully.",
                        Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to update employee in database.",
                        Alert.AlertType.ERROR);
            }

            innerContent.setCenter(buildEmployeeManagementTab());
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        formBox.getChildren().addAll(
                empIdLabel, empIdField,
                usernameLabel, usernameField,
                fullNameLabel, fullNameField,
                emailLabel, emailField,
                roleLabel, roleCombo, roleDesc,
                activeCheck,
                errorLabel
        );

        if (isSystemAdmin(emp) && Session.isAccountManager()) {
            roleCombo.setDisable(true);
            activeCheck.setDisable(true);

            Label restrictedNote = new Label("⚠ Role and status cannot be changed for Super Admin accounts.");
            restrictedNote.setFont(Font.font("Arial", 12));
            restrictedNote.setTextFill(Color.web("#E67E22"));
            restrictedNote.setWrapText(true);
            formBox.getChildren().add(restrictedNote);
        }

        card.getChildren().addAll(headerBox, topSep, formBox, buttonBox);
        content.getChildren().add(card);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + LIGHT_BLUE + "; -fx-background-color: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    // =============================================================
    // ADD EMPLOYEE DIALOG (ALLOWS MULTIPLE SUPER ADMINS)
    // =============================================================
    private void showAddEmployeeDialog() {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Add New Employee");
        dialog.setHeaderText("Create a new system employee");
        dialog.getDialogPane().setPrefWidth(480);

        ButtonType saveType = new ButtonType("Add Employee", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(25));

        // Employee ID - auto-generated, show but read-only
        OptionQueries queries = new OptionQueries();
        TextField empIdField = new TextField(queries.generateNextEmployeeId());
        empIdField.setEditable(false);
        empIdField.setPrefHeight(38);
        empIdField.setStyle(fieldStyle() + " -fx-background-color: #f5f5f5;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(38);
        usernameField.setStyle(fieldStyle());

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setPrefHeight(38);
        fullNameField.setStyle(fieldStyle());

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setPrefHeight(38);
        emailField.setStyle(fieldStyle());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(38);
        passwordField.setStyle(fieldStyle());

        ComboBox<EmployeeRole> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(
                EmployeeRole.ACCOUNT_MANAGER,
                EmployeeRole.OPERATIONS_OFFICER,
                EmployeeRole.BILLING_OFFICER,
                EmployeeRole.FLEET_MANAGER,
                EmployeeRole.CUSTOMER_OFFICER,
                EmployeeRole.AUDITOR
        );
        if (Session.isSuperAdmin()) {
            roleCombo.getItems().add(0, EmployeeRole.SUPER_ADMIN);
        }
        roleCombo.setValue(EmployeeRole.AUDITOR);
        roleCombo.setPrefHeight(38);
        roleCombo.setStyle(comboBoxStyle());

        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(true);

        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(empIdField, 1, 0);
        grid.add(new Label("Username:*"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Full Name:*"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Email:*"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Password:*"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Role:*"), 0, 5);
        grid.add(roleCombo, 1, 5);
        grid.add(activeCheck, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                return new Employee(
                        empIdField.getText(),
                        usernameField.getText().trim(),
                        fullNameField.getText().trim(),
                        emailField.getText().trim(),
                        passwordField.getText(),
                        roleCombo.getValue(),
                        activeCheck.isSelected()
                );
            }
            return null;
        });

        Button addButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please fill in all required fields.", Alert.AlertType.ERROR);
                event.consume();
                return;
            }
            if (password.length() < 6) {
                showAlert("Error", "Password must be at least 6 characters.", Alert.AlertType.ERROR);
                event.consume();
                return;
            }
            if (employeeList.stream().anyMatch(e -> e.getUsername().equalsIgnoreCase(username))) {
                showAlert("Error", "Username already exists.", Alert.AlertType.ERROR);
                event.consume();
            }
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(emp -> {

            boolean success = queries.insertUser(emp);

            if (success) {
                employeeList.add(emp);
                showAlert("Success", "Employee " + emp.getFullName() + " has been added.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to save employee to database.", Alert.AlertType.ERROR);
            }
            refreshEmployeeTab();
        });
    }

    private void resetPassword(Employee emp) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + emp.getUsername());
        dialog.setContentText("Enter new password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPass -> {
            if (newPass.length() < 6) {
                showAlert("Error", "Password must be at least 6 characters.", Alert.AlertType.ERROR);
                return;
            }

            emp.setPassword(newPass);

            OptionQueries queries = new OptionQueries();
            boolean success = queries.updateUserPassword(emp);

            if (success) {
                showAlert("Success", "Password has been reset.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to update password in database.", Alert.AlertType.ERROR);
            }
        });
    }

    private void confirmDelete(Employee emp) {
        // Any SUPER_ADMIN cannot be deleted
        if (isSystemAdmin(emp)) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Cannot Delete");
            warning.setHeaderText("Cannot Delete Super Admin");
            warning.setContentText("Super Administrator accounts cannot be deleted for security reasons.");
            warning.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Employee");
        confirm.setHeaderText("Delete " + emp.getUsername() + "?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            OptionQueries queries = new OptionQueries();
            boolean success = queries.deleteUser(emp); // add this
            if (success) {
                employeeList.remove(emp);
                showAlert("Success", "Employee has been deleted.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to delete employee from database.", Alert.AlertType.ERROR);
            }
            refreshEmployeeTab();
        }
    }

    private void refreshEmployeeTab() {
        innerContent.setCenter(buildEmployeeManagementTab());
    }

    private void updateRoleDescription(EmployeeRole role, Label descLabel) {
        if (role == null) {
            descLabel.setText("");
            return;
        }

        String desc = switch (role) {
            case SUPER_ADMIN ->
                "Full access to everything — all modules, system settings, and user account management.";
            case ACCOUNT_MANAGER ->
                "Handles everything related to system user accounts — creating new user profiles, assigning roles, resetting passwords, and deactivating accounts. Has NO access to operational data like shipments, billing, or fleet records. Scope is purely user management.";
            case OPERATIONS_OFFICER ->
                "Handles day-to-day shipping workflows. Full access to Booking & Shipment Management, Cargo Management, and Dashboard. Can view but not edit Customer and Vessel records. No access to Billing.";
            case BILLING_OFFICER ->
                "Focused on Billing and Documentation — creating invoices, logging payments, generating receipts, managing freight rates and surcharges.";
            case FLEET_MANAGER ->
                "Manages everything under Vessel and Fleet Management — vessel profiles, crew records, certifications, ports, and routes. Read-only access to bookings and shipments.";
            case CUSTOMER_OFFICER ->
                "Handles Customer Management — encoding, editing, and maintaining customer records. Can view bookings tied to customers.";
            case AUDITOR ->
                "View-only access across all modules. Useful for compliance officers, managers, or external auditors.";
        };
        descLabel.setText(desc);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // =============================================================
    // STYLES
    // =============================================================
    private String fieldStyle() {
        return "-fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 0 10 0 10;";
    }

    private String fieldStyleLarge() {
        return "-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 14 0 14; -fx-font-size: 13;";
    }

    private String comboBoxStyle() {
        return "-fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 5;";
    }

    private String comboBoxStyleLarge() {
        return "-fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 13;";
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    private String primaryButtonHoverStyle() {
        return "-fx-background-color: " + DARK_NAVY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    private String blueButtonStyle() {
        return "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0 15 0 15;";
    }

    private String blueButtonHoverStyle() {
        return "-fx-background-color: " + DARK_NAVY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0 15 0 15;";
    }

    private String outlineButtonStyle() {
        return "-fx-background-color: white; -fx-text-fill: " + NAVY_BLUE + "; -fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 12 5 12; -fx-font-size: 12;";
    }

    private String outlineButtonHoverStyle() {
        return "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; -fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 12 5 12; -fx-font-size: 12;";
    }

    private String dangerButtonStyle() {
        return "-fx-background-color: #DC3545; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-font-size: 11;";
    }

    private String dangerButtonHoverStyle() {
        return "-fx-background-color: #C82333; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-font-size: 11;";
    }

    private String warningButtonStyle() {
        return "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-font-size: 11;";
    }

    private String warningButtonHoverStyle() {
        return "-fx-background-color: #F57C00; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-font-size: 11;";
    }

    // =============================================================
    // EMPLOYEE MODEL
    // =============================================================
    public static class Employee {

        private final String employeeId;
        private String username;
        private String fullName;
        private String email;
        private String password;
        private EmployeeRole role;
        private boolean active;

        public Employee(String employeeId, String username, String fullName, String email, String password, EmployeeRole role, boolean active) {
            this.employeeId = employeeId;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.password = password;
            this.role = role;
            this.active = active;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public EmployeeRole getRole() {
            return role;
        }

        public void setRole(EmployeeRole role) {
            this.role = role;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    // =============================================================
    // EMPLOYEE ROLES
    // =============================================================
    public enum EmployeeRole {
        SUPER_ADMIN("Super Admin / System Administrator", "#C62828"),
        ACCOUNT_MANAGER("Account Manager / User Administrator", "#E67E22"),
        OPERATIONS_OFFICER("Operations Officer", "#1565C0"),
        BILLING_OFFICER("Billing / Finance Officer", "#2E7D32"),
        FLEET_MANAGER("Fleet / Crew Manager", "#6A1B9A"),
        CUSTOMER_OFFICER("Customer Relations Officer", "#00897B"),
        AUDITOR("Read-Only / Auditor", "#F57C00");

        private final String displayName;
        private final String color;

        EmployeeRole(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }
    }
}

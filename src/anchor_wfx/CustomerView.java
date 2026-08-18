package anchor_wfx;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
 * Manages the Customers section — list, add, edit, delete.
 */
public class CustomerView {

    private final BorderPane          root;
    private final ObservableList<Customer> customerList;

    public CustomerView(BorderPane root, ObservableList<Customer> customerList) {
        this.root         = root;
        this.customerList = customerList;
    }

    /** Entry point — called by Dashboard router. */
    public VBox build() {
        return buildListView();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LIST VIEW
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label pageTitle = new Label("Manage Customers");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add New Customer");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e  -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> root.setCenter(buildFormView(null)));

        header.getChildren().addAll(pageTitle, spacer, addBtn);

        // Search
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.setPrefWidth(300);
        searchField.setPrefHeight(36);

        ObservableList<Customer> filteredList = FXCollections.observableArrayList(customerList);
        searchField.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filteredList.setAll(customerList.filtered(c ->
                c.getFullName().toLowerCase().contains(q) ||
                c.getEmail().toLowerCase().contains(q)    ||
                c.getRole().toLowerCase().contains(q)
            ));
        });
        searchRow.getChildren().add(searchField);

        if (customerList.isEmpty()) {
            Label empty = new Label("There are no customer records yet.");
            empty.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            empty.setTextFill(Color.web("#888888"));
            empty.setPadding(new Insets(40));
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            content.getChildren().addAll(header, searchRow, empty);
        } else {
            TableView<Customer> table = buildTable(filteredList);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, searchRow, table);
        }

        return content;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TABLE
    // ══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private TableView<Customer> buildTable(ObservableList<Customer> data) {
        TableView<Customer> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(480);

        TableColumn<Customer, Integer> idCol    = new TableColumn<>("Customer ID");
        TableColumn<Customer, String>  nameCol  = new TableColumn<>("Full Name");
        TableColumn<Customer, String>  emailCol = new TableColumn<>("Email");
        TableColumn<Customer, String>  phoneCol = new TableColumn<>("Phone");
        TableColumn<Customer, String>  addrCol  = new TableColumn<>("Address");
        TableColumn<Customer, String>  roleCol  = new TableColumn<>("Role");
        TableColumn<Customer, Void>    actCol   = new TableColumn<>("Actions");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        idCol.setPrefWidth(100);
        actCol.setPrefWidth(110);

        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox   box       = new HBox(8, editBtn, deleteBtn);
            { box.setAlignment(Pos.CENTER); }

            {
                editBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    root.setCenter(buildFormView(c));
                });
                deleteBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Customer");
                    confirm.setHeaderText("Delete \"" + c.getFullName() + "\"?");
                    confirm.setContentText("This action cannot be undone.");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try{
                                Connection con = DBConnection.getConnection();
                                Statement st = con.createStatement();
                                String sql = "DELETE FROM customer WHERE customer_id =" + c.getId();

                                System.out.println(sql);
                                System.out.println(c.getId());
                                st.executeUpdate(sql);

                                //loadCustomerFromDB();
                                con.close();
                            }
                            catch(SQLException ex){
                                ex.printStackTrace();
                            }
                        root.setCenter(buildListView());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, addrCol, roleCol, actCol);
        return table;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADD / EDIT FORM
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildFormView(Customer existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(680);

        Label pageTitle = new Label(isEdit ? "Edit Customer Details" : "Add New Customer");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        TextField nameField  = AppStyles.formField("Full Name", isEdit ? existing.getFullName() : "");
        TextField emailField = AppStyles.formField("Email",     isEdit ? existing.getEmail()    : "");
        TextField phoneField = AppStyles.formField("Phone",     isEdit ? existing.getPhone()    : "");

        TextArea addressField = new TextArea(isEdit ? existing.getAddress() : "");
        addressField.setPromptText("Address");
        addressField.setPrefRowCount(3);
        addressField.setStyle(AppStyles.fieldStyle());

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Shipper", "Consignee", "Both");
        roleBox.setPromptText("Select role...");
        roleBox.setPrefWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(40);
        roleBox.setStyle(AppStyles.comboStyle());
        if (isEdit) roleBox.setValue(existing.getRole());

        Label errorLabel = AppStyles.errorLabel();

        // Buttons
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e  -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> root.setCenter(buildListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Customer");
        saveBtn.setPrefSize(140, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e  -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String name    = nameField.getText().trim();
            String email   = emailField.getText().trim();
            String phone   = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String role    = roleBox.getValue();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                address.isEmpty() || role == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }

            if (isEdit) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Save Changes");
                confirm.setHeaderText("Save changes for \"" + existing.getFullName() + "\"?");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    existing.setFullName(name);
                    existing.setEmail(email);
                    existing.setPhone(phone);
                    existing.setAddress(address);
                    existing.setRole(role);
                    root.setCenter(buildListView());
                }
            } else {
                customerList.add(new Customer(name, email, phone, address, role));
                root.setCenter(buildListView());
            }
        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
            AppStyles.formLabel("Full Name"),  nameField,
            AppStyles.formLabel("Email"),      emailField,
            AppStyles.formLabel("Phone"),      phoneField,
            AppStyles.formLabel("Address"),    addressField,
            AppStyles.formLabel("Role"),       roleBox,
            errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE +
                        "; -fx-background: " + AppStyles.LIGHT_BLUE + ";");
        return scroll;
    }
}
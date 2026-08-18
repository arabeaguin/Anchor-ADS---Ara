/*
 * Dashboard window for ANCHOR ADS - Cargo Handling System
 */
package anchor_wfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.io.File;
import java.util.Optional;
import javafx.stage.Stage;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author bleub
 */
public class Dashboard {

    private Stage primaryStage; //para sa logout sesh
    private BorderPane root;
    // private String currentView = "Dashboard";

    // Sidebar button
    private Button viewDashboardBtn;
    private Button manageCustomersBtn;
    private Button manageCargoBtn;
    private Button manageVesselsBtn;
    private Button bookingsBtn;
    private Button billingsBtn;
    private Button helpBtn;
    private Button settingsBtn;
    private Button infoBtn;

    OptionQueries query = new OptionQueries();
    // In-memory customer store (replace with DB calls later)
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private final ObservableList<Cargo> cargoList = FXCollections.observableArrayList();
    private final ObservableList<Vessel> vesselList = FXCollections.observableArrayList();
    private final ObservableList<Crew> crewList = FXCollections.observableArrayList();
    private final ObservableList<Port> portList = FXCollections.observableArrayList();
    private final ObservableList<Route> routeList = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final ObservableList<Shipment> shipmentList = FXCollections.observableArrayList();
    private final ObservableList<Container> containerList = FXCollections.observableArrayList();
    private final ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private final ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private final ObservableList<FreightRate> freightRateList = FXCollections.observableArrayList();
    private final ObservableList<Surcharge> surchargeList = FXCollections.observableArrayList();

    // Color scheme
    public static final String NAVY_BLUE = "#003B73";
    public static final String LIGHT_BLUE = "#C9D6EA";
    public static final String WHITE = "#FFFFFF";
    public static final String DARK_NAVY = "#002050";

    DashboardQueries dq = new DashboardQueries();

    // ──────────────────────────────────────────────────────────────────────
    public Scene createScene(Stage stage, int width, int height) {
        System.out.println("Create Scene started!");
        this.primaryStage = stage;
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        cargoList.addAll(query.getCargoListFromDatabase());
        System.out.println("cargolist loaded");

        crewList.addAll(query.getCrewListFromDatabase());
        System.out.println("crewlist loaded");

        vesselList.addAll(query.getVesselListFromDatabase());
        System.out.println("vessellist loaded");

        query.loadCrewAssignments(vesselList, crewList);

        portList.addAll(query.getPortListFromDatabase());
        System.out.println("portlist loadded");

        routeList.addAll(query.getRouteListFromDatabase());
        System.out.println("routelist loaded");

        containerList.addAll(query.getContainerListFromDatabase());
        System.out.println("containerList loaded");

        customerList.addAll(getCustomersFromDatabase());
        System.out.println("customerList loaded");

        bookingList.addAll(query.getBookingListFromDatabase());
        System.out.println("bookingList loaded");

        shipmentList.addAll(query.getShipmentListFromDatabase());
        System.out.println("shipmentList loaded");

        invoiceList.addAll(query.getInvoiceListFromDatabase());
        System.out.println("invoicelist loaded");

        paymentList.addAll(query.getPaymentListFromDatabase());
        System.out.println("paymentlist loaded");

        freightRateList.addAll(query.getFreightRateListFromDatabase());
        System.out.println("freightratelist loaded");

        surchargeList.addAll(query.getSurchargeListFromDatabase());
        System.out.println("surchargelist loaded");

        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        showDashboardView();

        return new Scene(root, width, height);
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPrefHeight(69);
        topBar.setPrefWidth(1280);
        topBar.setPadding(new Insets(10, 30, 10, 30));
        topBar.setSpacing(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: " + WHITE
                + "; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("Admin Navigation for Cargo Handling and Operations Records");
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        titleLabel.setTextFill(Color.web(NAVY_BLUE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminLabel = new Label(Session.getUser());
        adminLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        adminLabel.setTextFill(Color.web(NAVY_BLUE));

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setPrefSize(120, 40);
        styleOutlineBtn(logoutBtn, false);
        logoutBtn.setOnMouseEntered(e -> styleOutlineBtn(logoutBtn, true));
        logoutBtn.setOnMouseExited(e -> styleOutlineBtn(logoutBtn, false));
        logoutBtn.setOnAction(e -> handleLogout());

        topBar.getChildren().addAll(titleLabel, spacer, adminLabel, logoutBtn);
        return topBar;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────
    private BorderPane createSidebar() {

        BorderPane sidebar = new BorderPane();
        sidebar.setPrefWidth(267);
        sidebar.setStyle("-fx-background-color: " + WHITE + ";");

        // ── TOP: logo + admin label + menu buttons ─────────────────────
        VBox topSection = new VBox();
        topSection.setAlignment(Pos.TOP_CENTER);

        // Logo
        VBox logoContainer = new VBox();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPadding(new Insets(30, 0, 20, 0));
        ImageView logoView = loadLogo();
        if (logoView != null) {
            logoContainer.getChildren().add(logoView);
        } else {
            StackPane ph = new StackPane();
            ph.setPrefSize(92, 107);
            ph.setStyle("-fx-background-color: " + LIGHT_BLUE
                    + "; -fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2;");
            Label l = new Label("ANCHOR\nLOGO");
            l.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            l.setTextFill(Color.web(NAVY_BLUE));
            ph.getChildren().add(l);
            logoContainer.getChildren().add(ph);
        }

        Label adminActionsLabel = new Label("ADMIN ACTIONS");
        adminActionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        adminActionsLabel.setTextFill(Color.web(NAVY_BLUE));
        adminActionsLabel.setPadding(new Insets(10, 0, 15, 0));

        // Menu buttons
        viewDashboardBtn = createSidebarButton("View Dashboard");
        manageCustomersBtn = createSidebarButton("Manage Customers");
        manageCargoBtn = createSidebarButton("Manage Cargo");
        manageVesselsBtn = createSidebarButton("Manage Vessels & Fleets");
        bookingsBtn = createSidebarButton("Bookings & Shipments");
        billingsBtn = createSidebarButton("Billings & Documentation");

        viewDashboardBtn.setOnAction(e -> switchView("Dashboard"));
        manageCustomersBtn.setOnAction(e -> switchView("Customers"));
        manageCargoBtn.setOnAction(e -> switchView("Cargo"));
        manageVesselsBtn.setOnAction(e -> switchView("Vessels"));
        bookingsBtn.setOnAction(e -> switchView("Bookings"));
        billingsBtn.setOnAction(e -> switchView("Billings"));

        manageCustomersBtn.setVisible(Permission.canView("customers"));
        manageCustomersBtn.setManaged(Permission.canView("customers"));

        manageCargoBtn.setVisible(Permission.canView("cargo"));
        manageCargoBtn.setManaged(Permission.canView("cargo"));

        manageVesselsBtn.setVisible(Permission.canView("vessels"));
        manageVesselsBtn.setManaged(Permission.canView("vessels"));

        bookingsBtn.setVisible(Permission.canView("bookings"));
        bookingsBtn.setManaged(Permission.canView("bookings"));

        billingsBtn.setVisible(Permission.canView("billings"));
        billingsBtn.setManaged(Permission.canView("billings"));

        VBox menuButtons = new VBox(8);
        menuButtons.setPadding(new Insets(0, 15, 0, 15));
        menuButtons.setAlignment(Pos.TOP_CENTER);
        menuButtons.getChildren().addAll(
                viewDashboardBtn, manageCustomersBtn, manageCargoBtn,
                manageVesselsBtn, bookingsBtn, billingsBtn
        );

        topSection.getChildren().addAll(logoContainer, adminActionsLabel, menuButtons);

        // ── BOTTOM: help / settings / info icons — always pinned ───────
        helpBtn = createIconButton("?");
        settingsBtn = createIconButton("⚙");
        infoBtn = createIconButton("i");

        helpBtn.setOnAction(e -> switchView("Help"));
        settingsBtn.setOnAction(e -> switchView("Settings"));
        infoBtn.setOnAction(e -> switchView("Info"));

        HBox bottomIcons = new HBox(15, helpBtn, settingsBtn, infoBtn);
        bottomIcons.setAlignment(Pos.CENTER);
        bottomIcons.setPadding(new Insets(20, 0, 30, 0));

        // ── Assemble sidebar ────────────────────────────────────────────
        sidebar.setTop(topSection);
        sidebar.setBottom(bottomIcons);

        updateSidebarActive("Dashboard");
        return sidebar;
    }

    // ── Update sidebar active highlight ───────────────────────────────────
    private void updateSidebarActive(String viewName) {
        // Reset all main buttons
        setSidebarInactive(viewDashboardBtn);
        setSidebarInactive(manageCustomersBtn);
        setSidebarInactive(manageCargoBtn);
        setSidebarInactive(manageVesselsBtn);
        setSidebarInactive(bookingsBtn);
        setSidebarInactive(billingsBtn);

        // Reset icon buttons
        setIconInactive(helpBtn);
        setIconInactive(settingsBtn);
        setIconInactive(infoBtn);

        // Activate the selected one
        switch (viewName) {
            case "Dashboard" ->
                setSidebarActive(viewDashboardBtn);
            case "Customers" ->
                setSidebarActive(manageCustomersBtn);
            case "Cargo" ->
                setSidebarActive(manageCargoBtn);
            case "Vessels" ->
                setSidebarActive(manageVesselsBtn);
            case "Bookings" ->
                setSidebarActive(bookingsBtn);
            case "Billings" ->
                setSidebarActive(billingsBtn);
            case "Help" ->
                setIconActive(helpBtn);
            case "Settings" ->
                setIconActive(settingsBtn);
            case "Info" ->
                setIconActive(infoBtn);
        }
    }

    private void setSidebarActive(Button btn) {
        btn.setStyle(
                "-fx-background-color: " + DARK_NAVY + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"
        );
        // Remove hover handlers while active
        btn.setOnMouseEntered(null);
        btn.setOnMouseExited(null);
    }

    private void setSidebarInactive(Button btn) {
        btn.setStyle(
                "-fx-background-color: " + WHITE + "; -fx-text-fill: " + NAVY_BLUE + "; "
                + "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + LIGHT_BLUE + "; -fx-text-fill: " + NAVY_BLUE + "; "
                + "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + WHITE + "; -fx-text-fill: " + NAVY_BLUE + "; "
                + "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"
        ));
    }

    private void setIconActive(Button btn) {
        btn.setStyle(
                "-fx-background-color: " + DARK_NAVY + "; -fx-text-fill: white; "
                + "-fx-border-color: " + DARK_NAVY + "; -fx-border-width: 2; "
                + "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btn.setOnMouseEntered(null);
        btn.setOnMouseExited(null);
    }

    private void setIconInactive(Button btn) {
        btn.setStyle(
                "-fx-background-color: " + WHITE + "; -fx-text-fill: " + NAVY_BLUE + "; "
                + "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; "
                + "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + WHITE + "; -fx-text-fill: " + NAVY_BLUE + "; "
                + "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;"
        ));
    }

    // ── View router ───────────────────────────────────────────────────────
    private void switchView(String viewName) {
        System.out.println("updated switch");
        updateSidebarActive(viewName);
        switch (viewName) {
            case "Dashboard" ->
                showDashboardView();
            case "Customers" ->
                root.setCenter(buildCustomerListView());
            case "Cargo" ->
                root.setCenter(new CargoView(root, cargoList).build());
            case "Vessels" ->
                root.setCenter(
                        new VesselFleetView(root, vesselList, crewList, portList, routeList, containerList).build());
            case "Bookings" ->
                root.setCenter(
                        new BookingsShipmentsView(
                                root, bookingList, shipmentList,
                                vesselList, routeList,
                                customerList, cargoList, containerList 
                        ).build());
            case "Billings" ->
                root.setCenter(new BillingsView(
                        root, invoiceList, paymentList, freightRateList, surchargeList,
                        shipmentList, customerList, vesselList, bookingList, cargoList, routeList
                ).build());
            case "Help" ->
                root.setCenter(new HelpView().build());     
            case "Settings" -> {    
                if (Permission.canView("settings")) {
                    root.setCenter(new SettingsView(this).build());
                } else {
                    root.setCenter(createPlaceholderView(
                            "Access Denied",
                            "You do not have permission to access Settings."
                    ));
                }
            }
            case "Info" ->
                root.setCenter(new AboutView().build());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DASHBOARD VIEW
    // ══════════════════════════════════════════════════════════════════════
    private void showDashboardView() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + LIGHT_BLUE + "; -fx-background: " + LIGHT_BLUE + ";");

        VBox content = new VBox(24);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(NAVY_BLUE));

        // ── Row 1: key metrics ─────────────────────────────────────────────
        HBox row1 = new HBox(20);
        row1.getChildren().addAll(
                createStatCard("Active Shipments", String.valueOf(dq.getActiveShipments()), "#2196F3"),
                createStatCard("Pending Invoices", String.valueOf(dq.getPendingInvoices()), "#FF9800"),
                createStatCard("Total Customers", String.valueOf(dq.getTotalCustomers()), "#4CAF50"),
                createStatCard("Delivered This Month", String.valueOf(dq.getDeliveredThisMonth()), "#9C27B0")
        );

        // ── Row 2: vessel status ───────────────────────────────────────────
        Label vesselTitle = new Label("Vessel Status");
        vesselTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        vesselTitle.setTextFill(Color.web(NAVY_BLUE));

        HBox row2 = new HBox(20);
        row2.getChildren().addAll(
                createStatCard("Active Vessels", String.valueOf(dq.getVesselStatus()[0]), "#4CAF50"),
                createStatCard("Docked", String.valueOf(dq.getVesselStatus()[1]), "#2196F3"),
                createStatCard("Under Maintenance", String.valueOf(dq.getVesselStatus()[2]), "#F44336")
        );

        // ── Recent Bookings ────────────────────────────────────────────────
        Label bookingsTitle = new Label("Recent Bookings");
        bookingsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        bookingsTitle.setTextFill(Color.web(NAVY_BLUE));

        TableView<String[]> bookingsTable = createRecentBookingsTable();

        content.getChildren().addAll(title, row1, vesselTitle, row2, bookingsTitle, bookingsTable);
        scroll.setContent(content);
        root.setCenter(scroll);
    }

    private TableView<String[]> createRecentBookingsTable() {
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setMaxHeight(220);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> idCol = new TableColumn<>("Booking ID");
        TableColumn<String[], String> shipperCol = new TableColumn<>("Shipper");
        TableColumn<String[], String> arrowCol = new TableColumn<>("");
        TableColumn<String[], String> consigneeCol = new TableColumn<>("Consignee");
        TableColumn<String[], String> statusCol = new TableColumn<>("Status");

        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        shipperCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        arrowCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("→"));
        consigneeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));

        idCol.setPrefWidth(120);
        arrowCol.setPrefWidth(40);

        // Mock data
        javafx.collections.ObservableList<String[]> data
                = javafx.collections.FXCollections.observableArrayList();

        for (String[] row : dq.getLatestBookingsAsArray()) {
            data.add(row);
        }

        if (data.isEmpty()) {
            table.setPlaceholder(new Label("No bookings found."));
        }

        table.setItems(data);
        table.getColumns().addAll(idCol, shipperCol, arrowCol, consigneeCol, statusCol);
        return table;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MANAGE CUSTOMERS VIEW
    // ══════════════════════════════════════════════════════════════════════
    private void showCustomersView() {
        root.setCenter(buildCustomerListView());
    }

    // ── Customer List (default view) ───────────────────────────────────────
    private VBox buildCustomerListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        // Header row
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label pageTitle = new Label("Manage Customers");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(NAVY_BLUE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add New Customer");
        addBtn.setVisible(Permission.canAdd("customers"));
        addBtn.setManaged(Permission.canAdd("customers"));

        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        addBtn.setStyle(
                "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 16 0 16;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: " + DARK_NAVY + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 16 0 16;"
        ));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 16 0 16;"
        ));
        addBtn.setOnAction(e -> root.setCenter(buildCustomerFormView(null)));

        header.getChildren().addAll(pageTitle, spacer, addBtn);

        // Search bar
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.setPrefWidth(300);
        searchField.setPrefHeight(36);

        ObservableList<Customer> customerList = getCustomersFromDatabase();
        TableView<Customer> table = buildCustomerTable(customerList);

        // Search
        searchField.textProperty().addListener((obs, o, n) -> {
            if (n.isEmpty()) {
                table.setItems(customerList);
            } else {
                table.setItems(searchCustomersFromDatabase(n));
            }
        });

        searchRow.getChildren().add(searchField);

        // Table or empty state
        if (customerList.isEmpty()) {
            Label emptyLabel = new Label("There are no customer records yet.");
            emptyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            emptyLabel.setTextFill(Color.web("#888888"));
            emptyLabel.setPadding(new Insets(40));
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            emptyLabel.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 10;"
            );

            content.getChildren().addAll(header, searchRow, emptyLabel);
        } else {

            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, searchRow, table);
        }

        return content;
    }

    private ObservableList<Customer> searchCustomersFromDatabase(String keyword) {
        ObservableList<Customer> list = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();

            String sql = "SELECT * FROM customer WHERE "
                    + "name LIKE '%" + keyword + "%' OR "
                    + "email LIKE '%" + keyword + "%' OR "
                    + "role LIKE '%" + keyword + "%'";

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("customer_id"); // 👈 match your column
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String role = rs.getString("role");

                list.add(new Customer(id, name, email, phone, address, role));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    // ── Customer Table ─────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Customer> buildCustomerTable(ObservableList<Customer> data) {
        TableView<Customer> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(480);

        TableColumn<Customer, Integer> idCol = new TableColumn<>("Customer ID");
        TableColumn<Customer, String> nameCol = new TableColumn<>("Full Name");
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        TableColumn<Customer, String> addrCol = new TableColumn<>("Address");
        TableColumn<Customer, String> roleCol = new TableColumn<>("Role");
        TableColumn<Customer, Void> actCol = new TableColumn<>("Actions");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        idCol.setPrefWidth(100);
        actCol.setPrefWidth(110);

        // Actions column: edit + delete icons
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏");

            private final Button deleteBtn = new Button("🗑");

            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setVisible(Permission.canEdit("customers"));
                editBtn.setManaged(Permission.canEdit("customers"));

                deleteBtn.setVisible(Permission.canDelete("customers"));
                deleteBtn.setManaged(Permission.canDelete("customers"));

                box.setAlignment(Pos.CENTER);
                editBtn.setStyle(
                        "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; "
                        + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
                );
                deleteBtn.setStyle(
                        "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; "
                        + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
                );
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                        "-fx-background-color: #1565C0; -fx-text-fill: white; "
                        + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
                ));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(
                        "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; "
                        + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
                ));
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                        "-fx-background-color: #C62828; -fx-text-fill: white; "
                        + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
                ));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                        "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; "
                        + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
                ));

                editBtn.setOnAction(e -> {
                    Customer selected = getTableView().getItems().get(getIndex());

                    if (selected != null) {
                        root.setCenter(buildCustomerFormView(selected));
                    } else {
                        System.out.println("No customer selected.");
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Customer selected = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Customer");
                    confirm.setHeaderText("Delete \"" + selected.getFullName() + "\"?");
                    confirm.setContentText("This action cannot be undone.");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            Connection con = DBConnection.getConnection();
                            Statement st = con.createStatement();
                            String sql = "DELETE FROM customer WHERE customer_id =" + selected.getId();

                            st.executeUpdate(sql);

                            con.close();
                            root.setCenter(buildCustomerListView());
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
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

    // ── Add / Edit Customer Form ───────────────────────────────────────────
    private ScrollPane buildCustomerFormView(Customer existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");
        content.setMaxWidth(680);

        Label pageTitle = new Label(isEdit ? "Edit Customer Details" : "Add New Customer");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(NAVY_BLUE));

        // Form card
        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 3);"
        );

        // Fields
        TextField nameField = formField("Full Name", isEdit ? existing.getFullName() : "");
        TextField emailField = formField("Email", isEdit ? existing.getEmail() : "");
        TextField phoneField = formField("Phone", isEdit ? existing.getPhone() : "");
        TextArea addressField = new TextArea(isEdit ? existing.getAddress() : "");
        addressField.setPromptText("Address");
        addressField.setPrefRowCount(3);
        addressField.setStyle(fieldStyle());

        Label addrLabel = formLabel("Address");

        // Role combo box
        Label roleLabel = formLabel("Role");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Shipper", "Consignee", "Both");
        roleBox.setPromptText("Select role...");
        roleBox.setPrefWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(40);
        if (isEdit) {
            roleBox.setValue(existing.getRole());
        }
        roleBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #b0b8c9; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;"
        );

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        errorLabel.setTextFill(Color.web("#CC0000"));
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Buttons
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> root.setCenter(buildCustomerListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Customer");
        saveBtn.setPrefSize(140, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        saveBtn.setStyle(
                "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
                "-fx-background-color: " + DARK_NAVY + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand;"
        ));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
                "-fx-background-color: " + NAVY_BLUE + "; -fx-text-fill: white; "
                + "-fx-background-radius: 8; -fx-cursor: hand;"
        ));

        saveBtn.setOnAction(e -> {
            // Validation
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String role = roleBox.getValue();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                    || address.isEmpty() || role == null) {
                errorLabel.setText("Please fill in all fields.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            try {
                Connection con = DBConnection.getConnection();
                Statement st = con.createStatement();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for \"" + existing.getFullName() + "\"?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        String sql = "UPDATE customer SET "
                                + "name='" + name + "', "
                                + "email='" + email + "', "
                                + "phone='" + phone + "', "
                                + "address='" + address + "', "
                                + "role='" + role + "' "
                                + "WHERE customer_id=" + existing.getId();
                        st.executeUpdate(sql);
                        root.setCenter(buildCustomerListView());
                    }
                } else {
                    String sql = "INSERT INTO customer(name, email, phone, address, role)"
                            + "VALUES ('" + name + "', '" + email + "', '" + phone + "', '" + address + "', '" + role + "')";

                    st.executeUpdate(sql);

                    root.setCenter(buildCustomerListView());
                }

                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnRow.getChildren().addAll(cancelBtn, saveBtn);

        card.getChildren().addAll(
                formLabel("Full Name"), nameField,
                formLabel("Email"), emailField,
                formLabel("Phone"), phoneField,
                addrLabel, addressField,
                roleLabel, roleBox,
                errorLabel,
                btnRow
        );

        content.getChildren().addAll(pageTitle, card);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + LIGHT_BLUE + "; -fx-background: " + LIGHT_BLUE + ";");
        return scroll;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELP / SETTINGS / INFO MODULE VIEWS
    // ══════════════════════════════════════════════════════════════════════
    private VBox createHelpView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        Label title = sectionTitle("Help & Support");

        VBox card = infoCard();
        card.getChildren().addAll(
                infoRow("📧 Email Support", "support@anchorads.com"),
                new Separator(),
                infoRow("📞 Hotline", "+63 2 8888-0000"),
                new Separator(),
                infoRow("🕐 Office Hours", "Monday – Friday, 8:00 AM – 5:00 PM"),
                new Separator(),
                infoRow("📖 User Manual", "Available in the Resources folder of the system"),
                new Separator(),
                infoRow("🐛 Report a Bug", "Use the feedback form or email the IT department")
        );

        content.getChildren().addAll(title, card);
        return content;
    }

    private ObservableList<Customer> getCustomersFromDatabase() {

        ObservableList<Customer> list = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql = "SELECT * FROM customer";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Customer c = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("role")
                );

                list.add(c);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    private VBox createSettingsView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        Label title = sectionTitle("System Settings");

        VBox card = infoCard();

        // Theme placeholder toggle
        Label themeLabel = new Label("Dark Mode");
        themeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        themeLabel.setTextFill(Color.web(NAVY_BLUE));
        CheckBox themeToggle = new CheckBox();
        HBox themeRow = new HBox(16, themeLabel, themeToggle);
        themeRow.setAlignment(Pos.CENTER_LEFT);

        // Language placeholder
        Label langLabel = new Label("Language");
        langLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        langLabel.setTextFill(Color.web(NAVY_BLUE));
        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll("English (Default)", "Filipino");
        langBox.setValue("English (Default)");
        HBox langRow = new HBox(16, langLabel, langBox);
        langRow.setAlignment(Pos.CENTER_LEFT);

        Label note = new Label("Note: Full settings configuration will be available in a future update.");
        note.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        note.setTextFill(Color.web("#888888"));
        note.setWrapText(true);

        card.getChildren().addAll(themeRow, new Separator(), langRow, new Separator(), note);

        content.getChildren().addAll(title, card);
        return content;
    }

    private VBox createInfoView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        Label title = sectionTitle("About This System");

        VBox card = infoCard();
        card.getChildren().addAll(
                infoRow("🚢 System Name", "ANCHOR ADS – Cargo Handling & Operations Records"),
                new Separator(),
                infoRow("📦 Version", "1.0.0"),
                new Separator(),
                infoRow("🏢 Developed for", "ANCHOR ADS Corporation"),
                new Separator(),
                infoRow("👨‍💻 Developer", "ANCHOR ADS Development Team"),
                new Separator(),
                infoRow("©  Copyright", "2024 ANCHOR ADS. All rights reserved.")
        );

        content.getChildren().addAll(title, card);
        return content;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════
    private VBox createStatCard(String label, String value, String accentColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(210, 110);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 2);"
                + "-fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 5; -fx-border-radius: 10 0 0 10;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(accentColor));

        Label titleLabel = new Label(label);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setTextFill(Color.web("#555555"));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private VBox createPlaceholderView(String title, String desc) {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");
        Label t = new Label(title);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        t.setTextFill(Color.web(NAVY_BLUE));
        Label d = new Label(desc);
        d.setFont(Font.font("Arial", 15));
        d.setTextFill(Color.web("#555555"));
        content.getChildren().addAll(t, d);
        return content;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        l.setTextFill(Color.web(NAVY_BLUE));
        return l;
    }

    private VBox infoCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setMaxWidth(640);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 2);"
        );
        return card;
    }

    private HBox infoRow(String key, String value) {
        Label k = new Label(key);
        k.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        k.setTextFill(Color.web(NAVY_BLUE));
        k.setMinWidth(200);

        Label v = new Label(value);
        v.setFont(Font.font("Arial", 14));
        v.setTextFill(Color.web("#333333"));
        v.setWrapText(true);

        HBox row = new HBox(16, k, v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private TextField formField(String prompt, String value) {
        TextField f = new TextField(value);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setStyle(fieldStyle());
        return f;
    }

    private Label formLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        l.setTextFill(Color.web("#555555"));
        return l;
    }

    private String fieldStyle() {
        return "-fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-border-color: #b0b8c9; -fx-border-width: 1.5; "
                + "-fx-background-color: white; -fx-font-size: 14; -fx-padding: 8 12 8 12;";
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(237);
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        return btn;
    }

    private Button createIconButton(String icon) {
        Button btn = new Button(icon);
        btn.setPrefSize(40, 40);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        return btn;
    }

    private void styleOutlineBtn(Button btn, boolean hovered) {
        btn.setStyle(
                "-fx-background-color: " + (hovered ? NAVY_BLUE : WHITE) + "; "
                + "-fx-text-fill: " + (hovered ? "white" : NAVY_BLUE) + "; "
                + "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 5; -fx-background-radius: 5; "
                + "-fx-font-size: 13; -fx-cursor: hand;"
        );
    }

    private ImageView loadLogo() {
        String[] paths = {
            "src/anchor_wfx/Images/anchor_logo.png",
            "src/Images/anchor_logo.png", "Images/anchor_logo.png",
            "src/anchor_ads/Images/anchor_logo.png", "anchor_logo.png"
        };
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                try {
                    Image img = new Image(f.toURI().toString());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(92);
                    iv.setFitHeight(107);
                    iv.setPreserveRatio(true);
                    return iv;
                } catch (Exception e) {
                    System.err.println("Error loading logo: " + e.getMessage());
                }
            }
        }
        return null;
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Session.setUser("");
                LoginScene loginScene = new LoginScene();
                primaryStage.setScene(loginScene.createScene(primaryStage));
            }
        });
    }
    
    public void forceLogout() {
    Session.setUser("");
    Session.setRole("");
    LoginScene loginScene = new LoginScene();
    primaryStage.setScene(loginScene.createScene(primaryStage));
}
}

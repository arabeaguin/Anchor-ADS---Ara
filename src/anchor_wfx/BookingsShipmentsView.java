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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.util.List;

//
// * Manages Bookings and Shipments.
// *
// * Integrate with Dashboard.java — add to your existing lists:
// *
// *   private final ObservableList<Booking>   bookingList   = FXCollections.observableArrayList();
// *   private final ObservableList<Shipment>  shipmentList  = FXCollections.observableArrayList();
// *
// * Then pass all required lists when constructing:
// *
// *   new BookingsShipmentsView(root, bookingList, shipmentList,
// *                             vesselList, routeList,
// *                             customerList, cargoList, containerList)
// *
// * NOTE: customerList, cargoList, containerList should be ObservableList of your
// *       Customer, Cargo, and Container model classes respectively.
// *       Each must expose: getId(), getName() (or getDescription() for Cargo).
// *       Replace the lookup helper stub methods at the bottom if needed.
public class BookingsShipmentsView {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final BorderPane root;
    private final BorderPane innerContent;

    private final ObservableList<Booking> bookingList;
    private final ObservableList<Shipment> shipmentList;
    private final ObservableList<Vessel> vesselList;
    private final ObservableList<Route> routeList;

    private final ObservableList<Customer> customerList;
    private final ObservableList<Cargo> cargoList;
    private final ObservableList<Container> containerList;

    private final Button[] tabButtons = new Button[2];
    private static final String[] TAB_LABELS = {
        "📋  Bookings", "🚢  Shipments"
    };

    public BookingsShipmentsView(BorderPane root,
            ObservableList<Booking> bookingList,
            ObservableList<Shipment> shipmentList,
            ObservableList<Vessel> vesselList,
            ObservableList<Route> routeList,
            ObservableList<Customer> customerList,
            ObservableList<Cargo> cargoList,
            ObservableList<Container> containerList) {
        this.root = root;
        this.innerContent = new BorderPane();
        this.bookingList = bookingList;
        this.shipmentList = shipmentList;
        this.vesselList = vesselList;
        this.routeList = routeList;
        this.customerList = customerList;
        this.cargoList = cargoList;
        this.containerList = containerList;
    }

    // Entry point — called by Dashboard router. 
    public VBox build() {
        VBox shell = new VBox(0);
        shell.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        shell.setMaxHeight(Double.MAX_VALUE);
        shell.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(innerContent, Priority.ALWAYS);
        shell.getChildren().addAll(buildTabBar(), innerContent);

        navigateTo(0); // default: Bookings
        return shell;
    }

    // ── Tab bar ────────────────────────────────────────────────────────────
    private HBox buildTabBar() {
        HBox bar = new HBox(0);
        bar.setStyle(
                "-fx-background-color: white; "
                + "-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;"
        );
        for (int i = 0; i < TAB_LABELS.length; i++) {
            Button btn = new Button(TAB_LABELS[i]);
            btn.setPrefHeight(46);
            btn.setPrefWidth(220);
            btn.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            final int idx = i;
            btn.setOnAction(e -> navigateTo(idx));
            tabButtons[i] = btn;
            bar.getChildren().add(btn);
        }
        return bar;
    }

    private void navigateTo(int tabIndex) {
        updateTabStyles(tabIndex);
        switch (tabIndex) {
            case 0 -> {
                bookingList.setAll(new OptionQueries().getBookingListFromDatabase());
                innerContent.setCenter(buildBookingListView());
            }
            case 1 -> {
                shipmentList.setAll(new OptionQueries().getShipmentListFromDatabase()); // ← add
                innerContent.setCenter(buildShipmentListView());
            }
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

    // ══════════════════════════════════════════════════════════════════════
    //  📋  BOOKINGS — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildBookingListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        // ── Header ──────────────────────────────────────────────────────────
        Label title = AppStyles.sectionTitle("Bookings");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newBtn = new Button("➕  New Booking");
        newBtn.setVisible(Permission.canAdd("bookings"));
        newBtn.setManaged(Permission.canAdd("bookings"));
        newBtn.setPrefHeight(40);
        newBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(newBtn, false);
        newBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(newBtn, true));
        newBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(newBtn, false));
        newBtn.setOnAction(e -> innerContent.setCenter(buildBookingFormView(null)));

        HBox header = new HBox(12, title, spacer, newBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Search ───────────────────────────────────────────────────────────
        TextField search = searchField("Search by Booking ID, Shipper ID, Cargo ID, or Status...");
        ObservableList<Booking> filtered = FXCollections.observableArrayList(bookingList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(bookingList.filtered(b
                    -> String.valueOf(b.getId()).contains(q)
                    || String.valueOf(b.getShipperId()).contains(q)
                    || String.valueOf(b.getConsigneeId()).contains(q)
                    || String.valueOf(b.getCargoId()).contains(q)
                    || b.getStatusDisplay().toLowerCase().contains(q)
                    || b.getNotes().toLowerCase().contains(q)));
        });

        if (bookingList.isEmpty()) {
            content.getChildren().addAll(header, search, emptyLabel("No bookings created yet."));
        } else {
            TableView<Booking> table = buildBookingTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, search, table);
        }
        return content;
    }

    // ── Booking table ───────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Booking> buildBookingTable(ObservableList<Booking> data) {
        TableView<Booking> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Booking, Integer> idCol = col("Booking ID", "id");
        TableColumn<Booking, Integer> shipperCol = col("Shipper ID", "shipperId");
        TableColumn<Booking, Integer> consigneeCol = col("Consignee ID", "consigneeId");
        TableColumn<Booking, Integer> cargoCol = col("Cargo ID", "cargoId");
        TableColumn<Booking, String> notesCol = col("Notes", "notes");
        idCol.setPrefWidth(95);
        shipperCol.setPrefWidth(100);
        consigneeCol.setPrefWidth(115);
        cargoCol.setPrefWidth(90);
        notesCol.setPrefWidth(200);

        // Date column with formatted display
        TableColumn<Booking, LocalDate> dateCol = new TableColumn<>("Date Created");
        dateCol.setPrefWidth(130);
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        dateCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.format(DATE_FMT));
            }
        });

        // Status — coloured badge
        TableColumn<Booking, Void> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Booking b = getTableRow().getItem();
                Label badge = new Label(b.getStatusDisplay());
                badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle(
                        "-fx-background-radius: 12; -fx-text-fill: white; "
                        + "-fx-background-color: " + bookingStatusColor(b.getStatus()) + ";"
                );
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Actions: Edit | 🔄 Status | Delete
        TableColumn<Booking, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(195);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button statusBtn = statusButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(6, editBtn, statusBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("bookings"));
                editBtn.setManaged(Permission.canEdit("bookings"));
                deleteBtn.setVisible(Permission.canDelete("bookings"));
                deleteBtn.setManaged(Permission.canDelete("bookings"));
                statusBtn.setVisible(Permission.canEdit("bookings"));
                statusBtn.setManaged(Permission.canEdit("bookings"));
            }

            {

                editBtn.setOnAction(e -> {
                    Booking b = getTableRow().getItem();
                    if (b == null) {
                        return;
                    }
                    if (b.getStatus() == Booking.Status.CONVERTED
                            || b.getStatus() == Booking.Status.VOIDED
                            || b.getStatus() == Booking.Status.CANCELLED) {
                        showWarn("Cannot Edit Booking",
                                "Booking #" + b.getId() + " is " + b.getStatusDisplay() + ".",
                                "Only Pending or Confirmed bookings can be edited.");
                        return;
                    }
                    innerContent.setCenter(buildBookingFormView(b));
                });

                statusBtn.setOnAction(e -> {
                    Booking b = getTableRow().getItem();
                    if (b != null) {
                        showBookingStatusDialog(b, table);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Booking b = getTableRow().getItem();
                    if (b == null) {
                        return;
                    }

                    boolean linked = shipmentList.stream()
                            .anyMatch(s -> s.getBookingId() == b.getId());
                    if (linked) {
                        showWarn("Cannot Delete Booking",
                                "Booking #" + b.getId() + " has a linked shipment.",
                                "Delete the shipment first before removing this booking.");
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Booking");
                    confirm.setHeaderText("Delete Booking #" + b.getId() + "?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement(
                                        "DELETE FROM booking WHERE booking_id = ?"
                                );
                                ps.setInt(1, b.getId());
                                ps.executeUpdate();
                                con.close();

                                bookingList.setAll(new OptionQueries().getBookingListFromDatabase()); // ← refresh
                                innerContent.setCenter(buildBookingListView());
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                // Dim edit button for terminal statuses
                Booking b = getTableRow().getItem();
                boolean locked = b.getStatus() == Booking.Status.CONVERTED
                        || b.getStatus() == Booking.Status.VOIDED
                        || b.getStatus() == Booking.Status.CANCELLED;
                editBtn.setDisable(locked);
                editBtn.setOpacity(locked ? 0.4 : 1.0);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(
                idCol, dateCol, shipperCol, consigneeCol, cargoCol,
                statusCol, notesCol, actCol
        );
        return table;
    }

    // ── Booking status update dialog ────────────────────────────────────────
    private void showBookingStatusDialog(Booking booking, TableView<Booking> table) {
        Booking.Status current = booking.getStatus();

        // Terminal statuses — nothing to update
        if (current == Booking.Status.CONVERTED
                || current == Booking.Status.VOIDED
                || current == Booking.Status.CANCELLED) {
            showWarn("Status Locked",
                    "Booking #" + booking.getId() + " is already " + booking.getStatusDisplay() + ".",
                    "No further status changes are allowed.");
            return;
        }

        // Build valid transitions
        ObservableList<String> options = FXCollections.observableArrayList();
        if (current == Booking.Status.PENDING) {
            options.addAll("Confirmed", "Voided", "Cancelled");
        } else if (current == Booking.Status.CONFIRMED) {
            options.addAll("Convert to Shipment", "Voided", "Cancelled");
        }

        ChoiceDialog<String> dlg = new ChoiceDialog<>(options.get(0), options);
        dlg.setTitle("Update Booking Status");
        dlg.setHeaderText("Booking #" + booking.getId());
        dlg.setContentText("Select new status:");
        dlg.showAndWait().ifPresent(choice -> {
            Booking.Status newStatus = switch (choice) {
                case "Confirmed" ->
                    Booking.Status.CONFIRMED;
                case "Voided" ->
                    Booking.Status.VOIDED;
                case "Cancelled" ->
                    Booking.Status.CANCELLED;
                default ->
                    null;
            };

            if (newStatus != null) {
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                            "UPDATE booking SET status = ? WHERE booking_id = ?"
                    );
                    ps.setString(1, newStatus.name().toLowerCase());
                    ps.setInt(2, booking.getId());
                    ps.executeUpdate();
                    con.close();

                    booking.setStatus(newStatus);
                    bookingList.setAll(new OptionQueries().getBookingListFromDatabase());
                    table.refresh();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else if (choice.equals("Convert to Shipment")) {
                innerContent.setCenter(buildConvertToShipmentForm(booking));
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  📋  BOOKINGS — create / edit form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildBookingFormView(Booking existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(720);

        Label pageTitle = new Label(isEdit ? "Edit Booking" : "New Booking");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        ObservableList<Customer> shipperOptions = customerList.filtered(c
                -> c.getRole().equalsIgnoreCase("shipper")
                || c.getRole().equalsIgnoreCase("both")
        );
        ComboBox<Customer> shipperBox = new ComboBox<>(shipperOptions);
        shipperBox.setPromptText("Select shipper...");
        shipperBox.setPrefWidth(Double.MAX_VALUE);
        shipperBox.setPrefHeight(40);
        shipperBox.setStyle(AppStyles.comboStyle());
        shipperBox.setCellFactory(lv -> customerCell());
        shipperBox.setButtonCell(customerCell());
        if (isEdit) {
            shipperBox.setValue(customerById(existing.getShipperId()));
        }

        ObservableList<Customer> consigneeOptions = customerList.filtered(c
                -> c.getRole().equalsIgnoreCase("consignee")
                || c.getRole().equalsIgnoreCase("both")
        );
        ComboBox<Customer> consigneeBox = new ComboBox<>(consigneeOptions);
        consigneeBox.setPromptText("Select consignee...");
        consigneeBox.setPrefWidth(Double.MAX_VALUE);
        consigneeBox.setPrefHeight(40);
        consigneeBox.setStyle(AppStyles.comboStyle());
        consigneeBox.setCellFactory(lv -> customerCell());
        consigneeBox.setButtonCell(customerCell());
        if (isEdit) {
            consigneeBox.setValue(customerById(existing.getConsigneeId()));
        }

        ComboBox<Cargo> cargoBox = new ComboBox<>(cargoList);
        cargoBox.setPromptText("Select cargo...");
        cargoBox.setPrefWidth(Double.MAX_VALUE);
        cargoBox.setPrefHeight(40);
        cargoBox.setStyle(AppStyles.comboStyle());
        cargoBox.setCellFactory(lv -> cargoCell());
        cargoBox.setButtonCell(cargoCell());
        if (isEdit) {
            cargoBox.setValue(cargoById(existing.getCargoId()));
        }

        DatePicker bookingDatePicker = new DatePicker(
                isEdit ? existing.getCreatedDate() : LocalDate.now()
        );
        bookingDatePicker.setPrefWidth(Double.MAX_VALUE);
        bookingDatePicker.setPrefHeight(40);

        TextArea notesArea = new TextArea(isEdit ? existing.getNotes() : "");
        notesArea.setPromptText("Optional notes...");
        notesArea.setPrefHeight(90);
        notesArea.setWrapText(true);
        notesArea.setStyle(AppStyles.fieldStyle());

        ComboBox<String> statusBox = new ComboBox<>();
        if (isEdit) {
            statusBox.getItems().addAll("Pending", "Confirmed");
        } else {
            statusBox.getItems().add("Pending");
        }
        statusBox.setPrefWidth(Double.MAX_VALUE);
        statusBox.setPrefHeight(40);
        statusBox.setStyle(AppStyles.comboStyle());
        statusBox.setValue(isEdit ? existing.getStatusDisplay() : "Pending");

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildBookingListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Create Booking");
        saveBtn.setPrefSize(155, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {

            String notes = notesArea.getText().trim();
            String statusStr = statusBox.getValue();

            Customer shipper = shipperBox.getValue();
            Customer consignee = consigneeBox.getValue();
            Cargo cargo = cargoBox.getValue();

            LocalDate bookingDate = bookingDatePicker.getValue();

            if (shipper == null || consignee == null || cargo == null || bookingDate == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }

            int shipperId = shipper.getId();
            int consigneeId = consignee.getId();
            int cargoId = cargo.getId();

            Booking.Status status = Booking.Status.fromDisplay(statusStr);

            if (isEdit) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Save Changes");
                confirm.setHeaderText("Save changes for Booking #" + existing.getId() + "?");
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        try {
                            Connection con = DBConnection.getConnection();
                            String sql = "UPDATE booking SET "
                                    + "shipper_id = ?, "
                                    + "consignee_id = ?, "
                                    + "cargo_id = ?, "
                                    + "status = ?, "
                                    + "notes = ?, "
                                    + "booking_date = ? " // ← add
                                    + "WHERE booking_id = ?";
                            PreparedStatement ps = con.prepareStatement(sql);
                            ps.setInt(1, shipperId);
                            ps.setInt(2, consigneeId);
                            ps.setInt(3, cargoId);
                            ps.setString(4, status.name().toLowerCase());
                            ps.setString(5, notes);
                            ps.setDate(6, java.sql.Date.valueOf(bookingDate));
                            ps.setInt(7, existing.getId());
                            ps.executeUpdate();
                            con.close();

                            // Refresh in-memory list
                            bookingList.setAll(new OptionQueries().getBookingListFromDatabase());
                            innerContent.setCenter(buildBookingListView());
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            } else {
                try {
                    Connection con = DBConnection.getConnection();
                    String sql = "INSERT INTO booking (shipper_id, consignee_id, cargo_id, status, notes, booking_date) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, shipperId);
                    ps.setInt(2, consigneeId);
                    ps.setInt(3, cargoId);
                    ps.setString(4, status.name().toLowerCase());
                    ps.setString(5, notes);
                    ps.setDate(6, java.sql.Date.valueOf(bookingDate));
                    ps.executeUpdate();
                    con.close();

                    // Refresh in-memory list   
                    bookingList.setAll(new OptionQueries().getBookingListFromDatabase());
                    innerContent.setCenter(buildBookingListView());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Shipper (Customer ID)"), shipperBox,
                AppStyles.formLabel("Consignee (Customer ID)"), consigneeBox,
                AppStyles.formLabel("Cargo ID"), cargoBox,
                AppStyles.formLabel("Booking Date"), bookingDatePicker,
                AppStyles.formLabel("Status"), statusBox,
                AppStyles.formLabel("Notes"), notesArea,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  📋 → 🚢  CONVERT BOOKING TO SHIPMENT — form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildConvertToShipmentForm(Booking booking) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(720);

        Label pageTitle = new Label("Convert Booking #" + booking.getId() + " to Shipment");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        // Booking summary info card
        VBox infoCard = AppStyles.infoCard();
        infoCard.getChildren().addAll(
                AppStyles.infoRow("Booking ID", String.valueOf(booking.getId())),
                AppStyles.infoRow("Shipper ID", String.valueOf(booking.getShipperId())),
                AppStyles.infoRow("Consignee ID", String.valueOf(booking.getConsigneeId())),
                AppStyles.infoRow("Cargo ID", String.valueOf(booking.getCargoId())),
                AppStyles.infoRow("Notes", booking.getNotes().isEmpty() ? "—" : booking.getNotes())
        );

        VBox card = AppStyles.formCard();

        // Vessel selector
        ComboBox<Vessel> vesselBox = new ComboBox<>(vesselList);
        vesselBox.setPromptText("Select vessel...");
        vesselBox.setPrefWidth(Double.MAX_VALUE);
        vesselBox.setPrefHeight(40);
        vesselBox.setStyle(AppStyles.comboStyle());
        vesselBox.setCellFactory(lv -> vesselCell());
        vesselBox.setButtonCell(vesselCell());

        // Route selector
        ComboBox<Route> routeBox = new ComboBox<>(routeList);
        routeBox.setPromptText("Select route...");
        routeBox.setPrefWidth(Double.MAX_VALUE);
        routeBox.setPrefHeight(40);
        routeBox.setStyle(AppStyles.comboStyle());
        routeBox.setCellFactory(lv -> routeCell());
        routeBox.setButtonCell(routeCell());

        // Container selector
        ComboBox<Container> containerBox = new ComboBox<>(containerList);
        containerBox.setPromptText("Select container...");
        containerBox.setPrefWidth(Double.MAX_VALUE);
        containerBox.setPrefHeight(40);
        containerBox.setStyle(AppStyles.comboStyle());
        containerBox.setCellFactory(lv -> containerCell());
        containerBox.setButtonCell(containerCell());

        // Date pickers
        DatePicker departurePicker = new DatePicker();
        departurePicker.setPromptText("Departure Date");
        departurePicker.setPrefWidth(Double.MAX_VALUE);
        departurePicker.setPrefHeight(40);

        DatePicker arrivalPicker = new DatePicker();
        arrivalPicker.setPromptText("Estimated Arrival Date");
        arrivalPicker.setPrefWidth(Double.MAX_VALUE);
        arrivalPicker.setPrefHeight(40);

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildBookingListView()));

        Button convertBtn = new Button("Convert to Shipment");
        convertBtn.setPrefSize(190, 40);
        convertBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(convertBtn, false);
        convertBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(convertBtn, true));
        convertBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(convertBtn, false));

        convertBtn.setOnAction(e -> {
            Vessel vessel = vesselBox.getValue();
            Route route = routeBox.getValue();
            // FIX 2: Removed duplicate 'Container container' declaration and the
            //        duplicate null-check block that followed it.
            Container container = containerBox.getValue();
            LocalDate depDate = departurePicker.getValue();
            LocalDate arrDate = arrivalPicker.getValue();

            if (vessel == null || route == null || container == null
                    || depDate == null || arrDate == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }
            int containerId = container.getId();
            if (!arrDate.isAfter(depDate)) {
                AppStyles.showError(errorLabel,
                        "Estimated arrival date must be after departure date.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Convert to Shipment");
            confirm.setHeaderText("Convert Booking #" + booking.getId() + " to a new Shipment?");
            confirm.setContentText(
                    "Booking status will be set to Converted.\nThis action cannot be undone.");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        Connection con = DBConnection.getConnection();

                        // ── 1. INSERT shipment into DB ──────────────────────────
                        String shipSql = "INSERT INTO shipment "
                                + "(booking_id, vessel_id, route_id, container_id, status, departure_date, arrival_date) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement shipPs = con.prepareStatement(shipSql, Statement.RETURN_GENERATED_KEYS);
                        shipPs.setInt(1, booking.getId());
                        shipPs.setInt(2, vessel.getId());
                        shipPs.setInt(3, route.getId());
                        shipPs.setInt(4, containerId);
                        shipPs.setString(5, "pending_departure");
                        shipPs.setDate(6, java.sql.Date.valueOf(depDate));
                        shipPs.setDate(7, java.sql.Date.valueOf(arrDate));
                        shipPs.executeUpdate();

                        // ── 2. Get generated shipment_id ────────────────────────
                        int newShipmentId = -1;
                        ResultSet keys = shipPs.getGeneratedKeys();
                        if (keys.next()) {
                            newShipmentId = keys.getInt(1);
                        }

                        // ── 3. INSERT milestone ─────────────────────────────────
                        if (newShipmentId != -1) {
                            String mileSql = "INSERT INTO shipment_milestone "
                                    + "(shipment_id, stage, milestone_date, remarks) "
                                    + "VALUES (?, ?, ?, ?)";
                            PreparedStatement milePs = con.prepareStatement(mileSql);
                            milePs.setInt(1, newShipmentId);
                            milePs.setString(2, "booking_confirmed");
                            milePs.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                            milePs.setString(4, "Converted from Booking #" + booking.getId());
                            milePs.executeUpdate();
                        }

                        // ── 4. UPDATE booking status to converted ───────────────
                        PreparedStatement bookPs = con.prepareStatement(
                                "UPDATE booking SET status = 'converted' WHERE booking_id = ?"
                        );
                        bookPs.setInt(1, booking.getId());
                        bookPs.executeUpdate();

                        con.close();

                        // ── 5. Refresh in-memory lists ──────────────────────────
                        booking.setStatus(Booking.Status.CONVERTED);
                        bookingList.setAll(new OptionQueries().getBookingListFromDatabase());
                        shipmentList.setAll(new OptionQueries().getShipmentListFromDatabase());

                        navigateTo(1);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        HBox btnRow = new HBox(12, cancelBtn, convertBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Assign Vessel"), vesselBox,
                AppStyles.formLabel("Assign Route"), routeBox,
                AppStyles.formLabel("Assign Container"), containerBox,
                AppStyles.formLabel("Departure Date"), departurePicker,
                AppStyles.formLabel("Estimated Arrival Date"), arrivalPicker,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, infoCard, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  🚢  SHIPMENTS — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildShipmentListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        // ── Header ──────────────────────────────────────────────────────────
        Label title = AppStyles.sectionTitle("Shipments");
        Label hint = new Label("Shipments are created by converting a Confirmed booking.");
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.GRAY);

        HBox header = new HBox(12, title);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Search ───────────────────────────────────────────────────────────
        TextField search = searchField("Search by Shipment ID, Booking ID, Vessel ID, or Status...");
        ObservableList<Shipment> filtered = FXCollections.observableArrayList(shipmentList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(shipmentList.filtered(s
                    -> String.valueOf(s.getId()).contains(q)
                    || String.valueOf(s.getBookingId()).contains(q)
                    || String.valueOf(s.getVesselId()).contains(q)
                    || s.getStatusDisplay().toLowerCase().contains(q)));
        });

        if (shipmentList.isEmpty()) {
            content.getChildren().addAll(header, hint, search,
                    emptyLabel("No shipments yet. Convert a confirmed booking to create one."));
        } else {
            TableView<Shipment> table = buildShipmentTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, hint, search, table);
        }
        return content;
    }

    // ── Shipment table ──────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Shipment> buildShipmentTable(ObservableList<Shipment> data) {
        TableView<Shipment> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Shipment, Integer> idCol = col("Shipment ID", "id");
        TableColumn<Shipment, Integer> bookingIdCol = col("Booking ID", "bookingId");
        TableColumn<Shipment, Integer> vesselIdCol = col("Vessel ID", "vesselId");
        TableColumn<Shipment, Integer> routeIdCol = col("Route ID", "routeId");
        TableColumn<Shipment, Integer> contIdCol = col("Container ID", "containerId");
        idCol.setPrefWidth(100);
        bookingIdCol.setPrefWidth(100);
        vesselIdCol.setPrefWidth(90);
        routeIdCol.setPrefWidth(85);
        contIdCol.setPrefWidth(105);

        // Departure date
        TableColumn<Shipment, LocalDate> depCol = new TableColumn<>("Departure");
        depCol.setPrefWidth(115);
        depCol.setCellValueFactory(new PropertyValueFactory<>("departureDate"));
        depCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "—" : d.format(DATE_FMT));
            }
        });

        // Arrival date
        TableColumn<Shipment, LocalDate> arrCol = new TableColumn<>("Est. Arrival");
        arrCol.setPrefWidth(115);
        arrCol.setCellValueFactory(new PropertyValueFactory<>("arrivalDate"));
        arrCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "—" : d.format(DATE_FMT));
            }
        });

        // Status badge
        TableColumn<Shipment, Void> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(145);
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Shipment s = getTableRow().getItem();
                Label badge = new Label(s.getStatusDisplay());
                badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle(
                        "-fx-background-radius: 12; -fx-text-fill: white; "
                        + "-fx-background-color: " + shipmentStatusColor(s.getStatus()) + ";"
                );
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Actions: Assign | 🔄 Status | 📌 Milestone | Delete
        TableColumn<Shipment, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(230);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button assignBtn = assignButton();
            private final Button statusBtn = statusButton();
            private final Button milestoneBtn = milestoneButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box
                    = new HBox(5, assignBtn, statusBtn, milestoneBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);

                assignBtn.setVisible(Permission.canEdit("shipments"));
                assignBtn.setManaged(Permission.canEdit("shipments"));

                statusBtn.setVisible(Permission.canEdit("shipments"));
                statusBtn.setManaged(Permission.canEdit("shipments"));

                milestoneBtn.setVisible(Permission.canEdit("shipments"));
                milestoneBtn.setManaged(Permission.canEdit("shipments"));

                deleteBtn.setVisible(Permission.canDelete("shipments"));
                deleteBtn.setManaged(Permission.canDelete("shipments"));
            }

            {
                assignBtn.setOnAction(e -> {
                    Shipment s = getTableRow().getItem();
                    if (s != null) {
                        innerContent.setCenter(buildAssignShipmentForm(s));
                    }
                });

                statusBtn.setOnAction(e -> {
                    Shipment s = getTableRow().getItem();
                    if (s != null) {
                        showShipmentStatusDialog(s, table);
                    }
                });

                milestoneBtn.setOnAction(e -> {
                    Shipment s = getTableRow().getItem();
                    if (s != null) {
                        showMilestoneDialog(s, table);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Shipment s = getTableRow().getItem();
                    if (s == null) {
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Shipment");
                    confirm.setHeaderText("Delete Shipment #" + s.getId() + "?");
                    confirm.setContentText(
                            "The linked Booking #" + s.getBookingId()
                            + " will remain in Converted status.\nThis action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();

                                // milestones are deleted automatically via ON DELETE CASCADE
                                PreparedStatement ps = con.prepareStatement(
                                        "DELETE FROM shipment WHERE shipment_id = ?"
                                );
                                ps.setInt(1, s.getId());
                                ps.executeUpdate();
                                con.close();

                                shipmentList.setAll(new OptionQueries().getShipmentListFromDatabase());
                                innerContent.setCenter(buildShipmentListView());
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Shipment s = getTableRow().getItem();
                boolean terminal = s.getStatus() == Shipment.Status.DELIVERED
                        || s.getStatus() == Shipment.Status.CANCELLED;
                statusBtn.setDisable(terminal);
                statusBtn.setOpacity(terminal ? 0.4 : 1.0);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(
                idCol, bookingIdCol, vesselIdCol, routeIdCol, contIdCol,
                depCol, arrCol, statusCol, actCol
        );
        return table;
    }

    // ── Shipment status update dialog ───────────────────────────────────────
    private void showShipmentStatusDialog(Shipment shipment, TableView<Shipment> table) {
        Shipment.Status current = shipment.getStatus();

        if (current == Shipment.Status.DELIVERED
                || current == Shipment.Status.CANCELLED) {
            showWarn("Status Locked",
                    "Shipment #" + shipment.getId() + " is already " + shipment.getStatusDisplay() + ".",
                    "No further status changes are allowed.");
            return;
        }

        // Valid forward transitions
        ObservableList<String> options = FXCollections.observableArrayList();
        switch (current) {
            case PENDING_DEPARTURE ->
                options.addAll("Departed", "Cancelled");
            case DEPARTED ->
                options.add("In Transit");
            case IN_TRANSIT ->
                options.add("Arrived");
            case ARRIVED ->
                options.add("Customs Cleared");
            case CUSTOMS_CLEARED ->
                options.add("Delivered");
        }

        ChoiceDialog<String> dlg = new ChoiceDialog<>(options.get(0), options);
        dlg.setTitle("Update Shipment Status");
        dlg.setHeaderText("Shipment #" + shipment.getId()
                + "  (Booking #" + shipment.getBookingId() + ")");
        dlg.setContentText("Select new status:");
        dlg.showAndWait().ifPresent(choice -> {
            Shipment.Status newStatus = Shipment.Status.fromDisplay(choice);
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE shipment SET status = ? WHERE shipment_id = ?"
                );
                ps.setString(1, newStatus.name().toLowerCase());
                ps.setInt(2, shipment.getId());
                ps.executeUpdate();
                con.close();

                shipment.setStatus(newStatus);
                shipmentList.setAll(new OptionQueries().getShipmentListFromDatabase());
                table.refresh();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    // ── Log Milestone dialog ────────────────────────────────────────────────
    private void showMilestoneDialog(Shipment shipment, TableView<Shipment> table) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Milestones");
        dlg.setHeaderText("Shipment #" + shipment.getId()
                + "  —  Booking #" + shipment.getBookingId());

        DialogPane pane = dlg.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setPrefWidth(620);
        pane.setPrefHeight(600);

        VBox content = new VBox(16);
        content.setPadding(new Insets(16));

        // ── Milestone table ──────────────────────────────────────────────
        TableView<String[]> milestoneTable = new TableView<>();
        milestoneTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        milestoneTable.setPrefHeight(260);
        milestoneTable.setFixedCellSize(-1);

        TableColumn<String[], String> stageCol = new TableColumn<>("Stage");
        TableColumn<String[], String> dateCol = new TableColumn<>("Date");
        TableColumn<String[], String> remarksCol = new TableColumn<>("Remarks");

        stageCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        remarksCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        remarksCol.setCellFactory(col -> new TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty().bind(remarksCol.widthProperty().subtract(10));
                setGraphic(text);
                setPadding(new Insets(6, 4, 6, 4));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty || item == null ? null : item);
            }
        });

        stageCol.setPrefWidth(150);
        dateCol.setPrefWidth(110);
        remarksCol.setPrefWidth(280);

        // Load from DB
        Runnable loadMilestones = () -> {
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT milestone_id, stage, milestone_date, remarks FROM shipment_milestone "
                        + "WHERE shipment_id = ? ORDER BY milestone_date ASC"
                );
                ps.setInt(1, shipment.getId());
                ResultSet rs = ps.executeQuery();

                ObservableList<String[]> rows = FXCollections.observableArrayList();
                while (rs.next()) {
                    rows.add(new String[]{
                        rs.getString("milestone_id"),
                        rs.getString("stage"),
                        rs.getDate("milestone_date").toLocalDate().format(DATE_FMT),
                        rs.getString("remarks") != null ? rs.getString("remarks") : "—"
                    });
                }
                milestoneTable.setItems(rows);
                if (rows.isEmpty()) {
                    milestoneTable.setPlaceholder(new Label("No milestones logged yet."));
                }
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };
        loadMilestones.run(); // load immediately

        TableColumn<String[], Void> deleteCol = new TableColumn<>("Action");
        deleteCol.setPrefWidth(70);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = AppStyles.deleteButton();

            {
                delBtn.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    int milestoneId = Integer.parseInt(row[0]);

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Milestone");
                    confirm.setHeaderText("Delete milestone \"" + row[1] + "\"?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement(
                                        "DELETE FROM shipment_milestone WHERE milestone_id = ?"
                                );
                                ps.setInt(1, milestoneId);
                                ps.executeUpdate();
                                con.close();

                                loadMilestones.run(); // ← refresh table
                                table.refresh();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(delBtn);
                    setAlignment(Pos.CENTER); // ← add this
                }
            }
        });

        milestoneTable.getColumns().addAll(stageCol, dateCol, remarksCol, deleteCol);

        // ── Log new milestone form ───────────────────────────────────────
        Label formTitle = new Label("Log New Milestone");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        formTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
                "Booking Confirmed", "Departed", "Arrived", "Customs Cleared", "Delivered"
        );
        typeBox.setPromptText("Select milestone type...");
        typeBox.setPrefWidth(Double.MAX_VALUE);
        typeBox.setPrefHeight(40);
        typeBox.setStyle(AppStyles.comboStyle());

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);
        datePicker.setPrefHeight(40);

        TextArea remarksArea = new TextArea();
        remarksArea.setPromptText("Remarks (optional)...");
        remarksArea.setPrefHeight(70);
        remarksArea.setWrapText(true);
        remarksArea.setStyle(AppStyles.fieldStyle());

        Label errorLabel = AppStyles.errorLabel();

        Button logBtn = new Button("Log Milestone");
        logBtn.setPrefHeight(38);
        logBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(logBtn, false);
        logBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(logBtn, true));
        logBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(logBtn, false));

        logBtn.setOnAction(e -> {
            if (typeBox.getValue() == null || datePicker.getValue() == null) {
                AppStyles.showError(errorLabel, "Milestone type and date are required.");
                return;
            }

            ShipmentMilestone.Type mType
                    = ShipmentMilestone.Type.fromDisplay(typeBox.getValue());

            // ← Add confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Log Milestone");
            confirm.setHeaderText("Log \"" + typeBox.getValue() + "\" for Shipment #" + shipment.getId() + "?");
            confirm.setContentText("This milestone will be saved and cannot be undone.");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        Connection con = DBConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement(
                                "INSERT INTO shipment_milestone (shipment_id, stage, milestone_date, remarks) "
                                + "VALUES (?, ?, ?, ?)"
                        );
                        ps.setInt(1, shipment.getId());
                        ps.setString(2, mType.name().toLowerCase());
                        ps.setDate(3, java.sql.Date.valueOf(datePicker.getValue()));
                        ps.setString(4, remarksArea.getText().trim());
                        ps.executeUpdate();
                        con.close();

                        shipment.addMilestone(new ShipmentMilestone(
                                mType, datePicker.getValue(), remarksArea.getText().trim()
                        ));

                        // Reset form
                        typeBox.setValue(null);
                        datePicker.setValue(LocalDate.now());
                        remarksArea.clear();
                        errorLabel.setVisible(false);
                        errorLabel.setManaged(false);

                        // Reload table
                        loadMilestones.run();
                        table.refresh();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        VBox formBox = new VBox(8,
                formTitle,
                AppStyles.formLabel("Milestone Type"), typeBox,
                AppStyles.formLabel("Date"), datePicker,
                AppStyles.formLabel("Remarks"), remarksArea,
                errorLabel, logBtn
        );
        formBox.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        content.getChildren().addAll(milestoneTable, formBox);
        pane.setContent(content);
        dlg.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  🚢  SHIPMENTS — assign vessel / route / container form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildAssignShipmentForm(Shipment existing) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(720);

        Label pageTitle = new Label("Assign Resources — Shipment #" + existing.getId());
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        ComboBox<Vessel> vesselBox = new ComboBox<>(vesselList);
        vesselBox.setPromptText("Select vessel...");
        vesselBox.setPrefWidth(Double.MAX_VALUE);
        vesselBox.setPrefHeight(40);
        vesselBox.setStyle(AppStyles.comboStyle());
        vesselBox.setCellFactory(lv -> vesselCell());
        vesselBox.setButtonCell(vesselCell());
        if (existing.getVesselId() > 0) {
            vesselBox.setValue(vesselById(existing.getVesselId()));
        }

        ComboBox<Route> routeBox = new ComboBox<>(routeList);
        routeBox.setPromptText("Select route...");
        routeBox.setPrefWidth(Double.MAX_VALUE);
        routeBox.setPrefHeight(40);
        routeBox.setStyle(AppStyles.comboStyle());
        routeBox.setCellFactory(lv -> routeCell());
        routeBox.setButtonCell(routeCell());
        if (existing.getRouteId() > 0) {
            routeBox.setValue(routeById(existing.getRouteId()));
        }

        ComboBox<Container> containerBox = new ComboBox<>(containerList);
        containerBox.setPromptText("Select container...");
        containerBox.setPrefWidth(Double.MAX_VALUE);
        containerBox.setPrefHeight(40);
        containerBox.setStyle(AppStyles.comboStyle());
        containerBox.setCellFactory(lv -> containerCell());
        containerBox.setButtonCell(containerCell());
        if (existing.getContainerId() > 0) {
            containerBox.setValue(containerById(existing.getContainerId()));
        }

        DatePicker departurePicker = new DatePicker(existing.getDepartureDate());
        departurePicker.setPrefWidth(Double.MAX_VALUE);
        departurePicker.setPrefHeight(40);

        DatePicker arrivalPicker = new DatePicker(existing.getArrivalDate());
        arrivalPicker.setPrefWidth(Double.MAX_VALUE);
        arrivalPicker.setPrefHeight(40);

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildShipmentListView()));

        Button saveBtn = new Button("Save Assignment");
        saveBtn.setPrefSize(165, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            Vessel vessel = vesselBox.getValue();
            Route route = routeBox.getValue();
            Container container = containerBox.getValue(); // ← from ComboBox now
            LocalDate depDate = departurePicker.getValue();
            LocalDate arrDate = arrivalPicker.getValue();

            if (vessel == null || route == null || container == null
                    || depDate == null || arrDate == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }
            if (!arrDate.isAfter(depDate)) {
                AppStyles.showError(errorLabel, "Arrival date must be after departure date.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Save Assignment");
            confirm.setHeaderText("Save assignment for Shipment #" + existing.getId() + "?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        Connection con = DBConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement(
                                "UPDATE shipment SET "
                                + "vessel_id = ?, "
                                + "route_id = ?, "
                                + "container_id = ?, "
                                + "departure_date = ?, "
                                + "arrival_date = ? "
                                + "WHERE shipment_id = ?"
                        );
                        ps.setInt(1, vessel.getId());
                        ps.setInt(2, route.getId());
                        ps.setInt(3, container.getId());
                        ps.setDate(4, java.sql.Date.valueOf(depDate));
                        ps.setDate(5, java.sql.Date.valueOf(arrDate));
                        ps.setInt(6, existing.getId());
                        ps.executeUpdate();
                        con.close();

                        shipmentList.setAll(new OptionQueries().getShipmentListFromDatabase());
                        innerContent.setCenter(buildShipmentListView());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Vessel"), vesselBox,
                AppStyles.formLabel("Route"), routeBox,
                AppStyles.formLabel("Assign Container"), containerBox, // ← changed
                AppStyles.formLabel("Departure Date"), departurePicker,
                AppStyles.formLabel("Estimated Arrival Date"), arrivalPicker,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════════════════════════
    // Generic typed TableColumn backed by a JavaFX property name. 
    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> col(String header, String property) {
        TableColumn<S, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        return c;
    }

    // ── Colour helpers ──────────────────────────────────────────────────────
    private String bookingStatusColor(Booking.Status s) {
        return switch (s) {
            case PENDING ->
                "#F57C00";  // orange
            case CONFIRMED ->
                "#1565C0";  // blue
            case CONVERTED ->
                "#388E3C";  // green
            case VOIDED ->
                "#757575";  // grey
            case CANCELLED ->
                "#C62828";  // red
        };
    }

    private String shipmentStatusColor(Shipment.Status s) {
        return switch (s) {
            case PENDING_DEPARTURE ->
                "#F57C00";  // orange
            case DEPARTED ->
                "#1976D2";  // medium blue
            case IN_TRANSIT ->
                "#7B1FA2";  // purple
            case ARRIVED ->
                "#0097A7";  // teal
            case CUSTOMS_CLEARED ->
                "#388E3C";  // green
            case DELIVERED ->
                "#2E7D32";  // dark green
            case CANCELLED ->
                "#C62828";  // red
        };
    }

    // ── Lookup helpers ──────────────────────────────────────────────────────
    private Vessel vesselById(int id) {
        return vesselList.stream().filter(v -> v.getId() == id).findFirst().orElse(null);
    }

    private Route routeById(int id) {
        return routeList.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    // ── Cell factories ──────────────────────────────────────────────────────
    private ListCell<Vessel> vesselCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Vessel v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null
                        : v.getName() + " (" + v.getRegistrationNumber() + ")");
            }
        };
    }

    private ListCell<Route> routeCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Route r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText(null);
                    return;
                }
                setText("Route #" + r.getId()
                        + "  |  Port #" + r.getOriginPortId()
                        + " → Port #" + r.getDestinationPortId()
                        + "  (" + r.getTransitDays() + " days)");
            }
        };
    }

    // ── UI component helpers ────────────────────────────────────────────────
    private Label emptyLabel(String msg) {
        Label l = new Label(msg);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        l.setTextFill(Color.web("#888888"));
        l.setPadding(new Insets(40));
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        l.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        return l;
    }

    private TextField searchField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(420);
        tf.setPrefHeight(36);
        return tf;
    }

    private ScrollPane wrapScroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle(
                "-fx-background-color: " + AppStyles.LIGHT_BLUE
                + "; -fx-background: " + AppStyles.LIGHT_BLUE + ";"
        );
        return sp;
    }

    private Button statusButton() {
        Button btn = new Button("🔄");
        btn.setStyle(
                "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #E65100; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        return btn;
    }

    private Button assignButton() {
        Button btn = new Button("🔗");
        btn.setStyle(
                "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #1565C0; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        return btn;
    }

    private Button milestoneButton() {
        Button btn = new Button("📌");
        btn.setStyle(
                "-fx-background-color: #F3E5F5; -fx-text-fill: #6A1B9A; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #6A1B9A; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #F3E5F5; -fx-text-fill: #6A1B9A; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        return btn;
    }

    private void showWarn(String title, String header, String body) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(body);
        a.showAndWait();
    }

    private Customer customerById(int id) {
        return customerList.stream()
                .filter(c -> c.getId() == id)
                .findFirst().orElse(null);
    }

    private ListCell<Customer> customerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null
                        : "#" + c.getId() + " — " + c.getFullName() + " (" + c.getRole() + ")");
            }
        };
    }

    // ── Cargo helpers ────────────────────────────────────────────────────
    private Cargo cargoById(int id) {
        return cargoList.stream()
                .filter(c -> c.getId() == id)
                .findFirst().orElse(null);
    }

    private ListCell<Cargo> cargoCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Cargo c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null
                        : "#" + c.getId() + " — " + c.getDescription()
                        + (c.isHazardous() ? " ⚠ Hazardous" : ""));
            }
        };
    }

    // ── Container helpers ────────────────────────────────────────────────
    private Container containerById(int id) {
        return containerList.stream()
                .filter(c -> c.getId() == id)
                .findFirst().orElse(null);
    }

    private ListCell<Container> containerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Container c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null
                        : c.getContainerNumber() + " — " + c.getTypeDisplay()
                        + " (" + c.getStatusDisplay() + ")");
            }
        };
    }
}

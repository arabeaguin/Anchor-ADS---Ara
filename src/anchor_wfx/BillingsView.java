package anchor_wfx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Billings & Documentation module for ANCHOR ADS. 5 tabs: Invoices | Payments |
 * Freight Rates | Surcharges | Cargo Manifest
 *
 * Add to Dashboard.java fields: private final ObservableList<FreightRate>
 * freightRateList = FXCollections.observableArrayList(); private final
 * ObservableList<Surcharge> surchargeList =
 * FXCollections.observableArrayList(); private final ObservableList<Invoice>
 * invoiceList = FXCollections.observableArrayList(); private final
 * ObservableList<Payment> paymentList = FXCollections.observableArrayList();
 *
 * Add to switchView "Billings" case: root.setCenter(new BillingsView(root,
 * invoiceList, paymentList, freightRateList, surchargeList, shipmentList,
 * customerList, vesselList, bookingList, cargoList, routeList).build());
 */
public class BillingsView {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final String PESO = "\u20B1";  // ₱

    private final BorderPane root;
    private final BorderPane innerContent;

    private final ObservableList<Invoice> invoiceList;
    private final ObservableList<Payment> paymentList;
    private final ObservableList<FreightRate> freightRateList;
    private final ObservableList<Surcharge> surchargeList;
    private final ObservableList<Shipment> shipmentList;
    private final ObservableList<Customer> customerList;
    private final ObservableList<Vessel> vesselList;
    private final ObservableList<Booking> bookingList;
    private final ObservableList<Cargo> cargoList;
    private final ObservableList<Route> routeList;
    private final java.util.Set<ManifestRow> selectedForPrint = new java.util.HashSet<>();

    private final Button[] tabButtons = new Button[5];
    private static final String[] TAB_LABELS = {
        "\uD83E\uDDFE  Invoices",
        "\uD83D\uDCB3  Payments",
        "\uD83D\uDCE6  Freight Rates",
        "\u2795  Surcharges",
        "\uD83D\uDCCB  Cargo Manifest"
    };

    public BillingsView(BorderPane root,
            ObservableList<Invoice> invoiceList,
            ObservableList<Payment> paymentList,
            ObservableList<FreightRate> freightRateList,
            ObservableList<Surcharge> surchargeList,
            ObservableList<Shipment> shipmentList,
            ObservableList<Customer> customerList,
            ObservableList<Vessel> vesselList,
            ObservableList<Booking> bookingList,
            ObservableList<Cargo> cargoList,
            ObservableList<Route> routeList) {
        this.root = root;
        this.innerContent = new BorderPane();
        this.invoiceList = invoiceList;
        this.paymentList = paymentList;
        this.freightRateList = freightRateList;
        this.surchargeList = surchargeList;
        this.shipmentList = shipmentList;
        this.customerList = customerList;
        this.vesselList = vesselList;
        this.bookingList = bookingList;
        this.cargoList = cargoList;
        this.routeList = routeList;
    }

    /**
     * Entry point — called by Dashboard router.
     */
    public VBox build() {
        VBox shell = new VBox(0);
        shell.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        shell.setMaxHeight(Double.MAX_VALUE);
        shell.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(innerContent, Priority.ALWAYS);
        shell.getChildren().addAll(buildTabBar(), innerContent);
        navigateTo(0);
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
            btn.setPrefWidth(180);
            btn.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            final int idx = i;
            btn.setOnAction(e -> navigateTo(idx));
            tabButtons[i] = btn;
            bar.getChildren().add(btn);
        }
        return bar;
    }

    private void navigateTo(int idx) {
        updateTabStyles(idx);
        switch (idx) {
            case 0 ->
                innerContent.setCenter(buildInvoiceListView());
            case 1 ->
                innerContent.setCenter(buildPaymentListView());
            case 2 ->
                innerContent.setCenter(buildFreightRateListView());
            case 3 ->
                innerContent.setCenter(buildSurchargeListView());
            case 4 ->
                innerContent.setCenter(buildCargoManifestView());
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
    //  🧾  INVOICES — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildInvoiceListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        Label title = AppStyles.sectionTitle("Invoices");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newBtn = new Button("➕  Create Invoice");
        newBtn.setPrefHeight(40);
        newBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(newBtn, false);
        newBtn.setVisible(Permission.canAdd("billings"));
        newBtn.setManaged(Permission.canAdd("billings"));
        newBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(newBtn, true));
        newBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(newBtn, false));
        newBtn.setOnAction(e -> {
            if (shipmentList.isEmpty()) {
                showWarn("No Shipments", "No shipments available.",
                        "Create a shipment first before issuing an invoice.");
                return;
            }
            innerContent.setCenter(buildInvoiceFormView(null));
        });

        HBox header = new HBox(12, title, spacer, newBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = searchField("Search by Invoice ID, Shipment ID, Customer ID, or Status...");
        ObservableList<Invoice> filtered = FXCollections.observableArrayList(invoiceList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(invoiceList.filtered(inv
                    -> String.valueOf(inv.getId()).contains(q)
                    || String.valueOf(inv.getShipmentId()).contains(q)
                    || String.valueOf(inv.getCustomerId()).contains(q)
                    || inv.getStatusDisplay().toLowerCase().contains(q)));
        });

        if (invoiceList.isEmpty()) {
            content.getChildren().addAll(header, search, emptyLabel("No invoices created yet."));
        } else {
            TableView<Invoice> table = buildInvoiceTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, search, table);
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    private TableView<Invoice> buildInvoiceTable(ObservableList<Invoice> data) {
        TableView<Invoice> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Invoice, Integer> idCol = col("Invoice ID", "id");
        TableColumn<Invoice, Integer> shipCol = col("Shipment ID", "shipmentId");
        TableColumn<Invoice, Integer> custCol = col("Customer ID", "customerId");
        TableColumn<Invoice, Integer> rateCol = col("Freight Rate ID", "freightRateId");
        idCol.setPrefWidth(95);
        shipCol.setPrefWidth(100);
        custCol.setPrefWidth(105);
        rateCol.setPrefWidth(120);

        // Subtotal
        TableColumn<Invoice, Double> subCol = new TableColumn<>("Subtotal");
        subCol.setPrefWidth(110);
        subCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : PESO + String.format("%,.2f", v));
            }
        });

        // Total Amount
        TableColumn<Invoice, Double> totalCol = new TableColumn<>("Total Amount");
        totalCol.setPrefWidth(120);
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : PESO + String.format("%,.2f", v));
            }
        });

        // Invoice Date
        TableColumn<Invoice, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(110);
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        dateCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.format(DATE_FMT));
            }
        });

        // Status badge
        TableColumn<Invoice, Void> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Invoice inv = getTableRow().getItem();
                Label badge = new Label(inv.getStatusDisplay());
                badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle(
                        "-fx-background-radius: 12; -fx-text-fill: white; "
                        + "-fx-background-color: " + invoiceStatusColor(inv.getStatus()) + ";"
                );
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Actions: View | Surcharge | Delete
        TableColumn<Invoice, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(160);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = viewButton();
            private final Button surchargeBtn = surchargeButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(6, viewBtn, surchargeBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                surchargeBtn.setVisible(Permission.canEdit("billings"));
                surchargeBtn.setManaged(Permission.canEdit("billings"));
                deleteBtn.setVisible(Permission.canDelete("billings"));
                deleteBtn.setManaged(Permission.canDelete("billings"));

            }

            {
                viewBtn.setOnAction(e -> {
                    Invoice inv = getTableRow().getItem();
                    if (inv != null) {
                        showInvoiceDetailsDialog(inv);
                    }
                });

                surchargeBtn.setOnAction(e -> {
                    Invoice inv = getTableRow().getItem();
                    if (inv == null) {
                        return;
                    }
                    if (inv.getStatus() == Invoice.Status.PAID) {
                        showWarn("Invoice Paid",
                                "Invoice #" + inv.getId() + " is already fully paid.",
                                "Surcharges cannot be added to a paid invoice.");
                        return;
                    }
                    if (surchargeList.isEmpty()) {
                        showWarn("No Surcharges",
                                "No surcharges defined yet.",
                                "Go to the ➕ Surcharges tab to add surcharges first.");
                        return;
                    }
                    showApplySurchargeDialog(inv, table);
                });

                deleteBtn.setOnAction(e -> {
                    Invoice inv = getTableRow().getItem();
                    if (inv == null) {
                        return;
                    }
                    boolean hasPayments = paymentList.stream()
                            .anyMatch(p -> p.getInvoiceId() == inv.getId());
                    if (hasPayments) {
                        showWarn("Cannot Delete",
                                "Invoice #" + inv.getId() + " has logged payments.",
                                "Delete all payments for this invoice first.");
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Invoice");
                    confirm.setHeaderText("Delete Invoice #" + inv.getId() + "?");
                    confirm.setContentText("This action cannot be undone.");

                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement("DELETE FROM invoice WHERE invoice_id = ?");
                                ps.setInt(1, inv.getId());

                                System.out.println(ps);
                                System.out.println(inv.getId());
                                ps.executeUpdate();

                                loadInvoiceListFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            innerContent.setCenter(buildInvoiceListView());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(
                idCol, shipCol, custCol, rateCol, subCol, totalCol, dateCol, statusCol, actCol
        );
        table.setPlaceholder(new Label("No records found."));
        return table;
    }

    // ── Invoice details dialog ──────────────────────────────────────────────
    private void showInvoiceDetailsDialog(Invoice inv) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Invoice Details");
        dlg.setHeaderText("Invoice #" + inv.getId());
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(460);

        String customerName = customerList.stream()
                .filter(c -> c.getId() == inv.getCustomerId())
                .findFirst().map(Customer::getFullName).orElse("Customer #" + inv.getCustomerId());
        String freightName = freightRateList.stream()
                .filter(r -> r.getId() == inv.getFreightRateId())
                .findFirst().map(FreightRate::getName).orElse("Rate #" + inv.getFreightRateId());

        content.getChildren().addAll(
                AppStyles.infoRow("Invoice ID", String.valueOf(inv.getId())),
                AppStyles.infoRow("Shipment ID", String.valueOf(inv.getShipmentId())),
                AppStyles.infoRow("Customer", customerName),
                AppStyles.infoRow("Freight Rate", freightName),
                AppStyles.infoRow("Invoice Date", inv.getInvoiceDate() != null
                        ? inv.getInvoiceDate().format(DATE_FMT) : "—"),
                AppStyles.infoRow("Subtotal", PESO + String.format("%,.2f", inv.getSubtotal())),
                new Separator()
        );

        // Applied surcharges
        if (!inv.getAppliedSurcharges().isEmpty()) {
            Label surLbl = new Label("Applied Surcharges:");
            surLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            surLbl.setTextFill(Color.web(AppStyles.NAVY_BLUE));
            content.getChildren().add(surLbl);

            for (InvoiceSurcharge is : inv.getAppliedSurcharges()) {
                String surName = surchargeList.stream()
                        .filter(s -> s.getId() == is.getSurchargeId())
                        .findFirst().map(Surcharge::getName).orElse("Surcharge #" + is.getSurchargeId());
                content.getChildren().add(
                        AppStyles.infoRow("  " + surName,
                                PESO + String.format("%,.2f", is.getAppliedAmount()))
                );
            }
            content.getChildren().add(new Separator());
        }

        content.getChildren().add(
                AppStyles.infoRow("Total Amount", PESO + String.format("%,.2f", inv.getTotalAmount()))
        );

        // Payments summary
        double totalPaid = paymentList.stream()
                .filter(p -> p.getInvoiceId() == inv.getId())
                .mapToDouble(Payment::getAmountPaid).sum();
        double balance = inv.getTotalAmount() - totalPaid;

        content.getChildren().addAll(
                AppStyles.infoRow("Total Paid", PESO + String.format("%,.2f", totalPaid)),
                AppStyles.infoRow("Balance", PESO + String.format("%,.2f", Math.max(0, balance))),
                new Separator(),
                AppStyles.infoRow("Status", inv.getStatusDisplay())
        );

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPrefHeight(420);
        sp.setStyle("-fx-background-color: white; -fx-background: white;");
        dlg.getDialogPane().setContent(sp);
        dlg.showAndWait();
    }

    // ── Apply surcharge dialog ──────────────────────────────────────────────
    private void showApplySurchargeDialog(Invoice inv, TableView<Invoice> table) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Apply Surcharge");
        dlg.setHeaderText("Apply Surcharge to Invoice #" + inv.getId());
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setPrefWidth(400);

        ComboBox<Surcharge> surchargeBox = new ComboBox<>(surchargeList);
        surchargeBox.setPromptText("Select surcharge...");
        surchargeBox.setPrefWidth(Double.MAX_VALUE);
        surchargeBox.setPrefHeight(40);
        surchargeBox.setStyle(AppStyles.comboStyle());

        TextField amountField = AppStyles.formField("Applied Amount", "");

        // Auto-fill amount when surcharge selected
        surchargeBox.setOnAction(e -> {
            if (surchargeBox.getValue() != null) {
                amountField.setText(String.format("%.2f", surchargeBox.getValue().getDefaultAmount()));
            }
        });

        Label errorLabel = AppStyles.errorLabel();

        form.getChildren().addAll(
                AppStyles.formLabel("Surcharge"), surchargeBox,
                AppStyles.formLabel("Amount (" + PESO + ")"), amountField,
                errorLabel
        );
        dlg.getDialogPane().setContent(form);

        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (surchargeBox.getValue() == null || amountField.getText().trim().isEmpty()) {
                AppStyles.showError(errorLabel, "Please select a surcharge and enter an amount.");
                ev.consume();
                return;
            }
            try {
                double amt = Double.parseDouble(amountField.getText().trim());
                if (amt <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel, "Amount must be a positive number.");
                ev.consume();
            }
        });

        dlg.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                double amt = Double.parseDouble(amountField.getText().trim());
                int surchargeId = surchargeBox.getValue().getId();

                try {
                    Connection con = DBConnection.getConnection();

                    // 1. Insert into invoice_surcharge
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO invoice_surcharge (invoice_id, surcharge_id, applied_amount) "
                            + "VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE applied_amount = VALUES(applied_amount)"
                    );
                    ps.setInt(1, inv.getId());
                    ps.setInt(2, surchargeId);
                    ps.setDouble(3, amt);
                    ps.executeUpdate();

                    // 2. Recalculate and update total_amount in invoice
                    PreparedStatement updateTotal = con.prepareStatement(
                            "UPDATE invoice SET total_amount = subtotal + "
                            + "(SELECT COALESCE(SUM(applied_amount), 0) FROM invoice_surcharge "
                            + " WHERE invoice_id = ?) "
                            + "WHERE invoice_id = ?"
                    );
                    updateTotal.setInt(1, inv.getId());
                    updateTotal.setInt(2, inv.getId());
                    updateTotal.executeUpdate();

                    con.close();

                    // 3. Reload list so table reflects new total
                    loadInvoiceListFromDB();
                    innerContent.setCenter(buildInvoiceListView());

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  🧾  INVOICES — create form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildInvoiceFormView(Invoice existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(720);

        Label pageTitle = new Label(isEdit ? "Edit Invoice" : "Create Invoice");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        // Shipment selector
        ComboBox<Shipment> shipmentBox = new ComboBox<>(shipmentList);
        shipmentBox.setPromptText("Select shipment...");
        shipmentBox.setPrefWidth(Double.MAX_VALUE);
        shipmentBox.setPrefHeight(40);
        shipmentBox.setStyle(AppStyles.comboStyle());
        shipmentBox.setCellFactory(lv -> shipmentCell());
        shipmentBox.setButtonCell(shipmentCell());

        ComboBox<Customer> customerBox = new ComboBox<>();
        customerBox.setPromptText("Select customer...");
        customerBox.setPrefWidth(Double.MAX_VALUE);
        customerBox.setPrefHeight(40);
        customerBox.setStyle(AppStyles.comboStyle());
        customerBox.setCellFactory(lv -> customerCell());
        customerBox.setButtonCell(customerCell());

        // Freight rate selector
        ComboBox<FreightRate> rateBox = new ComboBox<>(freightRateList);
        rateBox.setPromptText("Select freight rate...");
        rateBox.setPrefWidth(Double.MAX_VALUE);
        rateBox.setPrefHeight(40);
        rateBox.setStyle(AppStyles.comboStyle());

        shipmentBox.setOnAction(e -> {
            customerBox.getItems().clear();
            customerBox.setValue(null);

            Shipment selected = shipmentBox.getValue();
            if (selected == null) {
                return;
            }

            // Find the booking linked to this shipment
            Booking booking = bookingList.stream()
                    .filter(b -> b.getId() == selected.getBookingId())
                    .findFirst().orElse(null);

            if (booking == null) {
                return;
            }

            // Only add the shipper and consignee from that booking
            ObservableList<Customer> relevantCustomers = FXCollections.observableArrayList();
            customerList.stream()
                    .filter(c -> c.getId() == booking.getShipperId()
                    || c.getId() == booking.getConsigneeId())
                    .forEach(relevantCustomers::add);

            customerBox.setItems(relevantCustomers);
        });
        if (isEdit) {
            shipmentBox.setValue(shipmentById(existing.getShipmentId()));
            shipmentBox.getOnAction().handle(null);
            customerBox.setValue(customerById(existing.getCustomerId()));
        }

        // Auto-fill subtotal when rate selected
        TextField subtotalField = AppStyles.formField(
                PESO + " 0.00", isEdit ? String.format("%.2f", existing.getSubtotal()) : "");

        rateBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                subtotalField.setText(String.format("%.2f", newVal.getBaseAmount()));
            }
        });

        if (isEdit) {
            rateBox.setValue(freightRateById(existing.getFreightRateId()));
        }

        DatePicker datePicker = new DatePicker(isEdit ? existing.getInvoiceDate() : LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);
        datePicker.setPrefHeight(40);

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildInvoiceListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Create Invoice");
        saveBtn.setPrefSize(155, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            Shipment ship = shipmentBox.getValue();
            Customer cust = customerBox.getValue();
            FreightRate rate = rateBox.getValue();
            String subStr = subtotalField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (ship == null || cust == null || rate == null || subStr.isEmpty() || date == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }

            int shipmentId = ship.getId();
            int customerId = cust.getId();
            int rateId = rate.getId();
            double subtotal;
            java.sql.Date invoice_date = java.sql.Date.valueOf(date);

            try {
                subtotal = Double.parseDouble(subStr);
                if (subtotal < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel, "Subtotal must be a valid positive number.");
                return;
            }

            double total = subtotal;  // moved to here
            String statusStr = isEdit
                    ? existing.getStatus().name().toLowerCase()
                    : "unpaid";

            try {
                Connection con = DBConnection.getConnection();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for Invoice #" + existing.getId() + "?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {

                        int inv = existing.getId();

                        String sql = "UPDATE invoice SET "
                                + "shipment_id=?, "
                                + "customer_id=?, "
                                + "freight_rate_id=?, "
                                + "subtotal=?, "
                                + "total_amount=?, "
                                + "status=?, "
                                + "invoice_date=? "
                                + "WHERE invoice_id=?";

                        PreparedStatement ps = con.prepareStatement(sql);

                        ps.setInt(1, shipmentId);
                        ps.setInt(2, customerId);
                        ps.setInt(3, rateId);
                        ps.setDouble(4, subtotal);
                        ps.setString(6, statusStr);
                        ps.setDouble(5, total);
                        ps.setDate(7, invoice_date);
                        ps.setInt(8, existing.getId());

                        ps.executeUpdate();
                        recalculateInvoiceStatus(existing.getId());
                        loadInvoiceListFromDB();
                        con.close();
                        innerContent.setCenter(buildInvoiceListView());
                    }
                } else {
                    String sql = "INSERT INTO invoice (shipment_id, customer_id, freight_rate_id, subtotal, total_amount, status, invoice_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, shipmentId);
                    ps.setInt(2, customerId);
                    ps.setInt(3, rateId);
                    ps.setDouble(4, subtotal);
                    ps.setString(6, statusStr);
                    ps.setDouble(5, total);
                    ps.setDate(7, invoice_date);
                    ps.executeUpdate();

                    System.out.println(sql);
                    loadInvoiceListFromDB();
                    con.close();
                    innerContent.setCenter(buildInvoiceListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Shipment"), shipmentBox,
                AppStyles.formLabel("Customer"), customerBox,
                AppStyles.formLabel("Freight Rate"), rateBox,
                AppStyles.formLabel("Subtotal (" + PESO + ")"), subtotalField,
                AppStyles.formLabel("Invoice Date"), datePicker,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  💳  PAYMENTS — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildPaymentListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        Label title = AppStyles.sectionTitle("Payments");

        // Info hint about "log payment"
        Label hint = new Label(
                "Logging a payment records money received against an outstanding invoice. "
                + "Invoice status updates automatically: Unpaid → Partially Paid → Paid."
        );
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.web("#555555"));
        hint.setWrapText(true);
        hint.setStyle(
                "-fx-background-color: #E3F2FD; -fx-background-radius: 8; "
                + "-fx-padding: 10 14 10 14;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logBtn = new Button("➕  Log Payment");
        logBtn.setPrefHeight(40);
        logBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(logBtn, false);
        logBtn.setVisible(Permission.canAdd("billings"));
        logBtn.setManaged(Permission.canAdd("billings"));
        logBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(logBtn, true));
        logBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(logBtn, false));
        logBtn.setOnAction(e -> {
            if (invoiceList.isEmpty()) {
                showWarn("No Invoices", "No invoices available.",
                        "Create an invoice first before logging a payment.");
                return;
            }
            innerContent.setCenter(buildLogPaymentForm(null));
        });

        HBox header = new HBox(12, title, spacer, logBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = searchField("Search by Payment ID, Invoice ID, or Receipt No...");
        ObservableList<Payment> filtered = FXCollections.observableArrayList(paymentList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(paymentList.filtered(p
                    -> String.valueOf(p.getId()).contains(q)
                    || String.valueOf(p.getInvoiceId()).contains(q)
                    || p.getReceiptNumber().toLowerCase().contains(q)));
        });

        if (paymentList.isEmpty()) {
            content.getChildren().addAll(header, hint, search,
                    emptyLabel("No payments logged yet."));
        } else {
            TableView<Payment> table = buildPaymentTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, hint, search, table);
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    private TableView<Payment> buildPaymentTable(ObservableList<Payment> data) {
        TableView<Payment> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(420);

        TableColumn<Payment, Integer> idCol = col("Payment ID", "id");
        TableColumn<Payment, Integer> invCol = col("Invoice ID", "invoiceId");
        TableColumn<Payment, String> recCol = col("Receipt No.", "receiptNumber");
        idCol.setPrefWidth(100);
        invCol.setPrefWidth(95);
        recCol.setPrefWidth(140);

        TableColumn<Payment, Double> amtCol = new TableColumn<>("Amount Paid");
        amtCol.setPrefWidth(130);
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        amtCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : PESO + String.format("%,.2f", v));
            }
        });

        TableColumn<Payment, LocalDate> dateCol = new TableColumn<>("Payment Date");
        dateCol.setPrefWidth(130);
        dateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        dateCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.format(DATE_FMT));
            }
        });

        // Actions: Print Receipt | Delete
        TableColumn<Payment, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(130);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button printBtn = printButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, printBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                deleteBtn.setVisible(Permission.canDelete("billings"));
                deleteBtn.setManaged(Permission.canDelete("billings"));
                printBtn.setVisible(Permission.canEdit("billings"));
                printBtn.setManaged(Permission.canEdit("billings"));
            }

            {
                printBtn.setOnAction(e -> {
                    Payment p = getTableRow().getItem();
                    if (p != null) {
                        showReceiptDialog(p);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Payment p = getTableRow().getItem();
                    if (p == null) {
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Payment");
                    confirm.setHeaderText("Delete Payment #" + p.getId() + "?");
                    confirm.setContentText(
                            "Invoice #" + p.getInvoiceId() + " status will be recalculated.\n"
                            + "This action cannot be undone.");

                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement("DELETE FROM payment WHERE payment_id = ?");
                                ps.setInt(1, p.getId());

                                System.out.println(ps);
                                System.out.println(p.getId());
                                ps.executeUpdate();

                                loadPaymentListFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            innerContent.setCenter(buildPaymentListView());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, invCol, recCol, amtCol, dateCol, actCol);
        table.setPlaceholder(new Label("No records found."));
        return table;
    }

    // ── Log Payment form ────────────────────────────────────────────────────
    private ScrollPane buildLogPaymentForm(Invoice preSelected) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(720);

        Label pageTitle = new Label("Log Payment");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        // Invoice selector — only unpaid / partially paid
        ObservableList<Invoice> openInvoices = invoiceList.filtered(
                inv -> inv.getStatus() != Invoice.Status.PAID);

        ComboBox<Invoice> invoiceBox = new ComboBox<>(openInvoices);
        invoiceBox.setPromptText("Select invoice...");
        invoiceBox.setPrefWidth(Double.MAX_VALUE);
        invoiceBox.setPrefHeight(40);
        invoiceBox.setStyle(AppStyles.comboStyle());
        invoiceBox.setCellFactory(lv -> invoiceCell());
        invoiceBox.setButtonCell(invoiceCell());
        if (preSelected != null) {
            invoiceBox.setValue(preSelected);
        }

        // Balance display — updates when invoice selected
        Label balanceLabel = new Label("Outstanding balance: —");
        balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        balanceLabel.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        invoiceBox.setOnAction(e -> {
            Invoice inv = invoiceBox.getValue();
            if (inv != null) {
                double paid = paymentList.stream()
                        .filter(p -> p.getInvoiceId() == inv.getId())
                        .mapToDouble(Payment::getAmountPaid).sum();
                double balance = inv.getTotalAmount() - paid;
                balanceLabel.setText("Outstanding balance: "
                        + PESO + String.format("%,.2f", Math.max(0, balance)));
            }
        });

        TextField amountField = AppStyles.formField(PESO + " 0.00", "");
        TextField receiptField = AppStyles.formField(
                "Auto-generated if left blank e.g. REC-0001", "");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);
        datePicker.setPrefHeight(40);

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildPaymentListView()));

        Button saveBtn = new Button("Log Payment");
        saveBtn.setPrefSize(140, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            Invoice inv = invoiceBox.getValue();
            String amtStr = amountField.getText().trim();
            LocalDate date = datePicker.getValue();
            String receipt = receiptField.getText().trim();

            if (inv == null || amtStr.isEmpty() || date == null) {
                AppStyles.showError(errorLabel, "Please fill in all required fields.");
                return;
            }

            int invoice_id = inv.getId();
            double amount;
            java.sql.Date payment_date = java.sql.Date.valueOf(date);

            try {
                amount = Double.parseDouble(amtStr);
                Invoice selectedInv = invoiceBox.getValue();
                double totalPaid = paymentList.stream()
                        .filter(p -> p.getInvoiceId() == selectedInv.getId())
                        .mapToDouble(Payment::getAmountPaid).sum();
                double balance = selectedInv.getTotalAmount() - totalPaid;

                if (amount > balance) {
                    AppStyles.showError(errorLabel,
                            String.format("Amount exceeds outstanding balance of %s%,.2f", PESO, balance));
                    return;
                }

                if (amount <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel, "Amount must be a positive number.");
                return;
            }

            // Auto-generate receipt number if blank
            if (receipt.isEmpty()) {
                try (Connection con = DBConnection.getConnection()) {
                    ResultSet rs = con.createStatement()
                            .executeQuery("SELECT COALESCE(MAX(payment_id), 0) + 1 AS next_id FROM payment");
                    int nextId = rs.next() ? rs.getInt("next_id") : 1;
                    receipt = "REC-" + String.format("%04d", nextId);
                } catch (SQLException ex) {
                    receipt = "REC-" + System.currentTimeMillis(); // fallback
                }
            }

            try {
                Connection con = DBConnection.getConnection();
                String sql = "INSERT INTO payment (invoice_id, amount_paid, payment_date, receipt_number) VALUES (?, ?, ?, ?)";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, invoice_id);
                ps.setDouble(2, amount);
                ps.setDate(3, payment_date);
                ps.setString(4, receipt);

                ps.executeUpdate();

                System.out.println(sql);
                loadPaymentListFromDB();
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            /*Payment payment = new Payment(inv.getId(), amount, date, receipt);
                paymentList.add(payment);*/
            recalculateInvoiceStatus(inv.getId());
            innerContent.setCenter(buildPaymentListView());
        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Invoice"), invoiceBox,
                balanceLabel,
                AppStyles.formLabel("Amount Paid (" + PESO + ")"), amountField,
                AppStyles.formLabel("Payment Date"), datePicker,
                AppStyles.formLabel("Receipt Number (optional)"), receiptField,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ── Official Receipt dialog ─────────────────────────────────────────────
    private void showReceiptDialog(Payment payment) {
        Invoice inv = invoiceById(payment.getInvoiceId());
        String customerName = inv == null ? "—"
                : customerList.stream().filter(c -> c.getId() == inv.getCustomerId())
                        .findFirst().map(Customer::getFullName).orElse("Customer #" + inv.getCustomerId());

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Official Receipt");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dlg.getDialogPane().lookupButton(ButtonType.OK)).setText("🖨  Print");
        ((Button) dlg.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Close");

        // Receipt layout
        VBox receipt = buildReceiptNode(payment, inv, customerName);
        receipt.setStyle(
                "-fx-background-color: white; -fx-border-color: #cccccc; "
                + "-fx-border-width: 1; -fx-padding: 0;"
        );

        VBox wrapper = new VBox(receipt);
        wrapper.setPadding(new Insets(20));
        wrapper.setStyle("-fx-background-color: #f0f0f0;");
        dlg.getDialogPane().setContent(wrapper);

        dlg.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                printNode(receipt);
            }
        });
    }

    private VBox buildReceiptNode(Payment payment, Invoice inv, String customerName) {
        VBox receipt = new VBox(8);
        receipt.setPadding(new Insets(30, 40, 30, 40));
        receipt.setPrefWidth(420);
        receipt.setStyle("-fx-background-color: white;");

        // Header
        Label company = new Label("ANCHOR");
        company.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        company.setTextFill(Color.web(AppStyles.NAVY_BLUE));
        company.setMaxWidth(Double.MAX_VALUE);
        company.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Cargo Handling and Operations Records");
        subtitle.setFont(Font.font("Arial", 11));
        subtitle.setTextFill(Color.web("#555555"));
        subtitle.setMaxWidth(Double.MAX_VALUE);
        subtitle.setAlignment(Pos.CENTER);

        Label receiptTitle = new Label("OFFICIAL RECEIPT");
        receiptTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        receiptTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));
        receiptTitle.setMaxWidth(Double.MAX_VALUE);
        receiptTitle.setAlignment(Pos.CENTER);
        receiptTitle.setPadding(new Insets(8, 0, 8, 0));

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        // Details
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 10, 0));
        ColumnConstraints c1 = new ColumnConstraints(150);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        addReceiptRow(grid, 0, "Receipt No.", payment.getReceiptNumber());
        addReceiptRow(grid, 1, "Payment Date", payment.getPaymentDate().format(DATE_FMT));
        addReceiptRow(grid, 2, "Invoice No.", String.valueOf(payment.getInvoiceId()));
        addReceiptRow(grid, 3, "Customer", customerName);
        if (inv != null) {
            addReceiptRow(grid, 4, "Shipment ID", String.valueOf(inv.getShipmentId()));
            addReceiptRow(grid, 5, "Invoice Total",
                    PESO + String.format("%,.2f", inv.getTotalAmount()));
        }

        // Amount — large
        Label amtLabel = new Label(PESO + String.format("%,.2f", payment.getAmountPaid()));
        amtLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        amtLabel.setTextFill(Color.web("#388E3C"));
        amtLabel.setMaxWidth(Double.MAX_VALUE);
        amtLabel.setAlignment(Pos.CENTER);
        amtLabel.setPadding(new Insets(12, 0, 12, 0));

        Label amtCaption = new Label("AMOUNT RECEIVED");
        amtCaption.setFont(Font.font("Arial", 11));
        amtCaption.setTextFill(Color.GRAY);
        amtCaption.setMaxWidth(Double.MAX_VALUE);
        amtCaption.setAlignment(Pos.CENTER);

        Label footer = new Label("Thank you for your payment.");
        footer.setFont(Font.font("Arial", 11));
        footer.setTextFill(Color.GRAY);
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 0, 0, 0));

        receipt.getChildren().addAll(
                company, subtitle, sep1, receiptTitle,
                grid, sep2, amtCaption, amtLabel, footer
        );
        return receipt;
    }

    private void addReceiptRow(GridPane grid, int row, String key, String value) {
        Label k = new Label(key + ":");
        k.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        k.setTextFill(Color.web("#555555"));
        Label v = new Label(value);
        v.setFont(Font.font("Arial", 12));
        v.setTextFill(Color.web("#222222"));
        grid.add(k, 0, row);
        grid.add(v, 1, row);
    }

    private void printNode(Node node) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean proceed = job.showPrintDialog(null);
            if (proceed && job.printPage(node)) {
                job.endJob();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  📦  FREIGHT RATES — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildFreightRateListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        Label title = AppStyles.sectionTitle("Freight Rates");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add Freight Rate");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setVisible(Permission.canAdd("billings"));
        addBtn.setManaged(Permission.canAdd("billings"));
        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> innerContent.setCenter(buildFreightRateForm(null)));

        HBox header = new HBox(12, title, spacer, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = searchField("Search freight rates...");
        ObservableList<FreightRate> filtered = FXCollections.observableArrayList(freightRateList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(freightRateList.filtered(r
                    -> r.getName().toLowerCase().contains(q)
                    || String.valueOf(r.getId()).contains(q)));
        });

        if (freightRateList.isEmpty()) {
            content.getChildren().addAll(header, emptyLabel("No freight rates added yet."));
        } else {
            TableView<FreightRate> table = buildFreightRateTable(filtered); // use filtered
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, search, table); // add search
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    private TableView<FreightRate> buildFreightRateTable(ObservableList<FreightRate> data) {
        TableView<FreightRate> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);

        TableColumn<FreightRate, Integer> idCol = col("Rate ID", "id");
        TableColumn<FreightRate, String> nameCol = col("Name", "name");
        idCol.setPrefWidth(90);

        TableColumn<FreightRate, Double> amtCol = new TableColumn<>("Base Amount");
        amtCol.setPrefWidth(160);
        amtCol.setCellValueFactory(new PropertyValueFactory<>("baseAmount"));
        amtCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : PESO + String.format("%,.3f", v));
            }
        });

        TableColumn<FreightRate, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(110);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("billings"));
                editBtn.setManaged(Permission.canEdit("billings"));
                deleteBtn.setVisible(Permission.canDelete("billings"));
                deleteBtn.setManaged(Permission.canDelete("billings"));
            }

            {
                editBtn.setOnAction(e
                        -> innerContent.setCenter(buildFreightRateForm(getTableRow().getItem())));

                deleteBtn.setOnAction(e -> {
                    FreightRate fr = getTableRow().getItem();
                    if (fr == null) {
                        return;
                    }
                    boolean used = invoiceList.stream()
                            .anyMatch(inv -> inv.getFreightRateId() == fr.getId());
                    if (used) {
                        showWarn("Cannot Delete",
                                "\"" + fr.getName() + "\" is used in existing invoices.",
                                "Remove all invoices referencing this rate before deleting.");
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Freight Rate");
                    confirm.setHeaderText("Delete \"" + fr.getName() + "\"?");
                    confirm.setContentText("This action cannot be undone.");

                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement("DELETE FROM freight_rate WHERE rate_id = ?");
                                ps.setInt(1, fr.getId());

                                System.out.println(ps);
                                System.out.println(fr.getId());
                                ps.executeUpdate();

                                loadFreightRateListFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            innerContent.setCenter(buildFreightRateListView());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, amtCol, actCol);
        table.setPlaceholder(new Label("No records found."));
        return table;
    }

    private ScrollPane buildFreightRateForm(FreightRate existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(600);

        Label pageTitle = new Label(isEdit ? "Edit Freight Rate" : "Add Freight Rate");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        TextField nameField = AppStyles.formField("Rate name e.g. Standard LCL",
                isEdit ? existing.getName() : "");
        TextField amountField = AppStyles.formField(PESO + " 0.000",
                isEdit ? String.format("%.3f", existing.getBaseAmount()) : "");

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildFreightRateListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Rate");
        saveBtn.setPrefSize(130, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String amtStr = amountField.getText().trim();
            if (name.isEmpty() || amtStr.isEmpty()) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amtStr);
                if (amount < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel, "Base amount must be a valid positive number.");
                return;
            }

            try {
                Connection con = DBConnection.getConnection();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for \"" + existing.getName() + "\"?");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        String sql = "UPDATE freight_rate SET name=?, base_amount=? WHERE rate_id=?";

                        PreparedStatement ps = con.prepareStatement(sql);

                        ps.setString(1, name);
                        ps.setDouble(2, amount);
                        ps.setInt(3, existing.getId());

                        ps.executeUpdate();
                        loadFreightRateListFromDB();
                        con.close();
                        innerContent.setCenter(buildFreightRateListView());
                    }
                } else {
                    String sql = "INSERT INTO freight_rate (name, base_amount) VALUES (?, ?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setDouble(2, amount);

                    ps.executeUpdate();

                    System.out.println(sql);
                    loadFreightRateListFromDB();
                    con.close();
                    innerContent.setCenter(buildFreightRateListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Rate Name"), nameField,
                AppStyles.formLabel("Base Amount (" + PESO + ")"), amountField,
                errorLabel, btnRow
        );
        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ➕  SURCHARGES — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildSurchargeListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        Label title = AppStyles.sectionTitle("Surcharges");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add Surcharge");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setVisible(Permission.canAdd("billings"));
        addBtn.setManaged(Permission.canAdd("billings"));
        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> innerContent.setCenter(buildSurchargeForm(null)));

        HBox header = new HBox(12, title, spacer, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = searchField("Search surcharges...");
        ObservableList<Surcharge> filtered = FXCollections.observableArrayList(surchargeList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(surchargeList.filtered(s
                    -> s.getName().toLowerCase().contains(q)
                    || String.valueOf(s.getId()).contains(q)));
        });

        Label hint = new Label(
                "Surcharges are additional charges (e.g. Fuel Surcharge, Handling Fee, Customs Fee) "
                + "that can be applied to any invoice from the 🧾 Invoices tab."
        );
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.web("#555555"));
        hint.setWrapText(true);
        hint.setStyle(
                "-fx-background-color: #FFF8E1; -fx-background-radius: 8; "
                + "-fx-padding: 10 14 10 14;"
        );

        if (surchargeList.isEmpty()) {
            content.getChildren().addAll(header, hint, emptyLabel("No surcharges defined yet."));
        } else {
            TableView<Surcharge> table = buildSurchargeTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, hint, search, table); // add search
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    private TableView<Surcharge> buildSurchargeTable(ObservableList<Surcharge> data) {
        TableView<Surcharge> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);

        TableColumn<Surcharge, Integer> idCol = col("Surcharge ID", "id");
        TableColumn<Surcharge, String> nameCol = col("Name", "name");
        idCol.setPrefWidth(110);

        TableColumn<Surcharge, Double> amtCol = new TableColumn<>("Default Amount");
        amtCol.setPrefWidth(160);
        amtCol.setCellValueFactory(new PropertyValueFactory<>("defaultAmount"));
        amtCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : PESO + String.format("%,.3f", v));
            }
        });

        TableColumn<Surcharge, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(110);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("billings"));
                editBtn.setManaged(Permission.canEdit("billings"));
                deleteBtn.setVisible(Permission.canDelete("billings"));
                deleteBtn.setManaged(Permission.canDelete("billings"));
            }

            {
                editBtn.setOnAction(e
                        -> innerContent.setCenter(buildSurchargeForm(getTableRow().getItem())));

                deleteBtn.setOnAction(e -> {
                    Surcharge s = getTableRow().getItem();
                    if (s == null) {
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Surcharge");
                    confirm.setHeaderText("Delete \"" + s.getName() + "\"?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement("DELETE FROM surcharge WHERE surcharge_id = ?");
                                ps.setInt(1, s.getId());

                                System.out.println(ps);
                                System.out.println(s.getId());
                                ps.executeUpdate();

                                loadSurchargeListFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            innerContent.setCenter(buildSurchargeListView());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, amtCol, actCol);
        table.setPlaceholder(new Label("No records found."));
        return table;
    }

    private ScrollPane buildSurchargeForm(Surcharge existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(600);

        Label pageTitle = new Label(isEdit ? "Edit Surcharge" : "Add Surcharge");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        TextField nameField = AppStyles.formField("e.g. Fuel Surcharge, Handling Fee",
                isEdit ? existing.getName() : "");
        TextField amountField = AppStyles.formField(PESO + " 0.000",
                isEdit ? String.format("%.3f", existing.getDefaultAmount()) : "");

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildSurchargeListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Surcharge");
        saveBtn.setPrefSize(145, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String amtStr = amountField.getText().trim();
            if (name.isEmpty() || amtStr.isEmpty()) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amtStr);
                if (amount < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel, "Default amount must be a valid positive number.");
                return;
            }

            try {
                Connection con = DBConnection.getConnection();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for \"" + existing.getName() + "\"?");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        String sql = "UPDATE surcharge SET "
                                + "name=?, "
                                + "default_amount=? "
                                + "WHERE surcharge_id=?";

                        PreparedStatement ps = con.prepareStatement(sql);

                        ps.setString(1, name);
                        ps.setDouble(2, amount);
                        ps.setInt(3, existing.getId());

                        ps.executeUpdate();
                        loadSurchargeListFromDB();
                        con.close();
                        innerContent.setCenter(buildSurchargeListView());
                    }
                } else {
                    String sql = "INSERT INTO surcharge (name, default_amount) VALUES (?, ?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setDouble(2, amount);
                    ps.executeUpdate();

                    System.out.println(sql);
                    loadSurchargeListFromDB();
                    con.close();
                    innerContent.setCenter(buildSurchargeListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Surcharge Name"), nameField,
                AppStyles.formLabel("Default Amount (" + PESO + ")"), amountField,
                errorLabel, btnRow
        );
        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  📋  CARGO MANIFEST
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildCargoManifestView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        selectedForPrint.clear();

        Label title = AppStyles.sectionTitle("Cargo Manifest");
        Label hint = new Label(
                "Showing all cargo currently on board. Use the filters to narrow by vessel or shipment."
        );
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.web("#555555"));
        hint.setWrapText(true);

        // ── Vessel ComboBox ──────────────────────────────────────────────────
        ComboBox<Vessel> vesselBox = new ComboBox<>();
        vesselBox.getItems().add(null);
        vesselBox.getItems().addAll(vesselList);
        vesselBox.setPrefWidth(200);
        vesselBox.setPrefHeight(38);
        vesselBox.setStyle(AppStyles.comboStyle());
        vesselBox.setCellFactory(lv -> new ListCell<Vessel>() {
            @Override
            protected void updateItem(Vessel v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : (v == null ? "All Vessels" : v.getName()));
            }
        });
        vesselBox.setButtonCell(new ListCell<Vessel>() {
            @Override
            protected void updateItem(Vessel v, boolean empty) {
                super.updateItem(v, empty);
                setText(v == null ? "All Vessels" : v.getName());
            }
        });
        vesselBox.setValue(null);

// ── Shipment ComboBox ────────────────────────────────────────────────
        ComboBox<Shipment> shipmentBox = new ComboBox<>();
        shipmentBox.getItems().add(null);
        shipmentBox.getItems().addAll(shipmentList);
        shipmentBox.setPrefWidth(260);
        shipmentBox.setPrefHeight(38);
        shipmentBox.setStyle(AppStyles.comboStyle());
        shipmentBox.setCellFactory(lv -> new ListCell<Shipment>() {
            @Override
            protected void updateItem(Shipment s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : (s == null ? "All Shipments"
                        : "Shipment #" + s.getId() + " (Booking #" + s.getBookingId() + ")"));
            }
        });
        shipmentBox.setButtonCell(new ListCell<Shipment>() {
            @Override
            protected void updateItem(Shipment s, boolean empty) {
                super.updateItem(s, empty);
                setText(s == null ? "All Shipments"
                        : "Shipment #" + s.getId() + " (Booking #" + s.getBookingId() + ")");
            }
        });
        shipmentBox.setValue(null);

        CheckBox selectAll = new CheckBox("Select All");
        selectAll.setFont(Font.font("Arial", 12));

        Button printBtn = new Button("🖨  Print / Export Manifest");
        printBtn.setPrefHeight(38);
        printBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(printBtn, false);
        printBtn.setVisible(Permission.canEdit("billings"));
        printBtn.setManaged(Permission.canEdit("billings"));
        printBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(printBtn, true));
        printBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(printBtn, false));

        ObservableList<ManifestRow> allRows = buildAllManifestRows(shipmentList);
        ObservableList<ManifestRow> filtered = FXCollections.observableArrayList(allRows);

        TableView<ManifestRow> table = buildManifestTable(filtered);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label statsLabel = new Label("Total cargo entries: " + allRows.size());
        statsLabel.setFont(Font.font("Arial", 12));
        statsLabel.setTextFill(Color.web("#666666"));

        selectAll.setOnAction(e -> {
            if (selectAll.isSelected()) {
                selectedForPrint.addAll(filtered);
            } else {
                selectedForPrint.clear();
            }
            table.refresh();
        });

        vesselBox.setOnAction(e -> {
            Vessel v = vesselBox.getValue();
            selectedForPrint.clear();
            selectAll.setSelected(false);

            shipmentBox.getItems().clear();
            shipmentBox.getItems().add(null);
            if (v == null) {
                shipmentBox.getItems().addAll(shipmentList);
            } else {
                shipmentBox.getItems().addAll(
                        shipmentList.filtered(s -> s.getVesselId() == v.getId())
                );
            }

            // ← Reset button cell after repopulating
            shipmentBox.setButtonCell(new ListCell<Shipment>() {
                @Override
                protected void updateItem(Shipment s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(s == null ? "All Shipments"
                            : "Shipment #" + s.getId() + " (Booking #" + s.getBookingId() + ")");
                }
            });
            shipmentBox.setValue(null);

            applyManifestFilter(allRows, filtered, v, null);
            statsLabel.setText("Total cargo entries: " + filtered.size());
            table.refresh();
        });

        shipmentBox.setOnAction(e -> {
            selectedForPrint.clear();
            selectAll.setSelected(false);
            applyManifestFilter(allRows, filtered, vesselBox.getValue(), shipmentBox.getValue());
            statsLabel.setText("Total cargo entries: " + filtered.size());
            table.refresh();
        });

        printBtn.setOnAction(e -> {
            if (selectedForPrint.isEmpty()) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Nothing Selected");
                warn.setHeaderText("No rows selected for printing.");
                warn.setContentText("Please check at least one row before printing.");
                warn.showAndWait();
                return;
            }
            ObservableList<ManifestRow> toPrint
                    = FXCollections.observableArrayList(selectedForPrint);
            VBox printable = buildAllManifestPrintNode(toPrint);
            printNode(printable);
        });

        HBox filterRow = new HBox(16, vesselBox, shipmentBox, selectAll, printBtn);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(title, hint, filterRow, statsLabel, table);

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE
                + "; -fx-background: " + AppStyles.LIGHT_BLUE + ";");

        VBox wrapper = new VBox(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return wrapper;
    }

    public static class ManifestRow {

        private final Shipment shipment;
        private final Vessel vessel;
        private final Cargo cargo;

        public ManifestRow(Shipment shipment, Vessel vessel, Cargo cargo) {
            this.shipment = shipment;
            this.vessel = vessel;
            this.cargo = cargo;
        }

        public Shipment getShipment() {
            return shipment;
        }

        public Vessel getVessel() {
            return vessel;
        }

        public Cargo getCargo() {
            return cargo;
        }
    }

    private ObservableList<ManifestRow> buildAllManifestRows(ObservableList<Shipment> ships) {
        ObservableList<ManifestRow> rows = FXCollections.observableArrayList();
        for (Shipment s : ships) {
            Booking booking = bookingList.stream()
                    .filter(b -> b.getId() == s.getBookingId())
                    .findFirst().orElse(null);
            Cargo cargo = booking == null ? null
                    : cargoList.stream()
                            .filter(c -> c.getId() == booking.getCargoId())
                            .findFirst().orElse(null);
            Vessel vessel = vesselById(s.getVesselId());
            rows.add(new ManifestRow(s, vessel, cargo));
        }
        return rows;
    }

    private void applyManifestFilter(
            ObservableList<ManifestRow> all,
            ObservableList<ManifestRow> filtered,
            Vessel vessel, Shipment shipment) {

        filtered.setAll(all.filtered(row -> {
            boolean matchVessel = vessel == null
                    || (row.getVessel() != null && row.getVessel().getId() == vessel.getId());
            boolean matchShipment = shipment == null
                    || row.getShipment().getId() == shipment.getId();
            return matchVessel && matchShipment;
        }));
    }

    private TableView<ManifestRow> buildManifestTable(ObservableList<ManifestRow> data) {
        TableView<ManifestRow> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        // ← STEP 2: Checkbox column — add this FIRST
        TableColumn<ManifestRow, Void> checkCol = new TableColumn<>("");
        checkCol.setPrefWidth(40);
        checkCol.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    ManifestRow row = getTableRow().getItem();
                    if (row == null) {
                        return;
                    }
                    if (checkBox.isSelected()) {
                        selectedForPrint.add(row);
                    } else {
                        selectedForPrint.remove(row);
                    }
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                ManifestRow row = getTableRow().getItem();
                checkBox.setSelected(selectedForPrint.contains(row));
                setGraphic(checkBox);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<ManifestRow, String> shipCol = new TableColumn<>("Shipment ID");
        shipCol.setPrefWidth(100);
        shipCol.setCellValueFactory(row
                -> new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(row.getValue().getShipment().getId())));

        TableColumn<ManifestRow, String> vesselCol = new TableColumn<>("Vessel");
        vesselCol.setPrefWidth(140);
        vesselCol.setCellValueFactory(row
                -> new javafx.beans.property.SimpleStringProperty(
                        row.getValue().getVessel() != null
                        ? row.getValue().getVessel().getName() : "—"));

        TableColumn<ManifestRow, String> cargoCol = new TableColumn<>("Cargo Description");
        cargoCol.setPrefWidth(180);
        cargoCol.setCellValueFactory(row
                -> new javafx.beans.property.SimpleStringProperty(
                        row.getValue().getCargo() != null
                        ? row.getValue().getCargo().getDescription() : "—"));

        TableColumn<ManifestRow, String> weightCol = new TableColumn<>("Weight (kg)");
        weightCol.setPrefWidth(110);
        weightCol.setCellValueFactory(row
                -> new javafx.beans.property.SimpleStringProperty(
                        row.getValue().getCargo() != null
                        ? String.format("%.2f", row.getValue().getCargo().getWeight()) : "—"));

        TableColumn<ManifestRow, String> volCol = new TableColumn<>("Volume (m³)");
        volCol.setPrefWidth(110);
        volCol.setCellValueFactory(row
                -> new javafx.beans.property.SimpleStringProperty(
                        row.getValue().getCargo() != null
                        ? String.format("%.2f", row.getValue().getCargo().getVolume()) : "—"));

        TableColumn<ManifestRow, String> hazCol = new TableColumn<>("Hazardous");
        hazCol.setPrefWidth(90);
        hazCol.setCellValueFactory(row
                -> new javafx.beans.property.SimpleStringProperty(
                        row.getValue().getCargo() != null
                        ? row.getValue().getCargo().getHazardousDisplay() : "—"));

        TableColumn<ManifestRow, String> statusCol = new TableColumn<>("Shipment Status");
        statusCol.setPrefWidth(130);
        statusCol.setCellValueFactory(row
                -> new javafx.beans.property.SimpleStringProperty(
                        row.getValue().getShipment().getStatusDisplay()));

        table.getColumns().addAll(
                checkCol, shipCol, vesselCol, cargoCol, weightCol, volCol, hazCol, statusCol
        );
        table.setPlaceholder(new Label("No cargo found."));
        return table;
    }

    private VBox buildAllManifestPrintNode(ObservableList<ManifestRow> rows) {
        VBox node = new VBox(10);
        node.setPadding(new Insets(30, 40, 30, 40));
        node.setPrefWidth(700);
        node.setStyle("-fx-background-color: white;");

        Label company = new Label("ANCHOR  —  Cargo Manifest");
        company.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        company.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        Label generated = new Label("Generated: " + LocalDate.now().format(DATE_FMT));
        generated.setFont(Font.font("Arial", 11));
        generated.setTextFill(Color.GRAY);

        node.getChildren().addAll(company, new Separator(), generated);

        for (ManifestRow row : rows) {
            node.getChildren().add(new Separator());
            node.getChildren().addAll(
                    AppStyles.infoRow("Shipment ID", String.valueOf(row.getShipment().getId())),
                    AppStyles.infoRow("Vessel", row.getVessel() != null
                            ? row.getVessel().getName() : "—"),
                    AppStyles.infoRow("Cargo", row.getCargo() != null
                            ? row.getCargo().getDescription() : "—"),
                    AppStyles.infoRow("Weight", row.getCargo() != null
                            ? String.format("%.2f kg", row.getCargo().getWeight()) : "—"),
                    AppStyles.infoRow("Status", row.getShipment().getStatusDisplay())
            );
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private TableView<Cargo> buildManifestCargoTable(Cargo cargo) {
        ObservableList<Cargo> data = FXCollections.observableArrayList();
        if (cargo != null) {
            data.add(cargo);
        }

        TableView<Cargo> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(180);

        TableColumn<Cargo, Void> idCol = new TableColumn<>("Cargo ID");
        idCol.setPrefWidth(90);
        idCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(String.valueOf(getTableRow().getItem().getId()));
            }
        });

        TableColumn<Cargo, Void> descCol = new TableColumn<>("Description");
        descCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(getTableRow().getItem().getDescription());
            }
        });

        TableColumn<Cargo, Void> wtCol = new TableColumn<>("Weight (kg)");
        wtCol.setPrefWidth(110);
        wtCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(String.format("%.2f", getTableRow().getItem().getWeight()));
            }
        });

        TableColumn<Cargo, Void> volCol = new TableColumn<>("Volume (m³)");
        volCol.setPrefWidth(110);
        volCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(String.format("%.2f", getTableRow().getItem().getVolume()));
            }
        });

        TableColumn<Cargo, Void> hazCol = new TableColumn<>("Hazardous");
        hazCol.setPrefWidth(90);
        hazCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(getTableRow().getItem().getHazardousDisplay());
            }
        });

        TableColumn<Cargo, Void> imdgCol = new TableColumn<>("IMDG Class");
        imdgCol.setPrefWidth(110);
        imdgCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(getTableRow().getItem().getImdgClass());
            }
        });

        table.getColumns().addAll(idCol, descCol, wtCol, volCol, hazCol, imdgCol);
        if (data.isEmpty()) {
            table.setPlaceholder(new Label("No cargo found for this shipment."));
        }
        return table;
    }

    private VBox buildManifestPrintNode(Shipment s, Vessel vessel,
            Route route, Cargo cargo) {
        VBox node = new VBox(10);
        node.setPadding(new Insets(30, 40, 30, 40));
        node.setPrefWidth(700);
        node.setStyle("-fx-background-color: white;");

        Label company = new Label("ANCHOR  —  Cargo Manifest");
        company.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        company.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        Label generated = new Label("Generated: " + LocalDate.now().format(DATE_FMT));
        generated.setFont(Font.font("Arial", 11));
        generated.setTextFill(Color.GRAY);

        node.getChildren().addAll(company, new Separator(), generated,
                AppStyles.infoRow("Shipment ID", String.valueOf(s.getId())),
                AppStyles.infoRow("Vessel", vessel != null
                        ? vessel.getName() + " (" + vessel.getRegistrationNumber() + ")" : "—"),
                AppStyles.infoRow("Route ID", String.valueOf(s.getRouteId())),
                AppStyles.infoRow("Departure", s.getDepartureDate() != null
                        ? s.getDepartureDate().format(DATE_FMT) : "—"),
                AppStyles.infoRow("Est. Arrival", s.getArrivalDate() != null
                        ? s.getArrivalDate().format(DATE_FMT) : "—"),
                new Separator()
        );

        if (cargo != null) {
            node.getChildren().addAll(
                    AppStyles.infoRow("Cargo ID", String.valueOf(cargo.getId())),
                    AppStyles.infoRow("Description", cargo.getDescription()),
                    AppStyles.infoRow("Weight", String.format("%.2f kg", cargo.getWeight())),
                    AppStyles.infoRow("Volume", String.format("%.2f m³", cargo.getVolume())),
                    AppStyles.infoRow("Hazardous", cargo.getHazardousDisplay()),
                    AppStyles.infoRow("IMDG Class", cargo.getImdgClass()),
                    AppStyles.infoRow("UN Number", cargo.getUnNumber()),
                    AppStyles.infoRow("PSN", cargo.getProperShippingName())
            );
        } else {
            node.getChildren().add(new Label("No cargo information available."));
        }

        return node;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BUSINESS LOGIC HELPERS
    // ══════════════════════════════════════════════════════════════════════
    /**
     * Recalculates and updates the status of an invoice based on total payments
     * logged. Call this after any payment is added or removed.
     */
    private void recalculateInvoiceStatus(int invoiceId) {
        Invoice inv = invoiceById(invoiceId);
        if (inv == null) {
            return;
        }

        double totalPaid = paymentList.stream()
                .filter(p -> p.getInvoiceId() == invoiceId)
                .mapToDouble(Payment::getAmountPaid).sum();

        Invoice.Status newStatus;
        if (totalPaid <= 0) {
            newStatus = Invoice.Status.UNPAID;
        } else if (totalPaid >= inv.getTotalAmount()) {
            newStatus = Invoice.Status.PAID;
        } else {
            newStatus = Invoice.Status.PARTIALLY_PAID;
        }

        inv.setStatus(newStatus);

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE invoice SET status=? WHERE invoice_id=?");
            ps.setString(1, newStatus.name().toLowerCase());
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOOKUP HELPERS
    // ══════════════════════════════════════════════════════════════════════
    private Invoice invoiceById(int id) {
        return invoiceList.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    private Shipment shipmentById(int id) {
        return shipmentList.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    private Customer customerById(int id) {
        return customerList.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private FreightRate freightRateById(int id) {
        return freightRateList.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    private Vessel vesselById(int id) {
        return vesselList.stream().filter(v -> v.getId() == id).findFirst().orElse(null);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  COLOUR HELPERS
    // ══════════════════════════════════════════════════════════════════════
    private String invoiceStatusColor(Invoice.Status s) {
        return switch (s) {
            case UNPAID ->
                "#C62828";  // red
            case PARTIALLY_PAID ->
                "#F57C00";  // orange
            case PAID ->
                "#388E3C";  // green
            };
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CELL FACTORIES
    // ══════════════════════════════════════════════════════════════════════
    private ListCell<Shipment> shipmentCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Shipment s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null
                        : "Shipment #" + s.getId() + "  (Booking #" + s.getBookingId() + ")");
            }
        };
    }

    private ListCell<Customer> customerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null
                        : c.getId() + " — " + c.getFullName());
            }
        };
    }

    private ListCell<Invoice> invoiceCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Invoice inv, boolean empty) {
                super.updateItem(inv, empty);
                setText(empty || inv == null ? null
                        : "Invoice #" + inv.getId()
                        + "  |  " + inv.getStatusDisplay()
                        + "  |  " + PESO + String.format("%,.2f", inv.getTotalAmount()));
            }
        };
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> col(String header, String property) {
        TableColumn<S, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        return c;
    }

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
        sp.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE
                + "; -fx-background: " + AppStyles.LIGHT_BLUE + ";");
        return sp;
    }

    private Button viewButton() {
        Button btn = new Button("\uD83D\uDC41");
        btn.setStyle(
                "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #2E7D32; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        return btn;
    }

    private Button surchargeButton() {
        Button btn = new Button("\u2795");
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

    private Button printButton() {
        Button btn = new Button("\uD83D\uDDA8");
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

    private void showWarn(String title, String header, String body) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(body);
        a.showAndWait();
    }

    public void loadInvoiceListFromDB() {
        invoiceList.clear();

        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM invoice");

            while (rs.next()) {
                Invoice.Status status = Invoice.Status.fromName(rs.getString("status"));
                java.sql.Date invoice_date = rs.getDate("invoice_date");

                Invoice inv = new Invoice(
                        rs.getInt("invoice_id"),
                        rs.getInt("shipment_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("freight_rate_id"),
                        rs.getDouble("subtotal"),
                        rs.getDouble("total_amount"),
                        status,
                        invoice_date != null ? invoice_date.toLocalDate() : null
                );

                // Load applied surcharges for this invoice
                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM invoice_surcharge WHERE invoice_id = ?"
                );
                ps.setInt(1, inv.getId());
                ResultSet srs = ps.executeQuery();
                while (srs.next()) {
                    inv.addSurcharge(new InvoiceSurcharge(
                            srs.getInt("invoice_id"),
                            srs.getInt("surcharge_id"),
                            srs.getDouble("applied_amount")
                    ));
                }

                invoiceList.add(inv);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void loadPaymentListFromDB() {
        paymentList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM payment");

            while (rs.next()) {
                java.sql.Date payment_date = rs.getDate("payment_date");

                paymentList.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("invoice_id"),
                        rs.getDouble("amount_paid"),
                        payment_date != null ? payment_date.toLocalDate() : null,
                        rs.getString("receipt_number")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void loadFreightRateListFromDB() {
        freightRateList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM freight_rate");

            while (rs.next()) {
                freightRateList.add(new FreightRate(
                        rs.getInt("rate_id"),
                        rs.getString("name"),
                        rs.getDouble("base_amount")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void loadSurchargeListFromDB() {
        surchargeList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM surcharge");

            while (rs.next()) {
                surchargeList.add(new Surcharge(
                        rs.getInt("surcharge_id"),
                        rs.getString("name"),
                        rs.getDouble("default_amount")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}

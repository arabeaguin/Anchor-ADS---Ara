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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages Vessels, Crew, Ports, and Routes
 *
 * Integrate with Dashboard.java: Add four ObservableLists to Dashboard: private
 * final ObservableList<Vessel> vesselList =
 * FXCollections.observableArrayList(); private final ObservableList<Crew>
 * crewList = FXCollections.observableArrayList(); private final
 * ObservableList<Port> portList = FXCollections.observableArrayList(); private
 * final ObservableList<Route> routeList = FXCollections.observableArrayList();
 *
 */
public class VesselFleetView {

    /**
     * Minimum crew required to register a vessel. Update as needed.
     */
    private static final int MIN_CREW_PER_VESSEL = 1;

    private final BorderPane root;         // Dashboard's outer BorderPane
    private final BorderPane innerContent; // Content area inside this module (preserves the tab bar)

    private final ObservableList<Vessel> vesselList;
    private final ObservableList<Crew> crewList;
    private final ObservableList<Port> portList;
    private final ObservableList<Route> routeList;
    private final ObservableList<Container> containerList;

    private final Button[] tabButtons = new Button[5];
    private static final String[] TAB_LABELS = {
        "⚓  Vessels", "👥  Crew", "🏭  Ports", "🗺  Routes", "📦  Containers"
    };

    public VesselFleetView(BorderPane root,
            ObservableList<Vessel> vesselList,
            ObservableList<Crew> crewList,
            ObservableList<Port> portList,
            ObservableList<Route> routeList,
            ObservableList<Container> containerList) {
        this.root = root;
        this.innerContent = new BorderPane();
        this.vesselList = vesselList;
        this.crewList = crewList;
        this.portList = portList;
        this.routeList = routeList;
        this.containerList = containerList;
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

        navigateTo(0); // default: Vessels
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
            btn.setPrefWidth(190);
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
            case 0 ->
                innerContent.setCenter(buildVesselListView());
            case 1 ->
                innerContent.setCenter(buildCrewListView());
            case 2 ->
                innerContent.setCenter(buildPortListView());
            case 3 ->
                innerContent.setCenter(buildRouteListView());
            case 4 ->
                innerContent.setCenter(buildContainerListView());
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
    //  ⚓  VESSELS — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildVesselListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        // ── Header ──────────────────────────────────────────────────────────
        Label title = AppStyles.sectionTitle("Vessels");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button registerBtn = new Button("➕  Register New Vessel");
        registerBtn.setPrefHeight(40);
        registerBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(registerBtn, false);
        registerBtn.setVisible(Permission.canAdd("vessels"));
        registerBtn.setManaged(Permission.canAdd("vessels"));
        registerBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(registerBtn, true));
        registerBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(registerBtn, false));
        registerBtn.setOnAction(e -> {
            if (crewList.isEmpty()) {
                showWarn("No Crew Available",
                        "You must add at least one crew member before registering a vessel.",
                        "Go to the 👥 Crew tab first.");
                return;
            }
            innerContent.setCenter(buildVesselFormView(null));
        });

        HBox header = new HBox(12, title, spacer, registerBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Search ───────────────────────────────────────────────────────────
        TextField search = searchField("Search by name, type, or registration...");
        ObservableList<Vessel> filtered = FXCollections.observableArrayList(vesselList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(vesselList.filtered(v
                    -> v.getName().toLowerCase().contains(q)
                    || v.getVesselType().toLowerCase().contains(q)
                    || v.getRegistrationNumber().toLowerCase().contains(q)
                    || v.getStatusDisplay().toLowerCase().contains(q)));
        });

        if (vesselList.isEmpty()) {
            content.getChildren().addAll(header, search, emptyLabel("No vessels registered yet."));
        } else {
            TableView<Vessel> table = buildVesselTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, search, table);
        }
        return content;
    }

    // ── Vessel table ────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Vessel> buildVesselTable(ObservableList<Vessel> data) {
        TableView<Vessel> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Vessel, Integer> idCol = col("Vessel ID", "id");
        TableColumn<Vessel, String> nameCol = col("Name", "name");
        TableColumn<Vessel, String> typeCol = col("Type", "vesselType");
        TableColumn<Vessel, Double> wtCol = col("Cap. Weight (MT)", "capacityWeight");
        TableColumn<Vessel, Double> volCol = col("Cap. Volume (CBM)", "capacityVolume");
        TableColumn<Vessel, String> regCol = col("Reg. No.", "registrationNumber");
        idCol.setPrefWidth(85);
        regCol.setPrefWidth(115);

        // Status — coloured badge
        TableColumn<Vessel, Void> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(145);
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Vessel vessel = getTableRow().getItem();
                Label badge = new Label(vessel.getStatusDisplay());
                badge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle(
                        "-fx-background-radius: 12; -fx-text-fill: white; "
                        + "-fx-background-color: " + statusColor(vessel.getStatus()) + ";"
                );
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Assigned Crew — computed on the fly from crewList
        TableColumn<Vessel, Void> crewCol = new TableColumn<>("Assigned Crew");
        crewCol.setPrefWidth(210);
        crewCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }

                Vessel vessel = getTableRow().getItem();

                String display = vessel.getAssignedCrew().stream()
                        .map(cr -> cr.getName() + " (" + cr.getRole() + ")")
                        .collect(Collectors.joining("\n"));

                setText(display.isEmpty() ? "—" : display);
                setWrapText(true);
                setFont(Font.font("Arial", 12));
            }
        });

        // Actions: Edit | 🔄 Status | Delete
        TableColumn<Vessel, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(175);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button statusBtn = statusButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(6, editBtn, statusBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("vessels"));
                editBtn.setManaged(Permission.canEdit("vessels"));
                statusBtn.setVisible(Permission.canEdit("vessels"));
                statusBtn.setManaged(Permission.canEdit("vessels"));
                deleteBtn.setVisible(Permission.canDelete("vessels"));
                deleteBtn.setManaged(Permission.canDelete("vessels"));
            }

            {
                editBtn.setOnAction(e -> {
                    Vessel v = getTableRow().getItem();
                    if (crewList.isEmpty()) {
                        showWarn("No Crew Available",
                                "Add crew members before editing vessel assignments.", "");
                        return;
                    }
                    innerContent.setCenter(buildVesselFormView(v));
                });

                statusBtn.setOnAction(e -> {
                    Vessel v = getTableRow().getItem();
                    showStatusUpdateDialog(v, table);
                });

                deleteBtn.setOnAction(e -> {
                    Vessel v = getTableRow().getItem();
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Vessel");
                    confirm.setHeaderText("Delete \"" + v.getName() + "\"?");
                    confirm.setContentText(
                            "All crew assigned to this vessel will be unassigned.\n"
                            + "This action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();

                                String deleteCrewSql = "DELETE FROM vessel_crew WHERE vessel_id = ?";
                                PreparedStatement crewPs = con.prepareStatement(deleteCrewSql);
                                crewPs.setInt(1, v.getId());
                                crewPs.executeUpdate();

                                String sql = "DELETE FROM vessel WHERE vessel_id=?";
                                PreparedStatement ps = con.prepareStatement(sql);
                                ps.setInt(1, v.getId());
                                ps.executeUpdate();

                                loadVesselListFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }

                            innerContent.setCenter(buildVesselListView());
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

        table.getColumns().addAll(idCol, nameCol, typeCol, wtCol, volCol, statusCol, regCol, crewCol, actCol);
        return table;
    }

    // ── Update Status dialog ────────────────────────────────────────────────
    private void showStatusUpdateDialog(Vessel vessel, TableView<Vessel> table) {
        ChoiceDialog<String> dlg = new ChoiceDialog<>(
                vessel.getStatusDisplay(),
                "Active", "Docked", "Under Maintenance"
        );
        dlg.setTitle("Update Vessel Status");
        dlg.setHeaderText("Vessel: " + vessel.getName());
        dlg.setContentText("Select new status:");
        dlg.showAndWait().ifPresent(choice -> {
            vessel.setStatus(Vessel.Status.fromDisplay(choice));
            table.refresh();
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ⚓  VESSELS — register / edit form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildVesselFormView(Vessel existing) {
        boolean isEdit = (existing != null);
        int existingId = isEdit ? existing.getId() : -1;

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(720);

        Label pageTitle = new Label(isEdit ? "Edit Vessel" : "Register New Vessel");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        // ── Basic fields ─────────────────────────────────────────────────────
        TextField nameField = AppStyles.formField("e.g. MV Anchor Pride",
                isEdit ? existing.getName() : "");
        TextField wtField = AppStyles.formField("e.g. 5000.000",
                isEdit ? String.valueOf(existing.getCapacityWeight()) : "");
        TextField volField = AppStyles.formField("e.g. 8500.000",
                isEdit ? String.valueOf(existing.getCapacityVolume()) : "");
        TextField regField = AppStyles.formField("e.g. PHL-00001",
                isEdit ? existing.getRegistrationNumber() : "");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
                "Cargo Ship", "Container Ship", "Bulk Carrier",
                "Tanker", "Roll-on/Roll-off (RoRo)", "General Cargo", "Other"
        );
        typeBox.setPromptText("Select vessel type...");
        typeBox.setPrefWidth(Double.MAX_VALUE);
        typeBox.setPrefHeight(40);
        typeBox.setStyle(AppStyles.comboStyle());
        if (isEdit) {
            typeBox.setValue(existing.getVesselType());
        }

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Docked", "Under Maintenance");
        statusBox.setPrefWidth(Double.MAX_VALUE);
        statusBox.setPrefHeight(40);
        statusBox.setStyle(AppStyles.comboStyle());
        statusBox.setValue(isEdit ? existing.getStatusDisplay() : "Active");

        // ── Crew selector ────────────────────────────────────────────────────
        Label crewSectionLbl = new Label(
                "Assign Crew  (minimum " + MIN_CREW_PER_VESSEL + " required)");
        crewSectionLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        crewSectionLbl.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        Label crewHint = new Label("Hold [ Ctrl ] to select multiple crew members.");
        crewHint.setFont(Font.font("Arial", 12));
        crewHint.setTextFill(Color.GRAY);

        ListView<Crew> crewSelector = new ListView<>(crewList);
        crewSelector.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        crewSelector.setPrefHeight(160);
        crewSelector.setStyle(
                "-fx-border-color: #b0b8c9; -fx-border-radius: 8; -fx-background-radius: 8;"
        );
        crewSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Crew c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                    return;
                }

                // Warn if assigned to a DIFFERENT vessel
                boolean assignedToThisVessel = false;

                if (isEdit) {
                    Vessel currentVessel = existing;

                    assignedToThisVessel = currentVessel.getAssignedCrew()
                            .stream()
                            .anyMatch(vc -> vc.getId() == c.getId());
                }

                String suffix = assignedToThisVessel ? " (Assigned)" : "";
                setText(c.getName() + " — " + c.getRole() + suffix);
                setTextFill(assignedToThisVessel ? Color.web("#E65100") : Color.BLACK);
            }
        });

        // Pre-select crew already on this vessel
        if (isEdit) {
            Vessel currentVessel = existing;

            for (int i = 0; i < crewList.size(); i++) {
                Crew c = crewList.get(i);

                boolean isAssigned = currentVessel.getAssignedCrew()
                        .stream()
                        .anyMatch(vc -> vc.getId() == c.getId());

                if (isAssigned) {
                    crewSelector.getSelectionModel().select(i);
                }
            }
        }

        Label errorLabel = AppStyles.errorLabel();

        // ── Buttons ──────────────────────────────────────────────────────────
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildVesselListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Register Vessel");
        saveBtn.setPrefSize(155, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String wtStr = wtField.getText().trim();
            String volStr = volField.getText().trim();
            String reg = regField.getText().trim();
            String type = typeBox.getValue();
            String status = statusBox.getValue();
            List<Crew> selectedCrew
                    = new ArrayList<>(crewSelector.getSelectionModel().getSelectedItems());

            if (name.isEmpty() || wtStr.isEmpty() || volStr.isEmpty()
                    || reg.isEmpty() || type == null || status == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }
            double weight, volume;
            try {
                weight = Double.parseDouble(wtStr);
                volume = Double.parseDouble(volStr);
                if (weight <= 0 || volume <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel,
                        "Capacity Weight and Volume must be positive numbers.");
                return;
            }
            if (selectedCrew.size() < MIN_CREW_PER_VESSEL) {
                AppStyles.showError(errorLabel,
                        "Please assign at least " + MIN_CREW_PER_VESSEL + " crew member(s).");
                return;
            }

            Vessel.Status vstatus = Vessel.Status.fromDisplay(status);

            try {
                Connection con = DBConnection.getConnection();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for \"" + existing.getName() + "\"?");
                    Optional<ButtonType> result = confirm.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        String sql = "UPDATE vessel SET name=?, vessel_type=?, capacity_weight=?, capacity_volume=?, status=?, registration_number=? "
                                + "WHERE vessel_id=?";

                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, name);
                        ps.setString(2, type);
                        ps.setDouble(3, weight);
                        ps.setDouble(4, volume);
                        ps.setString(5, vstatus.toDbValue());
                        ps.setString(6, reg);
                        ps.setInt(7, existing.getId());
                        ps.executeUpdate();
                        loadVesselListFromDB();

                        con.close();
                        innerContent.setCenter(buildVesselListView());
                    }
                } else {
                    String sql = "INSERT INTO vessel (name, vessel_type, capacity_weight, capacity_volume, status, registration_number)"
                            + "VALUES(?,?,?,?,?,?)";

                    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, name);
                    ps.setString(2, type);
                    ps.setDouble(3, weight);
                    ps.setDouble(4, volume);
                    ps.setString(5, vstatus.toDbValue());
                    ps.setString(6, reg);

                    ps.executeUpdate();

                    ResultSet rs = ps.getGeneratedKeys();
                    int vesselId = -1;

                    if (rs.next()) {
                        vesselId = rs.getInt(1);
                    }

                    String vcSql = "INSERT INTO vessel_crew (vessel_id, crew_id) VALUES (?, ?)";
                    PreparedStatement vcPs = con.prepareStatement(vcSql);

                    for (Crew c : selectedCrew) {
                        vcPs.setInt(1, vesselId);
                        vcPs.setInt(2, c.getId());
                        vcPs.addBatch();
                    }

                    vcPs.executeBatch();
                    loadVesselListFromDB();

                    con.close();
                    innerContent.setCenter(buildVesselListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Vessel Name"), nameField,
                AppStyles.formLabel("Vessel Type"), typeBox,
                AppStyles.formLabel("Capacity Weight (MT)"), wtField,
                AppStyles.formLabel("Capacity Volume (CBM)"), volField,
                AppStyles.formLabel("Status"), statusBox,
                AppStyles.formLabel("Registration Number"), regField,
                crewSectionLbl, crewHint, crewSelector,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  👥  CREW — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildCrewListView() {
        System.out.println("Crew List View has been built.");
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        Label title = AppStyles.sectionTitle("Crew Members");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // "Assign Crew to Vessel" button — sits next to Add Crew Member
        Button assignBtn = new Button("🔗  Assign to Vessel");
        assignBtn.setPrefHeight(40);
        assignBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        assignBtn.setStyle(
                "-fx-background-color: white; -fx-text-fill: " + AppStyles.NAVY_BLUE + "; "
                + "-fx-border-color: " + AppStyles.NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; "
                + "-fx-padding: 0 14 0 14;"
        );
        assignBtn.setOnMouseEntered(e -> assignBtn.setStyle(
                "-fx-background-color: " + AppStyles.NAVY_BLUE + "; -fx-text-fill: white; "
                + "-fx-border-color: " + AppStyles.NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; "
                + "-fx-padding: 0 14 0 14;"
        ));
        assignBtn.setOnMouseExited(e -> assignBtn.setStyle(
                "-fx-background-color: white; -fx-text-fill: " + AppStyles.NAVY_BLUE + "; "
                + "-fx-border-color: " + AppStyles.NAVY_BLUE + "; -fx-border-width: 2; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; "
                + "-fx-padding: 0 14 0 14;"
        ));
        assignBtn.setVisible(Permission.canEdit("vessels"));
        assignBtn.setManaged(Permission.canEdit("vessels"));

        assignBtn.setOnAction(e -> showAssignCrewDialog());

        Button addBtn = new Button("➕  Add Crew Member");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setVisible(Permission.canAdd("vessels"));
        addBtn.setManaged(Permission.canAdd("vessels"));

        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> innerContent.setCenter(buildCrewFormView(null)));

        HBox header = new HBox(12, title, spacer, assignBtn, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = searchField("Search by name, role, or license number...");
        ObservableList<Crew> filtered = FXCollections.observableArrayList(crewList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(crewList.filtered(c
                    -> c.getName().toLowerCase().contains(q)
                    || c.getRole().toLowerCase().contains(q)
                    || c.getLicenseNumber().toLowerCase().contains(q)));
        });

        System.out.println("umabot sa crewlist is empty");
        if (crewList.isEmpty()) {
            content.getChildren().addAll(header, search, emptyLabel("No crew members added yet."));
        } else {
            TableView<Crew> table = buildCrewTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, search, table);
        }
        return content;
    }

    // ── Crew table ──────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Crew> buildCrewTable(ObservableList<Crew> data) {
        System.out.println("Crew Table has been built.");
        TableView<Crew> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Crew, Integer> idCol = col("Crew ID", "id");
        TableColumn<Crew, String> nameCol = col("Name", "name");
        TableColumn<Crew, String> roleCol = col("Role", "role");
        TableColumn<Crew, String> contactCol = col("Contact Info", "contactInfo");
        TableColumn<Crew, String> licCol = col("License No.", "licenseNumber");
        idCol.setPrefWidth(80);

        // License expiry — red if expired
        TableColumn<Crew, Void> expiryCol = new TableColumn<>("License Expiry");
        expiryCol.setPrefWidth(125);
        expiryCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Crew crew = getTableRow().getItem();
                setText(crew.getLicenseExpiryDisplay());
                boolean expired = crew.getLicenseExpiry() != null
                        && crew.getLicenseExpiry().isBefore(LocalDate.now());
                setTextFill(expired ? Color.web("#C62828") : Color.BLACK);
                setFont(Font.font("Arial", expired ? FontWeight.BOLD : FontWeight.NORMAL, 12));
            }
        });

        Map<Integer, List<String>> crewVesselMap = new HashMap<>();
        try {
            String sql = """
                SELECT c.crew_id, v.name
                FROM vessel_crew vc
                JOIN crew c ON c.crew_id = vc.crew_id
                JOIN vessel v ON v.vessel_id = vc.vessel_id
            """;

            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int crewId = rs.getInt("crew_id");
                String vesselName = rs.getString("name");

                crewVesselMap
                        .computeIfAbsent(crewId, k -> new ArrayList<>())
                        .add(vesselName);
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Assigned vessel — name lookup
        TableColumn<Crew, Void> assignCol = new TableColumn<>("Assigned Vessel");
        assignCol.setPrefWidth(135);
        assignCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Crew crew = getTableRow().getItem();
                List<String> vessels = crewVesselMap.get(crew.getId());

                if (vessels != null && !vessels.isEmpty()) {
                    setText(String.join(", ", vessels));
                    setTextFill(Color.web(AppStyles.NAVY_BLUE));
                } else {
                    setText("Unassigned");
                    setTextFill(Color.GRAY);
                }

                setFont(Font.font("Arial", 12));
            }
        });

        TableColumn<Crew, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(100);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("vessels"));
                editBtn.setManaged(Permission.canEdit("vessels"));
                deleteBtn.setVisible(Permission.canDelete("vessels"));
                deleteBtn.setManaged(Permission.canDelete("vessels"));
            }

            {
                editBtn.setOnAction(e
                        -> innerContent.setCenter(buildCrewFormView(getTableRow().getItem())));

                deleteBtn.setOnAction(e -> {
                    Crew crew = getTableRow().getItem();
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Crew Member");
                    confirm.setHeaderText("Delete \"" + crew.getName() + "\"?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                Statement st = con.createStatement();
                                String sql = "DELETE FROM crew WHERE crew_id =" + crew.getId();

                                System.out.println(sql);
                                System.out.println(crew.getId());
                                st.executeUpdate(sql);

                                loadCrewListFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            innerContent.setCenter(buildCrewListView());
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

        table.getColumns().addAll(idCol, nameCol, roleCol, contactCol, licCol, expiryCol, assignCol, actCol);
        return table;
    }

    // ── Assign Crew to Vessel dialog ────────────────────────────────────────
    private void showAssignCrewDialog() {
        if (crewList.isEmpty()) {
            showWarn("No Crew", "No crew members exist yet.",
                    "Add crew members first before assigning.");
            return;
        }
        if (vesselList.isEmpty()) {
            showWarn("No Vessels", "No vessels registered yet.",
                    "Register a vessel first.");
            return;
        }

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Assign Crew to Vessel");
        dlg.setHeaderText("Select a crew member and the vessel to assign them to.");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Crew> crewBox = new ComboBox<>(crewList);
        crewBox.setPromptText("Select crew member...");
        crewBox.setPrefWidth(285);
        crewBox.setPrefHeight(36);
        crewBox.setStyle(AppStyles.comboStyle());
        crewBox.setCellFactory(lv -> crewCell());
        crewBox.setButtonCell(crewCell());

        ComboBox<Vessel> vesselBox = new ComboBox<>(vesselList);
        vesselBox.setPromptText("Select vessel...");
        vesselBox.setPrefWidth(285);
        vesselBox.setPrefHeight(36);
        vesselBox.setStyle(AppStyles.comboStyle());
        vesselBox.setCellFactory(lv -> vesselCell());
        vesselBox.setButtonCell(vesselCell());

        Label warnLbl = new Label("");
        warnLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        warnLbl.setTextFill(Color.web("#E65100"));
        warnLbl.setWrapText(true);

        crewBox.valueProperty().addListener((obs, oldCrew, newCrew) -> {
            if (newCrew == null) {
                warnLbl.setText("");
                return;
            }

            // Check if crew is already assigned
            Vessel assignedVessel = vesselList.stream()
                    .filter(v -> v.getAssignedCrew().contains(newCrew))
                    .findFirst()
                    .orElse(null);

            if (assignedVessel != null) {
                warnLbl.setText(
                        "⚠ " + newCrew.getName() + " is already assigned to "
                        + assignedVessel.getName()
                        + ". Reassigning will move them."
                );
            } else {
                warnLbl.setText("");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 30, 10, 20));
        grid.add(new Label("Crew Member:"), 0, 0);
        grid.add(crewBox, 1, 0);
        grid.add(new Label("Assign to Vessel:"), 0, 1);
        grid.add(vesselBox, 1, 1);
        grid.add(warnLbl, 0, 2, 2, 1);

        dlg.getDialogPane().setContent(grid);
        dlg.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Crew crew = crewBox.getValue();
                Vessel vessel = vesselBox.getValue();

                if (crew == null || vessel == null) {
                    showWarn("Incomplete", "Nothing was assigned.",
                            "Please select both a crew member and a vessel.");
                    return;
                }

                try {
                    Connection con = DBConnection.getConnection();

                    // Check if already assigned to same vessel
                    String checkSql = "SELECT COUNT(*) FROM vessel_crew WHERE vessel_id = ? AND crew_id = ?";
                    PreparedStatement checkPs = con.prepareStatement(checkSql);
                    checkPs.setInt(1, vessel.getId());
                    checkPs.setInt(2, crew.getId());

                    ResultSet rs = checkPs.executeQuery();
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        showWarn("Already Assigned",
                                "This crew is already assigned to this vessel.",
                                "No changes were made.");
                        con.close();
                        return;
                    }

                    // (Optional) Check if assigned to another vessel
                    String checkOtherSql = "SELECT vessel_id FROM vessel_crew WHERE crew_id = ?";
                    PreparedStatement otherPs = con.prepareStatement(checkOtherSql);
                    otherPs.setInt(1, crew.getId());

                    ResultSet rsOther = otherPs.executeQuery();

                    if (rsOther.next()) {
                        int oldVesselId = rsOther.getInt("vessel_id");

                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Reassign Crew");
                        confirm.setHeaderText("Crew already assigned to another vessel.");
                        confirm.setContentText("Move this crew to the new vessel?");

                        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                            con.close();
                            return;
                        }

                        // remove old assignment
                        String deleteSql = "DELETE FROM vessel_crew WHERE crew_id = ?";
                        PreparedStatement delPs = con.prepareStatement(deleteSql);
                        delPs.setInt(1, crew.getId());
                        delPs.executeUpdate();
                    }

                    // Insert new assignment
                    String insertSql = "INSERT INTO vessel_crew (vessel_id, crew_id) VALUES (?, ?)";
                    PreparedStatement insertPs = con.prepareStatement(insertSql);
                    insertPs.setInt(1, vessel.getId());
                    insertPs.setInt(2, crew.getId());

                    insertPs.executeUpdate();

                    con.close();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                innerContent.setCenter(buildCrewListView());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  👥  CREW — add / edit form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildCrewFormView(Crew existing) {
        System.out.println("Crew Form View has been built.");
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(680);

        Label pageTitle = new Label(isEdit ? "Edit Crew Member" : "Add Crew Member");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        TextField nameField = AppStyles.formField("Full Name",
                isEdit ? existing.getName() : "");
        TextField roleField = AppStyles.formField("Role / Position",
                isEdit ? existing.getRole() : "");
        TextField contactField = AppStyles.formField("Contact Info (phone or email)",
                isEdit ? existing.getContactInfo() : "");
        TextField licField = AppStyles.formField("License Number",
                isEdit ? existing.getLicenseNumber() : "");

        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setPromptText("License Expiry Date");
        expiryPicker.setPrefWidth(Double.MAX_VALUE);
        expiryPicker.setPrefHeight(40);
        if (isEdit) {
            expiryPicker.setValue(existing.getLicenseExpiry());
        }

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildCrewListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Crew Member");
        saveBtn.setPrefSize(160, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String role = roleField.getText().trim();
            String contact = contactField.getText().trim();
            String lic = licField.getText().trim();
            LocalDate expiry = expiryPicker.getValue();

            if (name.isEmpty() || role.isEmpty() || contact.isEmpty()
                    || lic.isEmpty() || expiry == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
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
                        String sql = "UPDATE crew SET "
                                + "name=?, "
                                + "role=?, "
                                + "contact_info=?, "
                                + "license_number=?, "
                                + "license_expiry=? "
                                + "WHERE crew_id=?";

                        PreparedStatement ps = con.prepareStatement(sql);

                        ps.setString(1, name);
                        ps.setString(2, role);
                        ps.setString(3, contact);
                        ps.setString(4, lic);
                        ps.setDate(5, java.sql.Date.valueOf(expiry));
                        ps.setInt(6, existing.getId());
                        ps.executeUpdate();
                        loadCrewListFromDB();
                        con.close();
                        innerContent.setCenter(buildCrewListView());
                    }
                } else {
                    String sql = "INSERT INTO crew (name, role, contact_info, license_number, license_expiry)"
                            + "VALUES (?,?,?,?,?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setString(2, role);
                    ps.setString(3, contact);
                    ps.setString(4, lic);
                    ps.setDate(5, java.sql.Date.valueOf(expiry));
                    ps.executeUpdate();
                    loadCrewListFromDB();
                    con.close();
                    innerContent.setCenter(buildCrewListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Full Name"), nameField,
                AppStyles.formLabel("Role / Position"), roleField,
                AppStyles.formLabel("Contact Info"), contactField,
                AppStyles.formLabel("License Number"), licField,
                AppStyles.formLabel("License Expiry"), expiryPicker,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  🏭  PORTS — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildPortListView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        Label title = AppStyles.sectionTitle("Ports");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add Port");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setVisible(Permission.canAdd("vessels"));
        addBtn.setManaged(Permission.canAdd("vessels"));
        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> innerContent.setCenter(buildPortFormView(null)));

        HBox header = new HBox(12, title, spacer, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = searchField("Search by name, city, or country...");
        ObservableList<Port> filtered = FXCollections.observableArrayList(portList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(portList.filtered(p
                    -> p.getName().toLowerCase().contains(q)
                    || p.getCity().toLowerCase().contains(q)
                    || p.getCountry().toLowerCase().contains(q)));
        });

        if (portList.isEmpty()) {
            content.getChildren().addAll(header, search, emptyLabel("No ports added yet."));
        } else {
            TableView<Port> table = buildPortTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, search, table);
        }
        return content;
    }

    // ── Port table ──────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Port> buildPortTable(ObservableList<Port> data) {

        TableView<Port> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Port, Integer> idCol = col("Port ID", "id");
        TableColumn<Port, String> nameCol = col("Name", "name");
        TableColumn<Port, String> countryCol = col("Country", "country");
        TableColumn<Port, String> cityCol = col("City", "city");
        idCol.setPrefWidth(90);

        TableColumn<Port, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(110);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("vessels"));
                editBtn.setManaged(Permission.canEdit("vessels"));
                deleteBtn.setVisible(Permission.canDelete("vessels"));
                deleteBtn.setManaged(Permission.canDelete("vessels"));
            }

            {
                editBtn.setOnAction(e -> {
                    Port p = getTableView().getItems().get(getIndex());
                    innerContent.setCenter(buildPortFormView(p));
                });

                deleteBtn.setOnAction(e -> {
                    Port p = getTableView().getItems().get(getIndex());
                    boolean usedInRoute = routeList.stream().anyMatch(r
                            -> r.getOriginPortId() == p.getId()
                            || r.getDestinationPortId() == p.getId());
                    if (usedInRoute) {
                        showWarn("Cannot Delete Port",
                                "\"" + p.getName() + "\" is used in existing routes.",
                                "Remove all routes referencing this port before deleting it.");
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Port");
                    confirm.setHeaderText("Delete \"" + p.getName() + "\"?");
                    confirm.setContentText("This action cannot be undone.");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            Connection con = DBConnection.getConnection();
                            Statement st = con.createStatement();
                            String sql = "DELETE FROM port WHERE port_id =" + p.getId();

                            System.out.println(sql);
                            System.out.println(p.getId());
                            st.executeUpdate(sql);

                            loadPortListFromDB();
                            con.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        root.setCenter(buildPortListView());
                    }
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, countryCol, cityCol, actCol);
        return table;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  🏭  PORTS — add / edit form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildPortFormView(Port existing) {
        System.out.println("Port Form View has been built.");
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(680);

        Label pageTitle = new Label(isEdit ? "Edit Port" : "Add New Port");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        TextField nameField = AppStyles.formField("Port Name",
                isEdit ? existing.getName() : "");
        TextField countryField = AppStyles.formField("Country",
                isEdit ? existing.getCountry() : "");
        TextField cityField = AppStyles.formField("City",
                isEdit ? existing.getCity() : "");

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildPortListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Port");
        saveBtn.setPrefSize(130, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String country = countryField.getText().trim();
            String city = cityField.getText().trim();

            if (name.isEmpty() || country.isEmpty() || city.isEmpty()) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
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
                        String sql = "UPDATE port SET "
                                + "name=?, "
                                + "country=?, "
                                + "city=? "
                                + "WHERE port_id=?";

                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, name);
                        ps.setString(2, country);
                        ps.setString(3, city);
                        ps.setInt(4, existing.getId());
                        ps.executeUpdate();

                        int rows = ps.executeUpdate();
                        System.out.println("Rows updated: " + rows);
                        System.out.println("Updating port ID: " + existing.getId());

                        loadPortListFromDB();
                        innerContent.setCenter(buildPortListView());
                        con.close();
                    }
                } else {
                    String sql = "INSERT INTO port (name, country, city)"
                            + " VALUES (?,?,?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setString(2, country);
                    ps.setString(3, city);
                    ps.executeUpdate();
                    loadPortListFromDB();
                    con.close();
                    innerContent.setCenter(buildPortListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Port Name"), nameField,
                AppStyles.formLabel("Country"), countryField,
                AppStyles.formLabel("City"), cityField,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  🗺  ROUTES — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildRouteListView() {
        System.out.println("Rout List View is built.");
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        Label title = AppStyles.sectionTitle("Routes");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add Route");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setVisible(Permission.canAdd("vessels"));
        addBtn.setManaged(Permission.canAdd("vessels"));
        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> {
            if (portList.size() < 2) {
                showWarn("Not Enough Ports",
                        "At least 2 ports are needed to create a route.",
                        "Go to the 🏭 Ports tab and add more ports first.");
                return;
            }
            innerContent.setCenter(buildRouteFormView(null));
        });

        HBox header = new HBox(12, title, spacer, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        if (routeList.isEmpty()) {
            content.getChildren().addAll(header, emptyLabel("No routes defined yet."));
        } else {
            TableView<Route> table = buildRouteTable(routeList);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, table);
        }
        return content;
    }

    // ── Route table ─────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Route> buildRouteTable(ObservableList<Route> data) {
        TableView<Route> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Route, Integer> idCol = col("Route ID", "id");
        idCol.setPrefWidth(90);

        // Origin port name (looked up from portList)
        TableColumn<Route, Void> originCol = new TableColumn<>("Origin Port");
        originCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(portName(getTableRow().getItem().getOriginPortId()));
            }
        });

        // Arrow column
        TableColumn<Route, Void> arrowCol = new TableColumn<>("");
        arrowCol.setPrefWidth(38);
        arrowCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : "→");
                setAlignment(Pos.CENTER);
                setFont(Font.font("Arial", FontWeight.BOLD, 16));
                setTextFill(Color.web(AppStyles.NAVY_BLUE));
            }
        });

        // Destination port name
        TableColumn<Route, Void> destCol = new TableColumn<>("Destination Port");
        destCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(portName(getTableRow().getItem().getDestinationPortId()));
            }
        });

        TableColumn<Route, Integer> daysCol = col("Transit Days", "transitDays");
        daysCol.setPrefWidth(110);

        TableColumn<Route, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(110);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("vessels"));
                editBtn.setManaged(Permission.canEdit("vessels"));
                deleteBtn.setVisible(Permission.canDelete("vessels"));
                deleteBtn.setManaged(Permission.canDelete("vessels"));
            }

            {
                editBtn.setOnAction(e
                        -> innerContent.setCenter(buildRouteFormView(getTableRow().getItem())));

                deleteBtn.setOnAction(e -> {
                    Route r = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Route");
                    confirm.setHeaderText("Delete Route #" + r.getId() + "?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                Statement st = con.createStatement();
                                String sql = "DELETE FROM route WHERE route_id =" + r.getId();

                                System.out.println(sql);
                                System.out.println(r.getId());
                                st.executeUpdate(sql);

                                loadRouteListFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            root.setCenter(buildRouteListView());
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

        table.getColumns().addAll(idCol, originCol, arrowCol, destCol, daysCol, actCol);
        return table;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  🗺  ROUTES — add / edit form
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildRouteFormView(Route existing) {
        System.out.println("Route Form View is built.");
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(680);

        Label pageTitle = new Label(isEdit ? "Edit Route" : "Add New Route");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        ComboBox<Port> originBox = new ComboBox<>(portList);
        originBox.setPromptText("Select origin port...");
        originBox.setPrefWidth(Double.MAX_VALUE);
        originBox.setPrefHeight(40);
        originBox.setStyle(AppStyles.comboStyle());
        if (isEdit) {
            originBox.setValue(portById(existing.getOriginPortId()));
        }

        ComboBox<Port> destBox = new ComboBox<>(portList);
        destBox.setPromptText("Select destination port...");
        destBox.setPrefWidth(Double.MAX_VALUE);
        destBox.setPrefHeight(40);
        destBox.setStyle(AppStyles.comboStyle());
        if (isEdit) {
            destBox.setValue(portById(existing.getDestinationPortId()));
        }

        TextField daysField = AppStyles.formField("e.g. 5",
                isEdit ? String.valueOf(existing.getTransitDays()) : "");

        Label errorLabel = AppStyles.errorLabel();

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildRouteListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Route");
        saveBtn.setPrefSize(130, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            Port origin = originBox.getValue();
            Port dest = destBox.getValue();
            String dStr = daysField.getText().trim();

            int originId = originBox.getValue().getId();
            int destId = destBox.getValue().getId();
            if (origin == null || dest == null || dStr.isEmpty()) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }
            if (origin.getId() == dest.getId()) {
                AppStyles.showError(errorLabel,
                        "Origin and destination cannot be the same port.");
                return;
            }
            int days;
            try {
                days = Integer.parseInt(dStr);
                if (days <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel,
                        "Transit days must be a positive whole number.");
                return;
            }

            try {
                Connection con = DBConnection.getConnection();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for Route #" + existing.getId() + "?");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            String sql = "UPDATE route SET "
                                    + "origin_port_id=?, "
                                    + "destination_port_id=?, "
                                    + "transit_days=? "
                                    + "WHERE route_id=?";

                            PreparedStatement ps = con.prepareStatement(sql);

                            ps.setInt(1, originId);
                            ps.setInt(2, destId);
                            ps.setString(3, dStr);
                            ps.setInt(4, existing.getId());

                            ps.executeUpdate();
                            loadRouteListFromDB();
                            con.close();
                            root.setCenter(buildRouteListView());
                        }
                    }
                } else {
                    String sql = "INSERT INTO route (origin_port_id, destination_port_id, transit_days)"
                            + "VALUES (?,?,?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, originId);
                    ps.setInt(2, destId);
                    ps.setString(3, dStr);
                    ps.executeUpdate();
                    loadRouteListFromDB();
                    con.close();
                    innerContent.setCenter(buildRouteListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Origin Port"), originBox,
                AppStyles.formLabel("Destination Port"), destBox,
                AppStyles.formLabel("Transit Days"), daysField,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  📦  CONTAINERS — list
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildContainerListView() {
        System.out.println("Container List View is built.");
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        // ── Header ──────────────────────────────────────────────────────────
        Label title = AppStyles.sectionTitle("Containers");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Register New Container");
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setVisible(Permission.canAdd("vessels"));
        addBtn.setManaged(Permission.canAdd("vessels"));
        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> innerContent.setCenter(buildContainerFormView(null)));

        HBox header = new HBox(12, title, spacer, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Search ───────────────────────────────────────────────────────────
        TextField search = searchField("Search by container number, type, or status...");
        ObservableList<Container> filtered = FXCollections.observableArrayList(containerList);
        search.textProperty().addListener((obs, o, n) -> {
            String q = n.toLowerCase();
            filtered.setAll(containerList.filtered(c
                    -> c.getContainerNumber().toLowerCase().contains(q)
                    || c.getRawType().toLowerCase().contains(q)
                    || c.getStatusDisplay().toLowerCase().contains(q)
            ));
        });

        if (containerList.isEmpty()) {
            content.getChildren().addAll(header, search, emptyLabel("No containers registered yet."));
        } else {
            TableView<Container> table = buildContainerTable(filtered);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, search, table);
        }
        return content;
    }

    // ── Container table ──────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Container> buildContainerTable(ObservableList<Container> data) {
        TableView<Container> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(460);

        TableColumn<Container, Integer> idCol = col("Container ID", "id");
        TableColumn<Container, String> numCol = col("Container Number", "containerNumber");
        TableColumn<Container, String> typeCol = col("Type", "rawType");
        TableColumn<Container, Double> weightCol = col("Max Weight (kg)", "maxWeightKg");
        TableColumn<Container, Double> volumeCol = col("Max Volume (cbm)", "maxVolumeCbm");

        idCol.setPrefWidth(110);

        // Status — coloured badge
        TableColumn<Container, Void> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(140);
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Container con = getTableView().getItems().get(getIndex());
                Label badge = new Label(con.getStatusDisplay());
                badge.setStyle(
                        "-fx-background-color: " + containerStatusColor(con.getStatus()) + "22; "
                        + "-fx-text-fill: " + containerStatusColor(con.getStatus()) + "; "
                        + "-fx-background-radius: 6; -fx-padding: 3 10 3 10; -fx-font-weight: bold;"
                );
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Actions
        TableColumn<Container, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(110);
        actCol.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("vessels"));
                editBtn.setManaged(Permission.canEdit("vessels"));
                deleteBtn.setVisible(Permission.canDelete("vessels"));
                deleteBtn.setManaged(Permission.canDelete("vessels"));
            }

            {
                editBtn.setOnAction(e -> {
                    Container con = getTableView().getItems().get(getIndex());
                    innerContent.setCenter(buildContainerFormView(con));
                });
                deleteBtn.setOnAction(e -> {
                    Container con = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Container");
                    confirm.setHeaderText("Delete container \"" + con.getContainerNumber() + "\"?");
                    confirm.setContentText("This action cannot be undone.");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection connect = DBConnection.getConnection();
                                Statement st = connect.createStatement();
                                String sql = "DELETE FROM container WHERE container_id =" + con.getId();

                                System.out.println(sql);
                                System.out.println(c.getId());
                                st.executeUpdate(sql);

                                loadContainerListFromDB();
                                connect.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            root.setCenter(buildContainerListView());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, numCol, typeCol, weightCol, volumeCol, statusCol, actCol);
        return table;
    }

    // ── Container form (add / edit) ──────────────────────────────────────
    private ScrollPane buildContainerFormView(Container existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(680);

        Label pageTitle = new Label(isEdit ? "Edit Container" : "Register New Container");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        // Container Number
        TextField numberField = AppStyles.formField(
                "e.g. MSCU1234567",
                isEdit ? existing.getContainerNumber() : ""
        );

        // Type dropdown
        ComboBox<String> typeBox = new ComboBox<>();
        for (Container.Type t : Container.Type.values()) {
            typeBox.getItems().add(t.getDisplay());
        }
        typeBox.setPromptText("Select container type...");
        typeBox.setPrefWidth(Double.MAX_VALUE);
        typeBox.setPrefHeight(40);
        typeBox.setStyle(AppStyles.comboStyle());
        if (isEdit) {
            typeBox.setValue(existing.getTypeDisplay());
        }

        // Max Weight — read only, auto-filled
        TextField weightField = AppStyles.formField("Auto-filled", "");
        weightField.setEditable(false);
        weightField.setStyle(AppStyles.fieldStyle()
                + "-fx-background-color: #f0f0f0;");
        if (isEdit) {
            weightField.setText(String.valueOf(existing.getMaxWeightKg()));
        }

        // Max Volume — read only, auto-filled
        TextField volumeField = AppStyles.formField("Auto-filled", "");
        volumeField.setEditable(false);
        volumeField.setStyle(AppStyles.fieldStyle()
                + "-fx-background-color: #f0f0f0;");
        if (isEdit) {
            volumeField.setText(String.valueOf(existing.getMaxVolumeCbm()));
        }

        // Auto-fill weight and volume when type is selected
        typeBox.setOnAction(e -> {
            String selected = typeBox.getValue();
            if (selected != null) {
                Container.Type t = Container.Type.fromDisplay(selected);
                weightField.setText(String.valueOf(t.getMaxWeightKg()));
                volumeField.setText(
                        t.getMaxVolumeCbm() == 0.0 ? "N/A" : String.valueOf(t.getMaxVolumeCbm())
                );
            }
        });

        ComboBox<String> statusBox = new ComboBox<>();
        for (Container.Status s : Container.Status.values()) {
            statusBox.getItems().add(s.getDisplay());
        }
        statusBox.setPrefWidth(Double.MAX_VALUE);
        statusBox.setPrefHeight(40);
        statusBox.setStyle(AppStyles.comboStyle());
        statusBox.setValue(isEdit
                ? existing.getStatusDisplay()
                : Container.Status.AVAILABLE.getDisplay() // default to Available
        );

        Label errorLabel = AppStyles.errorLabel();

        // Buttons
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> innerContent.setCenter(buildContainerListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Register Container");
        saveBtn.setPrefSize(160, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String number = numberField.getText().trim();
            String typeStr = typeBox.getValue();

            if (number.isEmpty() || typeStr == null) {
                AppStyles.showError(errorLabel, "Please fill in all fields.");
                return;
            }

            boolean duplicate = containerList.stream().anyMatch(c
                    -> c.getContainerNumber().equalsIgnoreCase(number)
                    && (!isEdit || c.getId() != existing.getId())
            );
            if (duplicate) {
                AppStyles.showError(errorLabel, "Container number already exists.");
                return;
            }

            Container.Type type = Container.Type.fromDisplay(typeStr);
            Container.Status status = Container.Status.fromDisplay(statusBox.getValue());

            try {
                Connection con = DBConnection.getConnection();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for \"" + existing.getContainerNumber() + "\"?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {

                        String sql = "UPDATE container SET "
                                + "container_number = ?, "
                                + "container_type = ?, "
                                + "max_weight = ?, "
                                + "max_volume = ?, "
                                + "status = ? "
                                + "WHERE container_id = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, number);
                        ps.setString(2, typeStr);
                        ps.setDouble(3, type.getMaxWeightKg());
                        ps.setDouble(4, type.getMaxVolumeCbm());
                        ps.setString(5, status.getDbValue());
                        ps.setInt(6, existing.getId());
                        ps.executeUpdate();

                        loadContainerListFromDB();
                        con.close();
                        innerContent.setCenter(buildContainerListView());
                    }
                } else {

                    String sql = "INSERT INTO container "
                            + "(container_number, container_type, max_weight, max_volume, status) "
                            + "VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, number);
                    ps.setString(2, typeStr);
                    ps.setDouble(3, type.getMaxWeightKg());
                    ps.setDouble(4, type.getMaxVolumeCbm());
                    ps.setString(5, status.getDbValue());
                    ps.executeUpdate();

                    loadContainerListFromDB();
                    con.close();
                    innerContent.setCenter(buildContainerListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Container Number"), numberField,
                AppStyles.formLabel("Container Type"), typeBox,
                AppStyles.formLabel("Max Weight (kg)"), weightField,
                AppStyles.formLabel("Max Volume (cbm)"), volumeField,
                AppStyles.formLabel("Status"), statusBox,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);
        return wrapScroll(content);
    }

    // ── Container status color ────────────────────────────────────────────
    private String containerStatusColor(Container.Status s) {
        return switch (s) {
            case AVAILABLE ->
                "#388E3C"; // green
            case IN_USE ->
                "#1565C0"; // blue
            case MAINTENANCE ->
                "#E65100"; // orange
        };
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════════════════════════
    /**
     * Generic typed TableColumn backed by a JavaFX property name.
     */
    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> col(String header, String property) {
        TableColumn<S, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        return c;
    }

    private String portName(int portId) {
        return portList.stream()
                .filter(p -> p.getId() == portId)
                .findFirst()
                .map(p -> p.getName() + " (" + p.getCity() + ", " + p.getCountry() + ")")
                .orElse("Port #" + portId);
    }

    private Port portById(int portId) {
        return portList.stream()
                .filter(p -> p.getId() == portId)
                .findFirst().orElse(null);
    }

    private String statusColor(Vessel.Status s) {
        return switch (s) {
            case ACTIVE ->
                "#388E3C";  // green
            case DOCKED ->
                "#1565C0";  // blue
            case UNDER_MAINTENANCE ->
                "#E65100";  // orange
        };
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
        tf.setPrefWidth(340);
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

    private ListCell<Crew> crewCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Crew c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName() + " — " + c.getRole());
            }
        };
    }

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

    private void showWarn(String title, String header, String body) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(body);
        a.showAndWait();
    }

    private void loadContainerListFromDB() {
        containerList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM container");

            while (rs.next()) {
                Container.Status status = Container.Status.fromName(rs.getString("status"));
                containerList.add(new Container(
                        rs.getInt("container_id"),
                        rs.getString("container_number"),
                        rs.getString("container_type"),
                        rs.getDouble("max_weight"),
                        rs.getDouble("max_volume"),
                        status
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadVesselListFromDB() {
        vesselList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM vessel");

            while (rs.next()) {
                String status = rs.getString("status");

                vesselList.add(new Vessel(
                        rs.getInt("vessel_id"),
                        rs.getString("name"),
                        rs.getString("vessel_type"),
                        rs.getDouble("capacity_weight"),
                        rs.getDouble("capacity_volume"),
                        Vessel.Status.fromDb(status),
                        rs.getString("registration_number")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        int maxId = getMaxVesselId();
        Vessel.resetCounter(maxId + 1);
    }

    public int getMaxVesselId() {
        int maxId = 0;

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT MAX(vessel_id) AS max_id FROM vessel";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                maxId = rs.getInt("max_id");
            }

            rs.close();
            st.close();
            con.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return maxId;
    }

    private void loadRouteListFromDB() {
        routeList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM route");

            while (rs.next()) {
                routeList.add(new Route(
                        rs.getInt("route_id"),
                        rs.getInt("origin_port_id"),
                        rs.getInt("destination_port_id"),
                        rs.getInt("transit_days")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadPortListFromDB() {
        portList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM port");

            while (rs.next()) {
                portList.add(new Port(
                        rs.getInt("port_id"),
                        rs.getString("name"),
                        rs.getString("country"),
                        rs.getString("city")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadCrewListFromDB() {
        crewList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM crew");

            while (rs.next()) {

                java.sql.Date sqlDate = rs.getDate("license_expiry");
                LocalDate expiry = rs.getObject("license_expiry", LocalDate.class);

                crewList.add(new Crew(
                        rs.getInt("crew_id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("contact_info"),
                        rs.getString("license_number"),
                        expiry
                ));
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

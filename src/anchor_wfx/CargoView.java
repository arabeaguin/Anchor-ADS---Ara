package anchor_wfx;

import java.util.Optional;
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

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Manages the Cargo section — list, filter, add, edit, delete.
 */
public class CargoView {

    private final BorderPane root;
    private final ObservableList<Cargo> cargoList;

    OptionQueries query = new OptionQueries();

    private static final String[] IMDG_CLASSES = {
        "Class 1: Explosives",
        "Class 2: Gases",
        "Class 3: Flammable Liquids",
        "Class 4: Flammable Solids",
        "Class 5: Oxidizers and Peroxides",
        "Class 6: Toxic and Infectious Substances",
        "Class 7: Radioactive Materials",
        "Class 8: Corrosive Substances",
        "Class 9: Miscellaneous Hazardous Materials"
    };

    public CargoView(BorderPane root, ObservableList<Cargo> cargoList) {
        this.root = root;
        this.cargoList = cargoList;
        System.out.println("cargoList is null? " + (cargoList == null));
    }

    /**
     * Entry point — called by Dashboard router.
     */
    public VBox build() {
        System.out.println("yes nabuild sha gurl");
        return buildListView();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LIST VIEW
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildListView() {
        System.out.println("yes umabot sha sa buildListView");
        VBox content = new VBox(16);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label pageTitle = new Label("Manage Cargo");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add New Cargo");
        addBtn.setVisible(Permission.canAdd("cargo"));
        addBtn.setManaged(Permission.canAdd("cargo"));
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.styleAddBtn(addBtn, false);
        addBtn.setOnMouseEntered(e -> AppStyles.styleAddBtn(addBtn, true));
        addBtn.setOnMouseExited(e -> AppStyles.styleAddBtn(addBtn, false));
        addBtn.setOnAction(e -> root.setCenter(buildFormView(null)));

        header.getChildren().addAll(pageTitle, spacer, addBtn);

        // Search
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search cargo...");
        searchField.setPrefWidth(300);
        searchField.setPrefHeight(36);
        searchRow.getChildren().add(searchField);

        // Filters
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter:");
        filterLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        filterLabel.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        ComboBox<String> hazFilter = new ComboBox<>();
        hazFilter.getItems().addAll("All", "Hazardous", "Non-Hazardous");
        hazFilter.setValue("All");
        hazFilter.setPrefHeight(36);

        ComboBox<String> imdgFilter = new ComboBox<>();
        imdgFilter.getItems().add("All Classes");
        imdgFilter.getItems().addAll(IMDG_CLASSES);
        imdgFilter.setValue("All Classes");
        imdgFilter.setPrefHeight(36);
        imdgFilter.setDisable(true);

        filterRow.getChildren().addAll(filterLabel, hazFilter, imdgFilter);

        // Combined filter logic
        ObservableList<Cargo> filteredList = FXCollections.observableArrayList(cargoList);

        Runnable applyFilters = () -> {
            String q = searchField.getText().toLowerCase();
            String hazVal = hazFilter.getValue();
            String imdgVal = imdgFilter.getValue();

            filteredList.setAll(cargoList.filtered(c -> {
                boolean matchSearch = c.getDescription().toLowerCase().contains(q)
                        || c.getImdgClass().toLowerCase().contains(q);
                boolean matchHaz = hazVal.equals("All")
                        || (hazVal.equals("Hazardous") && c.isHazardous())
                        || (hazVal.equals("Non-Hazardous") && !c.isHazardous());
                boolean matchImdg = imdgVal.equals("All Classes")
                        || c.getImdgClass().equals(imdgVal);
                return matchSearch && matchHaz && matchImdg;
            }));
        };

        searchField.textProperty().addListener((obs, o, n) -> applyFilters.run());
        hazFilter.valueProperty().addListener((obs, o, n) -> {
            imdgFilter.setDisable(!n.equals("Hazardous"));
            if (!n.equals("Hazardous")) {
                imdgFilter.setValue("All Classes");
            }
            applyFilters.run();
        });
        imdgFilter.valueProperty().addListener((obs, o, n) -> applyFilters.run());

        if (cargoList.isEmpty()) {
            Label empty = new Label("No cargo records yet.");
            empty.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            empty.setTextFill(Color.web("#888888"));
            empty.setPadding(new Insets(40));
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            content.getChildren().addAll(header, searchRow, filterRow, empty);
        } else {
            TableView<Cargo> table = buildTable(filteredList);
            VBox.setVgrow(table, Priority.ALWAYS);
            content.getChildren().addAll(header, searchRow, filterRow, table);
        }

        return content;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TABLE
    // ══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private TableView<Cargo> buildTable(ObservableList<Cargo> data) {
        TableView<Cargo> table = new TableView<>(data);
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(480);

        TableColumn<Cargo, Integer> idCol = new TableColumn<>("Cargo ID");
        TableColumn<Cargo, String> descCol = new TableColumn<>("Description");
        TableColumn<Cargo, Double> weightCol = new TableColumn<>("Weight (kg)");
        TableColumn<Cargo, Double> volumeCol = new TableColumn<>("Volume (cbm)");
        TableColumn<Cargo, String> hazCol = new TableColumn<>("Hazardous");
        TableColumn<Cargo, String> imdgCol = new TableColumn<>("IMDG Class");
        TableColumn<Cargo, String> unCol = new TableColumn<>("UN Number");
        TableColumn<Cargo, String> psnCol = new TableColumn<>("Proper Shipping Name");
        TableColumn<Cargo, Void> actCol = new TableColumn<>("Actions");

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));
        hazCol.setCellValueFactory(new PropertyValueFactory<>("hazardousDisplay"));
        imdgCol.setCellValueFactory(new PropertyValueFactory<>("imdgClass"));
        unCol.setCellValueFactory(new PropertyValueFactory<>("unNumber"));
        psnCol.setCellValueFactory(new PropertyValueFactory<>("properShippingName"));

        idCol.setPrefWidth(80);
        hazCol.setPrefWidth(90);
        actCol.setPrefWidth(110);

        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = AppStyles.editButton();
            private final Button deleteBtn = AppStyles.deleteButton();
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                editBtn.setVisible(Permission.canEdit("cargo"));
                editBtn.setManaged(Permission.canEdit("cargo"));
                deleteBtn.setVisible(Permission.canDelete("cargo"));
                deleteBtn.setManaged(Permission.canDelete("cargo"));
            }

            {
                editBtn.setOnAction(e -> {
                    Cargo c = getTableView().getItems().get(getIndex());
                    root.setCenter(buildFormView(c));
                });

                deleteBtn.setOnAction(e -> {
                    Cargo c = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Cargo");
                    confirm.setHeaderText("Delete cargo \"" + c.getDescription() + "\"?");
                    confirm.setContentText("This action cannot be undone.");

                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                Connection con = DBConnection.getConnection();
                                Statement st = con.createStatement();
                                String sql = "DELETE FROM cargo WHERE cargo_id =" + c.getId();

                                System.out.println(sql);
                                System.out.println(c.getId());
                                st.executeUpdate(sql);

                                loadCargoFromDB();
                                con.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            root.setCenter(buildListView());
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

        table.getColumns().addAll(idCol, descCol, weightCol, volumeCol,
                hazCol, imdgCol, unCol, psnCol, actCol);
        return table;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADD / EDIT FORM
    // ══════════════════════════════════════════════════════════════════════
    private ScrollPane buildFormView(Cargo existing) {
        boolean isEdit = (existing != null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE + ";");
        content.setMaxWidth(680);

        Label pageTitle = new Label(isEdit ? "Edit Cargo" : "Add New Cargo");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pageTitle.setTextFill(Color.web(AppStyles.NAVY_BLUE));

        VBox card = AppStyles.formCard();

        TextField descField = AppStyles.formField("Description", isEdit ? existing.getDescription() : "");
        TextField weightField = AppStyles.formField("Weight (kg)", isEdit ? String.valueOf(existing.getWeight()) : "");
        TextField volumeField = AppStyles.formField("Volume (cbm)", isEdit ? String.valueOf(existing.getVolume()) : "");

        // Hazardous checkbox
        CheckBox hazCheck = new CheckBox();
        hazCheck.setSelected(isEdit && existing.isHazardous());
        hazCheck.setStyle("-fx-cursor: hand;");
        HBox hazRow = new HBox(12, AppStyles.formLabel("Is Hazardous?"), hazCheck);
        hazRow.setAlignment(Pos.CENTER_LEFT);

        // Hazardous-only fields
        ComboBox<String> imdgBox = new ComboBox<>();
        imdgBox.getItems().addAll(IMDG_CLASSES);
        imdgBox.setPromptText("Select IMDG class...");
        imdgBox.setPrefWidth(Double.MAX_VALUE);
        imdgBox.setPrefHeight(40);
        imdgBox.setStyle(AppStyles.comboStyle());
        if (isEdit && existing.isHazardous()) {
            imdgBox.setValue(existing.getImdgClass());
        }

        TextField unField = AppStyles.formField("UN Number (e.g. UN1203)",
                isEdit && existing.isHazardous() ? existing.getUnNumber() : "");
        TextField psnField = AppStyles.formField("Proper Shipping Name",
                isEdit && existing.isHazardous() ? existing.getProperShippingName() : "");

        VBox hazFields = new VBox(10,
                AppStyles.formLabel("IMDG Class"), imdgBox,
                AppStyles.formLabel("UN Number"), unField,
                AppStyles.formLabel("Proper Shipping Name"), psnField
        );
        hazFields.setVisible(hazCheck.isSelected());
        hazFields.setManaged(hazCheck.isSelected());

        hazCheck.selectedProperty().addListener((obs, o, n) -> {
            hazFields.setVisible(n);
            hazFields.setManaged(n);
            if (!n) {
                imdgBox.setValue(null);
                unField.clear();
                psnField.clear();
            }
        });

        Label errorLabel = AppStyles.errorLabel();

        // Buttons
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefSize(110, 40);
        AppStyles.styleOutlineBtn(cancelBtn, false);
        cancelBtn.setOnMouseEntered(e -> AppStyles.styleOutlineBtn(cancelBtn, true));
        cancelBtn.setOnMouseExited(e -> AppStyles.styleOutlineBtn(cancelBtn, false));
        cancelBtn.setOnAction(e -> root.setCenter(buildListView()));

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Add Cargo");
        saveBtn.setPrefSize(140, 40);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        AppStyles.stylePrimaryBtn(saveBtn, false);
        saveBtn.setOnMouseEntered(e -> AppStyles.stylePrimaryBtn(saveBtn, true));
        saveBtn.setOnMouseExited(e -> AppStyles.stylePrimaryBtn(saveBtn, false));

        saveBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            String wtStr = weightField.getText().trim();
            String volStr = volumeField.getText().trim();
            boolean haz = hazCheck.isSelected();

            if (desc.isEmpty() || wtStr.isEmpty() || volStr.isEmpty()) {
                AppStyles.showError(errorLabel, "Please fill in Description, Weight, and Volume.");
                return;
            }

            double weight, volume;
            try {
                weight = Double.parseDouble(wtStr);
                volume = Double.parseDouble(volStr);
            } catch (NumberFormatException ex) {
                AppStyles.showError(errorLabel, "Weight and Volume must be valid numbers.");
                return;
            }

            String imdg = null, un = null, psn = null;
            if (haz) {
                imdg = imdgBox.getValue();
                un = unField.getText().trim();
                psn = psnField.getText().trim();
                if (imdg == null || un.isEmpty() || psn.isEmpty()) {
                    AppStyles.showError(errorLabel, "Fill in all hazardous fields.");
                    return;
                }
            }

            final String finalImdg = imdg;
            final String finalUn = un;
            final String finalPsn = psn;
            System.out.println("okay umabot siya sa before mag edit");

            try {
                Connection con = DBConnection.getConnection();

                if (isEdit) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Save Changes");
                    confirm.setHeaderText("Save changes for this cargo?");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        String sql = "UPDATE cargo SET "
                                + "description=?, "
                                + "weight_kg=?, "
                                + "volume_cbm=?, "
                                + "is_hazardous=?, "
                                + "imdg_class=?, "
                                + "un_number=?, "
                                + "proper_shipping_name=? "
                                + "WHERE cargo_id=?";

                        PreparedStatement ps = con.prepareStatement(sql);

                        ps.setString(1, desc);
                        ps.setDouble(2, weight);
                        ps.setDouble(3, volume);
                        ps.setInt(4, haz ? 1 : 0);

                        if (finalImdg != null) {
                            ps.setString(5, finalImdg);
                        } else {
                            ps.setNull(5, java.sql.Types.VARCHAR);
                        }

                        if (finalUn != null) {
                            ps.setString(6, finalUn);
                        } else {
                            ps.setNull(6, java.sql.Types.VARCHAR);
                        }

                        if (finalPsn != null) {
                            ps.setString(7, finalPsn);
                        } else {
                            ps.setNull(7, java.sql.Types.VARCHAR);
                        }

                        ps.setInt(8, existing.getId());

                        ps.executeUpdate();
                        loadCargoFromDB();
                        con.close();
                        root.setCenter(buildListView());
                    }
                } else {
                    String sql = "INSERT INTO cargo (description, weight_kg, volume_cbm, is_hazardous, imdg_class, un_number, proper_shipping_name) VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, desc);
                    ps.setDouble(2, weight);
                    ps.setDouble(3, volume);
                    ps.setInt(4, haz ? 1 : 0);
                    ps.setString(5, finalImdg);
                    ps.setString(6, finalUn);
                    ps.setString(7, finalPsn);

                    ps.executeUpdate();

                    System.out.println(sql);
                    loadCargoFromDB();
                    con.close();
                    root.setCenter(buildListView());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                AppStyles.formLabel("Description"), descField,
                AppStyles.formLabel("Weight (kg)"), weightField,
                AppStyles.formLabel("Volume (cbm)"), volumeField,
                hazRow, hazFields,
                errorLabel, btnRow
        );

        content.getChildren().addAll(pageTitle, card);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.LIGHT_BLUE
                + "; -fx-background: " + AppStyles.LIGHT_BLUE + ";");
        return scroll;
    }

    private void loadCargoFromDB() {
        cargoList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM cargo");

            while (rs.next()) {
                cargoList.add(new Cargo(
                        rs.getInt("cargo_id"),
                        rs.getString("description"),
                        rs.getDouble("weight_kg"),
                        rs.getDouble("volume_cbm"),
                        rs.getInt("is_hazardous") == 1,
                        rs.getString("imdg_class"),
                        rs.getString("un_number"),
                        rs.getString("proper_shipping_name")
                ));
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

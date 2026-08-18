package anchor_wfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class HelpView {

    private static final String NAVY_BLUE = Dashboard.NAVY_BLUE;
    private static final String LIGHT_BLUE = Dashboard.LIGHT_BLUE;
    private static final String WHITE = Dashboard.WHITE;
    private static final String DARK_NAVY = Dashboard.DARK_NAVY;

    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        // Header
        content.getChildren().add(pageTitle("Help & Support"));

        // Help sections container — plain VBox so ALL outer panes can be open at once
        VBox helpContainer = new VBox(8);
        helpContainer.getChildren().addAll(
                createOuterPane("Dashboard & Navigation", dashboardSection()),
                createOuterPane("Customer Management", customersSection()),
                createOuterPane("Cargo Management", cargoSection()),
                createOuterPane("Vessel & Fleet Management", vesselFleetSection()),
                createOuterPane("Bookings & Shipments", bookingsSection()),
                createOuterPane("Billing & Invoicing", billingSection()),
                createOuterPane("Troubleshooting & FAQs", troubleshootingSection())
        );
        content.getChildren().add(helpContainer);
        content.getChildren().add(contactCard());

        Region spacer = new Region();
        spacer.setPrefHeight(20);
        content.getChildren().add(spacer);

        ScrollPane mainScrollPane = new ScrollPane(content);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setStyle("-fx-background: " + LIGHT_BLUE + "; -fx-background-color: transparent;");

        return mainScrollPane;
    }

    // ───────── OUTER ACCORDION PANE (top-level sections) ─────────
    private TitledPane createOuterPane(String title, VBox content) {
        TitledPane pane = new TitledPane(title, content);
        pane.setAnimated(true);
        pane.setExpanded(false);
        pane.setStyle(
                "-fx-font-size: 16px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: " + NAVY_BLUE + ";"
        );
        return pane;
    }

    // ───────── INNER ACCORDION PANE (items inside each section) ─────────
    // Lives in a plain VBox so multiple inner panes can be open simultaneously.
    private TitledPane createInnerPane(String title, javafx.scene.Node content) {
        TitledPane pane = new TitledPane(title, content);
        pane.setAnimated(true);
        pane.setExpanded(false);
        pane.setStyle(
                "-fx-font-size: 13px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #37474F; "
                + "-fx-background-color: white; "
                + "-fx-border-color: #CFD8DC; "
                + "-fx-border-radius: 6; "
                + "-fx-background-radius: 6;"
        );
        return pane;
    }

    // ───────── SECTION CONTENT ─────────
    private VBox dashboardSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10, 0, 10, 0));
        v.getChildren().addAll(
                createInnerPane("Dashboard Overview",
                        infoCard("Dashboard Overview",
                                "The Dashboard provides real-time operational metrics and alerts.",
                                bulletList(
                                        "Active Shipments - shipments currently in progress",
                                        "Pending Invoices - unpaid invoices requiring attention",
                                        "Vessel Status Summary - quick summary of fleet availability",
                                        "Delivered This Month - track monthly delivery performance",
                                        "Recent Bookings - latest bookings with shipper/consignee info"
                                )
                        )
                )
        );
        return v;
    }

    private VBox customersSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10, 0, 10, 0));
        v.getChildren().addAll(
                createInnerPane("Adding a New Customer",
                        stepGuide("Adding a New Customer", new String[]{
                    "Navigate to Customers tab",
                    "Click \"Add New Customer\" button",
                    "Fill in all required fields: Full Name, Email, Phone, Address, Role",
                    "Customer ID is auto-generated - no need to enter manually",
                    "Click Save - confirmation dialog will appear"
                })
                ),
                createInnerPane("Editing a Customer",
                        stepGuide("Editing a Customer", new String[]{
                    "Locate the customer in the table",
                    "Click the Edit button in the Actions column",
                    "Update the required fields",
                    "Click Save Changes - confirmation will appear"
                })
                ),
                createInnerPane("Deleting a Customer",
                        wrapInVBox(
                                stepGuide("Deleting a Customer", new String[]{
                            "WARNING: This action cannot be undone!",
                            "Click Delete button in the Actions column",
                            "Confirm deletion in the dialog box",
                            "All associated bookings and shipments will also be deleted"
                        }),
                                warningBox(
                                        "IMPORTANT: Deleting a customer removes ALL associated bookings, shipments, and invoices. "
                                       
                                )
                        )
                )
        );
        return v;
    }

    private VBox cargoSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10, 0, 10, 0));
        v.getChildren().addAll(
                createInnerPane("Registering New Cargo",
                        stepGuide("Registering New Cargo", new String[]{
                    "Go to Cargo tab",
                    "Click \"Add New Cargo\" button",
                    "Fill cargo details: Name, Type, Weight, Dimensions, Hazard Class (if applicable)",
                    "For hazardous cargo: Make sure to classify them properly and provide a proper shipping name.",
                    "Click Save - confirmation will appear"
                })
                ),
                createInnerPane("Hazardous Cargo Guidelines",
                        wrapInVBox(
                                infoCard("What is Hazardous Cargo?",
                                        "When registering cargo, check \"Is Hazardous?\" and fill in the IMDG Class, "
                                        + "UN Number, and Proper Shipping Name. The system uses 9 IMDG classes — "
                                        + "each has specific handling, storage, and container requirements listed below.",
                                        bulletList(
                                                "UN Number format: UN followed by a 4-digit code (e.g. UN1203 for gasoline)",
                                                "Proper Shipping Name: the official IMDG name, not a trade name",
                                                "Always verify IMDG class with the cargo supplier's Safety Data Sheet (SDS)"
                                        )
                                ),
                                imdgClassCard("Class 1 — Explosives",
                                        "Gunpowder, fireworks, airbag inflators, ammunition.",
                                        new String[]{
                                            "Requires special government permits before loading",
                                            "Must be kept away from heat sources and open flames",
                                            "Cannot be stored near Classes 2, 3, 4, 5, 6, or 8",
                                            "Dedicated escort and security protocols required"
                                        },
                                        new String[]{"Flat Rack 20ft", "Flat Rack 40ft", "Platform 20ft", "Platform 40ft"}
                                ),
                                imdgClassCard("Class 2 — Gases",
                                        "Compressed gas, LPG, aerosols, acetylene, oxygen cylinders.",
                                        new String[]{
                                            "Must be upright and secured to prevent tipping",
                                            "Ensure adequate ventilation — never seal in airtight space",
                                            "Flammable gases: keep away from ignition sources",
                                            "Toxic gases: require leak-proof containment and gas detectors"
                                        },
                                        new String[]{"Tank 20ft", "Ventilated 20ft", "Open Top 20ft", "Open Top 40ft"}
                                ),
                                imdgClassCard("Class 3 — Flammable Liquids",
                                        "Gasoline, diesel, alcohol, paint, acetone, kerosene.",
                                        new String[]{
                                            "Use only approved sealed, leak-proof containers",
                                            "Keep below flash point temperature during transit",
                                            "No open flames or sparks within the storage area",
                                            "Reefer containers recommended for heat-sensitive variants"
                                        },
                                        new String[]{"Tank 20ft", "Reefer 20ft", "Reefer 40ft", "Standard 20ft (Dry)"}
                                ),
                                imdgClassCard("Class 4 — Flammable Solids",
                                        "Matches, sulfur, metal powders, self-reactive substances.",
                                        new String[]{
                                            "Keep dry — moisture can trigger spontaneous combustion",
                                            "Ensure cool, well-ventilated storage environment",
                                            "Self-reactive sub-types may need temperature control",
                                            "Segregate from oxidizers (Class 5) at all times"
                                        },
                                        new String[]{"Standard 20ft (Dry)", "Standard 40ft (Dry)", "Ventilated 20ft", "Half Height 20ft"}
                                ),
                                imdgClassCard("Class 5 — Oxidizers & Organic Peroxides",
                                        "Hydrogen peroxide, ammonium nitrate, bleach, benzoyl peroxide.",
                                        new String[]{
                                            "Must NEVER be stored with flammable materials (Classes 3 & 4)",
                                            "Keep in cool, dry areas — heat accelerates decomposition",
                                            "Organic peroxides may require refrigerated transport",
                                            "Containers must be clearly labeled with Class 5 placard"
                                        },
                                        new String[]{"Reefer 20ft", "Reefer 40ft", "Standard 20ft (Dry)", "Standard 40ft (Dry)"}
                                ),
                                imdgClassCard("Class 6 — Toxic & Infectious Substances",
                                        "Pesticides, medical waste, bacteria cultures, diagnostic specimens.",
                                        new String[]{
                                            "Personnel must use appropriate PPE during handling",
                                            "Infectious substances require UN-certified leak-proof packaging",
                                            "Must be segregated from foodstuffs and drinking water",
                                            "Spillage response plan must be prepared before shipment"
                                        },
                                        new String[]{"Reefer 20ft", "Reefer 40ft", "Standard 20ft (Dry)", "Standard 40ft (Dry)"}
                                ),
                                imdgClassCard("Class 7 — Radioactive Materials",
                                        "Medical isotopes, uranium ore, X-ray equipment, nuclear fuel rods.",
                                        new String[]{
                                            "Requires IAEA and government regulatory clearance",
                                            "Must maintain minimum separation distance from personnel",
                                            "Shielded packaging mandatory — check transport index (TI)",
                                            "Notify port authority in advance of arrival"
                                        },
                                        new String[]{"Standard 20ft (Dry)", "Standard 40ft (Dry)", "Platform 20ft", "Platform 40ft"}
                                ),
                                imdgClassCard("Class 8 — Corrosive Substances",
                                        "Sulfuric acid, hydrochloric acid, sodium hydroxide, batteries.",
                                        new String[]{
                                            "Use corrosion-resistant lined containers only",
                                            "Containers must be inspected for integrity before loading",
                                            "Liquid corrosives require secondary containment (drip trays)",
                                            "Keep away from Class 1, 4, and 5 cargo at all times"
                                        },
                                        new String[]{"Tank 20ft", "Open Top 20ft", "Open Top 40ft", "Standard 20ft (Dry)"}
                                ),
                                imdgClassCard("Class 9 — Miscellaneous Hazardous Materials",
                                        "Lithium batteries, dry ice, magnetized materials, elevated-temperature substances.",
                                        new String[]{
                                            "Lithium batteries: must not exceed state-of-charge limits for air transport",
                                            "Dry ice: ventilation required to prevent CO₂ buildup",
                                            "Magnetized materials: must be shielded to avoid compass interference",
                                            "Elevated-temp substances: use insulated or reefer containers"
                                        },
                                        new String[]{"Standard 20ft (Dry)", "Standard 40ft (Dry)", "Reefer 20ft", "Reefer 40ft"}
                                ),
                                warningBox(
                                        "Never load incompatible IMDG classes in the same container. "
                                        + "Always check the IMDG Segregation Table before assigning cargo to a shipment. "
                                      
                                )
                        )
                ),
                createInnerPane("IMDG Segregation Table",
                        buildSegregationTable()
                ),
                createInnerPane("Deletion Warning",
                        warningBox(
                                "NOTE: Cannot delete cargo that is associated with confirmed bookings. "
                                + "Remove cargo from all bookings first before attempting deletion."
                        )
                )
        );
        return v;
    }

    private VBox vesselFleetSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10, 0, 10, 0));

        // --- Vessels ---
        v.getChildren().addAll(
                createInnerPane("Vessel Management Overview",
                        infoCard("Vessel Management Overview",
                                "The Vessels tab manages your entire fleet. Key information tracked:",
                                bulletList(
                                        "Vessel ID - unique identifier (auto-generated)",
                                        "Name & Type - ship classification and specifications",
                                        "Capacity - maximum weight (MT) and volume (CBM)",
                                        "Status - Active, Docked, or Under Maintenance",
                                        "Registration Number - official vessel registration",
                                        "Assigned Crew - crew members currently assigned"
                                )
                        )
                ),
                createInnerPane("Registering a New Vessel",
                        stepGuide("Registering a New Vessel", new String[]{
                    "Go to Vessels tab",
                    "Click \"Register New Vessel\" button",
                    "Fill vessel details: Name, Type, Capacity Weight, Capacity Volume, Registration Number",
                    "Select initial Status (default: Active)",
                    "IMPORTANT: Assign at least 1 crew member from the list",
                    "Click Register Vessel to save"
                })
                ),
                createInnerPane("Updating Vessel Status",
                        stepGuide("Updating Vessel Status", new String[]{
                    "Locate the vessel in the table",
                    "Click the Status button in Actions column",
                    "Select new status: Active / Docked / Under Maintenance",
                    "Confirm the change"
                })
                ),
                createInnerPane("Vessel Status Meanings",
                        infoCard("Vessel Status Meanings",
                                bulletList(
                                        "Active - vessel is operational and available for assignments",
                                        "Docked - vessel is at port, not currently sailing",
                                        "Under Maintenance - vessel is being repaired/serviced"
                                )
                        )
                ),
                // --- Crew ---
                createInnerPane("Crew Management Overview",
                        infoCard("Crew Management Overview",
                                "The Crew tab manages all vessel crew members.",
                                bulletList(
                                        "Crew ID - unique identifier",
                                        "Name, Role, Contact Info - basic crew details",
                                        "License Number & Expiry - certification tracking",
                                        "Assigned Vessel - current vessel assignment"
                                )
                        )
                ),
                createInnerPane("Adding a Crew Member",
                        stepGuide("Adding a Crew Member", new String[]{
                    "Go to Crew tab",
                    "Click \"Add Crew Member\" button",
                    "Fill: Full Name, Role/Position, Contact Info, License Number",
                    "Set License Expiry Date (important for compliance)",
                    "Click Add Crew Member to save"
                })
                ),
                createInnerPane("Assigning Crew to Vessel",
                        stepGuide("Assigning Crew to Vessel", new String[]{
                    "Method 1: During vessel registration/editing",
                    "Method 2: Crew tab -> Click \"Assign to Vessel\" button",
                    "Select a crew member from dropdown",
                    "Select a vessel from dropdown",
                    "Click OK - the crew member is now assigned",
                    "Crew can be reassigned to different vessels as needed"
                })
                ),
                createInnerPane("License Management",
                        wrapInVBox(
                                infoCard("License Management",
                                        bulletList(
                                                "Expired licenses are highlighted in red in the table",
                                                "Update license expiry dates when crew renews certification",
                                                "System will warn if assigning crew with expired license"
                                        )
                                ),
                                warningBox(
                                        "A crew member can only be assigned to ONE vessel at a time. "
                                        + "Reassigning will automatically remove them from their previous vessel."
                                )
                        )
                ),
                // --- Ports ---
                createInnerPane("Ports Management",
                        wrapInVBox(
                                infoCard("Ports Management",
                                        "The Ports tab manages all port locations.",
                                        bulletList(
                                                "Port ID - unique identifier",
                                                "Port Name - official port name",
                                                "Country & City - port location"
                                        )
                                ),
                                stepGuide("Adding a Port", new String[]{
                            "Go to Ports tab",
                            "Click \"Add Port\" button",
                            "Fill: Port Name, Country, City",
                            "Click Add Port to save"
                        }),
                                warningBox(
                                        "Cannot delete a port that is used in existing routes. "
                                        + "Delete all routes referencing the port first."
                                )
                        )
                ),
                // --- Routes ---
                createInnerPane("Routes Management",
                        wrapInVBox(
                                infoCard("Routes Management",
                                        "The Routes tab defines shipping routes between ports.",
                                        bulletList(
                                                "Route ID - unique identifier",
                                                "Origin Port - departure port",
                                                "Destination Port - arrival port",
                                                "Transit Days - estimated sailing duration"
                                        )
                                ),
                                stepGuide("Creating a Route", new String[]{
                            "Go to Routes tab (requires at least 2 ports)",
                            "Click \"Add Route\" button",
                            "Select Origin Port from dropdown",
                            "Select Destination Port from dropdown",
                            "Enter Transit Days (estimated sailing days)",
                            "Click Add Route to save"
                        }),
                                infoCard("Route Planning Tips",
                                        bulletList(
                                                "Routes are directional - A->B is different from B->A",
                                                "Transit days affect shipment delivery estimates",
                                                "Consider weather patterns when setting transit days",
                                                "Seasonal routes may require different transit times"
                                        )
                                )
                        )
                ),
                // --- Containers ---
                createInnerPane("Containers Management",
                        wrapInVBox(
                                infoCard("Containers Management",
                                        "The Containers tab manages all shipping containers.",
                                        bulletList(
                                                "Container ID - unique identifier",
                                                "Container Number - tracking number",
                                                "Type - container classification (20ft, 40ft, etc.)",
                                                "Max Weight & Volume - capacity limits",
                                                "Status - Available, In Use, Maintenance"
                                        )
                                ),
                                stepGuide("Registering a Container", new String[]{
                            "Go to Containers tab",
                            "Click \"Register New Container\" button",
                            "Enter Container Number",
                            "Select Container Type (auto-fills weight and volume)",
                            "Select Status (default: Available)",
                            "Click Register Container to save"
                        }),
                                warningBox(
                                        "Cannot delete a vessel that has assigned shipments. "
                                        + "Transfer or complete all shipments before deletion."
                                )
                        )
                )
        );
        return v;
    }

    private VBox bookingsSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10, 0, 10, 0));
        v.getChildren().addAll(
                createInnerPane("Bookings Overview",
                        infoCard("Bookings Overview",
                                "Bookings represent customer shipping requests before operational assignment.",
                                bulletList(
                                        "Booking ID - unique tracking number",
                                        "Shipper - customer sending the cargo",
                                        "Consignee - customer receiving the cargo",
                                        "Cargo - items being shipped",
                                        "Status - Pending, Confirmed, Converted, Void",
                                        "Notes - special instructions or remarks"
                                )
                        )
                ),
                createInnerPane("Creating a Booking",
                        stepGuide("Creating a Booking", new String[]{
                    "Go to Bookings tab",
                    "Click \"Create Booking\" button",
                    "Select Shipper from dropdown (must exist in Customers)",
                    "Select Consignee from dropdown",
                    "Select Cargo item (must exist in Cargo)",
                    "Add any special notes or instructions",
                    "Click Save Booking"
                })
                ),
                createInnerPane("Converting Booking to Shipment",
                        wrapInVBox(
                                stepGuide("Converting Booking to Shipment", new String[]{
                            "Locate the booking in the table",
                            "Click \"Convert to Shipment\" button",
                            "System creates a linked Shipment record",
                            "Booking status changes to 'Converted'",
                            "Shipment can now be assigned vessel, route, and container"
                        }),
                                warningBox(
                                        "Once a booking is converted to a shipment, it cannot be edited or voided. "
                                        + "Make all necessary changes before conversion."
                                )
                        )
                )
        );
        return v;
    }

    private VBox billingSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10, 0, 10, 0));
        v.getChildren().addAll(
                createInnerPane("Creating an Invoice",
                        stepGuide("Creating an Invoice", new String[]{
                    "Go to Billing tab",
                    "Click \"Create Invoice\" button",
                    "Select Shipment (must be in 'Delivered' status)",
                    "Select Customer (auto-populates from shipment)",
                    "Enter Freight Rate (cost per unit)",
                    "Add Surcharges if applicable (fuel, insurance, handling)",
                    "Review Subtotal, Surcharges Total, and Grand Total",
                    "Click Generate Invoice"
                })
                ),
                createInnerPane("Recording Payment",
                        stepGuide("Recording Payment", new String[]{
                    "Find the invoice in the table",
                    "Click \"Log Payment\" button",
                    "Enter payment amount",
                    "Add reference number if applicable",
                    "Click Record Payment",
                    "Invoice status updates to 'Paid' when fully paid"
                })
                ),
                createInnerPane("Invoice Status Meanings",
                        infoCard("Invoice Status Meanings",
                                bulletList(
                                        "Pending - Invoice created, awaiting payment",
                                        "Paid - Payment received in full",
                                        "Overdue - Payment past due date",
                                        "Cancelled - Invoice voided or cancelled"
                                )
                        )
                ),
                createInnerPane("Surcharge Types",
                        infoCard("Surcharge Types",
                                bulletList(
                                        "Fuel Surcharge - variable based on fuel prices",
                                        "Insurance Fee - cargo insurance coverage",
                                        "Handling Fee - port handling and documentation",
                                        "Emergency Surcharge - unexpected operational costs"
                                )
                        )
                )
        );
        return v;
    }

    private VBox troubleshootingSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10, 0, 10, 0));
        v.getChildren().addAll(
                createInnerPane("Common Error Messages",
                        infoCard("Common Error Messages",
                                bulletList(
                                        "\"Please assign at least X crew member(s)\" -> Add more crew before saving vessel",
                                        "\"Container number already exists\" -> Each container must have unique number",
                                        "\"Cannot delete: record has dependencies\" -> Remove linked records first",
                                        "\"Please fill in all fields\" -> Check for empty required fields",
                                        "\"Cargo weight exceeds vessel capacity\" -> Reduce cargo or use larger vessel"
                                )
                        )
                ),
                createInnerPane("Quick Fixes",
                        infoCard("Quick Fixes",
                                bulletList(
                                        "Table not refreshing? Click on another tab and come back",
                                        "Form not saving? Check for red error messages at bottom",
                                        "Crew assignment not working? Ensure vessel exists first",
                                        "Can't find a record? Use the search bar above each table"
                                )
                        )
                )
        );
        return v;
    }

    // ───────── IMDG SEGREGATION TABLE ─────────
    private VBox buildSegregationTable() {
        VBox wrapper = new VBox(20);
        wrapper.setPadding(new Insets(12, 0, 16, 0));

        // Title & description
        Label title = new Label("IMDG Segregation Requirements");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#1A237E"));

        Label desc = new Label(
                "Minimum segregation distances required between different IMDG hazard classes when stowed on the same vessel.\n"
                + "Apply the stricter requirement when multiple classes are present. Table is symmetric.\n"
                + "For Class 9 (Miscellaneous Dangerous Goods), always consult the Dangerous Goods List (DGL) for specific segregation provisions."
        );
        desc.setFont(Font.font("Arial", 13));
        desc.setWrapText(true);
        desc.setTextFill(Color.web("#37474F"));

        // ── Table data ──────────────────────────────────────────────────────────
        String[] classNumbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] classNames = {
            "Explosives", "Gases", "Flammable Liquids",
            "Flammable Solids", "Oxidizers", "Toxic",
            "Radioactive", "Corrosive", "Miscellaneous"
        };

        // Segregation matrix (-1 = consult DGL, 0 = same class diagonal, 1 = Away from,
        // 2 = Separated from, 3 = Separate compartment, 4 = Separate longitudinally, 9 = Prohibited)
        // Updated: Class 9 now uses -1 (Consult DGL) instead of "no restriction"
        int[][] seg = {
            //   1    2    3    4    5    6    7    8    9
            {0, 4, 9, 9, 9, 2, 2, 9, -1}, // 1
            {4, 0, 2, 1, 2, 1, 1, 1, -1}, // 2
            {9, 2, 0, 1, 2, 1, 2, 1, -1}, // 3
            {9, 1, 1, 0, 2, 1, 2, 1, -1}, // 4
            {9, 2, 2, 2, 0, 2, 2, 2, -1}, // 5
            {2, 1, 1, 1, 2, 0, 2, 1, -1}, // 6
            {2, 1, 2, 2, 2, 2, 0, 1, -1}, // 7
            {9, 1, 1, 1, 2, 1, 1, 0, -1}, // 8
            {-1, -1, -1, -1, -1, -1, -1, -1, 0}, // 9
        };

        int n = classNumbers.length;
        double cellWidth = 58;
        double cellHeight = 48;
        double rowLabelWidth = 165;

        // ── Grid container ──────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(3);
        grid.setVgap(3);
        grid.setPadding(new Insets(6));
        grid.setStyle("-fx-background-color: #B0BEC5; -fx-background-radius: 8;");

        // Top-left corner cell
        Label corner = new Label("Class →\n↓ Class");
        corner.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        corner.setAlignment(Pos.CENTER);
        corner.setPrefSize(rowLabelWidth, cellHeight);
        corner.setStyle("-fx-background-color: #37474F; -fx-text-fill: white; -fx-background-radius: 6;");
        GridPane.setConstraints(corner, 0, 0);
        grid.getChildren().add(corner);

        // Column headers (class numbers)
        for (int c = 0; c < n; c++) {
            Label header = new Label(classNumbers[c]);
            header.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            header.setAlignment(Pos.CENTER);
            header.setPrefSize(cellWidth, cellHeight);
            header.setStyle("-fx-background-color: #37474F; -fx-text-fill: white; -fx-background-radius: 6;");
            GridPane.setConstraints(header, c + 1, 0);
            grid.getChildren().add(header);
        }

        // Rows: row label + data cells
        for (int r = 0; r < n; r++) {
            // Row label (class number + short name)
            Label rowLabel = new Label(classNumbers[r] + "  " + classNames[r]);
            rowLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            rowLabel.setTextFill(Color.WHITE);
            rowLabel.setAlignment(Pos.CENTER_LEFT);
            rowLabel.setPadding(new Insets(0, 0, 0, 10));
            rowLabel.setPrefSize(rowLabelWidth, cellHeight);
            rowLabel.setStyle("-fx-background-color: #37474F; -fx-background-radius: 6;");
            GridPane.setConstraints(rowLabel, 0, r + 1);
            grid.getChildren().add(rowLabel);

            // Data cells
            for (int c = 0; c < n; c++) {
                int val = seg[r][c];
                String code;
                String bgColor;
                String textColor;
                String tooltip;

                switch (val) {
                    case -1:
                        code = "?";
                        bgColor = "#E3F2FD";
                        textColor = "#1565C0";
                        tooltip = "Consult Dangerous Goods List (DGL) - requirements vary by substance";
                        break;
                    case 0:
                        code = "╲";
                        bgColor = "#ECEFF1";
                        textColor = "#90A4AE";
                        tooltip = "Same class (not applicable)";
                        break;
                    case 1:
                        code = "1";
                        bgColor = "#FFF9C4";
                        textColor = "#F57F17";
                        tooltip = "Away from – minimum 3 meters separation";
                        break;
                    case 2:
                        code = "2";
                        bgColor = "#FFE0B2";
                        textColor = "#E65100";
                        tooltip = "Separated from – different compartment or hold";
                        break;
                    case 3:
                        code = "3";
                        bgColor = "#FFCCBC";
                        textColor = "#BF360C";
                        tooltip = "Separated by a complete compartment";
                        break;
                    case 4:
                        code = "4";
                        bgColor = "#FFCDD2";
                        textColor = "#B71C1C";
                        tooltip = "Separated longitudinally – at least 24 meters apart";
                        break;
                    default:
                        code = "X";
                        bgColor = "#C62828";
                        textColor = "#FFFFFF";
                        tooltip = "Prohibited – cannot be stowed on same vessel";
                        break;
                }

                Label cell = new Label(code);
                cell.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                cell.setTextFill(Color.web(textColor));
                cell.setAlignment(Pos.CENTER);
                cell.setPrefSize(cellWidth, cellHeight);
                cell.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 6;");
                cell.setTooltip(new Tooltip(tooltip));
                GridPane.setConstraints(cell, c + 1, r + 1);
                grid.getChildren().add(cell);
            }
        }

        // ── Legend panel (enlarged) ─────────────────────────────────────────────
        VBox legendBox = new VBox(12);
        legendBox.setPadding(new Insets(14));
        legendBox.setStyle(
                "-fx-background-color: #FAFAFA;"
                + "-fx-border-color: #CFD8DC;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
        );

        Label legendTitle = new Label("Segregation Code Legend");
        legendTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        legendTitle.setTextFill(Color.web("#1A237E"));

        GridPane legendGrid = new GridPane();
        legendGrid.setHgap(25);
        legendGrid.setVgap(10);
        legendGrid.setPadding(new Insets(5, 0, 0, 0));

        Object[][] legendItems = {
            {"?", "#E3F2FD", "#1565C0", "Consult DGL - check individual schedule"},
            {"╲", "#ECEFF1", "#90A4AE", "Same class (not applicable)"},
            {"1", "#FFF9C4", "#F57F17", "Away from (≥3m)"},
            {"2", "#FFE0B2", "#E65100", "Separated from (different hold)"},
            {"3", "#FFCCBC", "#BF360C", "Separate compartment"},
            {"4", "#FFCDD2", "#B71C1C", "Separate longitudinally (≥24m)"},
            {"X", "#C62828", "#FFFFFF", "Prohibited (same vessel forbidden)"}
        };

        for (int i = 0; i < legendItems.length; i++) {
            Object[] item = legendItems[i];
            Label codeLabel = new Label((String) item[0]);
            codeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            codeLabel.setAlignment(Pos.CENTER);
            codeLabel.setPrefSize(44, 34);
            codeLabel.setStyle(
                    "-fx-background-color: " + item[1] + ";"
                    + "-fx-background-radius: 6;"
            );
            codeLabel.setTextFill(Color.web((String) item[2]));

            Label descLabel = new Label((String) item[3]);
            descLabel.setFont(Font.font("Arial", 13));
            descLabel.setTextFill(Color.web("#37474F"));

            GridPane.setConstraints(codeLabel, 0, i);
            GridPane.setConstraints(descLabel, 1, i);
            legendGrid.getChildren().addAll(codeLabel, descLabel);
        }

        legendBox.getChildren().addAll(legendTitle, legendGrid);

        // Footnote
        Label footnote = new Label(
                "Based on IMDG Code (Amdt. 41-22). Segregation requirements are minimum distances. "
                + "Always consult the full IMDG Code and the Dangerous Goods List (DGL) for special provisions. "
                + "Class 9 (Miscellaneous Dangerous Goods) requires individual schedule review."
        );
        footnote.setFont(Font.font("Arial", 11));
        footnote.setTextFill(Color.web("#607D8B"));
        footnote.setWrapText(true);

        wrapper.getChildren().addAll(title, desc, grid, legendBox, footnote);
        return wrapper;
    }



    /**
     * Utility: creates a uniform styled label cell for the grid.
     */
    private Label styledCell(String text, double w, double h,
            String fg, String bg, boolean center) {
        Label lbl = new Label(text);
        lbl.setPrefSize(w, h);
        lbl.setMinSize(w, h);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(fg));
        lbl.setAlignment(center ? Pos.CENTER : Pos.CENTER_LEFT);
        lbl.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 4;");
        return lbl;
    }

    // ───────── IMDG CLASS CARD ─────────
    // Shows class title, example goods, handling rules, and recommended containers.
    private VBox imdgClassCard(String classTitle, String examples,
            String[] rules, String[] containers) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #FFF8E1;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: #FFD54F;"
                + "-fx-border-radius: 8;"
                + "-fx-border-width: 1;"
        );

        Label titleLbl = new Label(classTitle);
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLbl.setTextFill(Color.web("#E65100"));

        Label examplesLbl = new Label("Examples: " + examples);
        examplesLbl.setFont(Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 12));
        examplesLbl.setWrapText(true);
        examplesLbl.setTextFill(Color.web("#5D4037"));

        // Handling rules
        Label rulesLbl = new Label("Handling Rules:");
        rulesLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        rulesLbl.setTextFill(Color.web("#37474F"));
        rulesLbl.setPadding(new Insets(4, 0, 0, 0));

        VBox rulesList = new VBox(3);
        rulesList.setPadding(new Insets(0, 0, 0, 14));
        for (String rule : rules) {
            Label r = new Label("• " + rule);
            r.setFont(Font.font("Arial", 12));
            r.setWrapText(true);
            r.setTextFill(Color.web("#37474F"));
            rulesList.getChildren().add(r);
        }

        // Recommended containers
        Label containerLbl = new Label("Recommended Containers:");
        containerLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        containerLbl.setTextFill(Color.web("#37474F"));
        containerLbl.setPadding(new Insets(4, 0, 0, 0));

        javafx.scene.layout.FlowPane containerTags = new javafx.scene.layout.FlowPane(6, 6);
        containerTags.setPadding(new Insets(2, 0, 0, 0));
        for (String c : containers) {
            Label tag = new Label(c);
            tag.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            tag.setPadding(new Insets(3, 8, 3, 8));
            tag.setStyle(
                    "-fx-background-color: #E3F2FD;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-color: #90CAF9;"
                    + "-fx-border-radius: 12;"
                    + "-fx-border-width: 1;"
            );
            tag.setTextFill(Color.web("#1565C0"));
            containerTags.getChildren().add(tag);
        }

        card.getChildren().addAll(titleLbl, examplesLbl, rulesLbl, rulesList,
                containerLbl, containerTags);
        return card;
    }

    // ───────── HELPER: wrap multiple nodes into a single VBox for createInnerPane ─────────
    private VBox wrapInVBox(javafx.scene.Node... nodes) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(8, 0, 4, 0));
        box.getChildren().addAll(nodes);
        return box;
    }

    // ───────── HELPER COMPONENTS ─────────
    private Label pageTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lbl.setTextFill(Color.web(NAVY_BLUE));
        return lbl;
    }

    private VBox stepGuide(String title, String[] steps) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: #E0E0E0;"
                + "-fx-border-radius: 8;"
                + "-fx-border-width: 1;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(NAVY_BLUE));

        VBox stepsBox = new VBox(4);
        stepsBox.setPadding(new Insets(5, 0, 0, 20));

        for (int i = 0; i < steps.length; i++) {
            Label step = new Label((i + 1) + ". " + steps[i]);
            step.setFont(Font.font("Arial", 12));
            step.setWrapText(true);
            stepsBox.getChildren().add(step);
        }

        box.getChildren().addAll(titleLabel, stepsBox);
        return box;
    }

    private VBox infoCard(String title, String description, VBox content) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #E3F2FD;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: #90CAF9;"
                + "-fx-border-radius: 8;"
                + "-fx-border-width: 1;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web(NAVY_BLUE));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setWrapText(true);
        descLabel.setTextFill(Color.web("#37474F"));

        card.getChildren().addAll(titleLabel, descLabel, content);
        return card;
    }

    private VBox infoCard(String title, VBox content) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: #E8F5E9;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: #A5D6A7;"
                + "-fx-border-radius: 8;"
                + "-fx-border-width: 1;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        card.getChildren().addAll(titleLabel, content);
        return card;
    }

    private VBox bulletList(String... items) {
        VBox v = new VBox(4);
        v.setPadding(new Insets(0, 0, 0, 16));
        for (String item : items) {
            Label lbl = new Label("- " + item);
            lbl.setFont(Font.font("Arial", 12));
            lbl.setWrapText(true);
            v.getChildren().add(lbl);
        }
        return v;
    }

    private HBox warningBox(String text) {
        Label lbl = new Label("Warning: " + text);
        lbl.setWrapText(true);
        lbl.setFont(Font.font("Arial", 12));
        HBox box = new HBox(lbl);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle(
                "-fx-background-color: #FFF3E0;"
                + "-fx-background-radius: 6;"
                + "-fx-border-color: #FFB74D;"
                + "-fx-border-radius: 6;"
                + "-fx-border-width: 1;"
        );
        return box;
    }

    private VBox contactCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + NAVY_BLUE + ";"
                + "-fx-background-radius: 10;"
        );

        Label title = new Label("Need More Help?");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(WHITE));

        Label email = new Label("Contact your system administrator.");
        email.setFont(Font.font("Arial", 12));
        email.setTextFill(Color.web("#E0E0E0"));

        Label phone = new Label("Phone: +63 912 397 4932");
        phone.setFont(Font.font("Arial", 12));
        phone.setTextFill(Color.web("#E0E0E0"));

        card.getChildren().addAll(title, email, phone);
        return card;
    }
}

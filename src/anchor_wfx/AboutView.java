package anchor_wfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class AboutView {

    private static final String NAVY_BLUE  = Dashboard.NAVY_BLUE;
    private static final String LIGHT_BLUE = Dashboard.LIGHT_BLUE;
    private static final String WHITE      = Dashboard.WHITE;
    private static final String DARK_NAVY  = AppStyles.DARK_NAVY;

    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        // 1. Page Title
        content.getChildren().add(pageTitle("About This System"));

        // 2. FIXED CARD: System Information (Hindi naka-toggle/accordion)
        content.getChildren().add(fixedSystemInfoCard());

        // 3. ACCORDION: Para sa Devs at Academic Info lang
        Accordion accordion = new Accordion();
        
        TitledPane devPane = new TitledPane("👨‍💻 Developers", developersSection());
        TitledPane academicPane = new TitledPane("🏫 Other Information", institutionSection());

        accordion.getPanes().addAll(devPane, academicPane);
        
        content.getChildren().add(accordion);

        // ScrollPane settings para sa background fill
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: " + LIGHT_BLUE + "; -fx-background: " + LIGHT_BLUE + ";");
        
        return scroll;
    }

    // ───────── FIXED CARD SECTION ─────────

    private VBox fixedSystemInfoCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + WHITE + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-radius: 12 12 12 12; " + // Kailangan din para sa border
            "-fx-border-color: " + NAVY_BLUE + "; " +
            "-fx-border-width: 3 0 0 0; " + // Navy accent sa taas
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label header = new Label("🚢 System Information");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setTextFill(Color.web(NAVY_BLUE));

        card.getChildren().addAll(
            header,
            new Separator(),
            AppStyles.infoRow("System Name", "ANCHOR"),
            new Separator(),
            AppStyles.infoRow("Full System Title", "Admin Navigation for Cargo Handling and Operations Records"),
            new Separator(),
            AppStyles.infoRow("Version", "1.0"),
            new Separator(),
            AppStyles.infoRow("Platform", "Desktop Application (JavaFX)"),
            new Separator(),
            descriptionLabel(
                "ANCHOR is a desktop-based administrative system designed for vessel-owning cargo shipping companies. " +
                "It centralises management of key operations including customers, cargo, vessels, crew, bookings, " +
                "shipments, and billing into a single, unified platform. The system streamlines administrative workflows, " +
                "ensures accurate record-keeping, and supports operational decision-making."
            )
        );
        return card;
    }

    // ───────── ACCORDION SECTIONS ─────────

    private VBox developersSection() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(10));
        
        Label sectionHeader = new Label("BSIT 2A | Developers:");
        sectionHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        v.getChildren().add(sectionHeader);
        
        String[] devs = {
            "Alapide, Sweet Arabella S.",
            "Bartolome, Pollyne Anne G.",
            "Loreto, Arlene Joy C.",
            "Sumala, John Aldrin S."
        };

        for (String name : devs) {
            Label lbl = new Label("• " + name);
            lbl.setFont(Font.font("Arial", 14));
            v.getChildren().add(lbl);
        }
        return v;
    }

    private VBox institutionSection() {
        VBox v = new VBox(10);
        v.setPadding(new Insets(10));
        v.getChildren().addAll(
            AppStyles.infoRow("Submitted to", "Mr. Mark David P. Otayde"),
            new Separator(),
            AppStyles.infoRow("Course", "IT 208 | Advanced Database Systems"),
            new Separator(),
            AppStyles.infoRow("Institution", "Bulacan State University - Hagony Campus "),
            new Separator(),
            AppStyles.infoRow("Date", "April 2026"),
            new Separator(),
            AppStyles.infoRow("©", "2026 ANCHOR. All rights reserved.")
        );
        return v;
    }

    // ───────── HELPERS ─────────

    private Label pageTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lbl.setTextFill(Color.web(NAVY_BLUE));
        return lbl;
    }

    private Label descriptionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", 13));
        lbl.setTextFill(Color.web("#555555"));
        lbl.setWrapText(true);
        return lbl;
    }
}
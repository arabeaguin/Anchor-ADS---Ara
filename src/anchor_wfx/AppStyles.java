package anchor_wfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Shared style constants and helper methods used across all views.
 */
public class AppStyles {

    public static final String NAVY_BLUE  = "#003B73";
    public static final String LIGHT_BLUE = "#C9D6EA";
    public static final String WHITE      = "#FFFFFF";
    public static final String DARK_NAVY  = "#002050";

    // ── Form helpers ───────────────────────────────────────────────────────
    public static TextField formField(String prompt, String value) {
        TextField f = new TextField(value);
        f.setPromptText(prompt);
        f.setPrefHeight(40);
        f.setStyle(fieldStyle());
        return f;
    }

    public static Label formLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        l.setTextFill(Color.web("#555555"));
        return l;
    }

    public static String fieldStyle() {
        return "-fx-background-radius: 8; -fx-border-radius: 8; " +
               "-fx-border-color: #b0b8c9; -fx-border-width: 1.5; " +
               "-fx-background-color: white; -fx-font-size: 14; -fx-padding: 8 12 8 12;";
    }

    public static String comboStyle() {
        return "-fx-background-color: white; -fx-border-color: #b0b8c9; " +
               "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;";
    }

    // ── Button helpers ─────────────────────────────────────────────────────
    public static void styleOutlineBtn(Button btn, boolean hovered) {
        btn.setStyle(
            "-fx-background-color: " + (hovered ? NAVY_BLUE : WHITE) + "; " +
            "-fx-text-fill: " + (hovered ? "white" : NAVY_BLUE) + "; " +
            "-fx-border-color: " + NAVY_BLUE + "; -fx-border-width: 2; " +
            "-fx-border-radius: 5; -fx-background-radius: 5; " +
            "-fx-font-size: 13; -fx-cursor: hand;"
        );
    }

    public static void stylePrimaryBtn(Button btn, boolean hovered) {
        btn.setStyle(
            "-fx-background-color: " + (hovered ? DARK_NAVY : NAVY_BLUE) + "; " +
            "-fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"
        );
    }

    public static void styleAddBtn(Button btn, boolean hovered) {
        btn.setStyle(
            "-fx-background-color: " + (hovered ? DARK_NAVY : NAVY_BLUE) + "; " +
            "-fx-text-fill: white; -fx-background-radius: 8; " +
            "-fx-cursor: hand; -fx-padding: 0 16 0 16;"
        );
    }

    // ── Card helpers ───────────────────────────────────────────────────────
    public static VBox formCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 3);"
        );
        return card;
    }

    public static VBox infoCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setMaxWidth(640);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 2);"
        );
        return card;
    }

    // ── Info row (key-value) ───────────────────────────────────────────────
    public static HBox infoRow(String key, String value) {
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

    // ── Section title ──────────────────────────────────────────────────────
    public static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        l.setTextFill(Color.web(NAVY_BLUE));
        return l;
    }

    // ── Error label ────────────────────────────────────────────────────────
    public static Label errorLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.web("#CC0000"));
        l.setVisible(false);
        l.setManaged(false);
        return l;
    }

    public static void showError(Label l, String msg) {
        l.setText(msg);
        l.setVisible(true);
        l.setManaged(true);
    }

    public static void hideError(Label l) {
        l.setVisible(false);
        l.setManaged(false);
    }

    // ── Action buttons (edit/delete) in table cells ────────────────────────
    public static Button editButton() {
        Button btn = new Button("✏");
        btn.setStyle(
            "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #1565C0; -fx-text-fill: white; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        return btn;
    }

    public static Button deleteButton() {
        Button btn = new Button("🗑");
        btn.setStyle(
            "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #C62828; -fx-text-fill: white; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14;"
        ));
        return btn;
    }
}
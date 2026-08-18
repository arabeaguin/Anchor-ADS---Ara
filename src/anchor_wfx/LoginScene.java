/*
 * Login Scene for ANCHOR ADS - Cargo Handling System
 */
package anchor_wfx;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.sql.*;
import javafx.animation.PauseTransition;

public class LoginScene {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private static final String VALID_USERNAME = "anchor";
    private static final String VALID_PASSWORD = "anchor123";

    private final String NAVY_BLUE = "#003B73";

    public Scene createScene(Stage primaryStage) {

        StackPane root = new StackPane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        // Full-screen background image
        BackgroundImage bgImage = loadBackgroundImage();
        if (bgImage != null) {
            root.setBackground(new Background(bgImage));
        } else {
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #3a6fa8, #1a4a7a);");
        }

        // Form floats over background, right-aligned
        VBox form = buildLoginForm(primaryStage);
        StackPane.setAlignment(form, Pos.CENTER_RIGHT);
        StackPane.setMargin(form, new Insets(175, 90, 0, 0)); //adjust ung position ng login

        root.getChildren().add(form);

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    // ── Background loader ──────────────────────────────────────────────────
    private BackgroundImage loadBackgroundImage() {
        String[] paths = {
            "src/anchor_wfx/Images/login_bg.png",
            "src/Images/login_bg.png",
            "src/Images/login_bg.jpg",
            "Images/login_bg.png",
            "Images/login_bg.jpg",
            "src/anchor_ads/Images/login_bg.png",
            "src/anchor_ads/Images/login_bg.jpg",
            "login_bg.png",
            "login_bg.jpg"
        };
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                try {
                    Image img = new Image(f.toURI().toString());
                    return new BackgroundImage(
                            img,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(
                                    BackgroundSize.AUTO, BackgroundSize.AUTO,
                                    false, false, true, true
                            )
                    );
                } catch (Exception e) {
                    System.err.println("Could not load login background: " + e.getMessage());
                }
            }
        }
        return null;
    }

    // ── Login form (no card background, no header text) ────────────────────
    private VBox buildLoginForm(Stage primaryStage) {
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPrefWidth(420);
        form.setMaxWidth(420);
        // Transparent background — fields float directly on the bg image
        form.setStyle("-fx-background-color: transparent;");
        // Shift the whole form 30px further left via left padding
        form.setPadding(new Insets(0, 0, 0, 0));
        form.setTranslateX(-50);
        // ── Admin label + field ────────────────────────────────────────────
        Label adminLabel = new Label("Admin");
        adminLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        adminLabel.setTextFill(Color.WHITE);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(44);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        styleTextField(usernameField, false);
        addTextFieldFocusEffect(usernameField);

        // ── Password label + field ─────────────────────────────────────────
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        passLabel.setTextFill(Color.WHITE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(44);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        styleTextField(passwordField, false);
        addTextFieldFocusEffect(passwordField);

        // ── Error label declaration ────────────────────────────────────────────────────
        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        errorLabel.setTextFill(Color.web("#FF4444"));
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setMaxWidth(Double.MAX_VALUE);

        Label successLabel = new Label("Login successful!");
        successLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        successLabel.setTextFill(Color.web("#00C851"));
        successLabel.setVisible(false);
        successLabel.setManaged(false);
        successLabel.setAlignment(Pos.CENTER);
        successLabel.setMaxWidth(Double.MAX_VALUE);

        // ── Button row: Clear left | Log In right ──────────────────────────
        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.setPadding(new Insets(6, 0, 0, 0));
        buttonRow.setMaxWidth(Double.MAX_VALUE);

        Button clearBtn = new Button("Clear");
        clearBtn.setPrefSize(120, 44);
        clearBtn.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        styleClearButton(clearBtn, false);
        addButtonHoverEffect(clearBtn, false);
        addButtonClickEffect(clearBtn);
        clearBtn.setOnAction(e -> {
            usernameField.clear();
            passwordField.clear();
            styleTextField(usernameField, false);
            styleTextField(passwordField, false);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });

        Button loginBtn = new Button("Log In");
        loginBtn.setPrefSize(150, 44);
        loginBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        styleLoginButton(loginBtn, false);
        addButtonHoverEffect(loginBtn, true);
        addButtonClickEffect(loginBtn);

        DropShadow btnShadow = new DropShadow();
        btnShadow.setColor(Color.rgb(0, 0, 0, 0.35));
        btnShadow.setRadius(10);
        btnShadow.setOffsetY(3);
        loginBtn.setEffect(btnShadow);

        // Spacer pushes Log In to the right end of the text field
        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);

        buttonRow.getChildren().addAll(clearBtn, btnSpacer, loginBtn);

        // ── Login logic ────────────────────────────────────────────────────
        Runnable doLogin = () -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                if (user.isEmpty()) {
                    styleTextField(usernameField, true);
                }
                if (pass.isEmpty()) {
                    styleTextField(passwordField, true);
                }
                return;
            }

            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'active'"
                );
                ps.setString(1, user);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    Session.setUser(rs.getString("username"));
                    Session.setRole(rs.getString("role"));
                    Permission.load(rs.getString("role"));
                    con.close();

                    // Show success message first
                    successLabel.setVisible(true);
                    successLabel.setManaged(true);
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);

                    // Then switch to dashboard after short delay
                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(e -> {
                        Dashboard dashboard = new Dashboard();
                        Scene dashScene = dashboard.createScene(primaryStage, WINDOW_WIDTH, WINDOW_HEIGHT);
                        primaryStage.setScene(dashScene);
                    });
                    pause.play();
                } else {

                    con.close();
                    errorLabel.setText("Invalid username or password.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    styleTextField(usernameField, true);
                    styleTextField(passwordField, true);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                errorLabel.setText("Database connection error.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }

        };

        loginBtn.setOnAction(e -> doLogin.run());
        passwordField.setOnAction(e -> doLogin.run());
        usernameField.setOnAction(e -> doLogin.run());

        usernameField.textProperty().addListener((obs, o, n) -> {
            styleTextField(usernameField, false);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });
        passwordField.textProperty().addListener((obs, o, n) -> {
            styleTextField(passwordField, false);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });

        form.getChildren().addAll(
                spacer(10),
                adminLabel,
                usernameField,
                spacer(4),
                passLabel,
                passwordField,
                errorLabel,
                successLabel, // ← add this
                spacer(15),
                buttonRow
        );

        return form;
    }

    // ── Style helpers ──────────────────────────────────────────────────────
    private void styleTextField(Control field, boolean isError) {
        String base
                = "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-padding: 8 12 8 12;"
                + "-fx-font-size: 14;";
        if (isError) {
            field.setStyle(base
                    + "-fx-border-color: #FF4444; -fx-border-width: 2;"
                    + "-fx-background-color: rgba(255,255,255,0.92);");
        } else {
            field.setStyle(base
                    + "-fx-border-color: #b0b8c9; -fx-border-width: 1.5;"
                    + "-fx-background-color: rgba(255,255,255,0.92);");
        }
    }

    private void addTextFieldFocusEffect(Control field) {
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!field.getStyle().contains("#FF4444")) {
                field.setStyle(
                        "-fx-background-radius: 8;"
                        + "-fx-border-radius: 8;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-font-size: 14;"
                        + "-fx-border-color: " + (isFocused ? NAVY_BLUE : "#b0b8c9") + ";"
                        + "-fx-border-width: " + (isFocused ? "2" : "1.5") + ";"
                        + "-fx-background-color: rgba(255,255,255,0.92);"
                );
            }
        });
    }

    private void styleClearButton(Button btn, boolean hovered) {
        btn.setStyle(
                "-fx-background-color: " + (hovered ? "rgba(255,255,255,0.25)" : "rgba(255,255,255,0.10)") + ";"
                + "-fx-text-fill: white;"
                + "-fx-border-color: white;"
                + "-fx-border-width: 2;"
                + "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-cursor: hand;"
        );
    }

    private void styleLoginButton(Button btn, boolean hovered) {
        btn.setStyle(
                "-fx-background-color: " + (hovered ? "#002a56" : NAVY_BLUE) + ";"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-cursor: hand;"
        );
    }

    private void addButtonHoverEffect(Button btn, boolean isPrimary) {
        btn.setOnMouseEntered(e -> {
            if (isPrimary) {
                styleLoginButton(btn, true);
            } else {
                styleClearButton(btn, true);
            }
        });
        btn.setOnMouseExited(e -> {
            if (isPrimary) {
                styleLoginButton(btn, false);
            } else {
                styleClearButton(btn, false);
            }
        });
    }

    private void addButtonClickEffect(Button btn) {
        btn.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            st.setToX(0.94);
            st.setToY(0.94);
            st.play();
        });
        btn.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private Region spacer(double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }
}

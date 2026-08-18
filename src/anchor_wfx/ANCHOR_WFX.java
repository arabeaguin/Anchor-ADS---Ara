/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package anchor_wfx;

import java.io.File;
import java.sql.Connection;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;

/**
 *
 * @author rdony
 */
public class ANCHOR_WFX extends Application {
    
    private static final int WINDOW_WIDTH  = 1280;
    private static final int WINDOW_HEIGHT = 720;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ANCHOR: Admin Navigation for Cargo Handling and Operations Records");
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(false);
        try {
            // load icon
            Image icon = new Image(getClass().getResourceAsStream("/anchor_ads/Images/anchor_logo2.png"));
            //set icon
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Can't find icon file: " + e.getMessage());
        }
        // Opening video → Login → Dashboard
        showOpeningScene(primaryStage);

        primaryStage.show();
    }

    private void showOpeningScene(Stage primaryStage) {
        try {
            File videoFile = findVideoFile();

            if (videoFile == null || !videoFile.exists()) {
                System.err.println("Video file not found. Skipping to login.");
                showLoginScene(primaryStage);
                return;
            }

            System.out.println("Loading video from: " + videoFile.getAbsolutePath());

            Media media = new Media(videoFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);

            mediaView.setFitWidth(WINDOW_WIDTH);
            mediaView.setFitHeight(WINDOW_HEIGHT);
            mediaView.setPreserveRatio(false);

            StackPane openingPane = new StackPane();
            openingPane.setAlignment(Pos.CENTER);
            openingPane.getChildren().add(mediaView);
            openingPane.setStyle("-fx-background-color: black;");

            Scene openingScene = new Scene(openingPane, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(openingScene);

            // Video finished → show login
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose();
                showLoginScene(primaryStage);
            });

            // Video error → show login
            mediaPlayer.setOnError(() -> {
                System.err.println("Error playing video: " + mediaPlayer.getError().getMessage());
                mediaPlayer.dispose();
                showLoginScene(primaryStage);
            });

            mediaPlayer.play();

            // Click to skip → show login
            openingPane.setOnMouseClicked(event -> {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                showLoginScene(primaryStage);
            });

        } catch (Exception e) {
            System.err.println("Error loading opening scene: " + e.getMessage());
            e.printStackTrace();
            showLoginScene(primaryStage);
        }
    }

    // ── Login scene ────────────────────────────────────────────────────────
    private void showLoginScene(Stage primaryStage) {
        LoginScene loginScene = new LoginScene();
        Scene scene = loginScene.createScene(primaryStage);
        primaryStage.setScene(scene);
    }

    // ── Dashboard (called from LoginScene after successful auth) ───────────
    // LoginScene navigates to Dashboard directly via the Stage reference,
    // but this helper stays here in case it is needed elsewhere.
    private void showDashboard(Stage primaryStage) {
        Dashboard dashboard = new Dashboard();
        Scene dashboardScene = dashboard.createScene(primaryStage, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(dashboardScene);
    }

    // ── Locate video file ──────────────────────────────────────────────────
    private File findVideoFile() {
        String[] possiblePaths = {
            "src/anchor_wfx/Videos/ANCHOR_openingScene1.mp4",
            
            
            "src/Videos/ANCHOR_openingScene1.mp4",
            "Videos/ANCHOR_openingScene1.mp4",
            "src/anchor_ads/Videos/ANCHOR_openingScene1.mp4",
            "ANCHOR_openingScene1.mp4",
            "src/ANCHOR_openingScene1.mp4",
            "resources/ANCHOR_openingScene1.mp4",
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("Found video at: " + path);
                return file;
            }
        }

        System.err.println("Video file not found. Searched in:");
        for (String path : possiblePaths) {
            System.err.println("  - " + new File(path).getAbsolutePath());
        }

        return null;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

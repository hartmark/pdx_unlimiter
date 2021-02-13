package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.SavedState;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.stream.Collectors;

public class PdxuApp extends Application {

    private static PdxuApp APP;
    private Image icon;
    private StackPane layout;
    private boolean windowActive;

    public static PdxuApp getApp() {
        return APP;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Scene getScene() {
        return layout.getScene();
    }

    public void setupWindowState() {
        Platform.runLater(() -> {
            Scene scene = getScene();
            Window w = getScene().getWindow();

            var s = SavedState.getInstance();

            if (s.getWindowX() != SavedState.INVALID) w.setX(s.getWindowX());
            if (s.getWindowY() != SavedState.INVALID) w.setY(s.getWindowY());
            if (s.getWindowWidth() != SavedState.INVALID) w.setWidth(s.getWindowWidth());
            if (s.getWindowHeight() != SavedState.INVALID) w.setHeight(s.getWindowHeight());

            scene.getWindow().xProperty().addListener((c, o, n) -> {
                if (windowActive) s.setWindowX(n.intValue());
            });
            scene.getWindow().yProperty().addListener((c, o, n) -> {
                if (windowActive) s.setWindowY(n.intValue());
            });
            scene.getWindow().widthProperty().addListener((c, o, n) -> {
                if (windowActive) s.setWindowWidth(n.intValue());
            });
            scene.getWindow().heightProperty().addListener((c, o, n) -> {
                if (windowActive) s.setWindowHeight(n.intValue());
            });

            ((Stage) w).show();
            windowActive = true;
        });
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            APP = this;
            icon = new Image(PdxuApp.class.getResourceAsStream("logo.png"));
            primaryStage.getIcons().add(icon);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    windowActive = false;
                    ComponentManager.finalTeardown();

                    // Close windows after teardown, otherwise saved window coordinates will be invalid!
                    Stage.getWindows().stream()
                            .filter(w -> !w.equals(getScene().getWindow()))
                            .collect(Collectors.toList())
                            .forEach(w -> w.fireEvent(event));
                }
            });

            layout = GuiLayout.createLayout();

            primaryStage.setTitle("Pdx-Unlimiter (" + PdxuInstallation.getInstance().getVersion() + ")");
            Scene scene = new Scene(layout, 1000, 720);
            primaryStage.setScene(scene);
            GuiStyle.addStylesheets(primaryStage.getScene());
            ComponentManager.additionalSetup();
        } catch (Exception ex) {
            ErrorHandler.handleTerminalException(ex);
        }

    }

    public Image getIcon() {
        return icon;
    }
}

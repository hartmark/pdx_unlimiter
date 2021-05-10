package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.lang.PdxuI18n;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GuiSettings {

    public static void showSettings() {
        Alert alert = GuiDialogHelper.createEmptyAlert();
        alert.getButtonTypes().add(ButtonType.APPLY);
        alert.getButtonTypes().add(ButtonType.CANCEL);
        alert.setTitle(PdxuI18n.get("SETTINGS"));

        Settings s = Settings.getInstance();
        Set<Runnable> applyFuncs = new HashSet<>();
        VBox vbox = new VBox(
                GuiSettingsComponents.section("GAME_DIRS", applyFuncs, s.eu4, s.ck3, s.hoi4, s.stellaris, s.ck2, s.vic2),
                new Separator(),
                GuiSettingsComponents.section("GENERAL", applyFuncs, s.enableAutoUpdate, s.storageDirectory, s.startSteam),
                new Separator(),
                GuiSettingsComponents.section("IMPORTS", applyFuncs,
                        s.deleteOnImport,
                        s.enabledTimedImports,
                        s.timedImportsInterval,
                        s.playSoundOnBackgroundImport),
                new Separator(),
                GuiSettingsComponents.section("INTERFACE", applyFuncs,
                        s.fontSize,
                        s.confirmDeletion,
                        s.useGameLanguage,
                        s.language),
                new Separator(),
                GuiSettingsComponents.section("IRONY", applyFuncs, s.ironyDir, s.launchIrony),
                new Separator(),
                GuiSettingsComponents.section("RAKALY", applyFuncs, s.rakalyUserId, s.rakalyApiKey),
                new Separator(),
                GuiSettingsComponents.section("CONVERTERS", applyFuncs, s.ck3toeu4Dir),
                new Separator(),
                GuiSettingsComponents.section("EU4SE", applyFuncs, s.enableEu4SaveEditor),
                new Separator(),
                GuiSettingsComponents.section("SKANDERBEG", applyFuncs, s.skanderbegApiKey));
        vbox.setSpacing(10);
        var sp = new ScrollPane(vbox);
        sp.setFitToWidth(true);
        alert.getDialogPane().setContent(sp);
        sp.setPrefWidth(650);
        sp.setPrefHeight(600);
        vbox.getStyleClass().add("settings-content");
        sp.getStyleClass().add("settings-container");

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().equals(ButtonType.APPLY)) {
            ComponentManager.reloadSettings(() -> applyFuncs.forEach(ru -> ru.run()));
        }
    }
}

package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;

import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_ENTRY_LIST;

public class GuiGameCampaignEntryList {

    public static Node createCampaignEntryList() {
        JFXListView<Node> grid = new JFXListView<>();
        grid.setOpacity(0.9);
        grid.getStyleClass().add(CLASS_ENTRY_LIST);

        SetChangeListener<GameCampaignEntry> l = (c) -> {
            Platform.runLater(() -> {
                if (c.wasAdded()) {
                    int index = GameIntegration.globalSelectedCampaignProperty().get().indexOf(c.getElementAdded());
                    grid.getItems().add(index, GuiGameCampaignEntry.createCampaignEntryNode(c.getElementAdded()));
                } else {
                    grid.getItems().remove(grid.getItems().stream()
                            .filter(n -> !c.getSet().contains(n.getProperties().get("entry"))).findAny().get());
                }
            });
        };

        GameIntegration.globalSelectedCampaignProperty().addListener((c, o, n) -> {
            if (o != null) {
                o.getEntries().removeListener(l);
            }

            if (n != null) {
                n.getEntries().addListener(l);
                Platform.runLater(() -> {
                    grid.setItems(FXCollections.observableArrayList(n.entryStream()
                            .map(GuiGameCampaignEntry::createCampaignEntryNode)
                            .collect(Collectors.toList())));
                });
            } else {
                Platform.runLater(() -> {
                    grid.setItems(FXCollections.observableArrayList());
                });
            }
        });

        GameIntegration.globalSelectedEntryProperty().addListener((c, o, n) -> {
            if (n != null) {
                int index = GameIntegration.globalSelectedCampaignProperty().get().indexOf(n);
                grid.scrollTo(index);
                grid.getSelectionModel().select(index);
                grid.getFocusModel().focus(index);
            } else {
                grid.getSelectionModel().clearSelection();
                grid.getFocusModel().focus(-1);
            }
        });

        return grid;
    }
}

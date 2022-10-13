package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.gui.GuiCoaViewer;
import com.crschnick.pdxu.editor.gui.GuiCoaViewerState;
import com.crschnick.pdxu.editor.gui.GuiEditorNodeTagFactory;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodePointer;
import com.jfoenix.controls.JFXButton;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.*;

public class Vic3SavegameAdapter implements EditorSavegameAdapter {

    static class CoaPreview extends GuiEditorNodeTagFactory {
        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            if (!state.isContextGameEnabled()) {
                return false;
            }

            if (node.getBackingNode().isArray()) {
                ArrayNode ar = (ArrayNode) node.getBackingNode();
                return ar.hasKey("pattern") || ar.hasKey("colored_emblem");
            }
            return false;
        }

        @Override
        public javafx.scene.Node create(EditorState state, EditorRealNode node, Region valueDisplay) {
            var b = new JFXButton(null, new FontIcon());
            b.setGraphic(new FontIcon());
            b.getStyleClass().add("coa-button");
            GuiTooltips.install(b, "Open in coat of arms preview window");
            b.setOnAction(e -> {
                var viewer = new GuiCoaViewer<>(new GuiCoaViewerState.Vic3GuiCoaViewerState(state, node));
                viewer.createStage();
            });
            return b;
        }
    }

    static class ImagePreview extends GuiEditorNodeTagFactory.ImagePreviewNodeTagFactory {

        private final String nodeName;

        public ImagePreview(Path base, String nodeName, String icon) {
            super(icon, node -> GameInstallation.ALL.get(Game.VIC3).getInstallDir().resolve("game")
                    .resolve(base).resolve(node.getBackingNode().getString()));
            this.nodeName = nodeName;
        }

        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            return state.isContextGameEnabled() && node.getKeyName().map(k -> k.equals(nodeName)).orElse(false);
        }
    }
    
    private static final Set<String> CACHED_KEYS = Set.of("domain_limit");

    private static final List<GuiEditorNodeTagFactory> FACTORIES = List.of(
            new CoaPreview(),
            new GuiEditorNodeTagFactory.CacheTagFactory(CACHED_KEYS),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("patterns"), "pattern", "mdi-file"),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("colored_emblems"), "texture", "mdi-file"),
            new GuiEditorNodeTagFactory.InfoNodeTagFactory(Set.of("meta_data"),
                    "This node contains basic information and the meta data of this savegame that is shown in the main menu. " +
                            "Editing anything inside of it, excluding mods and DLCs, only changes the main menu display, not the actual data in-game. " +
                            "Do not edit this node if you want to change something in-game, except applied mods and DLCs"));

    @Override
    public Game getGame() {
        return Game.VIC3;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) throws Exception {
        return Map.of();
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) throws Exception {
        return null;
    }

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return FACTORIES.stream()
                .filter(fac -> fac.checkIfApplicable(state, node))
                .findFirst().map(fac -> fac.create(state, node, valueDisplay))
                .orElse(null);
    }
}

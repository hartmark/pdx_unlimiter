package com.crschnick.pdxu.editor.gui;

import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.CoatOfArms;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.util.LinkedHashMap;

public abstract class GuiVic3CoaDisplayType extends GuiCoaDisplayType {

    public static GuiVic3CoaDisplayType NONE = new GuiVic3CoaDisplayType() {

        @Override
        public Image render(CoatOfArms coa, GameFileContext ctx) {
            return ImageHelper.toFXImage(
                    Ck3TagRenderer.renderImage(coa, ctx, size.get(), false));
        }
    };

    public static void init(GuiCoaViewerState.Vic3GuiCoaViewerState state, HBox box) {
        HBox options = new HBox();
        options.setSpacing(10);

        var sizes = new LinkedHashMap<String, Number>();
        sizes.put("64 x 64", 64);
        sizes.put("128 x 128", 128);
        sizes.put("256 x 256", 256);
        sizes.put("512 x 512", 512);
        box.getChildren().add(createChoices("Size", 256, sizes, t -> {
            state.getDisplayType().size.set(t.intValue());
            state.updateImage();
        }));
        state.getDisplayType().size.set(256);


        var types = new LinkedHashMap<String, GuiVic3CoaDisplayType>();
        types.put("None", NONE);

        box.getChildren().add(options);
    }
}

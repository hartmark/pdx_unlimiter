package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameLocalisation;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.*;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class Eu4GuiFactory extends GameGuiFactory<Eu4Tag, Eu4SavegameInfo> {

    public Eu4GuiFactory() {
        super(GameInstallation.EU4);
    }

    private static Region createRulerLabel(Eu4SavegameInfo.Ruler ruler, boolean isRuler) {
        VBox box = new VBox();
        if (isRuler) {
            box.getChildren().add(new Label(ruler.getName(), imageNode(EU4_ICON_RULER, CLASS_RULER_ICON)));
        } else {
            box.getChildren().add(new Label(ruler.getName(), imageNode(EU4_ICON_HEIR, CLASS_RULER_ICON)));
        }

        box.alignmentProperty().set(Pos.CENTER);
        box.getChildren().add(createRulerStatsNode(ruler));
        box.getStyleClass().add(CLASS_RULER);
        return box;
    }

    private static Region createRulerStatsNode(Eu4SavegameInfo.Ruler ruler) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        Label adm = new Label(ruler.getAdm() + "  ", imageNode(EU4_ICON_ADM, CLASS_POWER_ICON));
        box.getChildren().add(adm);

        Label dip = new Label(ruler.getDip() + "  ", imageNode(EU4_ICON_DIP, CLASS_POWER_ICON));
        box.getChildren().add(dip);

        Label mil = new Label(String.valueOf(ruler.getMil()), imageNode(EU4_ICON_MIL, CLASS_POWER_ICON));
        box.getChildren().add(mil);
        return box;
    }

    private static String getCountryTooltip(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, Set<Eu4Tag> tags) {
        StringBuilder b = new StringBuilder();
        for (Eu4Tag t : tags) {
            b.append(GameLocalisation.getTagNameForEntry(entry, t));
            b.append(", ");
        }
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

    private void createDiplomacyRow(JFXMasonryPane pane, GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, Node icon, Set<Eu4Tag> tags, String tooltipStart, String none, String style) {
        if (tags.size() == 0) {
            return;
        }

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(icon);
        for (Eu4Tag tag : tags) {
            Node n = GameImage.imageNode(eu4TagNode(entry, tag), CLASS_TAG_ICON);
            box.getChildren().add(n);
        }
        box.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        box.getStyleClass().add(style);
        box.setSpacing(6);
        GuiTooltips.install(box, tooltipStart + (tags.size() > 0 ? getCountryTooltip(entry, tags) : none));
        addNode(pane, box);
    }

    @Override
    public Image tagImage(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, Eu4Tag tag) {
        return eu4TagNode(GameImage.getEu4TagPath(tag.getTag()), entry);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(
                Files.newInputStream(GameInstallation.EU4.getPath().resolve("launcher-assets").resolve("font.ttf")), 12);
    }

    @Override
    public Pane background() {
        return GameImage.backgroundNode(EU4_BACKGROUND);
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(EU4_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry) {
        return new Background(new BackgroundFill(
                ColorHelper.colorFromInt(entry.getInfo().getTag().getMapColor(), 100),
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public void fillNodeContainer(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, JFXMasonryPane grid) {
        Eu4SavegameInfo info = entry.getInfo();
        if (info.isObserver()) {
            super.fillNodeContainer(entry, grid);
            return;
        }

        addNode(grid, createRulerLabel(info.getRuler(), true));
        if (info.getHeir().isPresent()) {
            addNode(grid, createRulerLabel(info.getHeir().get(), false));
        }

        if (entry.getInfo().isIronman()) {
            var ironman = new StackPane(imageNode(EU4_ICON_IRONMAN, CLASS_IMAGE_ICON, null));
            ironman.setAlignment(Pos.CENTER);
            GuiTooltips.install(ironman, "Ironman savegame");
            addNode(grid, ironman);
        }

        if (entry.getInfo().isRandomNewWorld()) {
            var rnw = new StackPane(imageNode(EU4_ICON_RANDOM_NEW_WORLD, CLASS_IMAGE_ICON, null));
            rnw.setAlignment(Pos.CENTER);
            GuiTooltips.install(rnw, "Random new world enabled");
            addNode(grid, rnw);
        }

        if (entry.getInfo().isCustomNationInWorld()) {
            var cn = new StackPane(imageNode(EU4_ICON_CUSTOM_NATION, CLASS_IMAGE_ICON, null));
            cn.setAlignment(Pos.CENTER);
            GuiTooltips.install(cn, "A custom nation exists in the world");
            addNode(grid, cn);
        }

        if (entry.getInfo().isReleasedVassal()) {
            var rv = new StackPane(imageNode(EU4_ICON_RELEASED_VASSAL, CLASS_IMAGE_ICON, null));
            rv.setAlignment(Pos.CENTER);
            GuiTooltips.install(rv, "Is playing as a released vassal");
            addNode(grid, rv);
        }

        for (Eu4SavegameInfo.War war : info.getWars()) {
            createDiplomacyRow(grid, entry, imageNode(EU4_ICON_WAR, CLASS_IMAGE_ICON), war.getEnemies(),
                    "Fighting in the " + war.getTitle() + " against ", "", CLASS_WAR);
        }

        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_ALLIANCE, CLASS_IMAGE_ICON), info.getAllies(),
                "Allies: ", "None", CLASS_ALLIANCE);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_ROYAL_MARRIAGE, CLASS_IMAGE_ICON), info.getMarriages(),
                "Royal marriages: ", "None", CLASS_MARRIAGE);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_GUARANTEE, CLASS_IMAGE_ICON), info.getGuarantees(),
                "Guarantees: ", "None", CLASS_GUARANTEE);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_VASSAL, CLASS_IMAGE_ICON), info.getVassals(),
                "Vassals: ", "None", CLASS_VASSAL);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_VASSAL, CLASS_IMAGE_ICON), info.getJuniorPartners(),
                "Personal union junior partners: ", "none", CLASS_VASSAL);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_TRIBUTARY, CLASS_IMAGE_ICON), info.getTributaryJuniors(),
                "Tributaries: ", "None", CLASS_VASSAL);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_MARCH, CLASS_IMAGE_ICON), info.getMarches(),
                "Marches: ", "None", CLASS_VASSAL);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_TRUCE, CLASS_IMAGE_ICON),
                info.getTruces().keySet(), "Truces: ", "None", CLASS_TRUCE);
        createDiplomacyRow(grid, entry, imageNode(EU4_ICON_VASSAL, CLASS_IMAGE_ICON),
                info.getSeniorPartner().map(Set::of).orElse(Set.of()),
                "Under personal union with ", "no country", CLASS_VASSAL);

        super.fillNodeContainer(entry, grid);
    }

    private Image eu4TagNode(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, Eu4Tag tag) {
        return eu4TagNode(GameImage.getEu4TagPath(tag.getTag()), entry);
    }

    private Image eu4TagNode(Path path, GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry) {
        var in = CascadeDirectoryHelper.openFile(
                path, entry, GameInstallation.EU4);
        return ImageLoader.loadImage(in.orElse(null), null);
    }
}

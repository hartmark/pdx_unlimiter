package com.crschnick.pdx_unlimiter.app.core.settings;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuI18n;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.InvalidInstallationException;
import com.crschnick.pdx_unlimiter.app.util.InstallLocationHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public abstract class SettingsEntry<T> {

    protected final Supplier<String> name;
    protected final Supplier<String> description;
    protected final String serializationName;
    protected final Type type;
    protected ObjectProperty<T> value;


    public SettingsEntry(String id, String serializationName, Type type) {
        this.name = () -> PdxuI18n.get(id);
        this.serializationName = serializationName;
        this.description = () -> PdxuI18n.get(id + "_DESC");
        this.type = type;
        this.value = new SimpleObjectProperty<>();
    }

    public SettingsEntry(Supplier<String> name, Supplier<String> description, String serializationName, Type type) {
        this.name = name;
        this.description = description;
        this.serializationName = serializationName;
        this.type = type;
        this.value = new SimpleObjectProperty<>();
    }

    public abstract void set(JsonNode node);

    public abstract JsonNode toNode();

    public void set(T newValue) {
        value.set(newValue);
    }

    public abstract void setDefault();

    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public Type getType() {
        return type;
    }

    public T getValue() {
        return value.get();
    }

    public String getSerializationName() {
        return serializationName;
    }

    public enum Type {
        BOOLEAN,
        INTEGER,
        STRING,
        PATH
    }


    public static abstract class SimpleEntry<T> extends SettingsEntry<T> {

        private final T defaultValue;

        public SimpleEntry(String id, String serializationName, Type type, T defaultValue) {
            super(id, serializationName, type);
            this.defaultValue = defaultValue;
        }

        @Override
        public final void setDefault() {
            this.value.set(defaultValue);
        }
    }

    public static class BooleanEntry extends SimpleEntry<Boolean> {

        public BooleanEntry(String id, String serializationName, boolean defaultValue) {
            super(id, serializationName, Type.BOOLEAN, defaultValue);
        }

        @Override
        public void set(JsonNode node) {
            this.value.set(node.booleanValue());
        }

        @Override
        public JsonNode toNode() {
            return BooleanNode.valueOf(value.get());
        }
    }

    public static class IntegerEntry extends SimpleEntry<Integer> {

        private final int min;
        private final int max;

        public IntegerEntry(String id, String serializationName, int defaultValue, int min, int max) {
            super(id, serializationName, Type.INTEGER, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(Integer newValue) {
            //TODO check range
            super.set(newValue);
        }

        @Override
        public void set(JsonNode node) {
            this.value.set(node.intValue());
        }

        @Override
        public JsonNode toNode() {
            return new IntNode(value.get());
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public static class StringEntry extends SimpleEntry<String> {

        public StringEntry(String id, String serializationName, String defaultValue) {
            super(id, serializationName, Type.STRING, defaultValue);
        }

        @Override
        public void set(JsonNode node) {
            this.value.set(node.textValue());
        }

        @Override
        public JsonNode toNode() {
            return new TextNode(value.get());
        }
    }

    public static abstract class DirectoryEntry extends SettingsEntry<Path> {

        protected boolean disabled;

        public DirectoryEntry(String id, String serializationName) {
            super(id, serializationName, Type.PATH);
            this.disabled = false;
        }

        public DirectoryEntry(Supplier<String> name, Supplier<String> description, String serializationName) {
            super(name, description, serializationName, Type.PATH);
            this.disabled = false;
        }

        @Override
        public void set(Path newPath) {
            if (disabled && newPath != null) {
                disabled = false;
            }

            this.value.set(newPath);
        }

        @Override
        public void set(JsonNode node) {
            if (node.isNull()) {
                this.disabled = true;
                this.value.set(null);
            } else {
                this.disabled = false;
                this.value.set(Path.of(node.textValue()));
            }
        }

        @Override
        public JsonNode toNode() {
            if (disabled) {
                return NullNode.getInstance();
            } else if (value.isNull().get()) {
                return null;
            } else {
                return new TextNode(value.get().toString());
            }
        }
    }

    public static class GameDirectory extends DirectoryEntry {

        private final Game game;
        private final Class<? extends GameInstallation> installClass;

        GameDirectory(String serializationName, Game game, Class<? extends GameInstallation> installClass) {
            super(() -> PdxuI18n.get("GAME_DIR", game.getAbbreviation()),
                    () -> PdxuI18n.get("GAME_DIR_DESC", game.getAbbreviation()),
                    serializationName);
            this.game = game;
            this.installClass = installClass;
        }

        private void showInstallErrorMessage(String msg) {
            String fullMsg = PdxuI18n.get("GAME_DIR_ERROR", game.getFullName()) + ":\n" +
                    msg + "\n\n" + PdxuI18n.get("GAME_DIR_ERROR_MSG");
            GuiErrorReporter.showSimpleErrorMessage(fullMsg);
        }

        @Override
        public void set(Path newPath) {
            super.set(newPath);

            try {
                var i = (GameInstallation) installClass.getDeclaredConstructors()[0].newInstance(newPath);
                i.loadData();
                super.set(newPath);
            } catch (InvalidInstallationException e) {
                this.disabled = true;
                showInstallErrorMessage(PdxuI18n.get(e.getMessageId()));
            } catch (Exception e) {
                this.disabled = true;
                showInstallErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        @Override
        public void setDefault() {
            InstallLocationHelper.getSteamGameInstallPath(game.getFullName()).ifPresent(p -> {
                this.set(p);
            });
        }
    }

    public static class StorageDirectory extends DirectoryEntry {

        public StorageDirectory(String id, String serializationName) {
            super(id, serializationName);
        }

        @Override
        public void set(Path newPath) {
            if (newPath.equals(value.get())) {
                return;
            }

            if (FileUtils.listFiles(newPath.toFile(), null, false).size() > 0) {
                GuiErrorReporter.showSimpleErrorMessage("New storage directory " + newPath + " must be empty!");
            } else {
                try {
                    Files.delete(newPath);
                    FileUtils.moveDirectory(value.get().toFile(), newPath.toFile());
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
                this.value.set(newPath);
            }
        }

        @Override
        public void setDefault() {
            this.value.set(PdxuInstallation.getInstance().getDefaultSavegamesLocation());
        }
    }

    public static class ThirdPartyDirectory extends DirectoryEntry {

        private final Path checkFile;
        private final Supplier<Path> defaultValue;

        public ThirdPartyDirectory(String id, String serializationName, Path checkFile, Supplier<Path> defaultValue) {
            super(id, serializationName);
            this.checkFile = checkFile;
            this.defaultValue = defaultValue;
        }

        @Override
        public void set(Path newPath) {
            var file = newPath.resolve(checkFile);
            boolean found = Files.exists(file);
            if (!found) {
                showErrorMessage();
            } else {
                this.value.set(newPath);
            }
        }

        @Override
        public void setDefault() {
            var df = defaultValue.get();
            if (df == null) {
                this.value.set(null);
                this.disabled = false;
                return;
            }

            var file = df.resolve(checkFile);
            boolean found = Files.exists(file);
            if (found) {
                this.value.set(df);
            }
        }

        private void showErrorMessage() {
            GuiErrorReporter.showSimpleErrorMessage(PdxuI18n.get("THIRD_PARTY_ERROR"));
        }
    }
}

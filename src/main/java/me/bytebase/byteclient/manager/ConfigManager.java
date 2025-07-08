package me.bytebase.byteclient.manager;

import com.google.gson.*;
import me.bytebase.byteclient.ByteClient;
import me.bytebase.byteclient.features.Feature;
import me.bytebase.byteclient.features.settings.Bind;
import me.bytebase.byteclient.features.settings.EnumConverter;
import me.bytebase.byteclient.features.settings.Setting;
import me.bytebase.byteclient.util.traits.Jsonable;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigManager {
    private static final Path ByteClient_PATH = FabricLoader.getInstance().getGameDir().resolve("byteclient");
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private final List<Jsonable> jsonables = List.of(ByteClient.friendManager, ByteClient.moduleManager, ByteClient.commandManager);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setValueFromJson(Feature feature, Setting setting, JsonElement element) {
        String str;
        switch (setting.getType()) {
            case "Boolean" -> {
                setting.setValue(element.getAsBoolean());
            }
            case "Double" -> {
                setting.setValue(element.getAsDouble());
            }
            case "Float" -> {
                setting.setValue(element.getAsFloat());
            }
            case "Integer" -> {
                setting.setValue(element.getAsInt());
            }
            case "String" -> {
                str = element.getAsString();
                setting.setValue(str.replace("_", " "));
            }
            case "Bind" -> {
                setting.setValue(new Bind(element.getAsInt()));
            }
            case "Enum" -> {
                try {
                    EnumConverter converter = new EnumConverter(((Enum) setting.getValue()).getClass());
                    Enum value = converter.doBackward(element);
                    setting.setValue((value == null) ? setting.getDefaultValue() : value);
                } catch (Exception exception) {
                }
            }
            default -> {
                ByteClient.LOGGER.error("Unknown Setting type for: " + feature.getName() + " : " + setting.getName());
            }
        }
    }

    public void load() {
        if (!ByteClient_PATH.toFile().exists()) ByteClient_PATH.toFile().mkdirs();
        for (Jsonable jsonable : jsonables) {
            try {
                String read = Files.readString(ByteClient_PATH.resolve(jsonable.getFileName()));
                jsonable.fromJson(JsonParser.parseString(read));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        if (!ByteClient_PATH.toFile().exists()) ByteClient_PATH.toFile().mkdirs();
        for (Jsonable jsonable : jsonables) {
            try {
                JsonElement json = jsonable.toJson();
                Files.writeString(ByteClient_PATH.resolve(jsonable.getFileName()), gson.toJson(json));
            } catch (Throwable e) {
            }
        }
    }
}

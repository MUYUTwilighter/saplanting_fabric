package cool.muyucloud.saplanting.util;

import com.google.gson.*;
import cool.muyucloud.saplanting.Saplanting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.constant.Constable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Config {
    private static final Logger LOGGER = Saplanting.getLogger();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("saplanting.json");

    private final JsonObject properties;
    private final HashSet<String> langs;

    /**
     * Return a default set of Config.
     * */
    public Config() {
        this.langs = new HashSet<>();
        this.langs.add("en_us");
        this.langs.add("zh_cn");

        this.properties = new JsonObject();
        this.properties.addProperty("plantEnable", true);
        this.properties.addProperty("plantLarge", true);
        this.properties.addProperty("blackListEnable", true);
        this.properties.addProperty("multiThread", true);
        this.properties.addProperty("allowSapling", true);
        this.properties.addProperty("allowCrop", false);
        this.properties.addProperty("allowMushroom", false);
        this.properties.addProperty("allowFungus", false);
        this.properties.addProperty("allowFlower", false);
        this.properties.addProperty("allowOther", false);
        this.properties.addProperty("showTitleOnOpConnected", false);
        this.properties.addProperty("ignoreShape", false);
        this.properties.addProperty("plantDelay", 40);
        this.properties.addProperty("avoidDense", 2);
        this.properties.addProperty("playerAround", 2);
        this.properties.addProperty("language", "en_us");
        this.properties.add("blackList", new JsonArray());

        this.load();
        this.save();
    }

    /**
     * Return copy, feel free to modify return value.
     * blackList and language is not included.
     * */
    public Set<String> getKeySet() {
        HashSet<String> set = new HashSet<>(this.properties.keySet());
        set.remove("blackList");
        set.remove("language");
        return set;
    }

    /**
     * Not a copy! Please do not modify return value!
     * */
    public Set<String> getValidLangs() {
        return this.langs;
    }

    /**
     * Load from file and update properties, language and black list
     * */
    public boolean load() {
        if (!Files.exists(CONFIG_PATH)) {
            // try to create new config file
            LOGGER.info("saplanting.json does not exist, generating.");
            try {
                Files.createFile(CONFIG_PATH);
            } catch (Exception e) {
                LOGGER.error("Failed to generate config file at %s.".formatted(CONFIG_PATH));
                e.printStackTrace();
                return false;
            }
        }

        // Reading config file
        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            JsonObject read = (new Gson()).fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);

            // Analyzing properties
            for (String key : read.keySet()) {
                if (this.properties.has(key) && !Objects.equals(key, "blackList")) {
                    JsonPrimitive dst = this.properties.getAsJsonPrimitive(key);
                    JsonPrimitive src = read.get(key).getAsJsonPrimitive();
                    try {
                        if (dst.isBoolean()) {
                            this.properties.addProperty(key, src.getAsBoolean());
                        } else if (dst.isNumber()) {
                            this.properties.addProperty(key, src.getAsNumber());
                        } else {
                            if (Objects.equals(key, "language")) {
                                if (!langs.contains(src.getAsString())) {
                                    this.properties.addProperty(key, "en_us");
                                    continue;
                                }
                            }
                            this.properties.addProperty(key, src.getAsString());
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Problems occurred during analyzing property %s.".formatted(key));
                    }
                }
            }

            // Analyzing blackList
            if (read.has("blackList")) {
                this.properties.add("blackList", new JsonArray());
                JsonArray array = read.getAsJsonArray("blackList");
                JsonArray blackList = this.properties.getAsJsonArray("blackList");
                for (int i = 0; i < array.size(); ++i) {
                    JsonElement jsonElement = array.get(i);
                    Identifier id = new Identifier(jsonElement.getAsString());
                    Item item = Registry.ITEM.get(id);
                    if (Registry.ITEM.containsId(id) && !blackList.contains(jsonElement) && Saplanting.isPlantItem(item)) {
                        blackList.add(jsonElement);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.warn("Problems occurred during reading config file.");
            return false;
        }
    }

    /**
     * Dump current set of config into file. File will be overwritten.
     * */
    public boolean save() {
        String json = (new GsonBuilder().setPrettyPrinting().create()).toJson(this.properties);
        if (!Files.exists(CONFIG_PATH)) {
            // try to create new config file
            LOGGER.info("saplanting.json does not exist, generating.");
            try {
                Files.createFile(CONFIG_PATH);
            } catch (Exception e) {
                LOGGER.error("Failed to generate config file at %s.".formatted(CONFIG_PATH));
                e.printStackTrace();
                return false;
            }
        }

        try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            LOGGER.warn("Problems occurred during writing config file.");
            return false;
        }
    }

    public String getAsString(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsString();
    }

    public boolean getAsBoolean(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isBoolean()) {
            throw new ClassCastException("Value of property %s is not a bool.".formatted(key));
        }

        return this.properties.get(key).getAsBoolean();
    }

    public int getAsInt(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isNumber()) {
            throw new ClassCastException("Value of property %s is not a integer.".formatted(key));
        }

        return this.properties.get(key).getAsInt();
    }

    public boolean set(String key, boolean value) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isBoolean()) {
            throw new ClassCastException("Value of property %s is not a bool.".formatted(key));
        }

        if (primitive.getAsBoolean() == value) {
            return false;
        }

        this.properties.addProperty(key, value);
        return true;
    }

    public boolean set(String key, int value) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isNumber()) {
            throw new ClassCastException("Value of property %s is not a integer.".formatted(key));
        }

        if (primitive.getAsNumber().intValue() == value) {
            return false;
        }

        this.properties.addProperty(key, value);
        return true;
    }

    public boolean set(String key, String value) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isString()) {
            throw new ClassCastException("Value of property %s is not a string.".formatted(key));
        }

        if (Objects.equals(primitive.getAsString(), value)) {
            return false;
        }

        this.properties.addProperty(key, value);
        return true;
    }

    public Class<? extends Constable> getType(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        JsonPrimitive primitive = this.properties.get(key).getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return Boolean.class;
        } else if (primitive.isNumber()) {
            return Integer.class;
        }
        return String.class;
    }

    /**
     * Not check Saplanting.isPlantItem().
     * */
    public boolean addToBlackList(Item item) {
        String id = Registry.ITEM.getId(item).toString();
        JsonElement jsonElement = new JsonPrimitive(id);
        JsonArray blackList = this.properties.getAsJsonArray("blackList");

        if (blackList.contains(jsonElement)) {
            return false;
        }

        blackList.add(id);
        return true;
    }

    /**
     * Not check Saplanting.isPlantItem().
     * Check in black list.
     * */
    public boolean removeFromBlackList(Item item) {
        JsonElement jsonElement = new JsonPrimitive(Registry.ITEM.getId(item).toString());
        JsonArray blackList = this.properties.getAsJsonArray("blackList");

        if (!blackList.contains(jsonElement)) {
            return false;
        }

        blackList.remove(jsonElement);
        return true;
    }

    /**
     * Not check Saplanting.isPlantItem().
     * */
    public boolean inBlackList(Item item) {
        JsonElement jsonElement = new JsonPrimitive(Registry.ITEM.getId(item).toString());
        return this.properties.getAsJsonArray("blackList").contains(jsonElement);
    }

    public String stringConfigPath() {
        return CONFIG_PATH.toString();
    }

    /**
     * Return value is not a copy! Please do not modify!
     * */
    public JsonArray getBlackList() {
        return this.properties.getAsJsonArray("blackList");
    }

    public int blackListSize() {
        return this.properties.getAsJsonArray("blackList").size();
    }

    public void clearBlackList() {
        this.properties.add("blackList", new JsonArray());
    }
}

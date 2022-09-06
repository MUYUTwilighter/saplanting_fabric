package cool.muyucloud.saplanting.util;

import com.google.gson.*;
import cool.muyucloud.saplanting.Saplanting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Config {
    private static final Logger LOGGER = Saplanting.getLogger();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("saplanting.json");

    private final JsonObject properties;
    private final HashSet<String> langs;
    private final ArrayList<String> keySet;

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

        HashSet<String> set = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : this.properties.entrySet()) {
            set.add(entry.getKey());
        }
        set.remove("blackList");
        set.remove("language");
        this.keySet = set.stream().sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Not a copy! Please do not modify return value!
     * */
    public ArrayList<String> getKeySet() {
        return this.keySet;
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
                LOGGER.error(String.format("Failed to generate config file at %s.", CONFIG_PATH));
                e.printStackTrace();
                return false;
            }
        }

        // Reading config file
        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            JsonObject read = (new Gson()).fromJson(IOUtils.toString(inputStream, StandardCharsets.UTF_8), JsonObject.class);

            // Analyzing properties
            for (Map.Entry<String, JsonElement> entry : read.entrySet()) {
                String key = entry.getKey();
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
                        LOGGER.warn(String.format("Problems occurred during analyzing property %s.", key));
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
                LOGGER.error(String.format("Failed to generate config file at %s.", CONFIG_PATH));
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
            throw new NullPointerException(String.format("Tried to access property %s but it does not exists!", key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsString();
    }

    public boolean getAsBoolean(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException(String.format("Tried to access property %s but it does not exists!", key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isBoolean()) {
            throw new ClassCastException(String.format("Value of property %s is not a bool.", key));
        }

        return this.properties.get(key).getAsBoolean();
    }

    public int getAsInt(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException(String.format("Tried to access property %s but it does not exists!", key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isNumber()) {
            throw new ClassCastException(String.format("Value of property %s is not a integer.", key));
        }

        return this.properties.get(key).getAsInt();
    }

    public boolean set(String key, boolean value) {
        if (!this.properties.has(key)) {
            throw new NullPointerException(String.format("Tried to access property %s but it does not exists!", key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isBoolean()) {
            throw new ClassCastException(String.format("Value of property %s is not a bool.", key));
        }

        if (primitive.getAsBoolean() == value) {
            return false;
        }

        this.properties.addProperty(key, value);
        return true;
    }

    public boolean set(String key, int value) {
        if (!this.properties.has(key)) {
            throw new NullPointerException(String.format("Tried to access property %s but it does not exists!", key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isNumber()) {
            throw new ClassCastException(String.format("Value of property %s is not a integer.", key));
        }

        if (primitive.getAsNumber().intValue() == value) {
            return false;
        }

        this.properties.addProperty(key, value);
        return true;
    }

    public boolean set(String key, String value) {
        if (!this.properties.has(key)) {
            throw new NullPointerException(String.format("Tried to access property %s but it does not exists!", key));
        }

        JsonPrimitive primitive = this.properties.getAsJsonPrimitive(key);
        if (!primitive.isString()) {
            throw new ClassCastException(String.format("Value of property %s is not a string.", key));
        }

        if (Objects.equals(primitive.getAsString(), value)) {
            return false;
        }

        this.properties.addProperty(key, value);
        return true;
    }

    public Class<? extends Serializable> getType(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException(String.format("Tried to access property %s but it does not exists!", key));
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

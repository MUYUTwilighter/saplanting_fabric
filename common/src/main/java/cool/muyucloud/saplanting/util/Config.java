package cool.muyucloud.saplanting.util;

import com.google.gson.*;
import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.constant.Constable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Config {
    private static final Logger LOGGER = Saplanting.getLogger();
    private static final Path CONFIG_PATH = Path.of("config").resolve("saplanting.json");

    private final JsonObject properties;
    private final HashSet<String> langs;
    private final ArrayList<String> keySet;

    /**
     * Return a default set of Config.
     */
    public Config() {
        this.langs = new HashSet<>();
        this.langs.add("en_us");
        this.langs.add("zh_cn");

        this.properties = new JsonObject();
        this.properties.addProperty("plantEnable", true);
        this.properties.addProperty("plantLarge", true);
        this.properties.addProperty("multiThread", true);
        this.properties.addProperty("showTitleOnOpConnected", false);
        this.properties.addProperty("ignoreShape", false);
        this.properties.addProperty("warnTaskQueue", true);
        this.properties.addProperty("autoBlackList", true);
        this.properties.addProperty("plantDelay", 40);
        this.properties.addProperty("avoidDense", 2);
        this.properties.addProperty("playerAround", 2);
        this.properties.addProperty("maxTask", 1000);
        this.properties.addProperty("language", "en_us");
        this.properties.add("blacklist", new JsonArray());
        this.properties.add("whitelist", new JsonArray());

        Set<String> set = new HashSet<>(this.properties.keySet());
        set.remove("whitelist");
        set.remove("blacklist");
        set.remove("language");
        this.keySet = new ArrayList<>(set);
        this.addToWhitelist("#minecraft:saplings");
    }

    /**
     * Not a copy! Please do not modify return value!
     */
    public ArrayList<String> getKeySet() {
        return this.keySet;
    }

    /**
     * Not a copy! Please do not modify return value!
     */
    public Set<String> getValidLangs() {
        return this.langs;
    }

    /**
     * Load from file and update properties, language and black list
     */
    public boolean load() {
        if (!Files.exists(CONFIG_PATH)) {
            // try to create new config file
            LOGGER.info("saplanting.json does not exist, generating.");
            try {
                if (!Files.exists(CONFIG_PATH.getParent())) {
                    Files.createDirectories(CONFIG_PATH.getParent());
                }
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
                if (this.properties.has(key) && !Objects.equals(key, "whitelist") && !Objects.equals(key, "blacklist")) {
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

            // Analyzing whitelist
            if (read.has("whitelist")) {
                JsonArray array = read.getAsJsonArray("whitelist");
                JsonArray whitelist = new JsonArray();
                this.properties.add("whitelist", whitelist);
                for (int i = 0; i < array.size(); ++i) {
                    JsonElement element = array.get(i);
                    String value = element.getAsString();
                    this.addToWhitelist(value);
                }
            }

            // Analyzing blacklist
            if (read.has("blacklist")) {
                JsonArray array = read.getAsJsonArray("blacklist");
                JsonArray blacklist = new JsonArray();
                this.properties.add("blacklist", blacklist);
                for (int i = 0; i < array.size(); ++i) {
                    JsonElement element = array.get(i);
                    String value = element.getAsString();
                    this.addToBlackList(value);
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
     */
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

    public String stringConfigPath() {
        return CONFIG_PATH.toString();
    }

    public boolean isInWhitelist(Item item) {
        if (!this.properties.has("whitelist")) {
            throw new NullPointerException("Tried to access whitelist but it does not exists!");
        }
        JsonElement element = this.properties.get("whitelist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of whitelist is not a array.");
        }

        JsonArray array = element.getAsJsonArray();
        if (array.isEmpty()) {
            return false;
        }
        for (JsonElement e : array) {
            String entry = e.getAsString();
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(entry.substring(1)));
                if (TagUtil.isIn(tag, item)) {
                    return true;
                }
            } else if (entry.equals("*")) {
                return true;
            } else {
                ResourceLocation entryId = ResourceLocation.parse(entry);
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (itemId.equals(entryId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addToWhitelist(String value) {
        if (!this.properties.has("whitelist")) {
            throw new NullPointerException("Tried to access whitelist but it does not exists!");
        }
        JsonElement element = this.properties.get("whitelist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of whitelist is not a array.");
        }

        JsonArray array = element.getAsJsonArray();
        String formatted = formatItemLike(value);
        if (formatted == null) {
            return false;
        }
        JsonPrimitive primitive = new JsonPrimitive(formatted);
        if (array.contains(primitive)) {
            return false;
        } else {
            array.add(primitive);
            return true;
        }
    }

    public boolean removeFromWhitelist(String value) {
        if (!this.properties.has("whitelist")) {
            throw new NullPointerException("Tried to access whitelist but it does not exists!");
        }
        JsonElement element = this.properties.get("whitelist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of whitelist is not a array.");
        }

        JsonArray array = element.getAsJsonArray();
        String formatted = formatItemLike(value);
        if (formatted == null) {
            return false;
        }
        JsonPrimitive primitive = new JsonPrimitive(formatted);
        if (array.contains(primitive)) {
            array.remove(primitive);
            return true;
        } else {
            return false;
        }
    }

    public int whitelistSize() {
        if (!this.properties.has("whitelist")) {
            throw new NullPointerException("Tried to access whitelist but it does not exists!");
        }
        JsonElement element = this.properties.get("whitelist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of whitelist is not a array.");
        }
        return element.getAsJsonArray().size();
    }

    public JsonArray getWhitelist() {
        if (!this.properties.has("whitelist")) {
            throw new NullPointerException("Tried to access whitelist but it does not exists!");
        }
        JsonElement element = this.properties.get("whitelist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of whitelist is not a array.");
        }
        return element.getAsJsonArray();
    }

    public void clearWhitelist() {
        if (this.properties.has("whitelist")) {
            this.properties.remove("whitelist");
        }
        this.properties.add("whitelist", new JsonArray());
    }

    public boolean isInBlacklist(Item item) {
        if (!this.properties.has("blacklist")) {
            throw new NullPointerException("Tried to access blacklist but it does not exists!");
        }
        JsonElement element = this.properties.get("blacklist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of blacklist is not a array.");
        }

        JsonArray array = element.getAsJsonArray();
        if (array.isEmpty()) {
            return false;
        }
        for (JsonElement e : array) {
            String entry = e.getAsString();
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(entry.substring(1)));
                if (TagUtil.isIn(tag, item)) {
                    return true;
                }
            } else if (entry.equals("*")) {
                return true;
            } else {
                ResourceLocation entryId = ResourceLocation.parse(entry);
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (itemId.equals(entryId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addToBlackList(String value) {
        if (!this.properties.has("blacklist")) {
            throw new NullPointerException("Tried to access blacklist but it does not exists!");
        }
        JsonElement element = this.properties.get("blacklist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of blacklist is not a array.");
        }

        JsonArray array = element.getAsJsonArray();
        String formatted = formatItemLike(value);
        if (formatted == null) {
            return false;
        }
        JsonPrimitive primitive = new JsonPrimitive(formatted);
        if (array.contains(primitive)) {
            return false;
        } else {
            array.add(primitive);
            return true;
        }
    }

    public boolean removeFromBlackList(String value) {
        if (!this.properties.has("blacklist")) {
            throw new NullPointerException("Tried to access blacklist but it does not exists!");
        }
        JsonElement element = this.properties.get("blacklist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of blacklist is not a array.");
        }

        JsonArray array = element.getAsJsonArray();
        JsonPrimitive primitive = new JsonPrimitive(value);
        if (array.contains(primitive)) {
            array.remove(primitive);
            return true;
        } else {
            return false;
        }
    }

    public int blacklistSize() {
        if (!this.properties.has("blacklist")) {
            throw new NullPointerException("Tried to access blacklist but it does not exists!");
        }
        JsonElement element = this.properties.get("blacklist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of blacklist is not a array.");
        }

        JsonArray array = element.getAsJsonArray();
        return array.size();
    }

    public JsonArray getBlackList() {
        if (!this.properties.has("blacklist")) {
            throw new NullPointerException("Tried to access blacklist but it does not exists!");
        }
        JsonElement element = this.properties.get("blacklist");
        if (!element.isJsonArray()) {
            throw new ClassCastException("Value of blacklist is not a array.");
        }
        return element.getAsJsonArray();
    }

    public void clearBlackList() {
        if (this.properties.has("blacklist")) {
            this.properties.remove("blacklist");
        }
        this.properties.add("blacklist", new JsonArray());
    }

    @Nullable
    public static String formatItemLike(String itemLike) {
        if (itemLike.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(itemLike.substring(1));
            return id == null ? null : "#" + id;
        } else if (itemLike.equals("*")) {
            return "*";
        } else {
            ResourceLocation id = ResourceLocation.tryParse(itemLike);
            return id == null ? null : id.toString();
        }
    }
}

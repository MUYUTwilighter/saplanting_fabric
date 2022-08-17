package cool.muyucloud.saplanting;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class Config {
    private static final Config CONFIG = new Config();
//    private static final String[] BOOL_PROPERTY_NAMES = {
//            "plantEnable",
//            "plantLarge",
//            "blackListEnable",
//            "allowSapling",
//            "allowCrop",
//            "allowMushroom",
//            "allowFungus",
//            "allowFlower",
//            "allowOther",
//            "showTitleOnPlayerConnected"
//    };
//    private static final String[] INT_PROPERTY_NAMES = {
//            "plantDelay",
//            "avoidDense",
//            "playerAround"
//    };

    private boolean plantEnable = true;
    private boolean plantLarge = true;
    private boolean blackListEnable = true;
    private boolean allowSapling = true;
    private boolean allowCrop = false;
    private boolean allowMushroom = false;
    private boolean allowFungus = false;
    private boolean allowFlower = false;
    private boolean allowOther = false;
    private boolean showTitleOnPlayerConnected = false;
    private boolean ignoreShape = false;    // plant a sapling in a shape that would not let it grow
    private int plantDelay = 40;
    private int avoidDense = 2;
    private int playerAround = 2;
    private final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("saplanting.json");
    private final HashSet<Item> plantableItem = new HashSet<>();
    private final HashSet<Item> blackList = new HashSet<>();

//    public static String[] getPropertyNames() {
//        ArrayList<String> output = new ArrayList<>();
//        output.addAll(Arrays.asList(BOOL_PROPERTY_NAMES));
//        output.addAll(Arrays.asList(INT_PROPERTY_NAMES));
//        return (String[]) output.toArray();
//    }
//
//    public static String[] getBoolPropertyNames() {
//        return BOOL_PROPERTY_NAMES;
//    }
//
//    public static String[] getIntPropertyNames() {
//        return INT_PROPERTY_NAMES;
//    }

    public static boolean isPlantableItem(Item item) {
        return CONFIG.plantableItem.contains(item);
    }

    public static void addBlackListItem(Item item) {
        CONFIG.blackList.add(item);
    }

    public static void rmBlackListItem(Item item) {
        CONFIG.blackList.remove(item);
    }

    public static void clearBlackList() {
        CONFIG.blackList.clear();
    }

    public static String stringBlackList() {
        StringBuilder output = new StringBuilder();
        boolean flag = false;

        for (Item item : CONFIG.blackList) {
            if (flag) {
                output.append(", ");
            } else {
                flag = true;
            }

            output.append(Registry.ITEM.getId(item).getNamespace())
                    .append(':')
                    .append(Registry.ITEM.getId(item).getPath());
        }

        return output.toString();
    }

    public static int blackListLength() {
        return CONFIG.blackList.size();
    }

    public static void load() {
        CONFIG.loadFromFile();
    }

    public static boolean load(String name) {
        return CONFIG.loadFromFile(name);
    }

    public static void saveConfig() {
        CONFIG.save();
    }

    public static boolean inBlackList(Item item) {
        return CONFIG.blackList.contains(item);
    }

    public static boolean itemOK(Item item) {
        if (!CONFIG.plantableItem.contains(item)) {
            return false;
        }

        if (((BlockItem) item).getBlock() instanceof SaplingBlock) {
            if (!Config.getAllowSapling()) {
                return false;
            }
        } else if (((BlockItem) item).getBlock() instanceof CropBlock) {
            if (!Config.getAllowCrop()) {
                return false;
            }
        } else if (((BlockItem) item).getBlock() instanceof MushroomBlock) {
            if (!Config.getAllowMushroom()) {
                return false;
            }
        } else if (((BlockItem) item).getBlock() instanceof FungusBlock) {
            if (!Config.getAllowFungus()) {
                return false;
            }
        } else if (((BlockItem) item).getBlock() instanceof FlowerBlock) {
            if (!Config.getAllowFlower()) {
                return false;
            }
        } else if (!Config.getAllowOther()) {
            return false;
        }

        if (CONFIG.blackListEnable) {
            return !CONFIG.blackList.contains(item);
        }

        return true;
    }

    public static String stringPath() {
        return CONFIG.CONFIG_PATH.toString();
    }

    public static boolean getPlantEnable() {
        return CONFIG.plantEnable;
    }

    public static boolean getPlantLarge() {
        return CONFIG.plantLarge;
    }

    public static boolean getBlackListEnable() {
        return CONFIG.blackListEnable;
    }

    public static boolean getAllowSapling() {
        return CONFIG.allowSapling;
    }

    public static boolean getAllowCrop() {
        return CONFIG.allowCrop;
    }

    public static boolean getAllowMushroom() {
        return CONFIG.allowMushroom;
    }

    public static boolean getAllowFungus() {
        return CONFIG.allowFungus;
    }

    public static boolean getAllowFlower() {
        return CONFIG.allowFlower;
    }

    public static boolean getAllowOther() {
        return CONFIG.allowOther;
    }

    public static boolean getShowTitleOnPlayerConnected() {
        return CONFIG.showTitleOnPlayerConnected;
    }

    public static boolean getIgnoreShape() {
        return CONFIG.ignoreShape;
    }

    public static int getPlantDelay() {
        return CONFIG.plantDelay;
    }

    public static int getAvoidDense() {
        return CONFIG.avoidDense;
    }

    public static int getPlayerAround() {
        return  CONFIG.playerAround;
    }

    public static void setPlantEnable(boolean plantEnable) {
        CONFIG.plantEnable = plantEnable;
    }

    public static void setPlantLarge(boolean plantLarge) {
        CONFIG.plantLarge = plantLarge;
    }

    public static void setBlackListEnable(boolean value) {
        CONFIG.blackListEnable = value;
    }

    public static void setAllowSapling(boolean value) {
        CONFIG.allowSapling = value;
    }

    public static void setAllowCrop(boolean value) {
        CONFIG.allowCrop = value;
    }

    public static void setAllowMushroom(boolean value) {
        CONFIG.allowMushroom = value;
    }

    public static void setAllowFungus(boolean value) {
        CONFIG.allowFungus = value;
    }

    public static void setAllowFlower(boolean value) {
        CONFIG.allowFlower = value;
    }

    public static void setAllowOther(boolean value) {
        CONFIG.allowOther = value;
    }

    public static void setShowTitleOnPlayerConnected(boolean value) {
        CONFIG.showTitleOnPlayerConnected = value;
    }

    public static void setIgnoreShape(boolean value) {
        CONFIG.ignoreShape = value;
    }

    public static void setPlantDelay(int plantDelay) {
        CONFIG.plantDelay = plantDelay;
    }

    public static void setAvoidDense(int avoidDense) {
        CONFIG.avoidDense = avoidDense;
    }

    public static void setPlayerAround(int playerAround) {
        CONFIG.playerAround = playerAround;
    }

    private boolean initPlantEnable(JsonObject jsonObject) {
        try {
            this.plantEnable = jsonObject.get("plantEnable").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initPlantLarge(JsonObject jsonObject) {
        try {
            this.plantLarge = jsonObject.get("plantLarge").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initBlackListEnable(JsonObject jsonObject) {
        try {
            this.blackListEnable = jsonObject.get("blackListEnable").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initAllowSapling(JsonObject jsonObject) {
        try {
            this.allowSapling = jsonObject.get("allowSapling").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initAllowCrop(JsonObject jsonObject) {
        try {
            this.allowCrop = jsonObject.get("allowCrop").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initAllowMushroom(JsonObject jsonObject) {
        try {
            this.allowMushroom = jsonObject.get("allowMushroom").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initAllowFungus(JsonObject jsonObject) {
        try {
            this.allowFungus = jsonObject.get("allowFungus").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initAllowFlower(JsonObject jsonObject) {
        try {
            this.allowFlower = jsonObject.get("allowFlower").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initAllowOther(JsonObject jsonObject) {
        try {
            this.allowOther = jsonObject.get("allowOther").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initShowTitleOnPlayerConnected(JsonObject jsonObject) {
        try {
            this.showTitleOnPlayerConnected = jsonObject.get("showTitleOnPlayerConnected").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initIgnoreShape(JsonObject jsonObject) {
        try {
            this.ignoreShape = jsonObject.get("showTitleOnPlayerConnected").getAsBoolean();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initPlantDelay(JsonObject jsonObject) {
        try {
            this.plantDelay = jsonObject.get("plantDelay").getAsInt();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initAvoidDense(JsonObject jsonObject) {
        try {
            this.avoidDense = jsonObject.get("avoidDense").getAsInt();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initPlayerAround(JsonObject jsonObject) {
        try {
            this.playerAround = jsonObject.get("playerAround").getAsInt();
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private boolean initBlackList(JsonObject jsonObject) {
        blackList.clear();
        JsonArray jsonArray;

        try {
            jsonArray = jsonObject.get("blackList").getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (jsonArray == null) {
            return true;
        }

        JsonArray finalJsonArray = jsonArray;
        new Thread(() -> {
            for (int i = 0; i < finalJsonArray.size(); i++) {
                try {
                    Item item = Registry.ITEM.get(new Identifier(finalJsonArray.get(i).getAsString()));
                    if (isValidItem(item)) {
                        blackList.add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    private boolean isValidItem(Item item) {
        return item instanceof BlockItem blockItem && blockItem.getBlock() instanceof PlantBlock;
    }

    private String stringJSON() {
        StringBuilder output = new StringBuilder();
        String indent = "    ";

        output.append('{').append('\n');
        output.append(indent).append("\"plantEnable\": ").append(plantEnable).append(",\n");
        output.append(indent).append("\"plantLarge\": ").append(plantLarge).append(",\n");
        output.append(indent).append("\"blackListEnable\": ").append(blackListEnable).append(",\n");
        output.append(indent).append("\"allowSapling\": ").append(allowSapling).append(",\n");
        output.append(indent).append("\"allowCrop\": ").append(allowCrop).append(",\n");
        output.append(indent).append("\"allowMushroom\": ").append(allowMushroom).append(",\n");
        output.append(indent).append("\"allowFungus\": ").append(allowFungus).append(",\n");
        output.append(indent).append("\"allowFlower\": ").append(allowFlower).append(",\n");
        output.append(indent).append("\"allowOther\": ").append(allowOther).append(",\n");
        output.append(indent).append("\"showTitleOnPlayerConnected\": ").append(showTitleOnPlayerConnected).append(",\n");
        output.append(indent).append("\"ignoreShape\": ").append(ignoreShape).append(",\n");
        output.append(indent).append("\"plantDelay\": ").append(plantDelay).append(",\n");
        output.append(indent).append("\"avoidDense\": ").append(avoidDense).append(",\n");
        output.append(indent).append("\"playerAround\": ").append(playerAround).append(",\n");

        output.append(indent).append("\"blackList\": [");
        if (blackList.isEmpty()) {
            output.append("]\n");
        } else {
            boolean flag = false;
            for (Item item : blackList) {
                if (!flag) {
                    flag = true;
                } else {
                    output.append(',');
                }

                output.append('\n').append(indent).append(indent).append('\"')
                        .append(Registry.ITEM.getId(item).getNamespace())
                        .append(':')
                        .append(Registry.ITEM.getId(item).getPath())
                        .append('\"');
            }
            output.append('\n').append(indent).append("]\n");
        }

        output.append('}');

        //System.out.println(output.toString());

        return output.toString();
    }

    private Config() {
        // initialize saplings
        new Thread(() -> Registry.ITEM.stream()
                .filter(this::isValidItem)
                .forEach(plantableItem::add)).start();
        // load setting from file
        loadFromFile();
    }

    private void save() {
        // in case config file disappear
        if (!Files.exists(CONFIG_PATH)) {
            try {
                Files.createFile(CONFIG_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // try to save
        try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
            outputStream.write(stringJSON().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        if (!Files.exists(CONFIG_PATH)) {
            // try to create new config file
            try {
                Files.createFile(CONFIG_PATH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // try to read properties from file
            try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);

                initPlantEnable(jsonObject);
                initPlantLarge(jsonObject);
                initBlackListEnable(jsonObject);
                initAllowSapling(jsonObject);
                initAllowCrop(jsonObject);
                initAllowMushroom(jsonObject);
                initAllowFungus(jsonObject);
                initAllowFlower(jsonObject);
                initAllowOther(jsonObject);
                initShowTitleOnPlayerConnected(jsonObject);
                initIgnoreShape(jsonObject);
                initPlantDelay(jsonObject);
                initAvoidDense(jsonObject);
                initPlayerAround(jsonObject);

                initBlackList(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean loadFromFile(String name) {
        if (Files.exists(CONFIG_PATH)) {
            // try to read properties from file
            try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);

                switch (name) {
                    case "plantEnable":
                        return initPlantEnable(jsonObject);
                    case "plantLarge":
                        return initPlantLarge(jsonObject);
                    case "blackListEnable":
                        return initBlackListEnable(jsonObject);
                    case "allowSapling":
                        return initAllowSapling(jsonObject);
                    case "allowCrop":
                        return initAllowCrop(jsonObject);
                    case "allowMushroom":
                        return initAllowMushroom(jsonObject);
                    case "allowFungus":
                        return initAllowFungus(jsonObject);
                    case "allowFlower":
                        return initAllowFlower(jsonObject);
                    case "allowOther":
                        return initAllowOther(jsonObject);
                    case "showTitleOnPlayerConnected":
                        return initShowTitleOnPlayerConnected(jsonObject);
                    case "ignoreShape":
                        return initIgnoreShape(jsonObject);
                    case "plantDelay":
                        return initPlantDelay(jsonObject);
                    case "avoidDense":
                        return initAvoidDense(jsonObject);
                    case "playerAround":
                        return initPlayerAround(jsonObject);
                    case "blackList":
                        return initBlackList(jsonObject);
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
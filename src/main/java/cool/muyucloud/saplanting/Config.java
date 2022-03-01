package cool.muyucloud.saplanting;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class Config {
    private static final Config CONFIG = new Config();

    private boolean plantEnable = true;
    private boolean plantLarge = true;
    private boolean blackListEnable = true;
    private boolean allowSapling = true;
    private boolean allowCrop = false;
    private boolean allowMushroom = false;
    private boolean allowFungus = false;
    private boolean allowFlower = false;
    private boolean allowOther = false;
    private int plantDelay = 40;
    private int avoidDense = 2;
    private int playerAround = 2;
    private final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("saplanting.json");
    private final HashSet<Item> plantableItem = new HashSet<>();
    private final HashSet<Item> blackList = new HashSet<>();

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

    public static void setPlantDelay(int plantDelay) {
        CONFIG.plantDelay = plantDelay;
    }

    public static void setAvoidDense(int avoidDense) {
        CONFIG.avoidDense = avoidDense;
    }

    public static void setPlayerAround(int playerAround) {
        CONFIG.playerAround = playerAround;
    }

    private void initPlantEnable(JSONObject jsonObject) {
        try {
            this.plantEnable = jsonObject.getBoolean("plantEnable");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initPlantLarge(JSONObject jsonObject) {
        try {
            this.plantLarge = jsonObject.getBoolean("plantLarge");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initBlackListEnable(JSONObject jsonObject) {
        try {
            this.blackListEnable = jsonObject.getBoolean("blackListEnable");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAllowSapling(JSONObject jsonObject) {
        try {
            this.allowSapling = jsonObject.getBoolean("allowSapling");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAllowCrop(JSONObject jsonObject) {
        try {
            this.allowCrop = jsonObject.getBoolean("allowCrop");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAllowMushroom(JSONObject jsonObject) {
        try {
            this.allowMushroom = jsonObject.getBoolean("allowMushroom");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAllowFungus(JSONObject jsonObject) {
        try {
            this.allowFungus = jsonObject.getBoolean("allowFungus");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAllowFlower(JSONObject jsonObject) {
        try {
            this.allowFlower = jsonObject.getBoolean("allowFlower");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAllowOther(JSONObject jsonObject) {
        try {
            this.allowOther = jsonObject.getBoolean("allowOther");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initPlantDelay(JSONObject jsonObject) {
        try {
            this.plantDelay = jsonObject.getInt("plantDelay");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAvoidDense(JSONObject jsonObject) {
        try {
            this.avoidDense = jsonObject.getInt("avoidDense");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initPlayerAround(JSONObject jsonObject) {
        try {
            this.playerAround = jsonObject.getInt("playerAround");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initBlackList(JSONObject jsonObject) {
        blackList.clear();
        JSONArray jsonArray = null;

        try {
            jsonArray = jsonObject.getJSONArray("blackList");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonArray == null) {
            return;
        }

        JSONArray finalJsonArray = jsonArray;
        new Thread(() -> {
            for (int i = 0; i < finalJsonArray.length(); i++) {
                try {
                    Item item = Registry.ITEM.get(new Identifier(finalJsonArray.getString(i)));
                    if (isValidItem(item)) {
                        blackList.add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                JSONObject jsonObject = new JSONObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));

                initPlantEnable(jsonObject);
                initPlantLarge(jsonObject);
                initBlackListEnable(jsonObject);
                initAllowSapling(jsonObject);
                initAllowCrop(jsonObject);
                initAllowMushroom(jsonObject);
                initAllowFungus(jsonObject);
                initAllowFlower(jsonObject);
                initAllowOther(jsonObject);
                initPlantDelay(jsonObject);
                initAvoidDense(jsonObject);
                initPlayerAround(jsonObject);

                initBlackList(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
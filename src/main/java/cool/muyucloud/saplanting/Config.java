package cool.muyucloud.saplanting;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.json.JSONArray;
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
    private int plantDelay = 40;
    private int avoidDense = 2;
    private int playerAround = 2;
    private final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("saplanting.json");
    private final HashSet<Item> saplings = new HashSet<>();
    private final HashSet<Item> blackList = new HashSet<>();

    public static void addBlackListItem(Item item) {
        if (item != Items.AIR) {
            CONFIG.blackList.add(item);
        }
    }

    public static void rmBlackListItem(Item item) {
        CONFIG.blackList.remove(item);
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
        if (CONFIG.blackListEnable) {
            return !CONFIG.blackList.contains(item) && CONFIG.saplings.contains(item);
        }
        return CONFIG.saplings.contains(item);
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

    public static void setPlantDelay(int plantDelay) {
        CONFIG.plantDelay = plantDelay;
    }

    public static void setAvoidDense(int avoidDense) {
        CONFIG.avoidDense = avoidDense;
    }

    public static void setPlayerAround(int playerAround) {
        CONFIG.playerAround = playerAround;
    }

    public static void addSapling(Item item) {
        CONFIG.saplings.add(item);
    }

    private void initBlackListEnable(boolean value) {
        this.blackListEnable = value;
    }

    private void initPlantEnable(boolean value) {
        this.plantEnable = value;
    }

    private void initPlantLarge(boolean value) {
        this.plantLarge = value;
    }

    private void initPlantDelay(int value) {
        this.plantDelay = value;
    }

    private void initAvoidDense(int value) {
        this.avoidDense = value;
    }

    private void initPlayerAround(int value) {
        this.playerAround = value;
    }

    private void initBlackList(String name) {
        blackList.add(Registry.ITEM.get(new Identifier(name)));
    }

    private String stringJSON() {
        StringBuilder output = new StringBuilder();
        String indent = "    ";

        output.append('{').append('\n');
        output.append(indent).append("\"plantEnable\": ").append(plantEnable).append(",\n");
        output.append(indent).append("\"plantLarge\": ").append(plantLarge).append(",\n");
        output.append(indent).append("\"blackListEnable\": ").append(blackListEnable).append(",\n");
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
        // load setting from file
        loadFromFile();
        // dump setting into file (correct typo)
        save();
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

                initPlantEnable(jsonObject.getBoolean("plantEnable"));
                //System.out.println(jsonObject.getBoolean("plantEnable"));
                initPlantLarge(jsonObject.getBoolean("plantLarge"));
                //System.out.println(jsonObject.getBoolean("plantLarge"));
                initBlackListEnable(jsonObject.getBoolean("blackListEnable"));
                //System.out.println(jsonObject.getBoolean("blackListEnable"));
                initPlantDelay(jsonObject.getInt("plantDelay"));
                //System.out.println(jsonObject.getInt("plantDelay"));
                initAvoidDense(jsonObject.getInt("avoidDense"));
                //System.out.println(jsonObject.getInt("avoidDense"));
                initPlayerAround(jsonObject.getInt("playerAround"));
                //System.out.println(jsonObject.getInt("playerAround"));

                JSONArray blackListNames = jsonObject.getJSONArray("blackList");
                int i = 0;
                while (i < blackListNames.length()) {
                    initBlackList(blackListNames.getString(i));
                    ++i;
                }
                blackList.remove(Items.AIR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
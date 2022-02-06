package io.github.muyutwilighter.saplanting;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

public class Config {
    private static final Config CONFIG = new Config();

    private boolean plantEnable = true;
    private boolean plantLarge = true;
    private int avoidDense = 3;
    private int plantDelay = 40;
    private final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("saplanting.properties");
    private final Properties properties = new Properties();

    public static Set<String> stringPropertyNames() {
        return CONFIG.properties.stringPropertyNames();
    }

    public static String stringPropertyValue(String key) {
        return CONFIG.properties.getProperty(key);
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

    public static int getAvoidDense() {
        return CONFIG.avoidDense;
    }

    public static int getPlantDelay() {
        return CONFIG.plantDelay;
    }

    public static void setPlantEnable(boolean plantEnable) {
        CONFIG.plantEnable = plantEnable;
    }

    public static void setPlantLarge(boolean plantLarge) {
        CONFIG.plantLarge = plantLarge;
    }

    public static void setAvoidDense(int avoidDense) {
        CONFIG.avoidDense = avoidDense;
    }

    public static void setPlantDelay(int plantDelay) {
        CONFIG.plantDelay = plantDelay;
    }

    private void setPlantEnable(String plantEnable) {
        this.plantEnable = Boolean.parseBoolean(plantEnable);
    }

    private void setPlantLarge(String plantLarge) {
        this.plantLarge = Boolean.parseBoolean(plantLarge);
    }

    private void setAvoidDense(String avoidDense) {
        try {
            this.avoidDense = Integer.parseInt(avoidDense);
            if (this.avoidDense < 0) {
                this.avoidDense = 3;
            }
        } catch (Exception ignore) {}
    }

    private void setPlantDelay(String plantDelay) {
        try {
            this.plantDelay = Integer.parseInt(plantDelay);
            if (this.plantDelay < 0) {
                this.plantDelay = 40;
            }
        } catch (Exception ignore) {}
    }

    private Config() {
        loadFromFile();
        // dump setting into file (correct typo)
        save();
    }

    private void save() {
        properties.clear();

        // dump current properties
        properties.setProperty("plantEnable", String.valueOf(plantEnable));
        properties.setProperty("plantLarge", String.valueOf(plantLarge));
        properties.setProperty("avoidDense", String.valueOf(avoidDense));
        properties.setProperty("plantDelay", String.valueOf(plantDelay));

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
            properties.store(outputStream, "Saplanting Configuration File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        CONFIG.save();
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
                properties.load(inputStream);
                // set properties
                setPlantEnable(properties.getProperty("plantEnable"));
                setPlantLarge(properties.getProperty("plantLarge"));
                setPlantDelay(properties.getProperty("plantDelay"));
                setAvoidDense(properties.getProperty("avoidDense"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void load() {
        CONFIG.loadFromFile();
    }
}
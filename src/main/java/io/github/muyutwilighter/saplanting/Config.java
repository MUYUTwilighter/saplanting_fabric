package io.github.muyutwilighter.saplanting;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    public static boolean plantEnable = true;
    public static int plantDelay = 40;
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("saplanting.properties");
    private static final Properties properties = new Properties();

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            // try to create new config file
            try {
                Files.createFile(CONFIG_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // try to read properties from file
            try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
                properties.load(inputStream);

                // set properties
                plantEnable = Boolean.parseBoolean(properties.getProperty("plantEnable"));
                try {
                    plantDelay = Integer.parseInt(properties.getProperty("plantDelay"));
                    if (plantDelay < 0) {
                        plantDelay = 40;
                    }
                } catch (NumberFormatException ignore) {}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // dump setting into file (correct mistyping)
        save();
    }

    public static void save() {
        // dump current properties
        properties.setProperty("plantDelay", String.valueOf(plantDelay));
        properties.setProperty("plantEnable", String.valueOf(plantEnable));

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
}
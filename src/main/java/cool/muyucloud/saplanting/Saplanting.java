package cool.muyucloud.saplanting;

import cool.muyucloud.saplanting.util.Command;
import cool.muyucloud.saplanting.util.Config;
import cool.muyucloud.saplanting.util.Translation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Saplanting implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Config CONFIG = new Config();

    // This should be in ItemEntityMixin, but was announced here to provide access for onServerStopping
    public static boolean THREAD_ALIVE = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing.");
        LOGGER.info("Saplanting waking up! OwO");

        LOGGER.info("Updating language.");
        Translation.updateLanguage(CONFIG.getAsString("language"));

        LOGGER.info("Registering events.");
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        LOGGER.info("Registering commands.");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            Command.register(dispatcher, registryAccess, environment.dedicated));
    }

    private void onServerStopping(MinecraftServer server) {
        THREAD_ALIVE = false;

        LOGGER.info("Saving saplanting config.");
        CONFIG.save();
    }

    private void onServerStarting(MinecraftServer server) {
        if (CONFIG.getAsBoolean("plantEnable")) {
            LOGGER.info("Saplanting is enabled now \\^o^/");
        } else {
            LOGGER.info("Saplanting is disabled QAQ");
            LOGGER.info("Use command \"/saplanting plantEnable true\" to enable saplanting");
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Config getConfig() {
        return CONFIG;
    }

    public static boolean isPlantItem(Item item) {
        return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof PlantBlock;
    }

    public static boolean isPlantAllowed(Item item) {
        if (!isPlantItem(item) || (CONFIG.getAsBoolean("blackListEnable") && CONFIG.inBlackList(item))) {
            return false;
        }

        PlantBlock block = ((PlantBlock) ((BlockItem) item).getBlock());
        if (block instanceof SaplingBlock) {
            return CONFIG.getAsBoolean("allowSapling");
        } else if (block instanceof CropBlock) {
            return CONFIG.getAsBoolean("allowCrop");
        } else if (block instanceof FungusBlock) {
            return CONFIG.getAsBoolean("allowFungus");
        } else if (block instanceof FlowerBlock) {
            return CONFIG.getAsBoolean("allowFlower");
        } else if (block instanceof MushroomPlantBlock) {
            return CONFIG.getAsBoolean("allowMushroom");
        } else {
            return CONFIG.getAsBoolean("allowOther");
        }
    }
}

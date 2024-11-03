package cool.muyucloud.saplanting;

import com.mojang.brigadier.CommandDispatcher;
import cool.muyucloud.saplanting.util.Command;
import cool.muyucloud.saplanting.util.Config;
import cool.muyucloud.saplanting.util.FakePlayer;
import cool.muyucloud.saplanting.util.Translation;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Saplanting {
    public static final String MOD_ID = "saplanting";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Config CONFIG = new Config();
    private static final Config DEFAULT_CONFIG = new Config();

    // This should be in ItemEntityMixin, but was announced here to provide access for onServerStopping
    public static boolean THREAD_ALIVE = false;

    public static void init() {
        LOGGER.info("Initializing.");
        LOGGER.info("Saplanting waking up! OwO");

        LOGGER.info("Loading config.");
        CONFIG.load();
        CONFIG.save();

        LOGGER.info("Updating language.");
        Translation.updateLanguage(CONFIG.getAsString("language"));
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext access, boolean dedicated) {
        LOGGER.info("Registering saplanting commands.");
        Command.register(dispatcher, access, dedicated);
    }

    public static void onServerStopping(MinecraftServer server) {
        THREAD_ALIVE = false;

        LOGGER.info("Saving saplanting config.");
        CONFIG.save();

        FakePlayer.onServerStopping();
    }

    public static void onServerStarting(MinecraftServer server) {
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

    public static Config getDefaultConfig() {
        return DEFAULT_CONFIG;
    }

    public static boolean isPlantItem(Item item) {
//        return item instanceof BlockItem;
        return true;
    }

    public static boolean isPlantAllowed(Item item) {
        return isPlantItem(item) && CONFIG.isInWhitelist(item) && !CONFIG.isInBlacklist(item);
    }
}

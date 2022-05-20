package cool.muyucloud.saplanting;

import cool.muyucloud.saplanting.command.SaplantingCommand;
import cool.muyucloud.saplanting.thread.ItemEntityThread;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Saplanting implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing.");
        LOGGER.info("Saplanting waking up! OwO");

        // register command /saplanting ...
        LOGGER.info("Registering commands '/saplanting' and its sub.");
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> SaplantingCommand.register(dispatcher)));

        // register events @serverStop
        LOGGER.info("Registering stop-server events.");
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        // register events @serverStarted
        LOGGER.info("Registering server-started events.");
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

        LOGGER.info("Initialized.");
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Stopping item entity thread.");
        ItemEntityThread.discardThread();
        LOGGER.info("Dumping current properties into file.");
        Config.saveConfig();
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("Initialize Config class");
        new Thread(() -> {
            if (Config.getPlantEnable()) {
                LOGGER.info("Saplanting is enabled now \\^o^/");
            } else {
                LOGGER.info("Saplanting is disabled QAQ");
                LOGGER.info("Use command \"/saplanting plantEnable true\" to enable saplanting");
            }
        }).start();
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
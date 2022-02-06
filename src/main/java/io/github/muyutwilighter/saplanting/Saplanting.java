package io.github.muyutwilighter.saplanting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Saplanting implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing.");
        LOGGER.info("Saplanting waking up! OwO");

        // register command /saplanting ...
        LOGGER.info("registering commands '/saplanting' and its sub.");
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            SaplantingCommand.register(dispatcher);
        }));

        // register events @serverStop
        LOGGER.info("registering stop-server events.");
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        LOGGER.info("Initialized.");
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("dumping current properties into file.");
        Config.saveConfig();
    }
}
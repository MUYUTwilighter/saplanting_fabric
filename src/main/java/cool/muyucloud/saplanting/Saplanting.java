package cool.muyucloud.saplanting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
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
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> SaplantingCommand.register(dispatcher)));

        // register events @serverStop
        LOGGER.info("registering stop-server events.");
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);


        if (FabricLauncherBase.getLauncher().getEnvironmentType() == EnvType.SERVER) {
            LOGGER.info("I'm in a server! Wow~");
            // register events @serverStart
            LOGGER.info("registering stop-start events.");
            ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        } else {
            LOGGER.info("I'm in a client! \\^o^/");
        }

        LOGGER.info("Initialized.");
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("dumping current properties into file.");
        Config.saveConfig();
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("registering sapling items");
        Registry.ITEM.stream()
                .filter(item -> (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock))
                .forEach(Config::addSapling);
    }
}
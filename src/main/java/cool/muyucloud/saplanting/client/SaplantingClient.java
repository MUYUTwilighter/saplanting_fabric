package cool.muyucloud.saplanting.client;

import cool.muyucloud.saplanting.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.block.SaplingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class SaplantingClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {
        if (FabricLauncherBase.getLauncher().getEnvironmentType() == EnvType.CLIENT) {
            LOGGER.info("I'm in a client! \\^o^/");
            LOGGER.info("registering client-stared events");
            ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
        }
    }

    private void onClientStarted(MinecraftClient client) {
        LOGGER.info("registering sapling items");
        Registry.ITEM.stream()
                .filter(item -> (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock))
                .forEach(Config::addSapling);
    }
}

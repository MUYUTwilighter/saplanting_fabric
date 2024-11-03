package cool.muyucloud.saplanting.fabric;

import cool.muyucloud.saplanting.Saplanting;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class SaplantingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Saplanting.init();

        Saplanting.getLogger().info("Registering events.");
        ServerLifecycleEvents.SERVER_STARTING.register(Saplanting::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(Saplanting::onServerStopping);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, selection) -> Saplanting.registerCommands(dispatcher, registryAccess, selection.includeDedicated));
    }
}

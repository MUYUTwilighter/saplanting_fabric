package cool.muyucloud.saplanting.neoforge;

import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(Saplanting.MOD_ID)
public final class SaplantingNeoForge {
    public SaplantingNeoForge() {
        Saplanting.init();

        Saplanting.getLogger().info("Registering events.");
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {
        Saplanting.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection().equals(Commands.CommandSelection.DEDICATED));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        Saplanting.onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        Saplanting.onServerStopping(event.getServer());
    }
}

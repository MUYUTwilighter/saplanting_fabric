package cool.muyucloud.saplanting.forge;

import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.Environment;

@Mod(Saplanting.MOD_ID)
public final class SaplantingForge {
    public SaplantingForge() {
        Saplanting.init();

        Saplanting.getLogger().info("Registering events.");
        MinecraftForge.EVENT_BUS.register(this);
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

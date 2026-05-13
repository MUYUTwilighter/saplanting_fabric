package cool.muyucloud.saplanting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientUtil {
    public static void message(Component component, boolean overlay) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (overlay) {
                player.sendOverlayMessage(component);
            } else {
                player.sendSystemMessage(component);
            }
        }
    }

    public static boolean isLocalPlayer(UUID uuid) {
        return Minecraft.getInstance().isLocalPlayer(uuid);
    }
}

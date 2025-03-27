package cool.muyucloud.saplanting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class ClientUtil {
    public static void message(Component component, boolean overlay) {
        Minecraft.getInstance().getChatListener().handleSystemMessage(component, overlay);
    }

    public static boolean isLocalPlayer(UUID uuid) {
        return Minecraft.getInstance().isLocalPlayer(uuid);
    }
}

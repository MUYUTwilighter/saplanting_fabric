package cool.muyucloud.saplanting.util;

import com.mojang.authlib.GameProfile;
import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakePlayer extends Player {
    /**
     * Default UUID, for fake players not associated with a specific (human) player.
     */
    public static final UUID DEFAULT_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");
    private static final GameProfile DEFAULT_PROFILE = new GameProfile(DEFAULT_UUID, "[Minecraft]");
    private static final Map<Level, FakePlayer> CACHE = new HashMap<>();

    public static FakePlayer get(Level level) {
        if (CACHE.containsKey(level)) {
            return CACHE.get(level);
        } else {
            FakePlayer player = new FakePlayer(level);
            CACHE.put(level, player);
            return player;
        }
    }

    public static void onServerStopping() {
        Saplanting.getLogger().info("Clearing fake player cache.");
        CACHE.clear();
    }

    public FakePlayer(Level level) {
        super(level, new BlockPos(0, 0, 0), 0, DEFAULT_PROFILE);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    public void setMainHandItem(ItemStack itemStack) {
        this.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
    }

    public void removeMainHandItem() {
        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }
}

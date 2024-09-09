package cool.muyucloud.saplanting.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakePlayer extends PlayerEntity {
    private static final Map<World, FakePlayer> FAKE_PLAYERS = new HashMap<>();

    public FakePlayer(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    public static @NotNull FakePlayer get(@NotNull World world) {
        if (!FAKE_PLAYERS.containsKey(world)) {
            UUID uuid = new UUID(0L, world.hashCode());
            GameProfile gameProfile = new GameProfile(uuid, "FakePlayer");
            FakePlayer fakePlayer = new FakePlayer(world, BlockPos.ORIGIN, 0, gameProfile, null);
            FAKE_PLAYERS.put(world, fakePlayer);
        }
        return FAKE_PLAYERS.get(world);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}

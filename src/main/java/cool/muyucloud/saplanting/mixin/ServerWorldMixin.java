package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Config;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnected", at = @At("TAIL"))
    public void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        // is player OP and saplanting disabled
        if (this.server.getPlayerManager().isOperator(player.getGameProfile()) && !Config.getPlantEnable() && Config.getShowTitleOnPlayerConnected()) {
            player.sendMessage(Text.translatable("saplanting.info.chat.onPlayerConnected.disabled"), MessageType.CHAT);
        }
    }
}

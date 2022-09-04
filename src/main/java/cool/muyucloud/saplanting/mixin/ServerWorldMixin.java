package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.util.Config;
import cool.muyucloud.saplanting.Saplanting;
import cool.muyucloud.saplanting.util.Translation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Shadow
    @Final
    private MinecraftServer server;
    private static final Config CONFIG = Saplanting.getConfig();
    private static final MutableText MSG = new LiteralText(Translation.translate("saplanting.onPlayerConnected.plantDisable"))
        .append(new LiteralText(Translation.translate("saplanting.onPlayerConnected.plantDisable.click"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/saplanting property plantEnable true"))
                .withColor(TextColor.parse("aqua"))
                .withUnderline(true)));

    @Inject(method = "onPlayerConnected", at = @At("TAIL"))
    public void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        boolean isOp = this.server.getPlayerManager().isOperator(player.getGameProfile());
        if (CONFIG.getAsBoolean("showTitleOnOpConnected") && isOp && !CONFIG.getAsBoolean("plantEnable")) {
            player.sendMessage(MSG, false);
        }
    }
}

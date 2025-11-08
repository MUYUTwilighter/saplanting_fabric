package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Saplanting;
import cool.muyucloud.saplanting.util.Config;
import cool.muyucloud.saplanting.util.PlantContext;
import cool.muyucloud.saplanting.util.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {
    @Shadow
    @Final
    private MinecraftServer server;
    @Unique
    private static final Config CONFIG = Saplanting.getConfig();
    @Unique
    private static final MutableComponent MSG = Component.literal(Translation.translate("saplanting.onPlayerConnected.plantDisable"))
        .append(Component.literal(Translation.translate("saplanting.onPlayerConnected.plantDisable.click"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent.RunCommand("/saplanting property plantEnable true"))
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)));

    @Inject(method = "addPlayer", at = @At("TAIL"))
    public void onPlayerConnected(ServerPlayer player, CallbackInfo ci) {
        boolean isOp = this.server.getPlayerList().isOp(new NameAndId(player.getGameProfile()));
        if (CONFIG.getAsBoolean("showTitleOnOpConnected") && isOp && !CONFIG.getAsBoolean("plantEnable")) {
            player.displayClientMessage(MSG, false);
        }
    }

    @Unique
    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        while (!PlantContext.PLANT_TASKS.isEmpty()) {
            PlantContext context = PlantContext.PLANT_TASKS.poll();
            context.plant();
        }
    }
}

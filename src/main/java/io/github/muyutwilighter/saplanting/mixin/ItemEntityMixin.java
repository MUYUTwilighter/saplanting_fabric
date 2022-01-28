package io.github.muyutwilighter.saplanting.mixin;

import io.github.muyutwilighter.saplanting.Config;
import io.github.muyutwilighter.saplanting.Saplanting;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
extends Entity {
    @Shadow public abstract ItemStack getStack();

    public int plantAge;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (Config.plantEnable) {
            // schedule planting
            if (this.plantable()) { // if OK to plant, plus age
                ++this.plantAge;
            } else {                // if not, reset age
                this.plantAge = 0;
            }

            // if age reach plant delay, do planting
            if (this.plantAge >= Config.plantDelay) {
                this.plantAge = 0;  // reset age
//                if (this.getStack().isIn(Saplanting.SAPLINGS_LARGE)) {
//
//                } // TODO plant 2x2 trees
                // plant at own position
                this.world.setBlockState(this.getBlockPos(),
                        ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState(),
                        Block.NOTIFY_ALL);
                // minus 1 count
                this.getStack().setCount(this.getStack().getCount() - 1);
            }
        }
    }

    private boolean plantable() {
        if (this.world != null  // the world should be loaded
                // the item is one of which is tagged as sapling
                && this.getStack().isIn(Saplanting.SAPLINGS)
                // the item got enough space
                && this.world.getBlockState(this.getBlockPos()).isIn(Saplanting.REPLACEABLE)
                // the item can be placed to block
                && this.getStack().getItem() instanceof BlockItem
        ) {
            // which category this item belongs to
            if (this.getStack().isIn(Saplanting.SAPLINGS_OVERWORLD)) {
                // if block below is OK
                return this.world.getBlockState(this.getBlockPos().down()).isIn(Saplanting.BASE_OVERWOLRD);
            } else if (this.getStack().isIn(Saplanting.SAPLINGS_NETHER)) {
                return this.world.getBlockState(this.getBlockPos().down()).isIn(Saplanting.BASE_NETHER);
            }
        }
        return false;
    }
}

























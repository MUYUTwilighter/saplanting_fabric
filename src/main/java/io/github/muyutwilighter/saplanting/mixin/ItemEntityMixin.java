package io.github.muyutwilighter.saplanting.mixin;

import io.github.muyutwilighter.saplanting.Config;
import io.github.muyutwilighter.saplanting.Saplanting;
import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
extends Entity {
    @Shadow public abstract ItemStack getStack();

    @Shadow @Final public float uniqueOffset;
    private int plantAge;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (Config.getPlantEnable()) {
            // schedule planting
            if (this.plantable()) { // if OK to plant, plus age
                ++this.plantAge;
            } else {                // if not, reset age
                this.plantAge = 0;
            }

            // if age reach plant delay, do planting
            if (this.plantAge >= Config.getPlantDelay()) {
                this.plantAge = 0;  // reset age

                // plant 2x2 tree
                if (Config.getPlantLarge()
                        && ((SaplingBlockAccessor) ((BlockItem) this.getStack().getItem()).getBlock()).getGenerator() instanceof LargeTreeSaplingGenerator
                        && this.getStack().getCount() >= 4) {
                    for (BlockPos pos : BlockPos.iterate(this.getBlockPos().add(-1, 0, -1), this.getBlockPos())) {
                        if (this.spaceOK2x2(pos)) {
                            this.fillSapling(pos);
                            this.getStack().setCount(this.getStack().getCount() - 4);
                            break;
                        }
                    }
                }

                // plant 1x1 tree
                if (this.getStack().getCount() > 0 && this.spaceOK(this.getBlockPos())) {
                    // plant at own position
                    this.world.setBlockState(this.getBlockPos(),
                            ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState(),
                            Block.NOTIFY_ALL);
                    // minus 1 count
                    this.getStack().setCount(this.getStack().getCount() - 1);
                }
            }
        }
    }

    private boolean plantable() {
        if (this.world != null  // is world loaded
                // is item a block
                && this.getStack().getItem() instanceof BlockItem
                // is item a sapling
                && ((BlockItem) this.getStack().getItem()).getBlock() instanceof SaplingBlock
        ) {
            if (Config.getAvoidDense() > 0 && this.hasOther()) {
                return false;
            }
            return this.spaceOK(this.getBlockPos());
        }
        return false;
    }

    private boolean spaceOK(BlockPos pos) {
        return ((((SaplingBlock) ((BlockItem) this.getStack().getItem()).getBlock()).canPlaceAt(
                ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState(), this.world, pos))
                && (this.world.getBlockState(pos).isIn(BlockTags.REPLACEABLE_PLANTS)
                || this.world.getBlockState(pos).isAir()));
    }

    private boolean spaceOK2x2(BlockPos pos) {
        for (BlockPos tmpos : BlockPos.iterate(pos, pos.add(1, 0, 1))) {
            if (!this.spaceOK(tmpos)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOther() {
        for (BlockPos pos : BlockPos.iterateOutwards(this.getBlockPos(),
                Config.getAvoidDense(), Config.getAvoidDense(), Config.getAvoidDense())) {
            if (this.world.getBlockState(pos).getBlock() instanceof LeavesBlock
                    || this.world.getBlockState(pos).getBlock() instanceof SaplingBlock
                    || this.world.getBlockState(pos).isIn(BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }

    private void fillSapling(BlockPos pos) {
        for (BlockPos tmpos : BlockPos.iterate(pos, pos.add(1, 0, 1))) {
            this.world.setBlockState(tmpos,
                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState());
        }
    }
}

























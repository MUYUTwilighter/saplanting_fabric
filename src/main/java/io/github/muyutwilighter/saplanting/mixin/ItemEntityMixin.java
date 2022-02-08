package io.github.muyutwilighter.saplanting.mixin;

import io.github.muyutwilighter.saplanting.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
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

    private int plantAge = 0;

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
            if (this.plantAge >= Config.getPlantDelay() && this.plantable()) {
                this.plantAge = 0;  // reset age

                // plant 2x2 tree
                if (Config.getPlantLarge()
                        && ((SaplingBlockAccessor) ((BlockItem) this.getStack().getItem()).getBlock()).getGenerator() instanceof LargeTreeSaplingGenerator
                        && this.getStack().getCount() >= 4) {
                    for (BlockPos pos : BlockPos.iterate(this.getBlockPos().add(-1, 0, -1), this.getBlockPos())) {
                        if (spaceOK2x2(this.world, pos, ((SaplingBlock) ((BlockItem) this.getStack().getItem()).getBlock()))) {
                            fillSapling(this.world, pos, ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState());
                            this.getStack().setCount(this.getStack().getCount() - 4);
                            break;
                        }
                    }
                }

                // plant 1x1 tree
                if (this.getStack().getCount() > 0
                        && spaceOK(this.world, this.getBlockPos()
                        , ((SaplingBlock) ((BlockItem) this.getStack().getItem()).getBlock()))) {
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
            if (Config.getPlayerAround() > 0 && playerAround(this.world, this.getBlockPos())) {
                return false;
            }
            if (Config.getAvoidDense() > 0 && hasOther(this.world, this.getBlockPos())) {
                return false;
            }
            return spaceOK(this.world, this.getBlockPos(), ((SaplingBlock) ((BlockItem) this.getStack().getItem()).getBlock()));
        }
        return false;
    }

    private static boolean spaceOK(World world, BlockPos pos, SaplingBlock sapling) {
        return sapling.canPlaceAt(sapling.getDefaultState(), world, pos)
                && world.getBlockState(pos).getMaterial().isReplaceable();
    }

    private static boolean spaceOK2x2(World world, BlockPos pos, SaplingBlock sapling) {
        for (BlockPos tmpos : BlockPos.iterate(pos, pos.add(1, 0, 1))) {
            if (!spaceOK(world, tmpos, sapling)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasOther(World world, BlockPos target) {
        for (BlockPos pos : BlockPos.iterateOutwards(target,
                Config.getAvoidDense(), Config.getAvoidDense(), Config.getAvoidDense())) {
            if (world.getBlockState(pos).getBlock() instanceof LeavesBlock
                    || world.getBlockState(pos).getBlock() instanceof SaplingBlock
                    || world.getBlockState(pos).isIn(BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }

    private static void fillSapling(World world, BlockPos pos, BlockState blockState) {
        for (BlockPos tmpos : BlockPos.iterate(pos, pos.add(1, 0, 1))) {
            world.setBlockState(tmpos, blockState);
        }
    }

    private static boolean playerAround(World world, BlockPos pos) {
        for (PlayerEntity player : world.getPlayers()) {
            if (player.getBlockPos().getManhattanDistance(pos) <= Config.getPlayerAround()) {
                return true;
            }
        }
        return false;
    }
}
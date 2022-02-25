package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Config;
import net.minecraft.block.*;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
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
                BlockPos pos = this.getBlockPos();
                if (this.getPos().getY() % 1 != 0) {
                    pos = pos.add(0, 1, 0);
                }
                this.plantAge = 0;  // reset age
                if (!(
                        (Config.getPlayerAround() > 0 && playerAround(this.world, pos))
                        || (Config.getAvoidDense() > 0 && hasOther(this.world, pos))
                )) {
                    // plant 2x2 tree
                    if (Config.getPlantLarge()
                            && ((BlockItem) this.getStack().getItem()).getBlock() instanceof SaplingBlock
                            && ((SaplingBlockAccessor) ((BlockItem) this.getStack().getItem()).getBlock()).getGenerator() instanceof LargeTreeSaplingGenerator
                            && this.getStack().getCount() >= 4) {
                        for (BlockPos tmpos : BlockPos.iterate(pos.add(-1, 0, -1), pos)) {
                            if (spaceOK2x2(this.world, tmpos, ((SaplingBlock) ((BlockItem) this.getStack().getItem()).getBlock()))) {
                                fillSapling(this.world, tmpos, ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState());
                                this.getStack().setCount(this.getStack().getCount() - 4);
                                break;
                            }
                        }
                    }

                    // plant other
                    if (this.getStack().getCount() > 0
                            && spaceOK(this.world, pos
                            , ((PlantBlock) ((BlockItem) this.getStack().getItem()).getBlock()))) {
                        // plant at own position
                        this.world.setBlockState(pos,
                                ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState(),
                                Block.NOTIFY_ALL);
                        // minus 1 count
                        this.getStack().setCount(this.getStack().getCount() - 1);
                    }
                }
            }
        }
    }

    private boolean plantable() {
        return this.world != null  // is world loaded
                // is touching ground
                && this.onGround
                // is item a block
                && Config.itemOK(this.getStack().getItem());
    }

    private static boolean spaceOK(World world, BlockPos pos, PlantBlock block) {
        if (block instanceof FluidFillable) {
            return world.getBlockState(pos).getFluidState().isOf(Fluids.WATER)
                    && block.canPlaceAt(block.getDefaultState(), world, pos);
        } else {
            return block.canPlaceAt(block.getDefaultState(), world, pos)
                    && world.getBlockState(pos).getMaterial().isReplaceable();
        }
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
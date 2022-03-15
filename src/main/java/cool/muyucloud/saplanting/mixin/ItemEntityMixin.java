package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Config;
import cool.muyucloud.saplanting.MultiThreadSignal;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOGGER = LogManager.getLogger();

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (!this.world.isClient()) {
            if (Config.isPlantableItem(this.getStack().getItem())) {
                MultiThreadSignal.addTask(this);
            }
            if (!MultiThreadSignal.threadAlive()) {
                LOGGER.info("Creating thread for entity processing.");
                if (!MultiThreadSignal.registerThread(new Thread(ItemEntityMixin::loop))) {
                    LOGGER.info("Thread already exists! Cancelled creating.");
                } else {
                    MultiThreadSignal.threadStart();
                }
            }
        }
    }

    private static void loop() {
        try {
            while (true) {
                if (MultiThreadSignal.taskEmpty()) {
                    Thread.sleep(1);
                }
                plant((ItemEntityMixin) MultiThreadSignal.popTask());
            }
        } catch (Exception ignored) {}
        MultiThreadSignal.killThread();
    }
    
    private static void plant(ItemEntityMixin itemEntityMixin) {
        if (itemEntityMixin == null) {
            return;
        }
        if (Config.getPlantEnable()) {
            // schedule planting
            if (itemEntityMixin.plantable()) {  // if OK to plant, plus age
                ++itemEntityMixin.plantAge;
            } else {                            // if not, reset age
                itemEntityMixin.plantAge = 0;
            }

            // if age reach plant delay, do planting
            if (itemEntityMixin.plantAge >= Config.getPlantDelay() && itemEntityMixin.plantable()) {
                BlockPos pos = itemEntityMixin.getBlockPos();
                if (itemEntityMixin.getPos().getY() % 1 != 0) {
                    pos = pos.add(0, 1, 0);
                }
                itemEntityMixin.plantAge = 0;  // reset age
                if (!((Config.getPlayerAround() > 0 && playerAround(itemEntityMixin.world, pos))
                        || (Config.getAvoidDense() > 0 && hasOther(itemEntityMixin.world, pos))
                )) {
                    // plant 2x2 tree
                    if (Config.getPlantLarge()
                            && ((BlockItem) itemEntityMixin.getStack().getItem()).getBlock() instanceof SaplingBlock
                            && ((SaplingBlockAccessor) ((BlockItem) itemEntityMixin.getStack().getItem()).getBlock()).getGenerator() instanceof LargeTreeSaplingGenerator
                            && itemEntityMixin.getStack().getCount() >= 4) {
                        for (BlockPos tmpos : BlockPos.iterate(pos.add(-1, 0, -1), pos)) {
                            if (spaceOK2x2(itemEntityMixin.world, tmpos, ((SaplingBlock) ((BlockItem) itemEntityMixin.getStack().getItem()).getBlock()))) {
                                fillSapling(itemEntityMixin.world, tmpos, ((BlockItem) itemEntityMixin.getStack().getItem()).getBlock().getDefaultState());
                                itemEntityMixin.getStack().setCount(itemEntityMixin.getStack().getCount() - 4);
                                break;
                            }
                        }
                    }

                    // plant other
                    if (itemEntityMixin.getStack().getCount() > 0
                            && spaceOK(itemEntityMixin.world, pos
                            , ((PlantBlock) ((BlockItem) itemEntityMixin.getStack().getItem()).getBlock()))) {
                        // plant at own position
                        itemEntityMixin.world.setBlockState(pos,
                                ((BlockItem) itemEntityMixin.getStack().getItem()).getBlock().getDefaultState(),
                                Block.NOTIFY_ALL);
                        // minus 1 count
                        itemEntityMixin.getStack().setCount(itemEntityMixin.getStack().getCount() - 1);
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
package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Config;
import cool.muyucloud.saplanting.Saplanting;
import cool.muyucloud.saplanting.thread.ItemEntityThread;
import net.minecraft.block.*;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    private static final Logger LOGGER = Saplanting.getLogger();
    private boolean plantOK = false;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (!this.world.isClient()) {
            // check if thread died
            if (!ItemEntityThread.isThreadExists()) {
                LOGGER.info("Creating thread for entity processing.");
                ItemEntityThread.initThread(ItemEntityMixin::run);
            }

            // add this item to task queue
            if (Config.isPlantableItem(this.getStack().getItem()) && !this.plantOK) {
                ItemEntityThread.addTask(this);
                if (ItemEntityThread.isThreadWaiting()) {
                    ItemEntityThread.wakeUp();
                }
            }

            // plant if ok to do so
            if (this.plantOK) {
                BlockPos pos = this.getBlockPos();

                // correct position
                if (this.getPos().getY() % 1 != 0) {
                    pos = pos.add(0, 1, 0);
                }

                // plant 2x2 tree
                if (Config.getPlantLarge()
                        && ((BlockItem) this.getStack().getItem()).getBlock() instanceof SaplingBlock
                        && ((SaplingBlockAccessor) ((BlockItem) this.getStack().getItem()).getBlock()).getGenerator() instanceof LargeTreeSaplingGenerator
                        && this.getStack().getCount() >= 4) {
                    for (BlockPos tmpos : BlockPos.iterate(pos.add(-1, 0, -1), pos)) {
                        if (spaceOK2x2(this.world, tmpos, ((SaplingBlock) ((BlockItem) this.getStack().getItem()).getBlock()))) {
                            fillSapling(this.world, tmpos, ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState());
                            this.getStack().setCount(this.getStack().getCount() - 4);
                            return;
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

                this.plantOK = false;
            }
        }
    }

    private synchronized static void run() {
        try {
            while (!ItemEntityThread.scheduledKill()) {
                if (ItemEntityThread.taskEmpty()) {
                    try {
                        ItemEntityThread.sleep();
                    } catch (Exception ignored) {}
                    continue;
                }
                plant(((ItemEntityMixin) ItemEntityThread.popTask()));
            }
        } catch (Exception e) {
            LOGGER.error("Saplanting item entity process exited unexpectedly.");
            e.printStackTrace();
        }
        LOGGER.info("Saplanting item entity process discarding");
        ItemEntityThread.markAsStopped();
    }
    
    private static void plant(ItemEntityMixin itemEntityMixin) {
        // exit if auto plant not enabled
        if (!Config.getPlantEnable()) {
            return;
        }

        // schedule planting
        if (itemEntityMixin.plantable()) {  // if OK to plant, plus age
            ++itemEntityMixin.plantAge;
        } else {                            // if not, reset age and exit
            itemEntityMixin.plantAge = 0;
            return;
        }

        // if age do not reach plant delay, exit
        if (itemEntityMixin.plantAge < Config.getPlantDelay()) {
            return;
        }

        BlockPos pos = itemEntityMixin.getBlockPos();

        // correct position
        if (itemEntityMixin.getPos().getY() % 1 != 0) {
            pos = pos.add(0, 1, 0);
        }

        // reset age
        itemEntityMixin.plantAge = 0;

        // plant around player? and is player around?
        if (Config.getPlayerAround() > 0
                && itemEntityMixin.world.isPlayerInRange(itemEntityMixin.getX(), itemEntityMixin.getY(), itemEntityMixin.getZ(), Config.getPlayerAround())) {
            return;
        }

        // avoid dense? and is too dense?
        if (Config.getAvoidDense() > 0
                && hasOther(itemEntityMixin.world, pos)) {
            return;
        }

        itemEntityMixin.plantOK = true;
    }

    private boolean plantable() {
        BlockPos pos = this.getBlockPos();

        // correct position
        if (this.getPos().getY() % 1 != 0) {
            pos = pos.add(0, 1, 0);
        }
        
        return this.world != null  // is world loaded
                // is touching ground
                && this.onGround
                // is item a block
                && Config.itemOK(this.getStack().getItem())
                // is space ok
                && spaceOK(this.world, pos, ((PlantBlock) ((BlockItem) this.getStack().getItem()).getBlock()));
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
}
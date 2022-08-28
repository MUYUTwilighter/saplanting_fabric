package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Config;
import cool.muyucloud.saplanting.Saplanting;
import cool.muyucloud.saplanting.access.ItemEntityAccess;
import cool.muyucloud.saplanting.thread.ItemEntityThread;
import net.minecraft.block.*;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
        extends Entity implements ItemEntityAccess {
    @Shadow
    public abstract ItemStack getStack();

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
            if (!this.plantOK) {
                ItemEntityThread.addTask(this);
                return;
            }

            // plant if ok to do so
            if (!this.plantable()) {
                this.plantOK = false;
                return;
            }

            // correct position
            BlockPos pos = (this.getPos().getY() % 1 != 0) ? this.getBlockPos().up() :
                    this.getBlockPos();

            // plant 2x2 tree
            if (Config.getPlantLarge()
                    && ((BlockItem) this.getStack().getItem()).getBlock() instanceof SaplingBlock
                    && ((SaplingBlockAccessor) ((BlockItem) this.getStack().getItem()).getBlock()).getGenerator() instanceof LargeTreeSaplingGenerator
                    && this.getStack().getCount() >= 4) {
                for (BlockPos tmpos : BlockPos.iterate(pos.add(-1, 0, -1), pos)) {
                    if (spaceOK2x2(this.world, tmpos, ((PlantBlock) ((BlockItem) this.getStack().getItem()).getBlock()))) {
                        fillSapling(this.world, tmpos, ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState());
                        this.getStack().setCount(this.getStack().getCount() - 4);
                        this.plantOK = false;
                        return;
                    }
                }
            }

            // plant other
            if (this.getStack().getCount() > 0
                    && spaceOK(this.world, pos
                    , ((PlantBlock) ((BlockItem) this.getStack().getItem()).getBlock()))
                    && shapeOK(this.getStack())) {
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

    private static boolean shapeOK(ItemStack itemStack) {
        if (Config.getIgnoreShape()) {
            return true;
        }

        return !(itemStack.getCount() < 4 &&
                ((SaplingGeneratorAccessor) ((SaplingBlockAccessor) ((BlockItem) itemStack.getItem()).getBlock()).getGenerator()).getTreeFeature(new Random(), true) == null);
    }

    private synchronized static void run() {
        try {
            while (!ItemEntityThread.scheduledKill()) {
                if (ItemEntityThread.taskEmpty()) {
                    ItemEntityThread.sleep(5);
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

        // reset age
        itemEntityMixin.plantAge = 0;

        // plant around player? and is player around?
        if (Config.getPlayerAround() > 0
                && itemEntityMixin.world.isPlayerInRange(itemEntityMixin.getX()
                , itemEntityMixin.getY(), itemEntityMixin.getZ(), Config.getPlayerAround())) {
            return;
        }

        // avoid dense? and is too dense?
        if (Config.getAvoidDense() > 0 && itemEntityMixin.hasOther()) {
            return;
        }

        itemEntityMixin.plantOK = true;
    }

    /*
     * This check:
     * 1. world loaded
     * 2. is on ground
     * 3. is permitted to plant in config
     * 4. this.spaceOK();
     *  */
    private boolean plantable() {
        // correct position
        BlockPos pos = (this.getPos().getY() % 1 != 0) ? this.getBlockPos().up() : this.getBlockPos();

        return this.world != null  // is world loaded
                // is touching ground
                && this.onGround
                // is item a block
                && Config.itemOK(this.getStack().getItem())
                // is space ok
                && spaceOK(this.world, pos, ((PlantBlock) ((BlockItem) this.getStack().getItem()).getBlock()));
    }

    /* This check:
     * 1. block.canPlaceAt()
     * 2. temp block pos is replaceable
     * 3. is not a waterlogged-only block
     * */
    private static boolean spaceOK(World world, BlockPos pos, PlantBlock block) {
        // if this is a block that CAN ONLY be planted in water
        if (block instanceof FluidFillable && block.getDefaultState().getFluidState().isOf(Fluids.WATER)) {
            return false;
        }
        return block.canPlaceAt(block.getDefaultState(), world, pos) && world.getBlockState(pos).getMaterial().isReplaceable();
    }

    private static boolean spaceOK2x2(World world, BlockPos pos, PlantBlock sapling) {
        for (BlockPos tmpos : BlockPos.iterate(pos, pos.add(1, 0, 1))) {
            if (!spaceOK(world, tmpos, sapling)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOther() {
        if (!(((BlockItem) this.getStack().getItem()).getBlock() instanceof SaplingBlock)) {
            return false;
        }

        for (ItemEntity entity : this.world.getEntitiesByType(EntityType.ITEM, Box.from(BlockBox.create(
                this.getBlockPos().add(-Config.getAvoidDense(), -Config.getAvoidDense(), -Config.getAvoidDense()),
                this.getBlockPos().add(Config.getAvoidDense(), Config.getAvoidDense(), Config.getAvoidDense()))), entity -> true)) {
            if (((ItemEntityAccess) entity).isPlantOK()) {
                return true;
            }
        }

        for (BlockPos pos : BlockPos.iterate(
                this.getBlockPos().add(-Config.getAvoidDense(), -Config.getAvoidDense(), -Config.getAvoidDense()),
                this.getBlockPos().add(Config.getAvoidDense(), Config.getAvoidDense(), Config.getAvoidDense()))) {
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

    @Override
    public boolean isPlantOK() {
        return this.plantOK;
    }
}
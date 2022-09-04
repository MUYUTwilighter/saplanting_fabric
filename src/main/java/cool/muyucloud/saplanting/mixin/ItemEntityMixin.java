package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.util.Config;
import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.block.*;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.Random;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow
    public abstract ItemStack getStack();

    private static final Config CONFIG = Saplanting.getConfig();
    private static final Logger LOGGER = Saplanting.getLogger();

    private static final LinkedList<ItemEntityMixin> TASKS = new LinkedList<>();

    int plantAge = 0;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Pre-operation that directly injected into ItemEntity#tick()
     * Including:
     * 1. filter unwanted item.
     * 2. kill/awaken thread according to property "multiThread"
     * 3. dispatch item entity as tasks and deal with them
     *  */
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        Item item = this.getStack().getItem();
        /* Is wanted item */
        if (this.world.isClient() || !Saplanting.isPlantItem(item) || !CONFIG.getAsBoolean("plantEnable")) {
            return;
        }

        /* Kill if multi thread disabled, and deal with item here */
        if (!CONFIG.getAsBoolean("multiThread")) {
            Saplanting.THREAD_ALIVE = false;
            this.run();
            return;
        }

        /* Run Thread if thread not alive */
        if (!Saplanting.THREAD_ALIVE) {
            Saplanting.THREAD_ALIVE = true;
            LOGGER.info("Launching Saplanting core thread.");
            Thread THREAD = new Thread(ItemEntityMixin::multiThreadRun);
            THREAD.setName("SaplantingCoreThread");
            THREAD.start();
        }

        /**/
        TASKS.add(this);
    }

    /**
     * This check validation of plant-operation every single game-tick,
     * it goes before round-check
     * Including:
     * 1. item is on ground
     * 2. saplanting is enabled
     * 3. is not a water-oriented plant
     * 4. block BELOW the itemEntity allows the plant to grow
     * 5. block AT the itemEntity is replaceable
     */
    private boolean tickCheck() {
        BlockItem item = ((BlockItem) this.getStack().getItem());
        if (!this.onGround || !CONFIG.getAsBoolean("plantEnable") || !Saplanting.isPlantAllowed(item)) {
            return false;
        }

        BlockPos pos = this.getBlockPos();
        if (this.getY() % 1 != 0) {
            pos = pos.up();
        }

        BlockState state = item.getBlock().getDefaultState();
        if (!state.getFluidState().isEmpty()) {
            return false;
        }

        return state.canPlaceAt(world, pos) && world.getBlockState(pos).getMaterial().isReplaceable();
    }

    /**
     * This check validation of plant-operation every time when reaching plant-delay,
     * it goes after tick-check
     * Including:
     * 1. are players nearby?
     * 2. are there other blocks nearby representing there might be trees?
     */
    private boolean roundCheck() {
        PlantBlock block = ((PlantBlock) ((BlockItem) this.getStack().getItem()).getBlock());
        BlockPos pos = this.getBlockPos();
        if (this.getY() % 1 != 0) {
            pos = pos.up();
        }

        /* Player Nearby Check */
        int playerAround = CONFIG.getAsInt("playerAround");
        if (playerAround > 0 && world.isPlayerInRange(getX(), getY(), getZ(), playerAround)) {
            return false;
        }

        int avoidDense = CONFIG.getAsInt("avoidDense");
        if (block instanceof SaplingBlock) {
            /* Avoid Dense Check */
            if (avoidDense > 0) {
                for (BlockPos tmpPos : BlockPos.iterate(
                    pos.add(avoidDense, avoidDense, avoidDense),
                    pos.add(-avoidDense, -avoidDense, -avoidDense))) {
                    Block tmpBlock = world.getBlockState(tmpPos).getBlock();
                    BlockState state = tmpBlock.getDefaultState();
                    if (tmpBlock instanceof LeavesBlock
                        || tmpBlock instanceof SaplingBlock
                        || state.isIn(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Plant operation, but also combines some checks.
     * Including:
     * 1. is planting large tree allowed? is it a large tree? So then do planting
     * 2. is it available to grow a small tree? So then do planting
     * Both of above involve environment check
     */
    private void plant() {
        ItemStack stack = this.getStack();
        PlantBlock block = ((PlantBlock) ((BlockItem) stack.getItem()).getBlock());
        BlockState state = block.getDefaultState();
        BlockPos pos = this.getBlockPos();
        if (this.getY() % 1 != 0) {
            pos = pos.up();
        }

        if (block instanceof SaplingBlock) {
            SaplingGeneratorAccessor generator = ((SaplingGeneratorAccessor) ((SaplingBlockAccessor) block).getGenerator());
            /* Plant Large Tree */
            if (CONFIG.getAsBoolean("plantLarge") && stack.getCount() >= 4 && generator instanceof LargeTreeSaplingGenerator) {
                for (BlockPos tmpPos : BlockPos.iterate(pos, pos.add(-1, 0, -1))) {
                    if (block.canPlaceAt(state, world, tmpPos) && world.getBlockState(tmpPos).getMaterial().isReplaceable()
                        && block.canPlaceAt(state, world, tmpPos.add(1, 0, 0)) && world.getBlockState(tmpPos.add(1, 0, 0)).getMaterial().isReplaceable()
                        && block.canPlaceAt(state, world, tmpPos.add(1, 0, 1)) && world.getBlockState(tmpPos.add(1, 0, 1)).getMaterial().isReplaceable()
                        && block.canPlaceAt(state, world, tmpPos.add(0, 0, 1)) && world.getBlockState(tmpPos.add(0, 0, 1)).getMaterial().isReplaceable()) {
                        world.setBlockState(tmpPos, state, Block.NOTIFY_ALL);
                        world.setBlockState(tmpPos.add(1, 0, 0), state, Block.NOTIFY_ALL);
                        world.setBlockState(tmpPos.add(0, 0, 1), state, Block.NOTIFY_ALL);
                        world.setBlockState(tmpPos.add(1, 0, 1), state, Block.NOTIFY_ALL);
                        stack.setCount(stack.getCount() - 4);
                        return;
                    }
                }
            }

            /* Ignore Shape */
            if (!CONFIG.getAsBoolean("ignoreShape") && generator.getTreeFeature(new Random(), true) == null) {
                return;
            }
        }

        /* Plant Small Objects(including sapling) */
        world.setBlockState(pos, state, Block.NOTIFY_ALL);
        stack.setCount(stack.getCount() - 1);
    }

    /**
     * Operations that deal with item entity every tick.
     * Saplanting.isPlantItem(item) should be done before.
     * Including:
     * 1. plantAge counter.
     * 2. tickCheck() and reset plantAge
     * 3. plantAge check then roundCheck()
     * 4. plant();
    * */
    public void run() {
        ++this.plantAge;

        if (!this.tickCheck()) {
            this.plantAge = 0;
            return;
        }

        if (this.plantAge < CONFIG.getAsInt("plantDelay")) {
            return;
        }

        if (this.roundCheck()) {
            this.plant();
        }
        this.plantAge = 0;
    }

    /**
     * Operations that take item entity from tasks and deal with them.
     * Including:
     * 1. take item entity from tasks and throw it to ItemEntityMixin::run()
     * 2. auto stop and sleep
     * */
    private static void multiThreadRun() {
        try {
            while (Saplanting.THREAD_ALIVE && CONFIG.getAsBoolean("plantEnable")) {
                if (TASKS.isEmpty()) {
                    Thread.sleep(50);
                    continue;
                }

                while (!TASKS.isEmpty() && CONFIG.getAsBoolean("plantEnable")) {
                    TASKS.removeFirst().run();
                }
            }
            LOGGER.info("Saplanting core thread exiting.");
        } catch (Exception e) {
            LOGGER.info("Saplanting core thread exited unexpectedly!");
            e.printStackTrace();
        }
        Saplanting.THREAD_ALIVE = false;
    }
}

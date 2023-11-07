package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Saplanting;
import cool.muyucloud.saplanting.util.Config;
import cool.muyucloud.saplanting.util.PlantContext;
import net.minecraft.block.*;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow
    public abstract ItemStack getStack();

    @Unique
    private static final Config CONFIG = Saplanting.getConfig();
    @Unique
    private static final Logger LOGGER = Saplanting.getLogger();

    @Unique
    private static final ConcurrentLinkedQueue<ItemEntityMixin> CHECK_TASKS = new ConcurrentLinkedQueue<>();
    @Unique
    private static final HashSet<Item> CONTAIN_ERROR = new HashSet<>();

    @Unique
    private int plantAge = 0;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Pre-operation that directly injected into ItemEntity#tick()
     * Including:
     * 1. filter unwanted item.
     * 2. kill/awaken thread according to property "multiThread"
     * 3. dispatch item entity as tasks and deal with them
     */
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.getWorld().isClient() || !CONFIG.getAsBoolean("plantEnable")) {
            return;
        }

        Item item = this.getStack().getItem();
        /* Is wanted item */
        if (CONTAIN_ERROR.contains(item) || !Saplanting.isPlantItem(item)) {
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
            Thread thread = new Thread(ItemEntityMixin::multiThreadRun);
            thread.setName("SaplantingCoreThread");
            thread.start();
        }

        /* Add item entity as tasks for multi thread run */
        this.addToQueue();
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
    @Unique
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
    @Unique
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
    @Unique
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
                        PlantContext context = new PlantContext();
                        context.setState(state);
                        context.setPos(tmpPos);
                        context.setWorld(world);
                        context.setLarge(true);
                        PlantContext.PLANT_TASKS.offer(context);
                        stack.setCount(stack.getCount() - 4);
                        return;
                    }
                }
            }

            /* Ignore Shape */
            if (!CONFIG.getAsBoolean("ignoreShape") && generator.getTreeFeature(Random.create(), true) == null) {
                return;
            }
        }

        /* Plant Small Objects(including sapling) */
        PlantContext context = new PlantContext();
        context.setState(state);
        context.setPos(pos);
        context.setWorld(world);
        context.setLarge(false);
        PlantContext.PLANT_TASKS.offer(context);
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
     */
    @Unique
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
            try {
                this.plant();
            } catch (Exception e) {
                LOGGER.error("Some Errors occurred during planting this item:  ");
                LOGGER.error(this.getDetail());
                e.printStackTrace();
                CONTAIN_ERROR.add(this.getStack().getItem());
                if (CONFIG.getAsBoolean("autoBlackList")) {
                    CONFIG.addToBlackList(this.getStack().getItem());
                }
            }
        }
        this.plantAge = 0;
    }

    /**
     * Operations that take item entity from tasks and deal with them.
     * Including:
     * 1. take item entity from tasks and throw it to ItemEntityMixin::run()
     * 2. auto stop and sleep
     */
    @Unique
    private static void multiThreadRun() {
        try {
            while (Saplanting.THREAD_ALIVE && CONFIG.getAsBoolean("plantEnable") && CONFIG.getAsBoolean("multiThread")) {
                long start = LocalTime.now().getLong(ChronoField.MILLI_OF_DAY);
                while (!CHECK_TASKS.isEmpty() && CONFIG.getAsBoolean("plantEnable") && Saplanting.THREAD_ALIVE && CONFIG.getAsBoolean("multiThread")) {
                    ItemEntityMixin task = CHECK_TASKS.poll();
                    Item item = task.getStack().getItem();
                    if (item instanceof AirBlockItem) { // In case item was removed mill-secs ago
                        continue;
                    }
                    task.run();
                }
                long end = LocalTime.now().getLong(ChronoField.MILLI_OF_DAY);
                long delta = end - start;
                long complement = delta < 0 || delta > 20 ? 0 : 20 - delta;
                Thread.sleep(complement);
            }
            LOGGER.info("Saplanting core thread exiting.");
        } catch (Exception e) {
            LOGGER.info("Saplanting core thread exited unexpectedly!");
            e.printStackTrace();
        }
        CHECK_TASKS.clear();
        Saplanting.THREAD_ALIVE = false;
    }

    /**
     * To visit task queue shared by saplanting-core-thread and MC server thread safely,
     * use this method to add items as tasks.
     * This should only be used by MC server thread.
     */
    @Unique
    private void addToQueue() {
        int size = CHECK_TASKS.size();
        if (size > CONFIG.getAsInt("maxTask")) {
            CHECK_TASKS.clear();
            if (CONFIG.getAsBoolean("warnTaskQueue")) {
                LOGGER.warn(String.format("Too many items! Cleared %s tasks.", size));
            }
        }
        CHECK_TASKS.add(this);
    }

    @Unique
    private String getDetail() {
        Vec3d pos = this.getPos();
        String biomes = this.world.getBiome(this.getBlockPos()).toString();
        String dim = this.world.getDimensionKey().getRegistry().toString();
        BlockItem item = ((BlockItem) this.getStack().getItem());
        Block block = item.getBlock();
        String output = String.format("ItemEntity: \"%s\" at %s in world \"%s\", biomes \"%s\"\n",
            this.getEntityName(), pos, dim, biomes);
        output += String.format("BlockItem: \"%s\"(%s)\n",
            item.getName(), Registries.ITEM.getId(item));
        output += String.format("Block: \"%s\"(%s)",
            block.getName(), block);
        return output;
    }
}
package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.Saplanting;
import cool.muyucloud.saplanting.access.BushBlockAccess;
import cool.muyucloud.saplanting.access.SaplingBlockAccess;
import cool.muyucloud.saplanting.access.TreeGrowerAccess;
import cool.muyucloud.saplanting.util.Config;
import cool.muyucloud.saplanting.util.PlantContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public abstract Component getName();

    @Shadow
    public abstract ItemStack getItem();

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

    public ItemEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    /**
     * Pre-operation that directly injected into ItemEntity#tick()<br/>
     * Including:<br/>
     * 1. filter unwanted item.<br/>
     * 2. kill/awaken thread according to property "multiThread"<br/>
     * 3. dispatch item entity as tasks and deal with them
     */
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.level().isClientSide() || !CONFIG.getAsBoolean("plantEnable")) {
            return;
        }

        Item item = this.getItem().getItem();
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
     * This check validation of plant-operation every single game-tick,<br/>
     * it goes before round-check<br/>
     * Including:<br/>
     * 1. item is on ground<br/>
     * 2. saplanting is enabled<br/>
     * 3. is not a water-oriented plant<br/>
     * 4. block BELOW the itemEntity allows the plant to grow<br/>
     * 5. block AT the itemEntity is replaceable
     */
    @Unique
    private boolean tickCheck() {
        Item item = this.getItem().getItem();
        if (!this.onGround() || !CONFIG.getAsBoolean("plantEnable") || !Saplanting.isPlantAllowed(item)) {
            return false;
        }
        BlockPos pos = this.blockPosition();
        if (this.getY() % 1 != 0) {
            pos = pos.above();
        }
        if (!level().getBlockState(pos).canBeReplaced()) {
            return false;
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            BlockState state = block.defaultBlockState();
            if (!state.getFluidState().isEmpty()) {
                return false;
            }
            return isPosValid(pos);
        }
        return true;
    }

    /**
     * This check validation of plant-operation every time when reaching plant-delay,<br/>
     * it goes after tick-check<br/>
     * Including:<br/>
     * 1. are players nearby?<br/>
     * 2. are there other blocks nearby representing there might be trees?
     */
    @Unique
    private boolean roundCheck() {
        Item item = this.getItem().getItem();
        if (!(item instanceof BlockItem blockItem)) {
            return true;
        }
        Block block = blockItem.getBlock();
        BlockPos pos = this.blockPosition();
        if (this.getY() % 1 != 0) {
            pos = pos.above();
        }

        /* Player Nearby Check */
        int playerAround = CONFIG.getAsInt("playerAround");
        if (playerAround > 0 && level().hasNearbyAlivePlayer(getX(), getY(), getZ(), playerAround)) {
            return false;
        }

        int avoidDense = CONFIG.getAsInt("avoidDense");
        if (block instanceof SaplingBlock) {
            /* Avoid Dense Check */
            if (avoidDense > 0) {
                for (BlockPos tmpPos : BlockPos.betweenClosed(
                    pos.offset(avoidDense, avoidDense, avoidDense),
                    pos.offset(-avoidDense, -avoidDense, -avoidDense))) {
                    Block tmpBlock = level().getBlockState(tmpPos).getBlock();
                    BlockState state = tmpBlock.defaultBlockState();
                    if (tmpBlock instanceof LeavesBlock
                        || tmpBlock instanceof SaplingBlock
                        || state.is(BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Plant operation, but also combines some checks.<br/>
     * Including:<br/>
     * 1. is planting large tree allowed? is it a large tree? So then do planting<br/>
     * 2. is it available to grow a small tree? So then do planting<br/>
     * Both of above involve environment check
     */
    @Unique
    private void plant() {
        Level world = level();
        ItemStack stack = this.getItem();
        BlockPos pos = this.blockPosition();
        if (this.getY() % 1 != 0) {
            pos = pos.above();
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            BlockState state = block.defaultBlockState();
            if (block instanceof SaplingBlock saplingBlock) {
                TreeGrowerAccess generator = (TreeGrowerAccess) (Object) ((SaplingBlockAccess) block).getTreeGrower();
                /* Plant Large Tree */
                if (CONFIG.getAsBoolean("plantLarge") && stack.getCount() >= 4 && generator.saplanting_fabric$hasLargeTree()) {
                    for (BlockPos tmpPos : BlockPos.betweenClosed(pos, pos.offset(-1, 0, -1))) {
                        if (((BushBlockAccess) saplingBlock).saplanting_fabric$invokeCanSurvive(state, world, tmpPos) && world.getBlockState(tmpPos).canBeReplaced()
                            && ((BushBlockAccess) saplingBlock).saplanting_fabric$invokeCanSurvive(state, world, tmpPos.offset(1, 0, 0)) && world.getBlockState(tmpPos.offset(1, 0, 0)).canBeReplaced()
                            && ((BushBlockAccess) saplingBlock).saplanting_fabric$invokeCanSurvive(state, world, tmpPos.offset(1, 0, 1)) && world.getBlockState(tmpPos.offset(1, 0, 1)).canBeReplaced()
                            && ((BushBlockAccess) saplingBlock).saplanting_fabric$invokeCanSurvive(state, world, tmpPos.offset(0, 0, 1)) && world.getBlockState(tmpPos.offset(0, 0, 1)).canBeReplaced()) {
                            PlantContext context = new PlantContext();
                            context.setStack(stack);
                            context.setPos(tmpPos);
                            context.setWorld((ServerLevel) world);
                            context.setLarge(true);
                            PlantContext.PLANT_TASKS.offer(context);
                            return;
                        }
                    }
                }
                /* Ignore Shape */
                if (!CONFIG.getAsBoolean("ignoreShape") && !generator.saplanting_fabric$hasSmallTree()) {
                    return;
                }
            }
        }

        /* Plant Small Objects(including sapling) */
        PlantContext context = new PlantContext();
        context.setStack(stack);
        context.setPos(pos);
        context.setWorld((ServerLevel) world);
        context.setLarge(false);
        PlantContext.PLANT_TASKS.offer(context);
    }

    @Unique
    @Nullable
    private BlockPos findLargeSpace(@NotNull BlockPos center) {
        // X X X
        // X ? X
        // X X X
        if (this.isPosValid(center)) {
            // X X X
            // ? 1 X
            // X X X
            BlockPos pos = center.offset(-1, 0, 0);
            if (this.isPosValid(pos)) {
                // X ? X
                // 1 1 X
                // X X X
                pos = center.offset(0, 0, 1);
                if (this.isPosValid(pos)) {
                    // ? 1 X
                    // 1 1 X
                    // X X X
                    pos = center.offset(-1, 0, 1);
                    if (this.isPosValid(pos)) {
                        // S 1 X
                        // 1 1 X
                        // X X X
                        return center.offset(-1, 0, 1);
                    } else {
                        // 0 1 X
                        // 1 1 X
                        // X ? X
                        pos = center.offset(0, 0, -1);
                        if (this.isPosValid(pos)) {
                            // 0 1 X
                            // 1 1 X
                            // ? 1 X
                            pos = center.offset(-1, 0, -1);
                            if (this.isPosValid(pos)) {
                                // 0 1 X
                                // S 1 X
                                // 1 1 X
                                return center.offset(-1, 0, 0);
                            } else {
                                // 0 1 X
                                // 1 1 ?
                                // 0 1 X
                                pos = center.offset(1, 0, 0);
                                if (this.isPosValid(pos)) {
                                    // 0 1 ?
                                    // 1 1 1
                                    // 0 1 X
                                    pos = center.offset(1, 0, 1);
                                    if (this.isPosValid(pos)) {
                                        // 0 S 1
                                        // 1 1 1
                                        // 0 1 X
                                        return center.offset(0, 0, 1);
                                    } else {
                                        // 0 1 0
                                        // 1 1 1
                                        // 0 1 ?
                                        pos = center.offset(1, 0, -1);
                                        if (this.isPosValid(pos)) {
                                            // 0 1 0
                                            // 1 S 1
                                            // 0 1 1
                                            return center;
                                        } else {
                                            // 0 1 0
                                            // 1 1 1
                                            // 0 1 0
                                            return null;
                                        }
                                    }
                                } else {
                                    // 0 1 X
                                    // 1 1 0
                                    // 0 1 X
                                    return null;
                                }
                            }
                        } else {
                            // 0 1 ?
                            // 1 1 ?
                            // X 0 X
                            if (this.isPosValid(center.offset(1, 0, 0)) && this.isPosValid(center.offset(1, 0, 1))) {
                                // 0 S 1
                                // 1 1 1
                                // X 0 X
                                return center.offset(0, 0, 1);
                            } else {
                                // 0 1 F
                                // 1 1 F
                                // X 0 X
                                return null;
                            }
                        }
                    }
                } else {
                    // X 0 X
                    // 1 1 X
                    // X ? X
                    pos = center.offset(0, 0, -1);
                    if (this.isPosValid(pos)) {
                        // X 0 X
                        // 1 1 X
                        // ? 1 X
                        pos = center.offset(-1, 0, -1);
                        if (this.isPosValid(pos)) {
                            // X 0 X
                            // 1 1 X
                            // 1 1 X
                            return center.offset(-1, 0, 0);
                        } else {
                            // X 0 X
                            // 1 1 ?
                            // 0 1 ?
                            if (this.isPosValid(center.offset(1, 0, 0)) && this.isPosValid(center.offset(1, 0, -1))) {
                                // X 0 X
                                // 1 S 1
                                // 0 1 1
                                return center;
                            } else {
                                // X 0 X
                                // 1 1 F
                                // 0 1 F
                                return null;
                            }
                        }
                    } else {
                        // X 0 X
                        // 1 1 X
                        // X 0 X
                        return null;
                    }
                }
            } else {
                // X X X
                // 0 1 ?
                // X X X
                pos = center.offset(1, 0, 0);
                if (this.isPosValid(pos)) {
                    // X ? X
                    // 0 1 1
                    // X X X
                    pos = center.offset(0, 0, 1);
                    if (this.isPosValid(pos)) {
                        // X 1 ?
                        // 0 1 1
                        // X X X
                        pos = center.offset(1, 0, 1);
                        if (this.isPosValid(pos)) {
                            // X S 1
                            // 0 1 1
                            // X X X
                            return center;
                        } else {
                            // X 1 0
                            // 0 1 1
                            // X ? ?
                            if (this.isPosValid(center.offset(0, 0, -1)) && this.isPosValid(center.offset(1, 0, -1))) {
                                // X 1 0
                                // 0 S 1
                                // X 1 1
                                return center;
                            } else {
                                // X 1 0
                                // 0 1 1
                                // X F F
                                return null;
                            }
                        }
                    } else {
                        // X 0 X
                        // 0 1 1
                        // X ? ?
                        if (this.isPosValid(center.offset(0, 0, -1)) && this.isPosValid(center.offset(1, 0, -1))) {
                            // X 0 X
                            // 0 S 1
                            // X 1 1
                            return center;
                        } else {
                            // X 0 X
                            // 0 1 1
                            // X F F
                            return null;
                        }
                    }
                } else {
                    // X X X
                    // 0 1 0
                    // X X X
                    return null;
                }
            }
        } else {
            // X X X
            // X 0 X
            // X X X
            return null;
        }
    }

    @Unique
    private Boolean isPosValid(@NotNull BlockPos pos) {
        Item item = this.getItem().getItem();
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof BushBlock plantBlock) {
            BlockState state = plantBlock.defaultBlockState();
            return ((BushBlockAccess) plantBlock).saplanting_fabric$invokeCanSurvive(state, this.level(), pos);
        } else {
            return this.level().getBlockState(pos).canBeReplaced();
        }
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
                CONTAIN_ERROR.add(this.getItem().getItem());
                if (CONFIG.getAsBoolean("autoBlackList")) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(this.getItem().getItem());
                    CONFIG.addToBlackList(id.toString());
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
                    Item item = task.getItem().getItem();
                    if (item instanceof AirItem) { // In case item was removed mill-secs ago
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
        Vec3 pos = this.position();
        Level world = this.level();
        String biomes = world.getBiome(this.blockPosition()).toString();
        String dim = world.dimension().registry().toString();
        Item item = this.getItem().getItem();
        String output = String.format("ItemEntity: \"%s\" at %s in world \"%s\", biomes \"%s\"\n",
            this.getDisplayName(), pos, dim, biomes);
        output += String.format("Item: \"%s\"(%s)",
            item.getName(), BuiltInRegistries.ITEM.getKey(item));
        return output;
    }
}
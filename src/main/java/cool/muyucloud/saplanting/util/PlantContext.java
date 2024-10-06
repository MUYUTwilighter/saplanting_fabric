package cool.muyucloud.saplanting.util;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PlantContext {
    public static final ConcurrentLinkedQueue<PlantContext> PLANT_TASKS = new ConcurrentLinkedQueue<>();

    private ItemStack stack;
    private ServerWorld world;
    private BlockPos pos;
    private Boolean large;
    private FakePlayer fakePlayer;

    public void plant() {
        if (large) {
            for (BlockPos tmpPos : BlockPos.iterate(pos, pos.add(1, 0, 1))) {
                this.useOnBlock(tmpPos);
            }
        } else {
            this.useOnBlock();
        }
    }

    private void useOnBlock() {
        this.useOnBlock(this.pos);
    }

    private void useOnBlock(BlockPos pos) {
        ItemUsageContext context = new ItemUsageContext(fakePlayer, Hand.MAIN_HAND, new BlockHitResult(pos.up().toCenterPos(), Direction.UP, pos, false));
        stack.useOnBlock(context);
        Block block = ((BlockItem) this.stack.getItem()).getBlock();
        if (this.world.getBlockState(pos).isOf(block)) {
            stack.setCount(stack.getCount() - 1);
        }
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(ServerWorld world) {
        this.world = world;
        fakePlayer = FakePlayer.get(world);
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public Boolean isLarge() {
        return large;
    }

    public void setLarge(Boolean large) {
        this.large = large;
    }
}

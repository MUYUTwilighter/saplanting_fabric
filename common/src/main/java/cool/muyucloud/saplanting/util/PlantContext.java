package cool.muyucloud.saplanting.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlantContext {
    public static final ConcurrentLinkedQueue<PlantContext> PLANT_TASKS = new ConcurrentLinkedQueue<>();

    private ItemStack stack;
    private ServerLevel world;
    private BlockPos pos;
    private Boolean large;
    private FakePlayer fakePlayer;

    public void plant() {
        if (large) {
            for (BlockPos tmpPos : BlockPos.betweenClosed(pos, pos.offset(1, 0, 1))) {
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
        fakePlayer.setMainHandItem(stack);
        UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, new BlockHitResult(pos.above().getCenter(), Direction.UP, pos, false));
        InteractionResult result = stack.useOn(context);
        fakePlayer.removeMainHandItem();
        Item item = stack.getItem();
        if (result.consumesAction()) {
            item.finishUsingItem(stack, world, fakePlayer);
        }
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public Level getWorld() {
        return world;
    }

    public void setWorld(ServerLevel world) {
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

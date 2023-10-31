package cool.muyucloud.saplanting.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlantContext {
    private BlockState state;
    private World world;
    private BlockPos pos;
    private Boolean large;

    public void plant() {
        if (large) {
            world.setBlockState(pos, state);
        } else {
            for (BlockPos tmpPos : BlockPos.iterate(pos, pos.add(1, 0, 1))) {
                world.setBlockState(tmpPos, state);
            }
        }
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
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

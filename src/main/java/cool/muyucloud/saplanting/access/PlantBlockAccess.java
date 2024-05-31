package cool.muyucloud.saplanting.access;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public interface PlantBlockAccess {
    boolean invokeCanPlaceAt(BlockState state, WorldView world, BlockPos pos);
}

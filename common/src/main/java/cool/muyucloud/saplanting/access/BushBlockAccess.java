package cool.muyucloud.saplanting.access;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface BushBlockAccess {
    boolean saplanting_fabric$invokeCanSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos);
}

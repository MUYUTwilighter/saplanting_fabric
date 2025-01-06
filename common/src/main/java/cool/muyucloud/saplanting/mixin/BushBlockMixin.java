package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.access.BushBlockAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BushBlock.class)
public abstract class BushBlockMixin implements BushBlockAccess {
    @Shadow protected abstract boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos);

    @Override
    public boolean saplanting_fabric$invokeCanSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return this.canSurvive(blockState, levelReader, blockPos);
    }
}
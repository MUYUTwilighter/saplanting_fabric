package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.access.PlantBlockAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlantBlock.class)
public abstract class PlantBlockMixin extends Block implements PlantBlockAccess {
    public PlantBlockMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    protected abstract boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos);

    @Override
    public boolean invokeCanPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return this.canPlaceAt(state, world, pos);
    }
}

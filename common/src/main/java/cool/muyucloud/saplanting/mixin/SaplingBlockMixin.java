package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.access.SaplingBlockAccess;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin extends VegetationBlock implements BonemealableBlock, SaplingBlockAccess {

    @Shadow @Final protected TreeGrower treeGrower;

    protected SaplingBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    @Unique
    public TreeGrower saplanting$getTreeGrower() {
        return this.treeGrower;
    }
}

package cool.muyucloud.saplanting.mixin;

import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin extends BushBlock implements BonemealableBlock, cool.muyucloud.saplanting.access.SaplingBlockAccess {

    @Shadow @Final protected TreeGrower treeGrower;

    protected SaplingBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    @Unique
    public TreeGrower getTreeGrower() {
        return this.treeGrower;
    }
}

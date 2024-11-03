package cool.muyucloud.saplanting.mixin;

import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin extends BushBlock implements BonemealableBlock, cool.muyucloud.saplanting.access.SaplingBlockAccess {
    @Shadow @Final private AbstractTreeGrower treeGrower;

    protected SaplingBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    @Unique
    public AbstractTreeGrower getTreeGrower() {
        return this.treeGrower;
    }
}

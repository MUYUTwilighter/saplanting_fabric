package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.access.SaplingGeneratorAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractTreeGrower.class)
public abstract class SaplingGeneratorMixin implements SaplingGeneratorAccess {
    @Shadow @Nullable protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource arg, boolean bl);

    @Unique
    private static final RandomSource RANDOM = RandomSource.create();

    @Unique
    @Override
    public boolean hasLargeTree() {
        return (AbstractTreeGrower) (Object) this instanceof AbstractMegaTreeGrower;
    }

    @Unique
    @Override
    public boolean hasSmallTree() {
        return this.getConfiguredFeature(RANDOM, false) != null;
    }
}

package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.access.TreeGrowerAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TreeGrower.class)
public abstract class TreeGrowerMixin implements TreeGrowerAccess {
    @Shadow @Nullable protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource arg, boolean bl);

    @Shadow @Nullable protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomSource);

    @Unique
    private static final RandomSource RANDOM = RandomSource.create();

    @Unique
    @Override
    public boolean hasLargeTree() {
        return this.getConfiguredMegaFeature(RANDOM) != null;
    }

    @Unique
    @Override
    public boolean hasSmallTree() {
        return this.getConfiguredFeature(RANDOM, false) != null;
    }
}

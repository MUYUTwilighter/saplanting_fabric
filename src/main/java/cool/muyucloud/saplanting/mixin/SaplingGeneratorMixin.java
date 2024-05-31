package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.access.SaplingGeneratorAccess;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SaplingGenerator.class)
public abstract class SaplingGeneratorMixin implements SaplingGeneratorAccess {
    private static final Random RANDOM = Random.create();

    @Shadow @Nullable protected abstract RegistryKey<ConfiguredFeature<?, ?>> getMegaTreeFeature(Random random);

    @Shadow @Nullable protected abstract RegistryKey<ConfiguredFeature<?, ?>> getSmallTreeFeature(Random random, boolean flowersNearby);

    @Override
    @Unique
    public boolean hasSmallTree() {
        return this.getSmallTreeFeature(RANDOM, false) != null;
    }

    @Override
    @Unique
    public boolean hasLargeTree() {
        return this.getMegaTreeFeature(RANDOM) != null;
    }
}

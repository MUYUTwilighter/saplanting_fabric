package cool.muyucloud.saplanting.mixin;

import cool.muyucloud.saplanting.access.SaplingGeneratorAccess;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SaplingGenerator.class)
public abstract class SaplingGeneratorMixin implements SaplingGeneratorAccess {
    @Shadow @Nullable protected abstract RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees);

    @Unique
    private static final Random RANDOM = Random.create();

    @Unique
    @Override
    public boolean hasLargeTree() {
        return (SaplingGenerator) (Object) this instanceof LargeTreeSaplingGenerator;
    }

    @Unique
    @Override
    public boolean hasSmallTree() {
        return this.getTreeFeature(RANDOM, false) != null;
    }
}

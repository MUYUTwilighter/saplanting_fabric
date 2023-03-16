package cool.muyucloud.saplanting.mixin;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SaplingGenerator.class)
public interface SaplingGeneratorAccessor {
    @Invoker("getTreeFeature")
    RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees);
}

package cool.muyucloud.saplanting.mixin;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

@Mixin(SaplingGenerator.class)
public interface SaplingGeneratorAccessor {
    @Invoker("getTreeFeature")
    RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees);
}

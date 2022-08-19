package cool.muyucloud.saplanting.mixin;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

@Mixin(SaplingGenerator.class)
public interface SaplingGeneratorAccessor {
    @Invoker("createTreeFeature")
    ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random random, boolean bees);
}

package cool.muyucloud.saplanting.reflection;

import com.google.common.reflect.Reflection;
import com.google.gson.internal.reflect.ReflectionHelper;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.lang.reflect.Method;

public class SaplingGeneratorReflection {
    private static final Method GET_SMALL_TREE_FEATURE;
    private static final Method GET_MEGA_TREE_FEATURE;

    static {
        try {
            GET_SMALL_TREE_FEATURE = SaplingGenerator.class.getDeclaredMethod("method_54087", Random.class, boolean.class);
            GET_MEGA_TREE_FEATURE = SaplingGenerator.class.getDeclaredMethod("method_54086", Random.class);
            GET_SMALL_TREE_FEATURE.setAccessible(true);
            GET_MEGA_TREE_FEATURE.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final SaplingGenerator generator;

    public SaplingGeneratorReflection(SaplingGenerator generator) {
        this.generator = generator;
    }

    public static SaplingGeneratorReflection of(SaplingGenerator generator) {
        return new SaplingGeneratorReflection(generator);
    }

    public RegistryKey<ConfiguredFeature<?, ?>> getSmallTreeFeature(Random random, boolean flowersNearby) {
        try {
            return (RegistryKey<ConfiguredFeature<?, ?>>) GET_SMALL_TREE_FEATURE.invoke(generator, random, flowersNearby);
        } catch (Exception ignored) {
            return null;
        }
    }

    public RegistryKey<ConfiguredFeature<?, ?>> getMegaTreeFeature(Random random) {
        try {
            return (RegistryKey<ConfiguredFeature<?, ?>>) GET_MEGA_TREE_FEATURE.invoke(generator, random);
        } catch (Exception ignored) {
            return null;
        }
    }
}

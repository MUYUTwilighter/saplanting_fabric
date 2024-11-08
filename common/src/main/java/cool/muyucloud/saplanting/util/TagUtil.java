package cool.muyucloud.saplanting.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;

public class TagUtil {
    public static <T> boolean isIn(TagKey<T> tagKey, T entry) {
        Optional<? extends Registry<?>> optionalRegistry = BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().location());
        if (optionalRegistry.isPresent()) {
            if (tagKey.isFor(optionalRegistry.get().key())) {
                Registry<T> registry = (Registry<T>) optionalRegistry.get();
                Optional<ResourceKey<T>> maybeKey = registry.getResourceKey(entry);
                // Check synced tag
                if (maybeKey.isPresent()) {
                    return registry.getOrThrow(maybeKey.get()).is(tagKey);
                }
            }
        }
        return false;
    }
}

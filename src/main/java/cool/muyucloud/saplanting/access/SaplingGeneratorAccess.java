package cool.muyucloud.saplanting.access;

import org.spongepowered.asm.mixin.Unique;

public interface SaplingGeneratorAccess {
    @Unique
    boolean hasSmallTree();

    @Unique
    boolean hasLargeTree();
}

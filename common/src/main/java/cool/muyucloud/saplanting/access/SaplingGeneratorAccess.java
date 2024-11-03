package cool.muyucloud.saplanting.access;

import org.spongepowered.asm.mixin.Unique;

public interface SaplingGeneratorAccess {
    @Unique
    boolean hasLargeTree();

    @Unique
    boolean hasSmallTree();
}

package cool.muyucloud.saplanting.access;

import org.spongepowered.asm.mixin.Unique;

public interface TreeGrowerAccess {
    @Unique
    boolean hasLargeTree();

    @Unique
    boolean hasSmallTree();
}

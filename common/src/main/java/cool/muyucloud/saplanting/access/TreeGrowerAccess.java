package cool.muyucloud.saplanting.access;

import org.spongepowered.asm.mixin.Unique;

public interface TreeGrowerAccess {
    @Unique
    boolean saplanting_fabric$hasLargeTree();

    @Unique
    boolean saplanting_fabric$hasSmallTree();
}

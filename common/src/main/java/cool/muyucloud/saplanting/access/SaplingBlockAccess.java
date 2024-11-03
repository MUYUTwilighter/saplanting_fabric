package cool.muyucloud.saplanting.access;

import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import org.spongepowered.asm.mixin.Unique;

public interface SaplingBlockAccess {
    @Unique
    AbstractTreeGrower getTreeGrower();
}

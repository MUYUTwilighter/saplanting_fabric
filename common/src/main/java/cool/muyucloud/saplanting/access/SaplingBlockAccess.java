package cool.muyucloud.saplanting.access;

import net.minecraft.world.level.block.grower.TreeGrower;
import org.spongepowered.asm.mixin.Unique;

public interface SaplingBlockAccess {
    @Unique
    TreeGrower getTreeGrower();
}

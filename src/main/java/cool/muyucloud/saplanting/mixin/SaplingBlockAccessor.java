package cool.muyucloud.saplanting.mixin;

import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SaplingBlock.class)
public interface SaplingBlockAccessor {
    @Accessor
    SaplingGenerator getGenerator();
}

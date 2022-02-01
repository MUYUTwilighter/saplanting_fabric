package io.github.muyutwilighter.saplanting.mixin;

import io.github.muyutwilighter.saplanting.Config;
import io.github.muyutwilighter.saplanting.Saplanting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
extends Entity {
    @Shadow public abstract ItemStack getStack();

    public int plantAge;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (Config.plantEnable) {
            // schedule planting
            if (this.plantable()) { // if OK to plant, plus age
                ++this.plantAge;
            } else {                // if not, reset age
                this.plantAge = 0;
            }

            // if age reach plant delay, do planting
            if (this.plantAge >= Config.plantDelay) {
                this.plantAge = 0;  // reset age
                // plant 2x2 tree
                if (Config.plantLarge && this.getStack().isIn(Saplanting.ItemTag.SAPLINGS_LARGE) && this.getStack().getCount() >= 4) {
                    if (this.spaceOK(this.getBlockPos(), this.getBlockPos().add(1, 0, 1))) {
                        this.fillBlockState(
                                this.getBlockPos(), this.getBlockPos().add(1, 0, 1),
                                ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                        );
                        this.getStack().setCount(this.getStack().getCount() - 4);
                    } else if (this.spaceOK(this.getBlockPos().add(-1, 0, 0), this.getBlockPos().add(0, 0, 1))) {
                        this.fillBlockState(
                                this.getBlockPos().add(-1, 0, 0), this.getBlockPos().add(0, 0, 1),
                                ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                        );
                        this.getStack().setCount(this.getStack().getCount() - 4);
                    } else if (this.spaceOK(this.getBlockPos().add(-1, 0, -1), this.getBlockPos())) {
                        this.fillBlockState(
                                this.getBlockPos().add(-1, 0, -1), this.getBlockPos(),
                                ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                        );
                        this.getStack().setCount(this.getStack().getCount() - 4);
                    } else if (this.spaceOK(this.getBlockPos().add(0, 0, -1), this.getBlockPos().add(1, 0, 0))) {
                        this.fillBlockState(
                                this.getBlockPos().add(0, 0, -1), this.getBlockPos().add(1, 0, 0),
                                ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                        );
                        this.getStack().setCount(this.getStack().getCount() - 4);
                    }
                }

                if (this.getStack().getCount() > 0 && this.spaceOK(this.getBlockPos())) {
                    // plant at own position
                    this.world.setBlockState(this.getBlockPos(),
                            ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState(),
                            Block.NOTIFY_ALL);
                    // minus 1 count
                    this.getStack().setCount(this.getStack().getCount() - 1);
                }
            }
        }
    }

    private boolean plantable() {
        if (this.world != null  // is world loaded
                // is item a block
                && this.getStack().getItem() instanceof BlockItem
                // is item a sapling
                && ((BlockItem) this.getStack().getItem()).getBlock() instanceof SaplingBlock
        ) {
            if (Config.avoidDense > 0) {
                if (this.areaHas(this.getBlockPos().add(-Config.avoidDense, 0, -Config.avoidDense),
                        this.getBlockPos().add(Config.avoidDense, 0, Config.avoidDense),
                        Saplanting.BlockTag.OTHERTREE)) {
                    return false;
                }
            }
            return this.spaceOK(this.getBlockPos());
        }
        return false;
    }

    private boolean spaceOK(BlockPos pos) {
        return ((((SaplingBlock) ((BlockItem) this.getStack().getItem()).getBlock()).canPlaceAt(
                ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState(), this.world, pos))
                && this.world.getBlockState(pos).isIn(Saplanting.BlockTag.REPLACEABLE));
    }

    private boolean spaceOK(BlockPos v1, BlockPos v2) {
        int a = v1.getX(), b, c;
        while (a <= v2.getX()) {
            b = v1.getY();
            while (b <= v2.getY()) {
                c = v1.getZ();
                while (c <= v2.getZ()) {
                    if (!this.spaceOK(new BlockPos(a, b, c))) {
                        return false;
                    }
                    ++c;
                }
                ++b;
            }
            ++a;
        }
        return true;
    }

    private boolean areaHas(BlockPos v1, BlockPos v2, Tag<Block> tag) {
        int a = v1.getX(), b, c;
        while (a <= v2.getX()) {
            b = v1.getY();
            while (b <= v2.getY()) {
                c = v1.getZ();
                while (c <= v2.getZ()) {
                    if (this.world.getBlockState(new BlockPos(a, b, c)).isIn(tag)) {
                        return true;
                    }
                    ++c;
                }
                ++b;
            }
            ++a;
        }
        return false;
    }

    private void fillBlockState(BlockPos v1, BlockPos v2, BlockState blockState) {
        int a = v1.getX(), b, c;
        while (a <= v2.getX()) {
            b = v1.getY();
            while (b <= v2.getY()) {
                c = v1.getZ();
                while (c <= v2.getZ()) {
                    this.world.setBlockState(new BlockPos(a, b, c), blockState, Block.NOTIFY_ALL);
                    ++c;
                }
                ++b;
            }
            ++a;
        }
    }
}

























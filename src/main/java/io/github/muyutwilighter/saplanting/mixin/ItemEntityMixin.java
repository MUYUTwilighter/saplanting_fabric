package io.github.muyutwilighter.saplanting.mixin;

import io.github.muyutwilighter.saplanting.Config;
import io.github.muyutwilighter.saplanting.Saplanting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
                // plant at own position
                this.world.setBlockState(this.getBlockPos(),
                        ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState(),
                        Block.NOTIFY_ALL);
                // minus 1 count
                this.getStack().setCount(this.getStack().getCount() - 1);
                // plant 2x2 tree
                if (Config.plantLarge && this.getStack().isIn(Saplanting.ItemTag.SAPLINGS_LARGE) && this.getStack().getCount() >= 3) {
                    if (this.getStack().isIn(Saplanting.ItemTag.SAPLINGS_NETHER)
                    ) {
                        if (this.areaIsIn(this.getBlockPos().down(), this.getBlockPos().add(1, -1, 1),
                                Saplanting.BlockTag.BASE_NETHER)) {
                            this.fillBlockState(
                                    this.getBlockPos(), this.getBlockPos().add(1, 0, 1),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        } else if (this.areaIsIn(this.getBlockPos().down().add(-1, 0, 0), this.getBlockPos().down().add(0, 0, 1),
                                Saplanting.BlockTag.BASE_NETHER)) {
                            this.fillBlockState(
                                    this.getBlockPos().add(-1, 0, 0), this.getBlockPos().add(0, 0, 1),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        } else if (this.areaIsIn(this.getBlockPos().down().add(-1, 0, -1), this.getBlockPos().down(),
                                Saplanting.BlockTag.BASE_NETHER)) {
                            this.fillBlockState(
                                    this.getBlockPos().add(-1, 0, -1), this.getBlockPos(),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        } else if (this.areaIsIn(this.getBlockPos().down().add(0, 0, -1), this.getBlockPos().down().add(1, 0, 0),
                                Saplanting.BlockTag.BASE_NETHER)) {
                            this.fillBlockState(
                                    this.getBlockPos().add(0, 0, -1), this.getBlockPos().add(1, 0, 0),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        }
                    } else if (this.getStack().isIn(Saplanting.ItemTag.SAPLINGS_OVERWORLD)) {
                        if (this.areaIsIn(this.getBlockPos().down(), this.getBlockPos().add(1, -1, 1),
                                Saplanting.BlockTag.BASE_OVERWORLD)) {
                            this.fillBlockState(
                                    this.getBlockPos(), this.getBlockPos().add(1, 0, 1),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        } else if (this.areaIsIn(this.getBlockPos().down().add(-1, 0, 0), this.getBlockPos().down().add(0, 0, 1),
                                Saplanting.BlockTag.BASE_OVERWORLD)) {
                            this.fillBlockState(
                                    this.getBlockPos().add(-1, 0, 0), this.getBlockPos().add(0, 0, 1),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        } else if (this.areaIsIn(this.getBlockPos().down().add(-1, 0, -1), this.getBlockPos().down(),
                                Saplanting.BlockTag.BASE_OVERWORLD)) {
                            this.fillBlockState(
                                    this.getBlockPos().add(-1, 0, -1), this.getBlockPos(),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        } else if (this.areaIsIn(this.getBlockPos().down().add(0, 0, -1), this.getBlockPos().down().add(1, 0, 0),
                                Saplanting.BlockTag.BASE_OVERWORLD)) {
                            this.fillBlockState(
                                    this.getBlockPos().add(0, 0, -1), this.getBlockPos().add(1, 0, 0),
                                    ((BlockItem) this.getStack().getItem()).getBlock().getDefaultState()
                            );
                            this.getStack().setCount(this.getStack().getCount() - 3);
                        }
                    }
                }
            }
        }
    }

    private boolean plantable() {
        if (this.world != null  // is world loaded
                // is item one of which is tagged as sapling
                && this.getStack().isIn(Saplanting.ItemTag.SAPLINGS)
                // is item got enough space
                && this.world.getBlockState(this.getBlockPos()).isIn(Saplanting.BlockTag.REPLACEABLE)
                // is item can be placed as block
                && this.getStack().getItem() instanceof BlockItem
        ) {
            if (Config.avoidDense > 0) {
                if (this.areaHas(this.getBlockPos().add(-Config.avoidDense, 0, -Config.avoidDense),
                        this.getBlockPos().add(Config.avoidDense, 0, Config.avoidDense),
                        Saplanting.BlockTag.OTHERTREE)) {
                    return false;
                }
            }
            return this.baseOK(this.getBlockPos().down());
        }
        return false;
    }

    private boolean baseOK(BlockPos pos) {
        // which category this item belongs to
        if (this.getStack().isIn(Saplanting.ItemTag.SAPLINGS_OVERWORLD)) {
            // is block below OK
            return this.world.getBlockState(pos).isIn(Saplanting.BlockTag.BASE_OVERWORLD);
        } else if (this.getStack().isIn(Saplanting.ItemTag.SAPLINGS_NETHER)) {
            return this.world.getBlockState(pos).isIn(Saplanting.BlockTag.BASE_NETHER);
        }
        return false;
    }

    private boolean areaIsIn(BlockPos v1, BlockPos v2, Tag<Block> tag) {
        int a = v1.getX(), b, c;
        while (a <= v2.getX()) {
            b = v1.getY();
            while (b <= v2.getY()) {
                c = v1.getZ();
                while (c <= v2.getZ()) {
                    if (!this.world.getBlockState(new BlockPos(a, b, c)).isIn(tag)) {
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

























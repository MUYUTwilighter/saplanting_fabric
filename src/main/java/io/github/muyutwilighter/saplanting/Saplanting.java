package io.github.muyutwilighter.saplanting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class Saplanting implements ModInitializer {
    public static class ItemTag {
        public static final Tag<Item> SAPLINGS_LARGE = TagFactory.ITEM.create(new Identifier("saplanting", "large2x2"));
    }

    public static class BlockTag {
        public static final Tag<Block> REPLACEABLE = TagFactory.BLOCK.create(new Identifier("saplanting", "replaceable"));
        public static final Tag<Block> OTHERTREE = TagFactory.BLOCK.create(new Identifier("saplanting", "othertree"));
    }

    @Override
    public void onInitialize() {
        Config.load();
    }
}
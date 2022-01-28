package io.github.muyutwilighter.saplanting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class Saplanting implements ModInitializer {
    public static final Tag<Item> SAPLINGS = TagFactory.ITEM.create(new Identifier("saplanting", "saplings"));
    public static final Tag<Item> SAPLINGS_OVERWORLD = TagFactory.ITEM.create(new Identifier("saplanting", "saplings/overworld"));
    public static final Tag<Item> SAPLINGS_NETHER = TagFactory.ITEM.create(new Identifier("saplanting", "saplings/nether"));
    public static final Tag<Item> SAPLINGS_LARGE = TagFactory.ITEM.create(new Identifier("saplanting", "saplings/overworld_large"));
    public static final Tag<Block> BASE_OVERWOLRD = TagFactory.BLOCK.create(new Identifier("saplanting", "base/overworld"));
    public static final Tag<Block> BASE_NETHER = TagFactory.BLOCK.create(new Identifier("saplanting", "base/nether"));
    public static final Tag<Block> REPLACEABLE = TagFactory.BLOCK.create(new Identifier("saplanting", "replaceable"));

    @Override
    public void onInitialize() {
        Config.load();
    }
}
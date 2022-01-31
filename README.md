# Saplanting(Fabric)
A fabric mod that automatically plants sapling drops in minecraft games.
Available for Minecraft 1.18.1
This is a fabric mod, fabric api is required.

**How to Install**

1. Download the .jar of proper version.
2. Copy or cut the jar file you've just downloaded to your mod path (usually ".../.minecraft/mods" or ".../.minecraft/versions/\<VersionName\>/mods")
3. Make sure you have installed fabric api and fabric of proper versions

**Configurations**

Config files is located at ".../.minecraft/config/saplanting.properties"

plantDelay: How many ticks before the sapling drop will be planted, default: 40, expect: nonnegative integers
avoidDense: The radius of area that will not plant when selected area already got other trees, default: 2, expect nonnegative integers
plantEnable: Enable auto-planting, default: true, expect: boolean
plantLarge: Try to plant 2x2 trees, defaut: true, expect: boolean

**Brief**

If you can not spare enough time and effort to plant saplings in Minecraft or the players on server are too lazy too do so, this datapack will help you!

When a sapling item drops on dirt(or something else that let saplings grow), it will automatically turn into sapling block in 2 seconds. The sapling-planting will not destroy the block that already take up the place(except air and blocks with tag "minecraft:replaceable").


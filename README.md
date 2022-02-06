# Saplanting(Fabric)
A fabric mod that automatically plants sapling drops in minecraft games.
Saplings from other mods are supported theoretically.
Available for Minecraft 1.18.1
This is a fabric mod, fabric api is required.

**How to Install**

1. Download the .jar of proper version.
2. Copy or cut the jar file you've just downloaded to your mod path (usually ".../.minecraft/mods" or ".../.minecraft/versions/\<VersionName\>/mods")
3. Make sure you have installed fabric api and fabric of proper versions

**Configurations**

Config files is located at ".../.minecraft/config/saplanting.properties"

 - plantDelay: How many ticks before the sapling drop will be planted, default: 40, expect: nonnegative integers
 - avoidDense: The radius of area that will not plant when selected area already got other trees, default: 2, expect nonnegative integers
 - plantEnable: Enable auto-planting, default: true, expect: boolean
 - plantLarge: Try to plant 2x2 trees, defaut: true, expect: boolean

**Command**

All the commands require admin-permission to execute, feedback only supports Simplified Chinese originally.

 - /saplanting: show all current properties;
 - /saplanting load: load properties from file;
 - /saplanting save: save current properties into file;
 - /saplanting \<PropertyName\>: show value of target property;
 - /saplanting \<PropertyName\> \<value\>: set value of target property;<br>
 Tip: this command will not get current properties saved into file immediately, current properties will be saved when using command "/saplanting save" or dicarding from world save.

**Brief**

If you can not spare enough time and effort to plant saplings in Minecraft or the players on server are too lazy too do so, this datapack will help you!

When a sapling item drops on dirt(or something else that let saplings grow), it will automatically turn into sapling block in 2 seconds. The sapling-planting will not destroy the block that already take up the place(except air and replaceable blocks).

**Contact Me**

GitHub: https://github.com/MUYUTwilighter/saplanting_fabric<br>
MCMOD: https://www.mcmod.cn/class/5221.html


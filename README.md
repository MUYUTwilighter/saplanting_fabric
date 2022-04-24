---
title: "Saplanting(Fabric) Manual"
author: MUYU_Twilighter
<<<<<<< HEAD
date: 2022.4.24
=======
date: 2022.3.24
>>>>>>> cbf8f254f6218ab8dae95c3289e693c576700bec
output: pdf_document
---
[![](http://cf.way2muchnoise.eu/full_saplanting_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/saplanting) [![Discord](https://img.shields.io/discord/966726130105217094)](https://discord.gg/JunKeKCJAY)  
# Saplanting(Fabric)
A fabric mod that automatically plants sapling drops in minecraft games.  
Saplings from other mods are supported theoretically.  
Other plants are also available, if you configure them.  
Make sure that your Minecraft version is supported by the mod.  
This is a fabric mod, fabric api is required.

## Introduction

If you can not spare enough time and effort to plant saplings in Minecraft or the players on server are too lazy to do so, this mod will help you!  
When a sapling item drops on dirt(or something else that let saplings grow), it will automatically turn into sapling block in 2 seconds. The sapling-planting will not destroy the block that already take up the place(except air and replaceable blocks).  
What's more surprising, other plants can be automatically planted now! But this feature is disabled by default, use commands below or configure the JSON file.

## How to Install

1. Download the Jar File of proper version.
2. Copy or cut the jar file you've just downloaded to your mod path (usually ".../.minecraft/mods" or ".../.minecraft/versions/\<VersionName\>/mods")
3. Make sure you have installed fabric api and fabric of proper versions

## Configurations

Config file is located at ".../.minecraft/config/saplanting.json"  
Config file will only be automatically loaded when a world-save is loaded. If you want the changes in this file loaded manually, use commands to do so.

 - plantEnable: Enable auto-planting, default: true, expect: boolean  
 - plantLarge: Try to plant 2x2 trees, default: true, expect: boolean  
Only for sapling blocks that can be planted in shape of 2x2.
 - blackListEnable: enable black list, default: true, expect: boolean
 - allowSapling: Enable auto-planting for saplings, default: true, expect: boolean
 - allowCrop: Enable auto-plant for crops, default: true, expect: boolean
 - allowMushroom: Enable auto-planting for mushrooms, default: false, expect: boolean
 - allowFungus: Enable auto-planting for fungus, default: false, expect: boolean
 - allowFlower: Enable auto-planting for flowers, default: false, expect: boolean
 - allowOther: Enable auto-planting for other plants, default: false, expect: boolean
 - plantDelay: How many ticks before the sapling drop will be planted, default: 40, expect: nonnegative integers
 - avoidDense: The radius of area that will not plant when selected area already got other trees, default: 2, expect nonnegative integers
 - playerAround: Not to plant if player around, default: 2, expect nonnegative integers
 - blackList: content of black list, add item like "minecraft:oak_sapling"

## Commands

All the commands require admin-permission to execute, feedback only supports Simplified Chinese by default.  
All the changes made by commands will not be saved to config file immediately. If you want to save them manually, use commands below.

### Overview
 - /saplanting: show all current properties;
 - /saplanting \<PropertyName\>: show value of target property.

### Configure File IO
 - /saplanting load: load properties from file;
 - /saplanting load <PropertyName>: load target property from file;
 - /saplanting save: save current properties into file.

### Black List  
Black list and properties that is named like "allowXXX" complements each other.  
For instance, if an item is in black list, no matter the corresponding allowXXX is true or false, the item will not be planted.  
And vice versa.
 - /saplanting blackList: show black list inability;
 - /saplanting blackList enable: enable black list;
 - /saplanting blackList disable: disable black list;
 - /saplanting blackList list: show content of black list;
 - /saplanting blackList add \<Item\>: add item to black list;  
 If target item already exists in black list or is not a plant, it will not be added to the black list.
 - /saplanting blackList remove \<Item\>: remove item from black list.

### Change Property
 - /saplanting \<PropertyName\> \<value\>: set value of target property.

## Rules in distribution and usage
### Distribution related
If you want to post a page for this mod on other websites, please contact the author and then do it with permission. Request from NetEase and MCBBS will not be allowed.  
If you want to share this mod personally, please share the link of this project or other authorized pages, instead of distributing files of this mod.  
### Usage related
#### For mod developer
- Expansive development is welcomed.
- You are allowed to replant this mod on other MC versions.  
- Operations above would NOT need to post a link related to this mod.

#### For mod pack authors
- Include this mod for offline package & installer of your mod pack is allowed, but citation of this mod and version is required; if the version is updated with authorized distributive websites, please note your update source(For example, "Version updates with CurseForge").  
- Modifications on this mod via various approaches is allowed, but we WON'T take responsibility if fatal problems occurred.  
- Operations above would NOT need to post a link related to this mod.

#### For players
- Usage on appropriate platform is OK, and feedback will always be listened.
- Feedback from Minecraft-NetEase will never get maintenance.  
- Streaming and videoing is allowed, as well as posting records on various platform.  

## Contact Me
buy me a coffee：https://afdian.net/@muyucloud  
Chat Group from QQ: https://t.bilibili.com/563537522129988306  
CurseForge: https://www.curseforge.com/minecraft/mc-mods/saplanting  
GitHub: https://github.com/MUYUTwilighter/saplanting_fabric  
Gitee: https://gitee.com/muyu-twilighter/saplanting_fabric  
MCMOD: https://www.mcmod.cn/class/5221.html  
bilibili: https://space.bilibili.com/291040380

---
title: "Saplanting(Fabric) Manual"
author: MUYU_Twilighter
date: 2022.8.17
---

[![](http://cf.way2muchnoise.eu/full_saplanting_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/saplanting) [![Discord](https://img.shields.io/discord/966726130105217094)](https://discord.gg/JunKeKCJAY)

# Saplanting(Fabric)

A fabric mod that automatically plants sapling drops in minecraft games.  
Saplings from other mods are supported theoretically.  
Other plants (Actually, all items) are also available, if you configure them.  
Make sure that your Minecraft version is supported by the mod.  
If you use Fabric client, fabric api is required.

## Introduction

If you can not spare enough time and effort to plant saplings in Minecraft or the players on server are too lazy to do
so, this mod will help you!  
When a sapling item drops on dirt(or something else that let saplings grow), it will automatically turn into sapling
block in 2 seconds. The sapling-planting will not destroy the block that already take up the place(except air and
replaceable blocks).  
What's more surprising, other plants can be automatically planted now! But this feature is disabled by default,
use [commands](#Commands) below or [configure the JSON file](#Configurations).

## How to Install

1. Download the Jar File of proper version.
2. Copy or cut the jar file you've just downloaded to your mod path (usually `.../.minecraft/mods` or
   `.../.minecraft/versions/\<VersionName\>/mods`)
3. If you are using fabric, make sure you have installed fabric api and fabric of proper versions

### About Multiplayer

Main logic in this mod is nested on the server side, so only server side is necessary to install this mod.
If only client side is installed, it only works for single player.

## Configurations

Config file is located at `.../.minecraft/config/saplanting.json`  
Config file will only be automatically loaded when a world-save is loaded. If you want the changes in this file loaded
manually, use [commands](#Commands) to do so.

- plantEnable: Enable auto-planting, default: true, expect: boolean
- plantLarge: Try to plant 2x2 trees, default: true, expect: boolean  
  Only for sapling blocks that can be planted in shape of 2x2.
- showTitleOnOpConnected: Show disability for Saplanting on a server when an **operator** connected to this server,
  default: false, expect: boolean
- ignoreShape: Plant no matter the sapling can grow in shape of 1x1, default: false, expect: boolean
- warnTaskQueue: Show warning if task queue was too large, default: true, expect: boolean
- autoBlackList: If errors occurred during handling a specific item, add this item to blacklist, default: true, expect
  boolean
- language: Current language of Saplanting, default: "en_us", expect: String
- plantDelay: How many ticks before the sapling drop will be planted, default: 40, expect: non-negative integers
- avoidDense: The radius of area that will not plant when selected area already got other trees, default: 2, expect
  non-negative integers
- playerAround: Not to plant if player around, default: 2, expect non-negative integers

### Whitelist & Blacklist

From 1.3.0, properties like `allowXXX` are permanently removed. Now you can filter items in whitelist and black list.

Saplanting will only plant if the item matches any entry in whitelist and does not match any entry in blacklist.

You can add item ID or item tag to whitelist or blacklist.
For example, you can add `minecraft:dirt` (Item ID) or `#minecraft:saplings` (Item Tag) to any list.

Besides, you can add `*` to any list to match any item.

## Commands

All the commands require admin-permission to execute.  
All the changes made by commands will not be saved to config file immediately. If you want to save them manually, use
commands below.

### Functional Properties

- /saplanting: show all current properties;
- /saplanting property \<PropertyName\>: show value of target property.
- /saplanting property \<PropertyName\> \<value\>: set value of target property.

### Configure File IO

- /saplanting file load: load properties from file;
- /saplanting file save: save current properties into file.

### Whitelist & Blacklist

You can view [here](#whitelist--blacklist) to understand how to use whitelist and blacklist.

- `/saplanting whitelist|blacklist`: show content of whitelist or blacklist.
- `/saplanting whitelist|blacklist add`: add item in your main hand to whitelist or blacklist.
- `/saplanting whitelist|blacklist add <ItemID|ItemTag|*>`: add item to whitelist or blacklist.
- `/saplanting whitelist|blacklist remove`: remove item in your main hand from whitelist or blacklist.
- `/saplanting whitelist|blacklist remove <ItemID|ItemTag|*>`: remove item from whitelist or blacklist.
- `/saplanting whitelist|blacklist clear`: clear all items in whitelist or blacklist.

### Language

- /saplanting language: show current language.
- /saplanting language switch \<Lang>: switch to target language.

## Rules in distribution and usage

### Distribution related

If you want to post a page for this mod on other websites, please contact the author and then do it with permission.
Request from NetEase and MCBBS will not be allowed.  
If you want to share this mod personally, please share the link of this project or other authorized pages, instead of
distributing files of this mod.

### Usage related

#### For mod developer

- Expansive development is welcomed.
- You are allowed to replant this mod on other MC versions.
- Operations above would NOT need to post a link related to this mod.

#### For mod pack authors

- Include this mod for offline package & installer of your mod pack is allowed, but citation of this mod and version is
  required; if the version is updated with authorized distributive websites, please note your update source(For
  example, "Version updates with CurseForge").
- Modifications on this mod via various approaches is allowed, but we WON'T take responsibility if fatal problems
  occurred.
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
MCMOD: https://www.mcmod.cn/class/5221.html  
bilibili: https://space.bilibili.com/291040380
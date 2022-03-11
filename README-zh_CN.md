---
title: "落苗生根(Fabric)使用手册"
author: 暮宇_Twilighter
date: 2022.2.27
output: pdf_document
---

# 落苗生根(Fabric)  
能够自动种植树苗掉落物的Fabric模组；  
其他模组加入的树苗理论上也能够种植；  
其他植物方块也可以被自动种植，但需要手动开启此功能。  
适用版本1.18.1，需要安装Fabric Loader与Fabric API。

## 简介  
如果你还在为砍树后一地的掉落物而发愁，这个模组能够帮到你！  
当一个掉落物掉在地上（如果这个方块允许这个物品被放置），它就会自动在两秒后种植在这个方块上。别担心，自动种植不会摧毁这个方块上方原有的方块，除非这个方块本身是可替换的（比如草、蕨等）。  
另外，最新版本支持自动种植所有植物方块！不过这个功能默认被关闭，你需要手动开启；详见下方指令与设置文件。

## 如何安装  
1. 下载正确版本的jar文件；
2. 将下载好的文件放入模组目录（通常在 ".../.minecraft/mods" 或 ".../.minecraft/versions/\<VersionName\>/mods"）；
3. 确保你的Fabric Loader和Fabric API安装无误后启动游戏。

## 设置文件  
设置文件位于 ".../.minecraft/config/saplanting.json"  
设置文件只会在一个存档被加载的时候才会读取并应用其中的设置，如果需要立刻应用其中内容，请见下方指令。

 - plantEnable：启用自动种植，默认：true，期望：boolean  
 - plantLarge：尝试种植2x2的树，默认：true，期望：boolean  
只会对可以以2x2形式长大的树生效
 - blackListEnable：启用黑名单，默认：true，期望：boolean
 - allowSapling：启用树苗的自动种植，默认：true，期望：boolean
 - allowCrop：启用农作物的自动种植，默认：true，期望：boolean
 - allowMushroom：启用蘑菇的自动种植，默认：false，期望：boolean
 - allowFungus：启用地狱菇的自动种植，默认：false，期望：boolean
 - allowFlower：启用花的自动种植，默认：false，期望：boolean
 - allowOther：启用其他植物的自动种植，默认：false，期望：boolean
 - plantDelay：自动种植的延迟（单位tick），默认：40，期望：nonnegative integers
 - avoidDense：其他树的检测半径（如果有其他树则不种植），默认：2，expect nonnegative integers
 - playerAround：玩家的检测半径（如果有玩家则不种植），默认：2，expect nonnegative integers
 - blackList：黑名单列表，成员例如 "minecraft:oak_sapling"

## 指令  
所有的指令都需要管理员权限，指令的回馈默认只能返回简体中文。  
由指令作出的所有更改只会在存档关闭时被写入文件，如果想要立刻保存，请见下方指令。

### 查询设置  
 - /saplanting：显示当前所有设置；
 - /saplanting \<设置项名称\>：显示指定设置项的值。

### 与设置文件交互  
 - /saplanting load：从设置文件加载所有设置；
 - /saplanting load <设置项名称>：从设置文件加载指定设置；
 - /saplanting save：将当前设置写入设置文件。

### 黑名单  
黑名单和形如"allowXXX"的设置项功能互补。  
例如，如果一个物品在黑名单中，不论对应的"allowXXX"是否为真，这个物品都不会被种植。  
反之亦然。
 - /saplanting blackList：显示黑名单是否被开启；
 - /saplanting blackList enable：开启黑名单；
 - /saplanting blackList disable：禁用黑名单；
 - /saplanting blackList list：显示黑名单列表；
 - /saplanting blackList add \<物品ID\>：将指定物品加入黑名单；  
 如果指定物品已存在于黑名单或这个物品不是植物方块，则不会加入到黑名单中。
 - /saplanting blackList remove \<物品ID\>：将指定物品从黑名单中移除。

### 更改其他设置  
 - /saplanting \<设置项名称\> \<值\>：将指定设置项设置为指定值；

## 与我联系  
爱发电：https://afdian.net/@muyucloud  
QQ交流群：https://t.bilibili.com/563537522129988306  
CurseForge：https://www.curseforge.com/minecraft/mc-mods/saplanting  
GitHub：https://github.com/MUYUTwilighter/saplanting_fabric  
Gitee：https://gitee.com/muyu-twilighter/saplanting_fabric  
MC模组百科：https://www.mcmod.cn/class/5221.html  
哔哩哔哩：https://space.bilibili.com/291040380
[![](http://cf.way2muchnoise.eu/full_saplanting_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/saplanting) [![Discord](https://img.shields.io/discord/966726130105217094)](https://discord.gg/JunKeKCJAY)

# 落苗生根

能够自动种植树苗掉落物的模组；  
其他模组加入的树苗理论上也能够种植；  
其他植物方块也可以被自动种植（实际上所有物品都可以被“种植”），但需要手动开启此功能。  
请注意模组适用的游戏版本，Fabric 端需要安装 Fabric Loader 与 Fabric API。

## 简介

如果你还在为砍树后一地的掉落物而发愁，这个模组能够帮到你！  
当一个掉落物掉在地上（如果这个方块允许这个物品被放置），它就会自动在两秒后种植在这个方块上。别担心，自动种植不会摧毁这个方块上方原有的方块，除非这个方块本身是可替换的（比如草、蕨等）。  
另外，最新版本支持自动种植所有植物方块！不过这个功能默认被关闭，你需要手动开启；详见下方 [指令](#指令) 与 [设置文件](#设置文件)。

## 如何安装

1. 下载正确版本的jar文件；
2. 将下载好的文件放入模组目录（通常在 `.../.minecraft/mods" 或 ".../.minecraft/versions/<VersionName>/mods`）；
3. 如果你使用的是 Fabric 客户端，你需要确保你的 Fabric Loader 和 Fabric API 安装无误后启动游戏。

### 关于多人游戏

该模组的主要逻辑位于服务端，因此只需要主机安装即可对所有玩家生效。
如果仅在客机安装，则仅对单人游戏生效。

## 设置文件

设置文件位于 `.../.minecraft/config/saplanting.json` 或 `.../.minecraft/versions/<VersionName>/config/saplanting.json`。  
设置文件只会在一个存档被加载的时候才会读取并应用其中的设置，如果需要立刻应用其中内容，请见下方[指令](#指令)。

- plantEnable：启用自动种植，默认：true，期望：boolean
- plantLarge：尝试种植 2x2 的树，默认：true，期望：boolean  
  只会对**可能**以 2x2 形式长大的树生效
- showTitleOnOpConnected: 在**管理员**加入服务器时，显示当前服务器是否启用 Saplanting，默认：false，期望：boolean
- ignoreShape: 不论树苗是否能在 1x1 的形状下生长，依然种植，默认：false，期望：boolean
- warnTaskQueue: 如果物品处理队列过大，在日志中发出警报，默认：true，期望：boolean
- autoBlackList: 如果处理某物品时发生错误，自动将其加入黑名单，默认：true，期望：boolean
- language: 使用的语言，默认："en_us"，期望：String
- plantDelay：自动种植的延迟（单位tick），默认：40，期望：non-negative integers
- avoidDense：其他树的检测半径（如果有其他树则不种植），默认：2，期望 non-negative integers
- playerAround：玩家的检测半径（如果有玩家则不种植），默认：2，期望 non-negative integers

### 白名单与黑名单

自 1.3.0 版本以后，原有的形如 `allowXXX` 的配置项被移除，现在使用 `blackList` 与 `whiteList` 来过滤物品。

Saplanting 只会种植在白名单中匹配，且不在黑名单中匹配的物品。

你可以添加物品 ID 或 物品标签到白名单或黑名单中;
例如，你可以添加 `minecraft:dirt`（物品 ID）或 `#minecraft:saplings`（物品标签）到任意列表中。

同样，你可以添加 `*` 到任意列表中来匹配任何物品。

## 指令

所有的指令都需要管理员权限。  
由指令作出的所有更改只会在存档关闭时被写入文件，如果想要立刻保存，请见下方指令。

### 功能性设置

- `/saplanting`：显示当前所有设置；
- `/saplanting property <设置项名称> <值>`：将指定设置项设置为指定值；
- `/saplanting property <设置项名称>`：显示指定设置项的值。

### 文件操作

- `/saplanting file load`：从设置文件加载所有设置；
- `/saplanting file save`：将当前设置写入设置文件。

### 白名单与黑名单

你可以在[此处](#白名单与黑名单)了解如何使用白名单与黑名单。

- `/saplanting whitelist|blacklist`：显示白名单或黑名单的内容；
- `/saplanting whitelist|blacklist add`：将主手物品添加到白名单或黑名单中；
- `/saplanting whitelist|blacklist add <物品ID|物品标签|*>`：将指定物品添加到白名单或黑名单中；
- `/saplanting whitelist|blacklist remove`：将主手物品从白名单或黑名单中移除；
- `/saplanting whitelist|blacklist remove <物品ID|物品标签|*>`：将指定物品从白名单或黑名单中移除；
- `/saplanting whitelist|blacklist clear`：清空白名单或黑名单。

### 语言

- `/saplanting language`：查询当前使用的语言；
- `/saplanting language <值>`：切换到指定语言；

## 共享与使用准则

### 分享相关

如果需要转载本模组到其他网站，请联系作者并获得准许后再进行相关操作。  
网易我的世界 和 MCBBS 的转载与分享请求不会被允许。  
如果你想分享此mod，请分享本项目或其他授权转载页的链接，而不是复制本文内容或直接散布模组文件。

### 使用相关

#### 如果你是模组开发者：

- 你可以基于源代码进行适当的拓展开发。
- 你可以将其移植到更旧的版本。
- 以上需求无须链接到此模组相关页面。

#### 如果你是整合包作者：

- 你可以在整合包中使用该模组并提供离线安装包，但请在发布时注明使用本模组且标注版本；如果是实时跟随模组发布网站并更新，请注明更新来源（如：“版本跟随CurseForge更新”）。
- 允许使用（包括但不仅限于）模组、插件对本模组功能进行魔改，但作者无法保证是否会导致任何异常。
- 以上需求无须链接到此模组相关页面。

#### 如果你是玩家：

- 你可以在合适的平台使用本模组，并实时向开发者提供使用反馈。
- 来自 网易我的世界 的使用反馈不会被采纳。
- 你可以在直播或视频录制的过程中使用本模组，并将相关录像投放到任意平台。

## 与我联系

爱发电：https://afdian.net/@muyucloud  
QQ交流群：https://t.bilibili.com/563537522129988306  
CurseForge：https://www.curseforge.com/minecraft/mc-mods/saplanting  
GitHub：https://github.com/MUYUTwilighter/saplanting_fabric  
MC模组百科：https://www.mcmod.cn/class/5221.html  
哔哩哔哩：https://space.bilibili.com/291040380
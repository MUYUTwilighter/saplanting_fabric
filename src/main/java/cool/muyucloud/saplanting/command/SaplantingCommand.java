package cool.muyucloud.saplanting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cool.muyucloud.saplanting.Config;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

public class SaplantingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // /saplanting
        final LiteralArgumentBuilder<ServerCommandSource> root = (CommandManager.literal("saplanting")
                .requires(source -> source.hasPermissionLevel(2)));

        // /saplanting <Integer>/NULL
        root.executes(context -> showAll(context.getSource(), 1));
        root.then(CommandManager.argument("page", IntegerArgumentType.integer()).executes(
                context -> showAll(context.getSource(), IntegerArgumentType.getInteger(context, "page"))
        ));

        // /saplanting plantEnable
        root.then(CommandManager.literal("plantEnable").executes(context -> getPlantEnable(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setPlantEnable(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // /saplanting plantEnable
        root.then(CommandManager.literal("plantLarge").executes(context -> getPlantLarge(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setPlantLarge(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting allowSapling
        root.then(CommandManager.literal("allowSapling").executes(context -> getAllowSapling(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setAllowSapling(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting allowCrop
        root.then(CommandManager.literal("allowCrop").executes(context -> getAllowCrop(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setAllowCrop(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting allowMushroom
        root.then(CommandManager.literal("allowMushroom").executes(context -> getAllowMushroom(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setAllowMushroom(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting allowFungus
        root.then(CommandManager.literal("allowFungus").executes(context -> getAllowFungus(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setAllowFungus(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting allowFlower
        root.then(CommandManager.literal("allowFlower").executes(context -> getAllowFlower(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setAllowFlower(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting allowOther
        root.then(CommandManager.literal("allowOther").executes(context -> getAllowOther(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setAllowOther(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting showTitleOnPlayerConnected
        root.then(CommandManager.literal("showTitleOnPlayerConnected").executes(context -> getShowTitleOnPlayerConnected(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setShowTitleOnPlayerConnected(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // saplanting ignoreShape
        root.then(CommandManager.literal("ignoreShape").executes(context -> getIgnoreShape(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setIgnoreShape(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // /saplanting plantDelay
        root.then(CommandManager.literal("plantDelay").executes(context -> getPlantDelay(context.getSource()))
                .then(CommandManager.argument("value", IntegerArgumentType.integer())
                        .executes(context -> setPlantDelay(context.getSource(), IntegerArgumentType.getInteger(context, "value")))));

        // /saplanting PlayerAround
        root.then(CommandManager.literal("playerAround").executes(context -> getPlayerAround(context.getSource()))
                .then(CommandManager.argument("value", IntegerArgumentType.integer())
                        .executes(context -> setPlayerAround(context.getSource(), IntegerArgumentType.getInteger(context, "value")))));

        // /saplanting AvoidDense
        root.then(CommandManager.literal("avoidDense").executes(context -> getAvoidDense(context.getSource()))
                .then(CommandManager.argument("value", IntegerArgumentType.integer())
                        .executes(context -> setAvoidDense(context.getSource(), IntegerArgumentType.getInteger(context, "value")))));

        // /saplanting blackList
        root.then(CommandManager.literal("blackList").executes(context -> getBlackListEnable(context.getSource()))
                .then(CommandManager.literal("enable").executes(context -> setBlackListEnable(context.getSource(), true)))
                .then(CommandManager.literal("disable").executes(context -> setBlackListEnable(context.getSource(), false)))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("itemName", ItemStackArgumentType.itemStack() )
                                .executes(context -> addBlackList(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "itemName").getItem()))))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack())
                                .executes(context -> removeBlackList(context.getSource()
                                        , ItemStackArgumentType.getItemStackArgument(context, "item").getItem()))))
                .then(CommandManager.literal("list")
                        .executes(context -> showBlackList(context.getSource())))
                .then(CommandManager.literal("clear")
                        .executes(context -> clearBlackList(context.getSource()))));

        // /saplanting load <Property>
        LiteralArgumentBuilder<ServerCommandSource> load = CommandManager.literal("load").executes(context -> loadProperty(context.getSource()));
        load.then(CommandManager.literal("plantEnable")
                .executes(context -> loadProperty(context.getSource(), "plantEnable")));
        load.then(CommandManager.literal("plantLarge")
                .executes(context -> loadProperty(context.getSource(), "plantLarge")));
        load.then(CommandManager.literal("blackListEnable")
                .executes(context -> loadProperty(context.getSource(), "blackListEnable")));
        load.then(CommandManager.literal("allowSapling")
                .executes(context -> loadProperty(context.getSource(), "allowSapling")));
        load.then(CommandManager.literal("allowCrop")
                .executes(context -> loadProperty(context.getSource(), "allowCrop")));
        load.then(CommandManager.literal("allowMushroom")
                .executes(context -> loadProperty(context.getSource(), "allowMushroom")));
        load.then(CommandManager.literal("allowFungus")
                .executes(context -> loadProperty(context.getSource(), "allowFungus")));
        load.then(CommandManager.literal("allowFlower")
                .executes(context -> loadProperty(context.getSource(), "allowFlower")));
        load.then(CommandManager.literal("allowOther")
                .executes(context -> loadProperty(context.getSource(), "allowOther")));
        load.then(CommandManager.literal("showTitleOnPlayerConnected")
                .executes(context -> loadProperty(context.getSource(), "showTitleOnPlayerConnected")));
        load.then(CommandManager.literal("ignoreShape")
                .executes(context -> loadProperty(context.getSource(), "ignoreShape")));
        load.then(CommandManager.literal("plantDelay")
                .executes(context -> loadProperty(context.getSource(), "plantDelay")));
        load.then(CommandManager.literal("avoidDense")
                .executes(context -> loadProperty(context.getSource(), "avoidDense")));
        load.then(CommandManager.literal("playerAround")
                .executes(context -> loadProperty(context.getSource(), "playerAround")));

        // /saplanting load
        root.then(load);

        // /saplanting save
        root.then(CommandManager.literal("save").executes(context -> saveProperty(context.getSource())));

        // register
        dispatcher.register(root);
    }

    public static int showBlackList(ServerCommandSource source) {
        if (Config.blackListLength() == 0) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.display.empty"), false);
        } else {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.display"), false);
            source.sendFeedback(new LiteralText(Config.stringBlackList()), false);
        }
        return Config.blackListLength();
    }

    public static int addBlackList(ServerCommandSource source, Item item) {
        if (Config.inBlackList(item)) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.add.inBlackList")
                    .setStyle(Style.EMPTY.withColor(TextColor.parse("red"))), false);
            return 0;
        } else if (!Config.isPlantableItem(item)) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.add.notPlantable")
                    .setStyle(Style.EMPTY.withColor(TextColor.parse("red"))), false);
            return 0;
        } else {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.add.success"), false);
            Config.addBlackListItem(item);
            return 1;
        }
    }

    public static int removeBlackList(ServerCommandSource source, Item item) {
        if (Config.inBlackList(item)) {
            Config.rmBlackListItem(item);
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.remove.success"), false);
            return 1;
        } else {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.remove.notInBlackList")
                    .setStyle(Style.EMPTY.withColor(TextColor.parse("red"))), false);
            return 0;
        }
    }

    public static int clearBlackList(ServerCommandSource source) {
        int output = Config.blackListLength();
        Config.clearBlackList();
        source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.clear"), false);
        return output;
    }

    public static int showAll(ServerCommandSource target, int page) {
        target.sendFeedback(new TranslatableText("saplanting.commands.saplanting.showAll")
                        .setStyle(Style.EMPTY.withColor(TextColor.parse("gold"))), false);

        if (page < 0 || page > 3) {
            page = 1;
        }

        switch (page) {
            case 1 -> {
                target.sendFeedback(new LiteralText(" - plantEnable:   " + Config.getPlantEnable()), false);
                target.sendFeedback(new LiteralText(" - plantLarge:    " + Config.getPlantLarge()), false);
                target.sendFeedback(new LiteralText(" - blackList:     " + Config.getBlackListEnable()), false);
                target.sendFeedback(new LiteralText(" - allowSapling:  " + Config.getAllowSapling()), false);
                target.sendFeedback(new LiteralText(" - allowCrop:     " + Config.getAllowCrop()), false);
                target.sendFeedback(new LiteralText(" - allowMushroom: " + Config.getAllowMushroom()), false);
                target.sendFeedback(new LiteralText(" - allowFungus:   " + Config.getAllowFungus()), false);
                target.sendFeedback(new LiteralText(" - allowFlower:   " + Config.getAllowFlower()), false);
                target.sendFeedback(new TranslatableText("saplanting.commands.saplanting.showAll.nextPage").setStyle(Style.EMPTY
                        .withColor(TextColor.parse("green"))
                        .withUnderline(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/saplanting " + (page + 1)))), false);
            }
            case 2 -> {
                target.sendFeedback(new LiteralText(" - allowOther:    " + Config.getAllowOther()), false);
                target.sendFeedback(new LiteralText(" - showTitle... : " + Config.getAllowOther()).setStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("showTitleOnPlayerConnected")))),
                        false);
                target.sendFeedback(new LiteralText(" - ignoreShape:   " + Config.getIgnoreShape()), false);
                target.sendFeedback(new LiteralText(" - plantDelay:    " + Config.getPlantDelay()), false);
                target.sendFeedback(new LiteralText(" - avoidDense:    " + Config.getAvoidDense()), false);
                target.sendFeedback(new LiteralText(" - playerAround:  " + Config.getPlayerAround()), false);
                target.sendFeedback(new TranslatableText("saplanting.commands.saplanting.showAll.formerPage").setStyle(Style.EMPTY
                        .withColor(TextColor.parse("green"))
                        .withUnderline(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/saplanting " + (page - 1)))), false);
            }
        }

        return 1;
    }

    public static int setPlantEnable(ServerCommandSource source, boolean value) {
        Config.setPlantEnable(value);
        source.sendFeedback(new LiteralText("plantEnable")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(new LiteralText(Boolean.toString(value))), false);
        return value ? 1 : 0;
    }

    public static int setPlantLarge(ServerCommandSource source, boolean value) {
        Config.setPlantLarge(value);
        source.sendFeedback(new LiteralText("plantLarge")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setBlackListEnable(ServerCommandSource source, boolean value) {
        Config.setBlackListEnable(value);
        if (value) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.enable"), false);
            return 1;
        } else {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.disable"), false);
            return 0;
        }
    }

    public static int setAllowSapling(ServerCommandSource source, boolean value) {
        Config.setAllowSapling(value);
        source.sendFeedback(new LiteralText("allowSapling")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setAllowCrop(ServerCommandSource source, boolean value) {
        Config.setAllowCrop(value);
        source.sendFeedback(new LiteralText("allowCrop")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setAllowMushroom(ServerCommandSource source, boolean value) {
        Config.setAllowMushroom(value);
        source.sendFeedback(new LiteralText("allowMushroom")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setAllowFungus(ServerCommandSource source, boolean value) {
        Config.setAllowFungus(value);
        source.sendFeedback(new LiteralText("allowFungus")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setAllowFlower(ServerCommandSource source, boolean value) {
        Config.setAllowFlower(value);
        source.sendFeedback(new LiteralText("allowFlower")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setAllowOther(ServerCommandSource source, boolean value) {
        Config.setAllowOther(value);
        source.sendFeedback(new LiteralText("allowOther")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setShowTitleOnPlayerConnected(ServerCommandSource source, boolean value) {
        Config.setShowTitleOnPlayerConnected(value);
        source.sendFeedback(new LiteralText("showTitleOnPlayerConnected")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setIgnoreShape(ServerCommandSource source, boolean value) {
        Config.setIgnoreShape(value);
        source.sendFeedback(new LiteralText("ignoreShape")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Boolean.toString(value)), false);
        return value ? 1 : 0;
    }

    public static int setPlantDelay(ServerCommandSource source, int value) {
        Config.setPlantDelay(value);
        source.sendFeedback(new LiteralText("plantDelay")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Integer.toString(value)), false);
        return value;
    }

    public static int setAvoidDense(ServerCommandSource source, int value) {
        Config.setAvoidDense(value);
        source.sendFeedback(new LiteralText("avoidDense")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Integer.toString(value)), false);
        return value;
    }

    public static int setPlayerAround(ServerCommandSource source, int value) {
        Config.setPlayerAround(value);
        source.sendFeedback(new LiteralText("playerAround")
                .append(new TranslatableText("saplanting.commands.saplanting.property.set"))
                .append(Integer.toString(value)), false);
        return value;
    }

    public static int getPlantEnable(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("plantEnable")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getPlantEnable())), false);
        return Config.getPlantEnable() ? 1 : 0;
    }

    public static int getPlantLarge(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("plantLarge")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getPlantLarge())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getBlackListEnable(ServerCommandSource source) {
        if (Config.getBlackListEnable()) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.enable"), false);
        } else {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.blackList.disable"), false);
        }
        return Config.getBlackListEnable() ? 1 : 0;
    }

    public static int getAllowSapling(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("allowSapling")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getAllowSapling())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getAllowCrop(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("allowCrop")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getAllowCrop())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getAllowMushroom(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("allowMushroom")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getAllowMushroom())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getAllowFungus(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("allowFungus")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getAllowFungus())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getAllowFlower(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("allowFlower")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getAllowFlower())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getAllowOther(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("allowOther")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getAllowOther())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getShowTitleOnPlayerConnected(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("showTitleOnPlayerConnected")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getShowTitleOnPlayerConnected())), false);
        return Config.getPlantLarge() ? 1 : 0;
    }

    public static int getIgnoreShape(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("ignoreShape")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Boolean.toString(Config.getIgnoreShape())), false);
        return Config.getIgnoreShape() ? 1 : 0;
    }

    public static int getPlantDelay(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("plantDelay")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Integer.toString(Config.getPlantDelay())), false);
        return Config.getPlantDelay();
    }

    public static int getAvoidDense(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("avoidDense")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Integer.toString(Config.getAvoidDense())), false);
        return Config.getAvoidDense();
    }

    public static int getPlayerAround(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("playerAround")
                .append(new TranslatableText("saplanting.commands.saplanting.property.show"))
                .append(Integer.toString(Config.getPlayerAround())), false);
        return Config.getPlayerAround();
    }

    public static int loadProperty(ServerCommandSource source) {
        try {
            Config.load();
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.load.success.head")
                    .append(new TranslatableText("saplanting.commands.saplanting.load.success.suggestCommand")
                            .setStyle(Style.EMPTY
                                    .withUnderline(true).withColor(TextColor.parse("green"))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/saplanting"))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new TranslatableText("saplanting.commands.saplanting.load.success.suggestEvent"))))
                    ).append(new TranslatableText("saplanting.commands.saplanting.load.success.tail")
                    ), false);
        } catch (Exception e) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.load.fail")
                    .setStyle(Style.EMPTY.withColor(TextColor.parse("red"))), false);
            return 0;
        }
        return 1;
    }

    public static int loadProperty(ServerCommandSource source, String name) {
        if (Config.load(name)) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.load.property.success")
                    .append(new LiteralText(name).setStyle(Style.EMPTY
                            .withUnderline(true).withColor(TextColor.parse("green"))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/saplanting " + name))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new TranslatableText("saplanting.commands.saplanting.load.property.success.suggestEvent"))))
                    ), false);
            return 1;
        } else {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.load.property.fail")
                    .setStyle(Style.EMPTY.withColor(TextColor.parse("red"))), false);
            return 0;
        }
    }

    public static int saveProperty(ServerCommandSource source) {
        try {
            Config.saveConfig();
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.save.success")
                    .append(new TranslatableText(Config.stringPath()).setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Config.stringPath()))
                            .withUnderline(true)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                                    , new TranslatableText("saplanting.commands.saplanting.save.path")))
                    )), false);
        } catch (Exception e) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.save.fail")
                    .setStyle(Style.EMPTY.withColor(TextColor.parse("red"))), false);
            return 0;
        }

        return 1;
    }
}

package cool.muyucloud.saplanting.util;

import com.google.gson.JsonArray;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

import java.util.List;

public class Command {
    private static final Config CONFIG = Saplanting.getConfig();
    private static final Config DEFAULT_CONFIG = Saplanting.getDefaultConfig();
    private static final Style CLICKABLE_COMMAND = Style.EMPTY
        .withColor(TextColor.parse("green"))
        .withUnderline(true);
    private static final Style CLICKABLE_FILE = Style.EMPTY
        .withUnderline(true);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, boolean dedicated) {
        /* /saplanting <PAGE> */
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("saplanting");
        root.requires(source -> source.hasPermissionLevel(2));
        root.executes(context -> displayAll(1, context.getSource()));
        root.then(CommandManager.argument("page", IntegerArgumentType.integer())
            .executes(context -> displayAll(IntegerArgumentType.getInteger(context, "page"), context.getSource())));

        /* /saplanting property <KEY> <VALUE> */
        LiteralArgumentBuilder<ServerCommandSource> property = CommandManager.literal("property");
        for (String key : CONFIG.getKeySet()) {
            LiteralArgumentBuilder<ServerCommandSource> propertyE = CommandManager.literal(key);
            propertyE.executes((context) -> getProperty(key, context.getSource()));
            if (CONFIG.getType(key) == Boolean.class) {
                propertyE.then(CommandManager.argument("value", BoolArgumentType.bool())
                    .executes((context -> setProperty(key, BoolArgumentType.getBool(context, "value"), context.getSource()))));
                propertyE.then(CommandManager.literal("default")
                    .executes(context -> setProperty(key, DEFAULT_CONFIG.getAsBoolean(key), context.getSource())));
            } else if (CONFIG.getType(key) == Integer.class) {
                propertyE.then(CommandManager.argument("value", IntegerArgumentType.integer())
                    .executes((context -> setProperty(key, IntegerArgumentType.getInteger(context, "value"), context.getSource()))));
                propertyE.then(CommandManager.literal("default")
                    .executes(context -> setProperty(key, DEFAULT_CONFIG.getAsInt(key), context.getSource())));
            }
            property.then(propertyE);
        }
        root.then(property);

        /* /saplanting language <OPERATION> [ARG] */
        // /saplanting language
        LiteralArgumentBuilder<ServerCommandSource> language = CommandManager.literal("language");
        language.executes(context -> queryLanguage(context.getSource()));
        // /saplanting language switch <LANG>
        LiteralArgumentBuilder<ServerCommandSource> change = CommandManager.literal("switch");
        for (String name : CONFIG.getValidLangs()) {
            change.then(CommandManager.literal(name).executes(context -> updateLanguage(name, context.getSource())));
        }
        // /saplanting language switch default
        change.then(CommandManager.literal("default")
            .executes(context -> updateLanguage("en_us", context.getSource())));
        language.then(change);
        root.then(language);

        /* /saplanting file <OPERATION> */
        LiteralArgumentBuilder<ServerCommandSource> file = CommandManager.literal("file");
        file.then(CommandManager.literal("load").executes(context -> load(context.getSource(), dedicated)));
        file.then(CommandManager.literal("save").executes(context -> save(context.getSource(), dedicated)));
        root.then(file);

        /* /saplanting blackList <OPERATION> [ARG] */
        LiteralArgumentBuilder<ServerCommandSource> blackList = CommandManager.literal("blackList");
        blackList.executes(context -> displayBlackList(1, context.getSource()));
        // saplanting blackList <page>
        blackList.then(CommandManager.argument("page", IntegerArgumentType.integer())
            .executes(context ->
                displayBlackList(IntegerArgumentType.getInteger(context, "page"), context.getSource())));
        // saplanting blackList add <item>
        blackList.then(CommandManager.literal("add")
            .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(access))
                .executes(context ->
                    addToBlackList(ItemStackArgumentType.getItemStackArgument(context, "item").getItem(),
                        context.getSource()))));
        // saplanting blackList remove <item>
        blackList.then(CommandManager.literal("remove")
            .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(access))
                .executes(context ->
                    removeFromBlackList(ItemStackArgumentType.getItemStackArgument(context, "item").getItem(),
                        context.getSource()))));
        // saplanting blackList clear
        blackList.then(CommandManager.literal("clear")
            .executes(context -> clearBlackList(context.getSource())));
        root.then(blackList);

        dispatcher.register(root);
    }

    private static int setProperty(String key, boolean value, ServerCommandSource source) {
        if (CONFIG.set(key, value)) {
            MutableText text = Text.literal(Translation.translate("command.saplanting.property.set.success")
                .formatted(key, Boolean.toString(value)));
            MutableText hover = Text.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
            text.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
            source.sendFeedback(text, false);
        } else {
            MutableText text = Text.literal(Translation.translate("command.saplanting.property.set.already")
                .formatted(key, Boolean.toString(value)));
            source.sendError(text);
        }
        return value ? 1 : 0;
    }

    private static int setProperty(String key, int value, ServerCommandSource source) {
        if (CONFIG.set(key, value)) {
            MutableText text = Text.literal(Translation.translate("command.saplanting.property.set.success")
                .formatted(key, Integer.toString(value)));
            MutableText hover = Text.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
            text.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
            source.sendFeedback(text, false);
        } else {
            MutableText text = Text.literal(Translation.translate("command.saplanting.property.set.already")
                .formatted(key, Integer.toString(value)));
            source.sendError(text);
        }
        return value;
    }

    private static int getProperty(String key, ServerCommandSource source) {
        MutableText hover = Text.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
        MutableText text = Text.literal(Translation.translate("command.saplanting.property.get")
            .formatted(key, CONFIG.getAsString(key)));
        text.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
        source.sendFeedback(text, false);
        return 1;
    }

    private static int updateLanguage(String name, ServerCommandSource source) {
        if (!CONFIG.set("language", name)) {
            MutableText text = Text.literal(Translation.translate("command.saplanting.language.already").formatted(name));
            source.sendError(text);
            return 0;
        }
        Translation.updateLanguage(name);
        MutableText text = Text.literal(Translation.translate("command.saplanting.language.success").formatted(name));
        source.sendFeedback(text, false);
        return 1;
    }

    private static int queryLanguage(ServerCommandSource source) {
        MutableText text = Text.literal(Translation.translate("command.saplanting.language.query").formatted(CONFIG.getAsString("language")));
        MutableText change = Text.literal(Translation.translate("command.saplanting.language.switch"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/saplanting language switch ")));
        source.sendFeedback(text.append(change), false);
        return 1;
    }

    private static int load(ServerCommandSource source, boolean dedicated) {
        MutableText text;
        MutableText hover = Text.literal(Translation.translate("command.saplanting.file.open"));
        MutableText file = Text.literal(CONFIG.stringConfigPath())
            .setStyle(CLICKABLE_FILE
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, CONFIG.stringConfigPath()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
        MutableText query = Text.literal(Translation.translate("command.saplanting.file.load.query"))
            .setStyle(CLICKABLE_COMMAND.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/saplanting")));
        if (CONFIG.load()) {
            text = Text.literal(Translation.translate("command.saplanting.file.load.success"));
            if (!dedicated) {
                text.append(file);
            }
            text.append(" ").append(query);
            source.sendFeedback(text, false);
            return 1;
        } else {
            text = Text.literal(Translation.translate("command.saplanting.file.load.fail"));
            if (!dedicated) {
                text.append(file);
            }
            source.sendError(text);
            return 0;
        }
    }

    private static int save(ServerCommandSource source, boolean dedicated) {
        MutableText text;
        MutableText hover = Text.literal(Translation.translate("commands.saplanting.file.open"));
        MutableText file = Text.literal(CONFIG.stringConfigPath())
            .setStyle(CLICKABLE_FILE
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, CONFIG.stringConfigPath()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
        if (CONFIG.save()) {
            text = Text.literal(Translation.translate("command.saplanting.file.save.success"));
            if (!dedicated) {
                text.append(file);
            }
            source.sendFeedback(text, false);
            return 1;
        } else {
            text = Text.literal(Translation.translate("command.saplanting.file.save.fail"));
            if (!dedicated) {
                text.append(file);
            }
            source.sendError(text);
            return 0;
        }
    }

    private static int displayAll(int page, ServerCommandSource source) {
        List<String> arr = CONFIG.getKeySet().stream().toList();
        if ((page - 1) * 8 > arr.size() || page < 1) {
            MutableText pageError = Text.literal(Translation.translate("command.saplanting.page404"));
            source.sendError(pageError);
            return 0;
        }

        /* ======TITLE====== */
        MutableText title = Text.literal(Translation.translate("command.saplanting.title")).setStyle(Style.EMPTY
            .withColor(TextColor.parse("gold")));
        source.sendFeedback(title, false);
        /* - <RESET> KEY : VALUE */
        for (int i = (page - 1) * 8; i < page * 8 && i < arr.size(); ++i) {
            String key = arr.get(i);
            MutableText head = Text.literal("- ");
            MutableText reset = Text.literal(Translation.translate("command.saplanting.reset"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/saplanting property %s default".formatted(key)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal(Translation.translate("command.saplanting.reset.hover")
                            .formatted(DEFAULT_CONFIG.getAsString(key))))));
            MutableText hover = Text.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
            MutableText property = Text.literal(key).setStyle(CLICKABLE_COMMAND
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/saplanting property %s ".formatted(key))));
            MutableText value = Text.literal(": " + CONFIG.getAsString(key));
            head.append(reset).append(" ").append(property).append(value);

            source.sendFeedback(head, false);
        }

        /* [FORMER] PAGE [NEXT] */
        MutableText next = Text.literal(Translation.translate("command.saplanting.next"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/saplanting " + (page + 1))));
        MutableText former = Text.literal(Translation.translate("command.saplanting.former"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/saplanting " + (page - 1))));
        MutableText foot;
        if (arr.size() <= 8) {
            foot = Text.literal(" 1 ");
        } else if (page == 1) {
            foot = Text.literal(" 1 >> ").append(next);
        } else if ((page * 8) >= arr.size()) {
            foot = Text.literal(" ").append(former).append(" << %d ".formatted(page));
        } else {
            foot = Text.literal(" ").append(former).append(" << %d >> ".formatted(page)).append(next);
        }
        source.sendFeedback(foot, false);

        return page;
    }

    private static int addToBlackList(Item item, ServerCommandSource source) {
        String id = Registries.ITEM.getId(item).toString();

        if (!Saplanting.isPlantItem(item)) {
            MutableText text = Text.literal(Translation.translate("command.saplanting.blackList.add.notPlant")
                .formatted(id));
            source.sendError(text);
            return 0;
        }

        if (CONFIG.addToBlackList(item)) {
            MutableText text = Text.literal(Translation.translate("command.saplanting.blackList.add.success")
                .formatted(id));
            MutableText undo = Text.literal(Translation.translate("command.saplanting.blackList.add.undo"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/saplanting blackList remove %s".formatted(id))));
            source.sendFeedback(text.append(" ").append(undo), false);
            return 1;
        }

        MutableText text = Text.literal(Translation.translate("command.saplanting.blackList.add.inBlackList")
            .formatted(id));
        source.sendError(text);
        return 0;
    }

    private static int removeFromBlackList(Item item, ServerCommandSource source) {
        String id = Registries.ITEM.getId(item).toString();
        if (CONFIG.removeFromBlackList(item)) {
            MutableText text = Text.literal(Translation.translate("command.saplanting.blackList.remove.success")
                .formatted(id));
            MutableText undo = Text.literal(Translation.translate("command.saplanting.blackList.remove.undo"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/saplanting blackList add %s".formatted(id))));
            source.sendFeedback(text.append(" ").append(undo), false);
            return 1;
        }

        MutableText text = Text.literal(Translation.translate("command.saplanting.blackList.remove.notInBlackList")
            .formatted(id));
        source.sendError(text);
        return 0;
    }

    private static int displayBlackList(int page, ServerCommandSource source) {
        if (CONFIG.blackListSize() == 0) {
            source.sendError(Text.literal(Translation.translate("command.saplanting.blackList.empty")));
            return 0;
        }

        /* Page validation */
        JsonArray blackList = CONFIG.getBlackList();
        if ((page - 1) * 8 > blackList.size() || page < 1) {
            MutableText pageError = Text.literal(Translation.translate("command.saplanting.page404"));
            source.sendError(pageError);
            return 0;
        }

        /* TITLE: */
        MutableText title = Text.literal(Translation.translate("command.saplanting.blackList.title"));
        source.sendFeedback(title, false);

        /* - ITEM */
        for (int i = (page - 1) * 8; (i < (page * 8)) && (i < blackList.size()); ++i) {
            String id = blackList.get(i).getAsString();
            MutableText head = Text.literal("- ");
            MutableText remove = Text.literal(Translation.translate("command.saplanting.blackList.click.remove"));
            remove.setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/saplanting blackList remove %s".formatted(id))));
            MutableText item = Text.literal(id);
            source.sendFeedback(head.append(remove).append(" ").append(item), false);
        }

        /* [FORMER] << PAGE >> [NEXT] */
        MutableText next = Text.literal(Translation.translate("command.saplanting.next"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/saplanting blackList " + (page + 1))));
        MutableText former = Text.literal(Translation.translate("command.saplanting.former"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/saplanting blackList " + (page - 1))));
        MutableText foot;
        if (blackList.size() <= 8) {
            foot = Text.literal(" 1 ");
        } else if (page == 1) {
            foot = Text.literal(" 1 >> ").append(next);
        } else if ((page * 8) > blackList.size()) {
            foot = Text.literal(" ").append(former).append(" << %d ".formatted(page));
        } else {
            foot = Text.literal(" ").append(former).append(" << %d >> ".formatted(page)).append(next);
        }
        source.sendFeedback(foot, false);
        return CONFIG.blackListSize();
    }

    private static int clearBlackList(ServerCommandSource source) {
        if (CONFIG.blackListSize() == 0) {
            source.sendError(Text.literal(Translation.translate("command.saplanting.blackList.empty")));
            return 0;
        }
        int i = CONFIG.blackListSize();
        MutableText text = Text.literal(Translation.translate("command.saplanting.blackList.clear"));
        source.sendFeedback(text, false);
        CONFIG.clearBlackList();
        return i;
    }
}

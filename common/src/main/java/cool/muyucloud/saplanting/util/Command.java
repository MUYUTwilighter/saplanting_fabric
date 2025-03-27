package cool.muyucloud.saplanting.util;

import com.google.gson.JsonArray;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;

public class Command {
    private static final Config CONFIG = Saplanting.getConfig();
    private static final Config DEFAULT_CONFIG = Saplanting.getDefaultConfig();
    private static final Style CLICKABLE_COMMAND = Style.EMPTY
        .withColor(ChatFormatting.GREEN)
        .withUnderlined(true);
    private static final Style CLICKABLE_FILE = Style.EMPTY
        .withUnderlined(true);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext access, boolean dedicated) {
        /* /saplanting <PAGE> */
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("saplanting");
        root.requires(source -> source.hasPermission(2));
        root.executes(context -> displayAll(1, context.getSource()));
        root.then(Commands.argument("page", IntegerArgumentType.integer())
            .executes(context -> displayAll(IntegerArgumentType.getInteger(context, "page"), context.getSource())));

        /* /saplanting property <KEY> <VALUE> */
        LiteralArgumentBuilder<CommandSourceStack> property = Commands.literal("property");
        for (String key : CONFIG.getKeySet()) {
            LiteralArgumentBuilder<CommandSourceStack> propertyE = Commands.literal(key);
            propertyE.executes((context) -> getProperty(key, context.getSource()));
            if (CONFIG.getType(key) == Boolean.class) {
                propertyE.then(Commands.argument("value", BoolArgumentType.bool())
                    .executes((context -> setProperty(key, BoolArgumentType.getBool(context, "value"), context.getSource()))));
                propertyE.then(Commands.literal("default")
                    .executes(context -> setProperty(key, DEFAULT_CONFIG.getAsBoolean(key), context.getSource())));
            } else if (CONFIG.getType(key) == Integer.class) {
                propertyE.then(Commands.argument("value", IntegerArgumentType.integer())
                    .executes((context -> setProperty(key, IntegerArgumentType.getInteger(context, "value"), context.getSource()))));
                propertyE.then(Commands.literal("default")
                    .executes(context -> setProperty(key, DEFAULT_CONFIG.getAsInt(key), context.getSource())));
            }
            property.then(propertyE);
        }
        root.then(property);

        /* /saplanting language <OPERATION> [ARG] */
        // /saplanting language
        LiteralArgumentBuilder<CommandSourceStack> language = Commands.literal("language");
        language.executes(context -> queryLanguage(context.getSource()));
        // /saplanting language <LANG>
        for (String name : CONFIG.getValidLangs()) {
            language.then(Commands.literal(name).executes(context -> updateLanguage(name, context.getSource())));
        }
        // /saplanting language default
        language.then(Commands.literal("default")
            .executes(context -> updateLanguage("en_us", context.getSource())));
        root.then(language);

        /* /saplanting file <OPERATION> */
        LiteralArgumentBuilder<CommandSourceStack> file = Commands.literal("file");
        file.then(Commands.literal("load").executes(context -> load(context.getSource(), dedicated)));
        file.then(Commands.literal("save").executes(context -> save(context.getSource(), dedicated)));
        root.then(file);

        /* /saplanting blacklist <OPERATION> [ARG] */
        LiteralArgumentBuilder<CommandSourceStack> blacklist = Commands.literal("blacklist");
        blacklist.executes(context -> displayBlackList(1, context.getSource()));
        // saplanting blacklist <page>
        blacklist.then(Commands.argument("page", IntegerArgumentType.integer())
            .executes(context ->
                displayBlackList(IntegerArgumentType.getInteger(context, "page"), context.getSource())));
        // saplanting blacklist add <item>
        blacklist.then(Commands.literal("add")
            .executes(context -> {
                Player player = context.getSource().getPlayer();
                if (player == null) {
                    return 0;
                }
                Item item = player.getMainHandItem().getItem();
                if (item.equals(Items.AIR)) {
                    return 0;
                }
                String value = BuiltInRegistries.ITEM.getKey(item).toString();
                return addToBlackList(value, context.getSource());
            })
            .requires(CommandSourceStack::isPlayer)
            .then(Commands.argument("item", StringArgumentType.greedyString())
                .suggests((context, builder) -> {
                    Set<ResourceLocation> itemIds = BuiltInRegistries.ITEM.keySet();
                    for (ResourceLocation id : itemIds) {
                        builder.suggest(id.toString());
                    }
                    builder.suggest("*");
                    return builder.buildFuture();
                })
                .executes(context ->
                    addToBlackList(
                        StringArgumentType.getString(context, "item"),
                        context.getSource()
                    )
                )
            )
        );
        // saplanting blacklist remove <item>
        blacklist.then(Commands.literal("remove")
            .executes(context -> {
                Player player = context.getSource().getPlayer();
                if (player == null) {
                    return 0;
                }
                Item item = player.getMainHandItem().getItem();
                if (item.equals(Items.AIR)) {
                    return 0;
                }
                String value = BuiltInRegistries.ITEM.getKey(item).toString();
                return removeFromBlackList(value, context.getSource());
            })
            .requires(CommandSourceStack::isPlayer)
            .then(Commands.argument("item", StringArgumentType.greedyString())
                .suggests((context, builder) -> {
                    Set<ResourceLocation> itemIds = BuiltInRegistries.ITEM.keySet();
                    for (ResourceLocation id : itemIds) {
                        builder.suggest(id.toString());
                    }
                    builder.suggest("*");
                    return builder.buildFuture();
                })
                .executes(context ->
                    removeFromBlackList(StringArgumentType.getString(context, "item"),
                        context.getSource()
                    )
                )
            )
        );
        // saplanting blacklist clear
        blacklist.then(Commands.literal("clear")
            .executes(context -> clearBlackList(context.getSource())));
        root.then(blacklist);

        /* /saplanting whitelist <OPERATION> [ARG] */
        LiteralArgumentBuilder<CommandSourceStack> whitelist = Commands.literal("whitelist");
        whitelist.executes(context -> displayWhitelist(context.getSource(), 1));
        // saplanting whitelist <page>
        whitelist.then(Commands.argument("page", IntegerArgumentType.integer())
            .executes(context ->
                displayWhitelist(context.getSource(), IntegerArgumentType.getInteger(context, "page"))));
        // saplanting whitelist add <item>
        whitelist.then(Commands.literal("add")
                .executes(context -> {
                    Player player = context.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    Item item = player.getMainHandItem().getItem();
                    if (item.equals(Items.AIR)) {
                        return 0;
                    }
                    String value = BuiltInRegistries.ITEM.getKey(item).toString();
                    return addToWhitelist(value, context.getSource());
                })
                .requires(CommandSourceStack::isPlayer)
            .then(Commands.argument("item", StringArgumentType.greedyString())
                .suggests((context, builder) -> {
                    Set<ResourceLocation> itemIds = BuiltInRegistries.ITEM.keySet();
                    for (ResourceLocation id : itemIds) {
                        builder.suggest(id.toString());
                    }
                    builder.suggest("*");
                    return builder.buildFuture();
                })
                .executes(context ->
                    addToWhitelist(
                        StringArgumentType.getString(context, "item"),
                        context.getSource()
                    )
                )
            )
        );
        // saplanting whitelist remove <item>
        whitelist.then(Commands.literal("remove")
            .executes(context -> {
                Player player = context.getSource().getPlayer();
                if (player == null) {
                    return 0;
                }
                Item item = player.getMainHandItem().getItem();
                if (item.equals(Items.AIR)) {
                    return 0;
                }
                String value = BuiltInRegistries.ITEM.getKey(item).toString();
                return removeFromWhitelist(value, context.getSource());
            })
            .requires(CommandSourceStack::isPlayer)
            .then(Commands.argument("item", StringArgumentType.greedyString())
                .suggests((context, builder) -> {
                    Set<ResourceLocation> itemIds = BuiltInRegistries.ITEM.keySet();
                    for (ResourceLocation id : itemIds) {
                        builder.suggest(id.toString());
                    }
                    builder.suggest("*");
                    return builder.buildFuture();
                })
                .executes(context ->
                    removeFromWhitelist(StringArgumentType.getString(context, "item"),
                        context.getSource()))));
        // saplanting whitelist clear
        whitelist.then(Commands.literal("clear")
            .executes(context -> clearWhitelist(context.getSource())));
        root.then(whitelist);

        dispatcher.register(root);
    }

    private static int setProperty(String key, boolean value, CommandSourceStack source) {
        if (CONFIG.set(key, value)) {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.property.set.success")
                .formatted(key, Boolean.toString(value)));
            MutableComponent hover = Component.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
            text.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(hover)));
            sendFeedback(source, text, false);
        } else {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.property.set.already")
                .formatted(key, Boolean.toString(value)));
            source.sendFailure(text);
        }
        return value ? 1 : 0;
    }

    private static int setProperty(String key, int value, CommandSourceStack source) {
        if (CONFIG.set(key, value)) {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.property.set.success")
                .formatted(key, Integer.toString(value)));
            MutableComponent hover = Component.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
            text.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(hover)));
            sendFeedback(source, text, false);
        } else {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.property.set.already")
                .formatted(key, Integer.toString(value)));
            source.sendFailure(text);
        }
        return value;
    }

    private static int getProperty(String key, CommandSourceStack source) {
        MutableComponent hover = Component.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
        MutableComponent text = Component.literal(Translation.translate("command.saplanting.property.get")
            .formatted(key, CONFIG.getAsString(key)));
        text.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(hover)));
        sendFeedback(source, text, false);
        return 1;
    }

    private static int updateLanguage(String name, CommandSourceStack source) {
        if (!CONFIG.set("language", name)) {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.language.already").formatted(name));
            source.sendFailure(text);
            return 0;
        }
        Translation.updateLanguage(name);
        MutableComponent text = Component.literal(Translation.translate("command.saplanting.language.success").formatted(name));
        sendFeedback(source, text, false);
        return 1;
    }

    private static int queryLanguage(CommandSourceStack source) {
        MutableComponent text = Component.literal(Translation.translate("command.saplanting.language.query").formatted(CONFIG.getAsString("language")));
        MutableComponent change = Component.literal(Translation.translate("command.saplanting.language.switch"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.SuggestCommand("/saplanting language switch ")));
        sendFeedback(source, text.append(change), false);
        return 1;
    }

    private static int load(CommandSourceStack source, boolean dedicated) throws CommandSyntaxException {
        MutableComponent text;
        MutableComponent hover = Component.literal(Translation.translate("command.saplanting.file.open"));
        MutableComponent file = Component.literal(CONFIG.stringConfigPath());
        if (isLocal(source, dedicated)) {
            file.setStyle(CLICKABLE_FILE
                .withClickEvent(new ClickEvent.OpenFile(CONFIG.stringConfigPath()))
                .withHoverEvent(new HoverEvent.ShowText(hover)));
        }
        MutableComponent query = Component.literal(Translation.translate("command.saplanting.file.load.query"))
            .setStyle(CLICKABLE_COMMAND.withClickEvent(new ClickEvent.RunCommand("/saplanting")));
        if (CONFIG.load()) {
            text = Component.literal(Translation.translate("command.saplanting.file.load.success"));
            if (!dedicated) {
                text.append(file);
            }
            text.append(" ").append(query);
            sendFeedback(source, text, false, dedicated);
            return 1;
        } else {
            text = Component.literal(Translation.translate("command.saplanting.file.load.fail"));
            if (!dedicated) {
                text.append(file);
            }
            source.sendFailure(text);
            return 0;
        }
    }

    private static int save(CommandSourceStack source, boolean dedicated) {
        MutableComponent text;
        MutableComponent hover = Component.literal(Translation.translate("commands.saplanting.file.open"));
        MutableComponent file = Component.literal(CONFIG.stringConfigPath());
        if (isLocal(source, dedicated)) {
            file.setStyle(CLICKABLE_FILE
                .withClickEvent(new ClickEvent.OpenFile(CONFIG.stringConfigPath()))
                .withHoverEvent(new HoverEvent.ShowText(hover)));
        }
        if (CONFIG.save()) {
            text = Component.literal(Translation.translate("command.saplanting.file.save.success"));
            if (!dedicated) {
                text.append(file);
            }
            sendFeedback(source, text, false, dedicated);
            return 1;
        } else {
            text = Component.literal(Translation.translate("command.saplanting.file.save.fail"));
            if (!dedicated) {
                text.append(file);
            }
            source.sendFailure(text);
            return 0;
        }
    }

    private static int displayAll(int page, CommandSourceStack source) {
        List<String> arr = CONFIG.getKeySet().stream().toList();
        if ((page - 1) * 8 > arr.size() || page < 1) {
            MutableComponent pageError = Component.literal(Translation.translate("command.saplanting.page404"));
            source.sendFailure(pageError);
            return 0;
        }

        /* ======TITLE====== */
        MutableComponent title = Component.literal(Translation.translate("command.saplanting.title")).setStyle(Style.EMPTY
            .withColor(ChatFormatting.GOLD));
        sendFeedback(source, title, false);
        /* - <RESET> KEY : VALUE */
        for (int i = (page - 1) * 8; i < page * 8 && i < arr.size(); ++i) {
            String key = arr.get(i);
            MutableComponent head = Component.literal("- ");
            MutableComponent reset = Component.literal(Translation.translate("command.saplanting.reset"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent.RunCommand(
                        "/saplanting property %s default".formatted(key)))
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal(Translation.translate("command.saplanting.reset.hover")
                            .formatted(DEFAULT_CONFIG.getAsString(key))))));
            MutableComponent hover = Component.literal(Translation.translate("config.saplanting.property.%s".formatted(key)));
            MutableComponent property = Component.literal(key).setStyle(CLICKABLE_COMMAND
                .withHoverEvent(new HoverEvent.ShowText(hover))
                .withClickEvent(new ClickEvent.SuggestCommand("/saplanting property %s ".formatted(key))));
            MutableComponent value = Component.literal(": " + CONFIG.getAsString(key));
            head.append(reset).append(" ").append(property).append(value);

            sendFeedback(source, head, false);
        }

        /* [FORMER] PAGE [NEXT] */
        MutableComponent next = Component.literal(Translation.translate("command.saplanting.next"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand("/saplanting " + (page + 1))));
        MutableComponent former = Component.literal(Translation.translate("command.saplanting.former"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand("/saplanting " + (page - 1))));
        MutableComponent foot;
        if (arr.size() <= 8) {
            foot = Component.literal(" 1 ");
        } else if (page == 1) {
            foot = Component.literal(" 1 >> ").append(next);
        } else if ((page * 8) >= arr.size()) {
            foot = Component.literal(" ").append(former).append(" << %d ".formatted(page));
        } else {
            foot = Component.literal(" ").append(former).append(" << %d >> ".formatted(page)).append(next);
        }
        sendFeedback(source, foot, false);

        return page;
    }

    private static int addToBlackList(String id, CommandSourceStack source) {
        if (CONFIG.addToBlackList(id)) {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.blacklist.add.success")
                .formatted(id));
            MutableComponent undo = Component.literal(Translation.translate("command.saplanting.list.click.remove"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent.RunCommand(
                        "/saplanting blacklist remove %s".formatted(Config.formatItemLike(id)))));
            sendFeedback(source, text.append(" ").append(undo), false);
            return 1;
        }

        MutableComponent text = Component.literal(Translation.translate("command.saplanting.blacklist.add.error")
            .formatted(id));
        source.sendFailure(text);
        return 0;
    }

    private static int removeFromBlackList(String id, CommandSourceStack source) {
        if (CONFIG.removeFromBlackList(id)) {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.blacklist.remove.success")
                .formatted(id));
            MutableComponent undo = Component.literal(Translation.translate("command.saplanting.list.click.undo"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent.RunCommand(
                        "/saplanting blacklist add %s".formatted(Config.formatItemLike(id)))));
            sendFeedback(source, text.append(" ").append(undo), false);
            return 1;
        }

        MutableComponent text = Component.literal(Translation.translate("command.saplanting.blacklist.remove.notExist")
            .formatted(id));
        source.sendFailure(text);
        return 0;
    }

    private static int displayBlackList(int page, CommandSourceStack source) {
        if (CONFIG.blacklistSize() == 0) {
            source.sendFailure(Component.literal(Translation.translate("command.saplanting.blacklist.empty")));
            return 0;
        }

        /* Page validation */
        JsonArray blacklist = CONFIG.getBlackList();
        if ((page - 1) * 8 > blacklist.size() || page < 1) {
            MutableComponent pageError = Component.literal(Translation.translate("command.saplanting.page404"));
            source.sendFailure(pageError);
            return 0;
        }

        /* TITLE: */
        MutableComponent title = Component.literal(Translation.translate("command.saplanting.blacklist.title"));
        sendFeedback(source, title, false);

        /* - ITEM */
        for (int i = (page - 1) * 8; (i < (page * 8)) && (i < blacklist.size()); ++i) {
            String id = blacklist.get(i).getAsString();
            MutableComponent head = Component.literal("- ");
            MutableComponent remove = Component.literal(Translation.translate("command.saplanting.list.click.remove"));
            remove.setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand(
                    "/saplanting blacklist remove %s".formatted(id))));
            MutableComponent item = Component.literal(id);
            sendFeedback(source, head.append(remove).append(" ").append(item), false);
        }

        /* [FORMER] << PAGE >> [NEXT] */
        MutableComponent next = Component.literal(Translation.translate("command.saplanting.next"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand("/saplanting blacklist " + (page + 1))));
        MutableComponent former = Component.literal(Translation.translate("command.saplanting.former"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand("/saplanting blacklist " + (page - 1))));
        MutableComponent foot;
        if (blacklist.size() <= 8) {
            foot = Component.literal(" 1 ");
        } else if (page == 1) {
            foot = Component.literal(" 1 >> ").append(next);
        } else if ((page * 8) > blacklist.size()) {
            foot = Component.literal(" ").append(former).append(" << %d ".formatted(page));
        } else {
            foot = Component.literal(" ").append(former).append(" << %d >> ".formatted(page)).append(next);
        }
        sendFeedback(source, foot, false);
        return CONFIG.blacklistSize();
    }

    private static int clearBlackList(CommandSourceStack source) {
        if (CONFIG.blacklistSize() == 0) {
            source.sendFailure(Component.literal(Translation.translate("command.saplanting.blacklist.empty")));
            return 0;
        }
        int i = CONFIG.blacklistSize();
        MutableComponent text = Component.literal(Translation.translate("command.saplanting.blacklist.clear"));
        sendFeedback(source, text, false);
        CONFIG.clearBlackList();
        return i;
    }

    private static int addToWhitelist(String id, CommandSourceStack source) {
        if (CONFIG.addToWhitelist(id)) {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.whitelist.add.success")
                .formatted(id));
            MutableComponent undo = Component.literal(Translation.translate("command.saplanting.list.click.undo"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent.RunCommand(
                        "/saplanting whitelist remove %s".formatted(Config.formatItemLike(id)))));
            sendFeedback(source, text.append(" ").append(undo), false);
            return 1;
        }

        MutableComponent text = Component.literal(Translation.translate("command.saplanting.whitelist.add.error")
            .formatted(id));
        source.sendFailure(text);
        return 0;
    }

    private static int removeFromWhitelist(String id, CommandSourceStack source) {
        if (CONFIG.removeFromWhitelist(id)) {
            MutableComponent text = Component.literal(Translation.translate("command.saplanting.whitelist.remove.success")
                .formatted(id));
            MutableComponent undo = Component.literal(Translation.translate("command.saplanting.list.click.undo"))
                .setStyle(CLICKABLE_COMMAND
                    .withClickEvent(new ClickEvent.RunCommand(
                        "/saplanting whitelist add %s".formatted(Config.formatItemLike(id)))));
            sendFeedback(source, text.append(" ").append(undo), false);
            return 1;
        }

        MutableComponent text = Component.literal(Translation.translate("command.saplanting.whitelist.remove.notExist")
            .formatted(id));
        source.sendFailure(text);
        return 0;
    }

    private static int displayWhitelist(CommandSourceStack source, int page) {
        if (CONFIG.whitelistSize() == 0) {
            source.sendFailure(Component.literal(Translation.translate("command.saplanting.whitelist.empty")));
            return 0;
        }

        /* Page validation */
        JsonArray whitelist = CONFIG.getWhitelist();
        if ((page - 1) * 8 > whitelist.size() || page < 1) {
            MutableComponent pageError = Component.literal(Translation.translate("command.saplanting.page404"));
            source.sendFailure(pageError);
            return 0;
        }

        /* TITLE: */
        MutableComponent title = Component.literal(Translation.translate("command.saplanting.whitelist.title"));
        sendFeedback(source, title, false);

        /* - ITEM */
        for (int i = (page - 1) * 8; (i < (page * 8)) && (i < whitelist.size()); ++i) {
            String id = whitelist.get(i).getAsString();
            MutableComponent head = Component.literal("- ");
            MutableComponent remove = Component.literal(Translation.translate("command.saplanting.list.click.remove"));
            remove.setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand(
                    "/saplanting whitelist remove %s".formatted(id))));
            MutableComponent item = Component.literal(id);
            sendFeedback(source, head.append(remove).append(" ").append(item), false);
        }

        /* [FORMER] << PAGE >> [NEXT] */
        MutableComponent next = Component.literal(Translation.translate("command.saplanting.next"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand("/saplanting whitelist " + (page + 1))));
        MutableComponent former = Component.literal(Translation.translate("command.saplanting.former"))
            .setStyle(CLICKABLE_COMMAND
                .withClickEvent(new ClickEvent.RunCommand("/saplanting whitelist " + (page - 1))));
        MutableComponent foot;
        if (whitelist.size() <= 8) {
            foot = Component.literal(" 1 ");
        } else if (page == 1) {
            foot = Component.literal(" 1 >> ").append(next);
        } else if ((page * 8) > whitelist.size()) {
            foot = Component.literal(" ").append(former).append(" << %d ".formatted(page));
        } else {
            foot = Component.literal(" ").append(former).append(" << %d >> ".formatted(page)).append(next);
        }
        sendFeedback(source, foot, false);
        return CONFIG.whitelistSize();
    }

    private static int clearWhitelist(CommandSourceStack source) {
        if (CONFIG.whitelistSize() == 0) {
            source.sendFailure(Component.literal(Translation.translate("command.saplanting.whitelist.empty")));
            return 0;
        }
        int i = CONFIG.whitelistSize();
        MutableComponent text = Component.literal(Translation.translate("command.saplanting.whitelist.clear"));
        sendFeedback(source, text, false);
        CONFIG.clearWhitelist();
        return i;
    }

    private static void sendFeedback(CommandSourceStack source, Component text, boolean broadcastToOps) {
        sendFeedback(source, text, broadcastToOps, true);
    }

    private static void sendFeedback(CommandSourceStack source, Component text, boolean broadcastToOps, boolean dedicated) {
        if (isLocal(source, dedicated)) {
            ClientUtil.message(text, false);
        } else {
            source.sendSuccess(() -> text, broadcastToOps);
        }
    }

    public static boolean isLocal(CommandSourceStack source, boolean dedicated) {
        return !dedicated && source.getEntity() instanceof Player player && ClientUtil.isLocalPlayer(player.getUUID());
    }
}

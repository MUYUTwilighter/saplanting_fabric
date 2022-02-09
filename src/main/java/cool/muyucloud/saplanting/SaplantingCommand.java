package cool.muyucloud.saplanting;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

import java.util.Objects;

public class SaplantingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // /saplanting
        final LiteralArgumentBuilder<ServerCommandSource> root = (CommandManager.literal("saplanting")
                .requires(source -> source.hasPermissionLevel(2)));
        root.executes(context -> showAll(context.getSource()));

        // /saplanting plantEnable
        root.then(CommandManager.literal("plantEnable").executes(context -> getPlantEnable(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setPlantEnable(context.getSource(), BoolArgumentType.getBool(context, "value")))));

        // /saplanting plantEnable
        root.then(CommandManager.literal("plantLarge").executes(context -> getPlantLarge(context.getSource()))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> setPlantLarge(context.getSource(), BoolArgumentType.getBool(context, "value")))));

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

        // /saplanting load
        root.then(CommandManager.literal("load").executes(context -> loadProperty(context.getSource())));

        // /saplanting save
        root.then(CommandManager.literal("save").executes(context -> saveProperty(context.getSource())));

        // register
        dispatcher.register(root);
    }

    public static int showAll(ServerCommandSource target) {
        target.sendFeedback(new TranslatableText("saplanting.commands.saplanting.showAll")
                .setStyle(Style.EMPTY.withColor(TextColor.parse("gold"))), false);
        target.sendFeedback(new TranslatableText(" - plantEnable:  " + Config.getPlantEnable()), false);
        target.sendFeedback(new TranslatableText(" - plantLarge:   " + Config.getPlantLarge()), false);
        target.sendFeedback(new TranslatableText(" - plantDelay:   " + Config.getPlantDelay()), false);
        target.sendFeedback(new TranslatableText(" - avoidDense:   " + Config.getAvoidDense()), false);
        target.sendFeedback(new TranslatableText(" - playerAround: " + Config.getPlayerAround()), false);

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

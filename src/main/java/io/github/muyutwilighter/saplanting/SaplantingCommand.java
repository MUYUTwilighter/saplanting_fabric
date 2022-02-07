package io.github.muyutwilighter.saplanting;

import com.mojang.brigadier.CommandDispatcher;
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

        // /saplanting <property>
        for (String property : Config.stringPropertyNames()) {
            root.then((CommandManager.literal(property).executes(context -> {
                context.getSource().sendFeedback(new TranslatableText(property + ": " + Config.stringPropertyValue(property)), false);
                return 1;
            })).then(CommandManager.argument("value", TextArgumentType.text()).executes(
                    context -> setProperty(context.getSource(), property, TextArgumentType.getTextArgument(context, "value").getString())
            )));
        }

        // /saplanting load
        root.then(CommandManager.literal("load").executes(context -> loadProperty(context.getSource())));

        // /saplanting save
        root.then(CommandManager.literal("save").executes(context -> saveProperty(context.getSource())));

        // register
        dispatcher.register(root);
    }

    public static int showAll(ServerCommandSource target) {
        target.sendFeedback(new TranslatableText("saplanting.commands.saplanting.showAll"), false);
        target.sendFeedback(new TranslatableText(" - plantEnable: " + Config.getPlantEnable()), false);
        target.sendFeedback(new TranslatableText(" - plantLarge:  " + Config.getPlantLarge()), false);
        target.sendFeedback(new TranslatableText(" - plantDelay:  " + Config.getPlantDelay()), false);
        target.sendFeedback(new TranslatableText(" - avoidDense:  " + Config.getAvoidDense()), false);
        target.sendFeedback(new TranslatableText(" - playerAround:  " + Config.getPlayerAround()), false);

        return 1;
    }

    public static int setProperty(ServerCommandSource source, String name, String value) {
        if (Objects.equals(name, "plantEnable")) {
            Config.setPlantEnable(Boolean.parseBoolean(value));
            source.sendFeedback(new TranslatableText(name)
                            .append(new TranslatableText("saplanting.commands.saplanting.property.set.success")
                                    .append(new TranslatableText(value)))
                    , false);
        } else if (Objects.equals(name, "plantLarge")) {
            Config.setPlantLarge(Boolean.parseBoolean(value));
            source.sendFeedback(new TranslatableText(name)
                            .append(new TranslatableText("saplanting.commands.saplanting.property.set.success")
                                    .append(new TranslatableText(value)))
                    , false);
        } else if (Objects.equals(name, "plantDelay")) {
            try {
                int tmp = Integer.parseInt(value);
                if (tmp < 0) {
                    throw new Exception("Input is not nonnegative");
                }
                Config.setPlantDelay(tmp);
                source.sendFeedback(new TranslatableText(name)
                        .append(new TranslatableText("saplanting.commands.saplanting.property.set.success")
                                .append(new TranslatableText(value)))
                        , false);
            } catch (Exception e) {
                e.printStackTrace();
                source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.property.set.fail"), false);
                return 0;
            }
        } else if (Objects.equals(name, "avoidDense")) {
            try {
                int tmp = Integer.parseInt(value);
                if (tmp < 0) {
                    throw new Exception("Input is not nonnegative");
                }
                Config.setAvoidDense(tmp);
                source.sendFeedback(new TranslatableText(name)
                                .append(new TranslatableText("saplanting.commands.saplanting.property.set.success")
                                        .append(new TranslatableText(value)))
                        , false);
            } catch (Exception e) {
                e.printStackTrace();
                source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.property.set.fail"), false);
                return 0;
            }
        } else if (Objects.equals(name, "playerAround")) {
            try {
                int tmp = Integer.parseInt(value);
                if (tmp < 0) {
                    throw new Exception("Input is not nonnegative");
                }
                Config.setPlayerAround(tmp);
                source.sendFeedback(new TranslatableText(name)
                                .append(new TranslatableText("saplanting.commands.saplanting.property.set.success")
                                        .append(new TranslatableText(value)))
                        , false);
            } catch (Exception e) {
                e.printStackTrace();
                source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.property.set.fail"), false);
                return 0;
            }
        }
        return 1;
    }

    public static int loadProperty(ServerCommandSource source) {
        try {
            Config.load();
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.load.success"), false);
        } catch (Exception e) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.load.fail"), false);
            return 0;
        }
        return 1;
    }

    public static int saveProperty(ServerCommandSource source) {
        try {
            Config.saveConfig();
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.save.success")
                    .append(new TranslatableText(Config.stringPath())), false);
        } catch (Exception e) {
            source.sendFeedback(new TranslatableText("saplanting.commands.saplanting.save.fail"), false);
            return 0;
        }

        return 1;
    }
}

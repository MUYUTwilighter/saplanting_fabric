package cool.muyucloud.saplanting.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import cool.muyucloud.saplanting.Config;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BoolPropertyArgumentType implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = List.of(Config.getBoolPropertyNames());

    private BoolPropertyArgumentType() {

    }

    public static BoolPropertyArgumentType property() {
        return new BoolPropertyArgumentType();
    }

    public static String getBoolProperty(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (String property : EXAMPLES) {
            if (property.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(property);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

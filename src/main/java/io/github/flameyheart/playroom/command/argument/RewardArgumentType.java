package io.github.flameyheart.playroom.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.flameyheart.playroom.tiltify.Reward;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class RewardArgumentType extends EnumArgumentType<Reward> {
    private static final Collection<String> EXAMPLES = Arrays.asList("STRING", "MINECRAFT");
    public static final DynamicCommandExceptionType INVALID_ENUM_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("commands.error.enum_not_found", id));

    protected RewardArgumentType() {
        super(Reward.CODEC, Reward::values);
    }


    public static RewardArgumentType reward() {
        return new RewardArgumentType();
    }

    public static Reward getReward(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, Reward.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return suggestIdentifiers(Arrays.stream(Reward.values()).map(Enum::name).toList(), builder);
    }

    public CompletableFuture<Suggestions> suggestIdentifiers(Iterable<String> candidates, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toUpperCase(Locale.ROOT);
        forEachMatching(candidates, string, id -> id, id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }

    public <S> void forEachMatching(Iterable<S> candidates, String remaining, Function<S, String> identifier, Consumer<S> action) {
        boolean bl = remaining.indexOf(58) > -1;
        for (S object : candidates) {
            var content = identifier.apply(object);
            if (bl) {
                String string = content.toString();
                if (!CommandSource.shouldSuggest(remaining, string)) continue;
                action.accept(object);
                continue;
            }
            if (!CommandSource.shouldSuggest(remaining, content) && !content.contains(remaining)) continue;
            action.accept(object);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

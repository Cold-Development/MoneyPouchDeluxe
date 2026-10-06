package dev.padrewin.moneypouchdeluxe.commands.arguments;

import dev.padrewin.colddev.command.argument.StringArgumentHandler;
import dev.padrewin.colddev.command.framework.Argument;
import dev.padrewin.colddev.command.framework.CommandContext;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Accepts any text, with tab completion from the given suggestions. The command itself checks the
 * value, so it can answer with its own message (e.g. "the pouch X does not exist").
 */
public class StringSuggestingArgumentHandler extends StringArgumentHandler {

    private final Function<CommandContext, List<String>> suggestionsFunction;

    public StringSuggestingArgumentHandler(String... suggestions) {
        this(context -> Arrays.asList(suggestions));
    }

    public StringSuggestingArgumentHandler(Function<CommandContext, List<String>> suggestionsFunction) {
        this.suggestionsFunction = suggestionsFunction;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return this.suggestionsFunction.apply(context);
    }

}

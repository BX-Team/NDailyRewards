package org.bxteam.ndailyrewards.commands.argument;

import com.google.inject.Inject;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bxteam.ndailyrewards.configuration.Language;
import org.bxteam.ndailyrewards.utils.TextUtils;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SetDayArgument extends ArgumentResolver<CommandSender, Integer> {
    private final Plugin plugin;

    public static final String KEY = "day";

    @Override
    public ParseResult<Integer> parse(Invocation<CommandSender> invocation, Argument<Integer> context, String argument) {
        try {
            int day = Integer.parseInt(argument);
            ConfigurationSection rewards = this.plugin.getConfig().getConfigurationSection("rewards.days");

            if (rewards == null || !rewards.contains(String.valueOf(day))) {
                return ParseResult.failure(Language.PREFIX.asColoredString() + TextUtils.applyColor("&cDay &e" + day + " &cdoes not exist in the configuration"));
            }

            return ParseResult.success(day);
        } catch (NumberFormatException e) {
            return ParseResult.failure(Language.PREFIX.asColoredString() + TextUtils.applyColor("&cInvalid day format, must be a number"));
        }
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Integer> argument, SuggestionContext context) {
        return SuggestionResult.of(
            this.plugin.getConfig().getConfigurationSection("rewards.days").getKeys(false)
                .stream()
                .map(String::valueOf)
                .toList()
        );
    }
}

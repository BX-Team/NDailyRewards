package space.bxteam.ndailyrewards.managers.command.matcher;

import org.bukkit.util.StringUtil;
import space.bxteam.ndailyrewards.managers.command.ArgumentMatcher;

import java.util.ArrayList;
import java.util.List;

public class StringArgumentMatcher implements ArgumentMatcher {
    @Override
    public List<String> filter (List<String> tabCompletions, String argument) {
        List<String> result = new ArrayList<>();

        StringUtil.copyPartialMatches(argument, tabCompletions, result);

        return result;
    }
}

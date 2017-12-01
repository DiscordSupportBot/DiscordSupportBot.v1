package github.scarsz.discordsupportbot.util;

import github.scarsz.discordsupportbot.GuildConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConversionUtil {

    private static final JSONParser JSON_PARSER = new JSONParser();

    public static Collection<GuildConfig> convertLegacyGuilds(String rawJson) throws Exception {
        if (!rawJson.contains(",\"guildId\"")) throw new RuntimeException("Configuration file already upgraded");

        Set<GuildConfig> builtConfigs = new HashSet<>();
        JSONArray guilds = (JSONArray) JSON_PARSER.parse(rawJson);
        for (Object guildObject : guilds) {
            JSONObject data = (JSONObject) guildObject;
            GuildConfig guildConfig = new GuildConfig(
                    (boolean) data.getOrDefault("pmTranscriptsOnClose", true),
                    (String) data.getOrDefault("firstMessageChannelId", "0"),
                    (String) data.getOrDefault("defaultReactionEmoji", "✅"),
                    (boolean) data.getOrDefault("authorCanCloseTicket", true),
                    (int) data.getOrDefault("secondsUntilTicketCloses", 60),
                    (JSONArray) data.getOrDefault("rolesAllowedToCloseTickets", new JSONArray()),
                    (int) data.getOrDefault("hoursUntilChannelTimeout", 168),
                    (int) data.getOrDefault("maxOpenTickets", 0),
                    (String) data.getOrDefault("guildId", null)
            );
            builtConfigs.add(guildConfig);
        }

        return builtConfigs;
    }

}

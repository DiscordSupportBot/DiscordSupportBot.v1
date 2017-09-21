package github.scarsz.discordsupportbot;

import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GuildConfig {

    public final Guild guild;
    public final List<String> starterQuestions;

    public GuildConfig(JSONObject guildConfigJson) {
        this.guild = DiscordSupportBot.instance.jda.getGuildById((String) guildConfigJson.get("id"));
        System.out.println("Setting up GuildConfig for " + guild);

        this.starterQuestions = StreamSupport.stream(((JSONArray) guildConfigJson.get("starterQuestions")).spliterator(), false).map(o -> (String) o).collect(Collectors.toList());

        if (guild == null) {
            System.out.println("Guild is null, GuildConfig being left half-initialized");
            return;
        }
    }

}

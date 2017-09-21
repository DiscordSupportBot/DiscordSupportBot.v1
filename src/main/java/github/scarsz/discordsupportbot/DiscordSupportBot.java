package github.scarsz.discordsupportbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiscordSupportBot {

    public static DiscordSupportBot instance;
    public static final File dataFile = new File("data.json");

    public final JDA jda;
    public final List<GuildConfig> guildConfigs = new ArrayList<>();

    public DiscordSupportBot(String botToken) throws LoginException, InterruptedException, RateLimitedException, IOException {
        DiscordSupportBot.instance = this;

        this.jda = new JDABuilder(AccountType.BOT)
                .setToken(botToken)
                .setAudioEnabled(false)
                .setAutoReconnect(true)
                .setBulkDeleteSplittingEnabled(false)
                .setGame(Game.of("with tickets"))
                .buildBlocking();

        System.out.println("JDA connected, initializing guilds");

        JSONObject guilds = new JSONObject(FileUtils.readFileToString(dataFile, "utf-8"));
        for (Object guildObject : (JSONArray) guilds.get("guilds")) {
            GuildConfig config = new GuildConfig((JSONObject) guildObject);
        }
    }

}

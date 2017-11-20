package github.scarsz.discordsupportbot;

import github.scarsz.discordsupportbot.listeners.DiscordSetupListener;
import github.scarsz.discordsupportbot.listeners.DiscordSupportTicketCloseListener;
import github.scarsz.discordsupportbot.listeners.DiscordSupportTicketCreationListener;
import lombok.Getter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DiscordSupportBot {

    @Getter private File jarFolder;
    @Getter private File guildConfigurationsFile;

    @Getter private static DiscordSupportBot discordSupportBot;
    @Getter private JDA jda;
    @Getter private List<GuildInfo> registeredGuilds = new ArrayList<>();

    public DiscordSupportBot(String botToken) {
        DiscordSupportBot.discordSupportBot = this;

        try {
            jarFolder = new File(DiscordSupportBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(2);
        }
        guildConfigurationsFile = new File(jarFolder, "guilds.json");

        System.out.print("Hooking shutdown... ");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (getJda() != null) jda.shutdown();

            if (registeredGuilds.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (GuildInfo registeredGuild : registeredGuilds) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("authorCanCloseTicket", registeredGuild.isAuthorCanCloseTicket());
                    jsonObject.put("defaultReactionEmoji", registeredGuild.getDefaultReactionEmoji());
                    jsonObject.put("firstMessageChannelId", registeredGuild.getFirstMessageChannelId());
                    jsonObject.put("pmTranscriptsOnClose", registeredGuild.isPmTranscriptsOnClose());
                    jsonObject.put("hoursUntilChannelTimeout", registeredGuild.getHoursUntilChannelTimeout());
                    jsonObject.put("rolesAllowedToCloseTickets", registeredGuild.getRolesAllowedToCloseTickets());
                    jsonObject.put("secondsUntilTicketCloses", registeredGuild.getSecondsUntilTicketCloses());
                    jsonObject.put("maxOpenTickets", registeredGuild.getMaxOpenTickets());
                    jsonObject.put("guildId", registeredGuild.getGuildId());

                    jsonArray.put(jsonObject);
                }
                try {
                    FileUtils.writeStringToFile(guildConfigurationsFile, jsonArray.toString(), Charset.forName("UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        System.out.println("done");

        System.out.print("Loading guild configurations... ");
        if (guildConfigurationsFile.exists()) {
            try {
                long startTime = System.currentTimeMillis();
                JSONArray jsonArray = new JSONArray(FileUtils.readFileToString(guildConfigurationsFile, Charset.forName("UTF-8")));
                for (Object object : jsonArray) {
                    JSONObject jsonObject = (JSONObject) object;
                    HashMap<String, Object> data = new HashMap<String, Object>() {{
                        String[] keys = {"pmTranscriptsOnClose", "firstMessageChannelId", "defaultReactionEmoji",
                        "authorCanCloseTicket", "secondsUntilTicketCloses", "rolesAllowedToCloseTickets", "hoursUntilChannelTimeout",
                        "maxOpenTickets", "guildId"};
                        for (String key : keys) if (jsonObject.has(key)) put(key, jsonObject.get(key));
                    }};
                    data.values().removeIf(Objects::isNull);
                    registeredGuilds.add(new GuildInfo(data));
                }
                System.out.println("finished in " + (System.currentTimeMillis() - startTime) + "ms: " + registeredGuilds.size() + " guilds loaded");
            } catch (IOException e) {
                System.out.println("failed: I/O exception occurred while reading file");
                e.printStackTrace();
            }
        } else {
            System.out.println("failed: guild configuration file didn't already exist");
        }

        System.out.println("Logging in to Discord...");
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setAudioEnabled(false)
                    .setAutoReconnect(true)
                    .setBulkDeleteSplittingEnabled(false)
                    .setGame(Game.of("with tickets"))
                    .setToken(botToken)
                    .buildBlocking();
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            System.out.println("Failed to login to Discord: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
        System.out.println("Logged in as " + jda.getSelfUser());

        jda.addEventListener(new DiscordSetupListener());
        jda.addEventListener(new DiscordSupportTicketCreationListener());
        jda.addEventListener(new DiscordSupportTicketCloseListener());
    }

    public static DiscordSupportBot get() {
        return DiscordSupportBot.discordSupportBot;
    }
    public static boolean isSetUp(Guild guild) {
        for (GuildInfo guildInfo : get().getRegisteredGuilds()) {
            if (!guildInfo.getGuildId().equals(guild.getId())) continue;
            return get().getJda().getTextChannelById(guildInfo.getFirstMessageChannelId()) != null;
        }
        return false;
    }
    public static GuildInfo getGuildInfo(Guild guild) {
        for (GuildInfo guildInfo : get().getRegisteredGuilds()) {
            if (guildInfo.getGuildId().equals(guild.getId())) return guildInfo;
        }
        return null;
    }

}

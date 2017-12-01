package github.scarsz.discordsupportbot;

import github.scarsz.discordsupportbot.thread.ShutdownHookThread;
import github.scarsz.discordsupportbot.util.ConversionUtil;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class DiscordSupportBot {

    private final Set<GuildConfig> guildConfigs = new HashSet<>();
    private final File guildConfigurationFile;
    private final File jarFolder;
    private final JDA jda;

    public DiscordSupportBot(String botToken) throws Exception {
        jarFolder = new File(DiscordSupportBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        guildConfigurationFile = new File(jarFolder, "guilds.json");

        // hook shutdown to save guild file
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(this));
        } catch (Exception e) {
            System.err.println("Failed to add shutdown hook, not starting");
            System.exit(2);
        }

        // load guild configs from file
        String fileSource = FileUtils.readFileToString(guildConfigurationFile, Charset.forName("UTF-8"));
        ConversionUtil.convertLegacyGuilds(fileSource);
        //TODO

        jda = new JDABuilder(AccountType.BOT)
                .setToken(botToken)
                .setAudioEnabled(false)
                .buildBlocking();
    }

}

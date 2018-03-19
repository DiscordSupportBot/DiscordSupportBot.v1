package github.scarsz.discordsupportbot;

import github.scarsz.discordsupportbot.thread.ShutdownHookThread;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class SupportBot {

    public static final JSONParser JSON_PARSER = new JSONParser();

    private final Map<String, GuildConfig> guildConfigs = new HashMap<>();
    private final File guildConfigurationFile;
    private final File jarFolder;
    private final JDA jda;

    public SupportBot(String botToken) throws Exception {
        jarFolder = new File(SupportBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        guildConfigurationFile = new File(jarFolder, "guilds.json");

        // hook shutdown to save guild file
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(this));
        } catch (Exception e) {
            System.err.println("Failed to add shutdown hook, not starting");
            System.exit(2);
        }

        // load guild configs from file
        if (guildConfigurationFile.exists()) {
            String fileSource = FileUtils.readFileToString(guildConfigurationFile, Charset.forName("UTF-8"));
            JSONObject configJson = (JSONObject) JSON_PARSER.parse(fileSource);
            for (Object o : configJson.entrySet()) {
                Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) o;
                guildConfigs.put(entry.getKey(), GuildConfig.from(this, entry.getValue()));
            }
        }

        jda = new JDABuilder(AccountType.BOT)
                .setToken(botToken)
                .setAudioEnabled(false)
                .buildBlocking();
    }



    public JDA getJda() {
        return jda;
    }

}

package github.scarsz.discordsupportbot;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;

import java.util.*;
import java.util.stream.Collectors;

public class GuildInfo {

    @Getter @Setter private boolean authorCanCloseTicket;
    @Getter @Setter private String defaultReactionEmoji;
    @Getter @Setter private String firstMessageChannelId;
    @Getter @Setter private boolean pmTranscriptsOnClose;
    @Getter @Setter private int hoursUntilChannelTimeout;
    @Getter @Setter private List<String> rolesAllowedToCloseTickets;
    @Getter @Setter private int secondsUntilTicketCloses;
    @Getter @Setter private int maxOpenTickets;
    @Getter @Setter private String guildId;

    public GuildInfo(boolean pmTranscriptsOnClose, String firstMessageChannelId, String defaultReactionEmoji, boolean authorCanCloseTicket, int secondsUntilTicketCloses, String rolesAllowedToCloseTickets, int hoursUntilChannelTimeout, int maxOpenTickets, String guildId) {
        this(pmTranscriptsOnClose, firstMessageChannelId, defaultReactionEmoji, authorCanCloseTicket, secondsUntilTicketCloses, rolesAllowedToCloseTickets.split(","), hoursUntilChannelTimeout, maxOpenTickets, guildId);
    }
    public GuildInfo(boolean pmTranscriptsOnClose, String firstMessageChannelId, String defaultReactionEmoji, boolean authorCanCloseTicket, int secondsUntilTicketCloses, String[] rolesAllowedToCloseTickets, int hoursUntilChannelTimeout, int maxOpenTickets, String guildId) {
        this.pmTranscriptsOnClose = pmTranscriptsOnClose;
        this.firstMessageChannelId = firstMessageChannelId;
        this.defaultReactionEmoji = defaultReactionEmoji;
        this.authorCanCloseTicket = authorCanCloseTicket;
        this.secondsUntilTicketCloses = secondsUntilTicketCloses;
        this.rolesAllowedToCloseTickets = Arrays.asList(rolesAllowedToCloseTickets);
        this.hoursUntilChannelTimeout = hoursUntilChannelTimeout;
        this.maxOpenTickets = maxOpenTickets;
        this.guildId = guildId;
    }
    public GuildInfo() {
        this(new HashMap<>(0));
    }
    public GuildInfo(Map<String, Object> data) {
        this(
                (boolean) data.getOrDefault("pmTranscriptsOnClose", true),
                (String) data.getOrDefault("firstMessageChannelId", "0"),
                (String) data.getOrDefault("defaultReactionEmoji", "✅"),
                (boolean) data.getOrDefault("authorCanCloseTicket", true),
                (int) data.getOrDefault("secondsUntilTicketCloses", 60),
                ((JSONArray) data.getOrDefault("rolesAllowedToCloseTickets", new JSONArray())).toList().toArray(new String[0]),
                (int) data.getOrDefault("hoursUntilChannelTimeout", 168),
                (int) data.getOrDefault("maxOpenTickets", 0),
                (String) data.getOrDefault("guildId", null)
        );
    }

    public TextChannel getFirstMessageChannel() {
        return DiscordSupportBot.get().getJda().getTextChannelById(firstMessageChannelId);
    }

    public boolean isSetUp() {
        return DiscordSupportBot.get().getJda().getTextChannelById(firstMessageChannelId) != null;
    }

    public List<TextChannel> getActiveTickets() {
        return isSetUp()
                ? getFirstMessageChannel().getGuild().getTextChannels().stream()
                    .filter(textChannel -> textChannel.getName().startsWith("support-") && StringUtils.isNumeric(textChannel.getName().replace("support-", "")) && DiscordSupportBot.get().getJda().getUserById(textChannel.getName().replace("support-", "")) != null)
                    .collect(Collectors.toList())
                : new ArrayList<>();
    }

}

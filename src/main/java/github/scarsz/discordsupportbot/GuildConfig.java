package github.scarsz.discordsupportbot;

import github.scarsz.discordsupportbot.util.Emoji;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.simple.JSONObject;

public class GuildConfig {

    private final SupportBot bot;
    private boolean authorCanCloseTicket;
    private String defaultReactionEmoji;
    private TextChannel firstMessageChannel;
    private boolean pmTranscriptsOnClose;
    private int maxOpenTickets;
    private String startingMessage;
    private Role ticketAdminRole;

    public GuildConfig(SupportBot bot, boolean authorCanCloseTicket, String defaultReactionEmoji, String firstMessageChannel, boolean pmTranscriptsOnClose, int maxOpenTickets, String startingMessage, String ticketAdminRole) {
        this.bot = bot;
        this.authorCanCloseTicket = authorCanCloseTicket;
        this.defaultReactionEmoji = defaultReactionEmoji;
        this.firstMessageChannel = bot.getJda().getTextChannelById(firstMessageChannel);
        this.pmTranscriptsOnClose = pmTranscriptsOnClose;
        this.maxOpenTickets = maxOpenTickets;
        this.startingMessage = startingMessage;
        this.ticketAdminRole = bot.getJda().getRoleById(ticketAdminRole);
    }
    public static GuildConfig from(SupportBot bot, JSONObject json) {
        return new GuildConfig(
                bot,
                (boolean) json.getOrDefault(ConfigKey.AUTHOR_CAN_CLOSE_TICKET.getId(), true),
                (String) json.getOrDefault(ConfigKey.DEFAULT_REACTION_EMOJI.getId(), Emoji.WhiteCheckMark),
                (String) json.getOrDefault(ConfigKey.TRIGGER_CHANNEL_ID.getId(), null),
                (boolean) json.getOrDefault(ConfigKey.PM_TRANSCRIPT.getId(), true),
                (int) json.getOrDefault(ConfigKey.MAX_OPEN_TICKETS.getId(), 0),
                (String) json.getOrDefault(ConfigKey.STARTING_MESSAGE.getId(), "**__Author:__** %author%\n**__Message:__** %message%"),
                (String) json.getOrDefault(ConfigKey.TICKET_ADMIN_ROLE.getId(), null)
        );
    }

    public JSONObject serialize() {
        JSONObject object = new JSONObject();
        object.put(ConfigKey.AUTHOR_CAN_CLOSE_TICKET.getId(), this.authorCanCloseTicket);
        object.put(ConfigKey.DEFAULT_REACTION_EMOJI.getId(), this.defaultReactionEmoji);
        object.put(ConfigKey.TRIGGER_CHANNEL_ID.getId(), this.firstMessageChannel.getId());
        object.put(ConfigKey.PM_TRANSCRIPT.getId(), this.pmTranscriptsOnClose);
        object.put(ConfigKey.MAX_OPEN_TICKETS.getId(), this.maxOpenTickets);
        object.put(ConfigKey.STARTING_MESSAGE.getId(), this.startingMessage);
        object.put(ConfigKey.TICKET_ADMIN_ROLE.getId(), this.ticketAdminRole.getId());
        return object;
    }

    enum ConfigKey {

        AUTHOR_CAN_CLOSE_TICKET(1),
        DEFAULT_REACTION_EMOJI(2),
        TRIGGER_CHANNEL_ID(3),
        PM_TRANSCRIPT(4),
        MAX_OPEN_TICKETS(5),
        STARTING_MESSAGE(6),
        TICKET_ADMIN_ROLE(7);

        private final int id;

        ConfigKey(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    }

}

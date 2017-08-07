package github.scarsz.discordsupportbot.listeners;

import github.scarsz.discordsupportbot.DiscordSupportBot;
import github.scarsz.discordsupportbot.DiscordUtil;
import github.scarsz.discordsupportbot.GuildInfo;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordSetupListener extends ListenerAdapter {

    private List<String> guildsBeingSetup = new ArrayList<>();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        GuildInfo guildInfo = DiscordSupportBot.getGuildInfo(event.getGuild());
        if (guildInfo == null || !guildInfo.isSetUp()) {
            Thread setupThread = new Thread(() -> handleSetup(event));
            setupThread.setName("Setup thread - " + event.getGuild().getId() + " by " + event.getAuthor().getId());
            setupThread.start();
        }
    }

    public void handleSetup(GuildMessageReceivedEvent event) {
        if (guildsBeingSetup.contains(event.getGuild().getId())) return;
        if (event.getMember() == null || event.getMember.isBot() || !event.getMember().isOwner()) return;

        GuildInfo guildInfo = DiscordSupportBot.getGuildInfo(event.getGuild());
        DiscordSupportBot.get().getRegisteredGuilds().remove(guildInfo);

        if ((guildInfo != null && guildInfo.isSetUp()) || !event.getMessage().getRawContent().startsWith("...")) return;

        System.out.println("Setting up " + event.getGuild());
        guildsBeingSetup.add(event.getGuild().getId());

        boolean pmTranscriptsOnClose = DiscordUtil.pullYesOrNo(event.getChannel().sendMessage("PM chat transcript to all participants of a given ticket when it is closed? (Suggested: ✅)").complete(), event.getAuthor());
        boolean authorCanCloseTicket = DiscordUtil.pullYesOrNo(event.getChannel().sendMessage("Author can close their tickets themselves? (otherwise only approved ticket-closers) (Suggested: ✅)").complete(), event.getAuthor());
        String defaultReactionEmoji = DiscordUtil.pullGuildMessageReactionAddEvent(event.getChannel().sendMessage("React to this message to set the default emoji to react with for the first message in a ticket. (Suggested: ✅) ***__NOTE:__ CUSTOM EMOJIS NOT SUPPORTED CURRENTLY***").complete(), event.getAuthor()).getReactionEmote().getName();
        int secondsUntilTicketCloses = DiscordUtil.pullInteger(event.getChannel().sendMessage("Seconds until a ticket's text channel is deleted after the ticket being marked as solved? (Suggested: 60)").complete().getTextChannel(), event.getAuthor());
        int hoursUntilChannelTimeout = DiscordUtil.pullInteger(event.getChannel().sendMessage("How many hours can go by since the last message in a channel before the channel is deleted due to inactivity? (Suggested: 72 [3 days])").complete().getTextChannel(), event.getAuthor());
        int maxOpenTickets = DiscordUtil.pullInteger(event.getChannel().sendMessage("How many tickets can be open in this server at any given time? (Suggested: 0 [unlimited])").complete().getTextChannel(), event.getAuthor());
        String[] rolesAllowedToCloseTickets = new String[0];
        while (rolesAllowedToCloseTickets.length == 0) {
            event.getChannel().sendMessage("Enter a comma separated list of roles in this server that are able to close tickets. (Available: `" + event.getGuild().getRoles().stream().map(Role::getName).filter(s -> !s.equals("@everyone")).collect(Collectors.joining(",")) + "`)").complete();
            rolesAllowedToCloseTickets = Arrays.stream(DiscordUtil.pullGuildMessageReceivedEvent(event.getAuthor()).getMessage().getRawContent().split(","))
                    .map(s -> DiscordUtil.getRoleByNameFromGuild(event.getGuild(), s))
                    .filter(Objects::nonNull)
                    .map(ISnowflake::getId)
                    .toArray(String[]::new);
        }

        guildInfo = new GuildInfo(pmTranscriptsOnClose, event.getChannel().getId(), defaultReactionEmoji, authorCanCloseTicket, secondsUntilTicketCloses, rolesAllowedToCloseTickets, hoursUntilChannelTimeout, maxOpenTickets, event.getGuild().getId());
        DiscordSupportBot.get().getRegisteredGuilds().add(guildInfo);

        event.getChannel().deleteMessages(event.getChannel().getHistory().retrievePast(100).complete()).complete();
        event.getChannel().sendMessage("This channel is now set up to accept support ticket requests by people sending messages here. Take the time to send the channel's first message instructing users on how to request support (hint: saying messages here). To re-setup your support system, recreate this channel and type `...`. Note that support channels do not get made for you as you're the guild owner so you can send messages here freely.").queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));

        guildsBeingSetup.remove(event.getGuild().getId());
    }

}

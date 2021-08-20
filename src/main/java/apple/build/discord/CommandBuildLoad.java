package apple.build.discord;

import apple.build.query.QuerySavingService;
import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterSingle;
import apple.discord.acd.reaction.DiscordEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBuildLoad extends ACDCommand {
    public CommandBuildLoad(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "load")
    public void onLoad(MessageReceivedEvent event, @ParameterSingle(usage = "build_id") String buildId) {
        event.getMessage().addReaction(DiscordEmoji.WORKING.getEmoji()).queue();
        if (!QuerySavingService.queue(buildId, (query) -> {
            query.toQueryMessage(acd, event.getMember(), event.getChannel()).makeFirstMessage();
            event.getMessage().removeReaction(DiscordEmoji.WORKING.getEmoji(), acd.getSelfUser()).queue();
        })) {
            event.getChannel().sendMessage("That build id does not exist.").queue();
            event.getMessage().removeReaction(DiscordEmoji.WORKING.getEmoji(), acd.getSelfUser()).queue();
        }
    }
}

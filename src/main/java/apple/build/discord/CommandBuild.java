package apple.build.discord;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBuild extends ACDCommand {
    private ACD acd;

    public CommandBuild(ACD acd) {
        super(acd);
        this.acd = acd;
    }

    @DiscordCommandAlias(alias = "build")
    public void build(MessageReceivedEvent event) {
        final BuildQueryMessage buildMessage = new BuildQueryMessage(acd, event.getMember(), event.getChannel());
        buildMessage.makeFirstMessage();
    }
}

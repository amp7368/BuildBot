package apple.build.discord.build;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBuild extends ACDCommand {
    private final ACD acd;

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

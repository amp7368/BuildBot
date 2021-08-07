package apple.build.discord.commands;

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
        final BuildMessage buildMessage = new BuildMessage(acd, event.getChannel());
        buildMessage.makeFirstMessage();
    }
}

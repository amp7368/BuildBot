package apple.build.discord.commands;

import apple.build.discord.reactions.BuilderMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBuild {
    public static void dealWithCommand(MessageReceivedEvent event) {
        new BuilderMessage();
    }
}

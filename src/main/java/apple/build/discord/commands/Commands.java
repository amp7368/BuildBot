package apple.build.discord.commands;

import apple.build.discord.DiscordBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.Consumer;

public enum Commands {
    HELP("build", CommandBuild::dealWithCommand);

    private final Consumer<MessageReceivedEvent> method;
    private final String commandName;

    Commands(String commandName, Consumer<MessageReceivedEvent> method) {
        this.method = method;
        this.commandName = commandName;
    }

    public boolean isCommand(String msg) {
        return msg.toLowerCase().startsWith(DiscordBot.PREFIX + commandName);
    }

    public void dealWithCommand(MessageReceivedEvent event) {
        method.accept(event);
    }
}

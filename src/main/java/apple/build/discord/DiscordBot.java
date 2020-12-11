package apple.build.discord;

import apple.build.BuildMain;
import apple.build.discord.commands.Commands;
import apple.build.discord.reactions.AllReactables;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public static final String PREFIX = "b!";
    public static final long APPLEPTR16 = 253646208084475904L;
    public static final int BOT_COLOR = 0x4e80f7;

    public static String discordToken; // my bot
    public static JDA client;

    public DiscordBot() throws LoginException {
        List<String> list = Arrays.asList(BuildMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        String BOT_TOKEN_FILE_PATH = String.join("/", list.subList(0, list.size() - 1)) + "/config/discordToken.data";
        File file = new File(BOT_TOKEN_FILE_PATH);
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException ignored) {
            }
            System.err.println("Please fill in the token for the discord bot in '" + BOT_TOKEN_FILE_PATH + "'");
            System.exit(1);
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            discordToken = reader.readLine();
            reader.close();
        } catch (IOException e) {
            System.err.println("Please fill in the token for the discord bot in '" + BOT_TOKEN_FILE_PATH + "'");
            System.exit(1);
        }
        enableDiscord();
    }

    private void enableDiscord() throws LoginException {
        JDABuilder builder = JDABuilder.create(discordToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        builder.addEventListeners(this);
        client = builder.build();
//        client.getPresence().setPresence(Activity.playing("b!help"), true);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;
        // the author is not a bot

        if (event.getChannelType() != ChannelType.TEXT) return;

        String messageContent = event.getMessage().getContentStripped().toLowerCase();
        try {
            // deal with the different commands
            for (Commands command : Commands.values()) {
                if (command.isCommand(messageContent)) {
                    command.dealWithCommand(event);
                    return;
                }
            }
        } catch (InsufficientPermissionException e) {
            SendLogs.sendLogs(Collections.singletonList(e.getGuild(client).getName() + " did not give me the perms: " + e.getPermission().getName()));
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null || user.isBot()) {
            return;
        }
        AllReactables.dealWithReaction(event);
    }
}

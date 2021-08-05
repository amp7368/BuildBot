package apple.build.discord;

import apple.build.BuildMain;
import apple.build.discord.commands.CommandBuild;
import apple.discord.acd.ACD;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
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
    private static ACD ACD;
    public static Guild HOME_GUILD = null;

    public static String discordToken; // my bot
    public static JDA client;

    static {
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
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            discordToken = reader.readLine();
            reader.close();
        } catch (IOException e) {
            System.err.println("Please fill in the token for the discord bot in '" + BOT_TOKEN_FILE_PATH + "'");
            System.exit(1);
        }
    }

    public DiscordBot() throws LoginException {
        JDABuilder builder = JDABuilder.create(discordToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        builder.addEventListeners(this);
        builder.setToken(discordToken);
        client = builder.build();
        ACD = new ACD(PREFIX, client);
        HOME_GUILD = client.getGuildById(603039156892860417L);
        client.getPresence().setPresence(Activity.playing("b!help"), true);
        new CommandBuild(ACD);
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        System.out.println("whoa");
    }

    @Override
    public void onSelectionMenu(@Nonnull SelectionMenuEvent event) {
        System.out.println("spooky");
    }
}

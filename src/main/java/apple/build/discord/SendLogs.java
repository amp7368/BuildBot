package apple.build.discord;

import apple.build.BuildMain;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SendLogs {
    private static long SENDER;

    static {
        List<String> list = Arrays.asList(BuildMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        String path = String.join("/", list.subList(0, list.size() - 1)) + "/config/logSender.data";
        File file = new File(path);
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException ignored) {
            }
            System.err.println("Please fill in who to send logs to in '" + path + "'");
            System.exit(1);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            SENDER = Long.parseLong(reader.readLine());
            reader.close();
        } catch (IOException e) {
            System.err.println("Please fill in who to send logs to in '" + path + "'");
            System.exit(1);
        }
    }

    public static void sendLogs(List<String> logs) {
        MessageChannel dms = DiscordBot.client.getTextChannelById(SENDER);
        if (dms == null) {
            System.err.println("Dms is null for sendLogs");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (String log : logs) {
            if (log.length() + builder.length() > 1999) {
                dms.sendMessage(builder.toString()).queue();
                builder = new StringBuilder();
            }
            builder.append(log);
            builder.append('\n');
        }
        if (builder.length() != 0)
            dms.sendMessage(builder.toString()).queue();
    }
}
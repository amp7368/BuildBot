package apple.build.discord.saved;

import apple.build.discord.DiscordPermissions;
import apple.build.query.QuerySavingService;
import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterSingle;
import apple.discord.acd.parameters.ParameterVargs;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSaveAsPreset extends ACDCommand {
    public CommandSaveAsPreset(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "save default", overlappingCommands = "save", order = 1, permission = DiscordPermissions.BUILD_ADMIN)
    public void adminSave(MessageReceivedEvent event,
                          @ParameterSingle(usage = "uuid") String buildUUID,
                          @ParameterVargs(usage = "name", nonEmpty = true) String buildName) {
        QuerySavingService.getDefaultPresets().putAndSave(buildName, buildUUID);
        event.getChannel().sendMessage(String.format("I saved %s as default/%s", buildUUID, buildName)).queue();
    }

    @DiscordCommandAlias(alias = "save", overlappingCommands = "save", order = 2)
    public void save(MessageReceivedEvent event,
                     @ParameterSingle(usage = "uuid") String buildUUID,
                     @ParameterVargs(usage = "name", nonEmpty = true) String buildName) {
        QuerySavingService.getPresets(event.getAuthor().getIdLong(), (preset) -> preset.putAndSave(buildName, buildUUID));
        event.getChannel().sendMessage(String.format("I saved %s as %s under your presets", buildUUID, buildName)).queue();
    }
}

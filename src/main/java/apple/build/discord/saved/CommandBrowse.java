package apple.build.discord.saved;

import apple.build.query.Preset;
import apple.build.query.QuerySavingService;
import apple.build.utils.Pair;
import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import apple.discord.acd.parameters.ParameterSingle;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandBrowse extends ACDCommand {
    public CommandBrowse(ACD acd) {
        super(acd);
    }

    @DiscordCommandAlias(alias = "presets", overlappingCommands = "presets", order = 1, channelType = ChannelType.TEXT)
    public void listBuilds(MessageReceivedEvent event, @ParameterSingle(usage = "name") String filter) {
        new ListPresetsMessage(acd, event.getMember(), event.getChannel(), QuerySavingService.getDefaultPresets().getPresets(filter)).makeFirstMessage();
    }

    @DiscordCommandAlias(alias = "presets", overlappingCommands = "presets", order = 2, channelType = ChannelType.TEXT)
    public void listBuilds(MessageReceivedEvent event) {
        new ListPresetsMessage(acd, event.getMember(), event.getChannel(), QuerySavingService.getDefaultPresets().getAll()).makeFirstMessage();
    }

    @DiscordCommandAlias(alias = "mypresets", overlappingCommands = "mypresets", order = 1, channelType = ChannelType.TEXT)
    public void listUserBuilds(MessageReceivedEvent event, @ParameterSingle(usage = "name") String filter) {
        QuerySavingService.getPresets(event.getAuthor().getIdLong(), (presets) ->
                new ListPresetsMessage(acd, event.getMember(), event.getChannel(), presets.getPresets(filter)).makeFirstMessage()
        );
    }

    @DiscordCommandAlias(alias = "mypresets", overlappingCommands = "mypresets", order = 2, channelType = ChannelType.TEXT)
    public void listUserBuilds(MessageReceivedEvent event) {
        QuerySavingService.getPresets(event.getAuthor().getIdLong(), (presets) ->
                new ListPresetsMessage(acd, event.getMember(), event.getChannel(), presets.getAll()).makeFirstMessage()
        );
    }
}

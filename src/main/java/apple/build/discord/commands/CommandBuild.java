package apple.build.discord.commands;

import apple.discord.acd.ACD;
import apple.discord.acd.command.ACDCommand;
import apple.discord.acd.command.DiscordCommandAlias;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandBuild extends ACDCommand {
    private ACD acd;

    public CommandBuild(ACD acd) {
        super(acd);
        this.acd = acd;
    }

    @DiscordCommandAlias(alias = "build")
    public void build(MessageReceivedEvent event) {
        new BuildMessage(acd, event.getChannel());
        MessageBuilder message = new MessageBuilder();
        List<ActionRow> rows = new ArrayList<>();
        ButtonImpl button = new ButtonImpl("test1", "label", ButtonStyle.DANGER, false, Emoji.fromUnicode("\uD83D\uDE80"));
        rows.add(ActionRow.of(button, button, button, button));
        rows.add(ActionRow.of(button, button, button, button));
        rows.add(ActionRow.of(new SelectionMenuImpl("test1", "placeholder", 1, 5, false,
                List.of(
                        SelectOption.of("label15", "value1"),
                        SelectOption.of("label14", "value2"),
                        SelectOption.of("label1", "value3"),
                        SelectOption.of("label2", "value4"),
                        SelectOption.of("label3", "value5"),
                        SelectOption.of("label4", "value6")
                )
        )));
        rows.add(ActionRow.of(button, button, button, button));
        message.setActionRows(rows);
        message.setContent("hello");
        event.getChannel().sendMessage(message.build()).queue();
    }
}

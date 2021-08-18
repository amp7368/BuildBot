package apple.build.discord;

import apple.build.search.Build;
import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class BuildShowListMessage extends ACDGuiPageable {
    private BuildQueryMessage parent;

    public BuildShowListMessage(ACD acd, Message msg, @NotNull Collection<Build> buildsAll, BuildQueryMessage parent) {
        super(acd, msg, parent);
        this.parent = parent;
        for (Build build : buildsAll) {
            addPage((Supplier<BuildTotalMessage>) () -> new BuildTotalMessage(acd, message, build, this));
        }
    }

    @Override
    protected Message emptyPage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("There were no builds generated");
        messageBuilder.setEmbeds(embed.build());
        messageBuilder.setActionRows(List.of(
                ActionRow.of(
                        getBackButton(),
                        getForwardButton()
                ),
                ActionRow.of(
                        getRestartEditButton()
                )
        ));
        return messageBuilder.build();
    }

    @Override
    protected Collection<ActionRow> getNavigationRow() {
        return null;
    }

    private ButtonImpl getRestartEditButton() {
        addManualSimpleButton((e) -> this.resetEdit(), "resetEdit2");
        return new ButtonImpl("resetEdit2", "Edit Query", ButtonStyle.PRIMARY, false, null);
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    public void resetEdit() {
        parent.resetEdit();
    }
}

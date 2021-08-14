package apple.build.discord;

import apple.build.search.Build;
import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class BuildShowListMessage extends ACDGuiPageable<BuildTotalMessage> {
    public BuildShowListMessage(ACD acd, Message msg, @NotNull Collection<Build> buildsAll) {
        super(acd, msg);
        for (Build build : buildsAll) {
            addPage(() -> new BuildTotalMessage(acd, message, build));
        }
    }
    @Override
    protected ButtonImpl getBackButton() {
        addManualButton(this::back, "back2");
        return new ButtonImpl("back2", "Prev Build", ButtonStyle.DANGER, false, null);
    }

    @Override
    protected ButtonImpl getForwardButton() {
        addManualButton(this::forward, "next2");
        return new ButtonImpl("next2", "Next Build", ButtonStyle.SUCCESS, false, null);
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }
}

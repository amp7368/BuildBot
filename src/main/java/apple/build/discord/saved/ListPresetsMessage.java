package apple.build.discord.saved;

import apple.build.discord.build.BuildQueryMessage;
import apple.build.query.Preset;
import apple.build.query.QuerySavingService;
import apple.build.utils.Pair;
import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDEntryPage;
import apple.discord.acd.reaction.gui.ACDGuiEntryList;
import apple.discord.acd.reaction.gui.GuiEntryNumbered;
import apple.discord.acd.reaction.gui.GuiEntryStringable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ListPresetsMessage extends ACDGuiEntryList {
    private final Member author;

    public ListPresetsMessage(ACD acd, Member member, MessageChannel channel, List<Pair<String, Preset>> presets) {
        super(acd, channel);
        this.author = member;
        addPage(new ListPresetsPage(this, presets.stream().map(PresetEntryStringable::new).collect(Collectors.toList())));
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    private Member getAuthor() {
        return author;
    }

    private static class ListPresetsPage extends ACDEntryPage<PresetEntryStringable> {
        private final ListPresetsMessage gui;
        private String extraMessage = null;

        public ListPresetsPage(ListPresetsMessage gui, List<PresetEntryStringable> entries) {
            super(gui, PresetEntryStringable::compareTo, 5);
            this.gui = gui;
            addAllEntry(entries);
        }

        @Override
        protected Message asMessage(List<GuiEntryNumbered<PresetEntryStringable>> entriesThisPage) {
            MessageBuilder messageBuilder = new MessageBuilder();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            StringBuilder content = new StringBuilder();
            Collection<ButtonImpl> presetButtons = new ArrayList<>();
            int i = 0;
            for (GuiEntryNumbered<PresetEntryStringable> entry : entriesThisPage) {
                if (i != 0) content.append("\n");
                content.append(entry.asString(i++));
                final String presetName = entry.entry().name();
                presetButtons.add(new ButtonImpl(presetName, presetName, ButtonStyle.PRIMARY, false, null));
                getGui().addManualButton((event) -> {
                    extraMessage = "I'm working on getting that preset";
                    gui.editAsReply(event);
                    if (!QuerySavingService.queue(entry.entry().entry.getValue().name, (query) -> {
                        extraMessage = null;
                        new BuildQueryMessage(gui.acd, gui.getAuthor(), gui.message.getChannel(), query).makeFirstMessage();
                    })) {
                        // the file doesn't exist, so tell them
                        gui.message.getChannel().sendMessage("That query doesn't exist").queue();
                    }
                }, presetName);
            }
            embedBuilder.setTitle("Presets Page (" + (getPageNumber() + 1) + ")");
            if (extraMessage != null)
                embedBuilder.setAuthor(extraMessage);
            embedBuilder.setDescription(content);
            messageBuilder.setEmbeds(embedBuilder.build());
            if (!presetButtons.isEmpty())
                messageBuilder.setActionRows(ActionRow.of(presetButtons));
            return messageBuilder.build();
        }
    }

    private record PresetEntryStringable(
            Pair<String, Preset> entry) implements GuiEntryStringable, Comparable<PresetEntryStringable> {

        @Override
        public String asEntryString(int indexInPage, int indexInList) {
            return String.format("%d. **%s** === **%s**", indexInList + 1, entry.getKey(), entry.getValue().toString());
        }

        @Override
        public int compareTo(@NotNull ListPresetsMessage.PresetEntryStringable o) {
            return entry.getKey().compareToIgnoreCase(o.entry.getKey());
        }

        public String name() {
            return entry.getKey();
        }
    }
}

package apple.build.discord;

import apple.build.search.Build;
import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;
import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BuildTotalMessage extends ACDGuiPageable {
    private final Build build;
    private final BuildShowListMessage mainGui;
    private String queryId;

    public BuildTotalMessage(ACD acd, Message msg, Build build, BuildShowListMessage mainGui, @Nullable String queryId) {
        super(acd, msg, mainGui);
        this.build = build;
        this.mainGui = mainGui;
        this.queryId = queryId;
        this.addPage(this::items);
    }

    private Message items() {
        MessageBuilder messageBuilder = new MessageBuilder();
        List<Item> items = new ArrayList<>(build.items);
        EmbedBuilder embed = new EmbedBuilder();
        if (queryId != null) {
            embed.setAuthor("Query id: " + queryId);
        }
        embed.setTitle(String.format("Items ( %d / %d )", (mainGui.getPageIndex() + 1), mainGui.size()));
        for (Item.ItemType desiredType : Arrays.asList(
                Item.ItemType.HELMET,
                Item.ItemType.CHESTPLATE,
                null,
                Item.ItemType.LEGGINGS,
                Item.ItemType.BOOTS,
                null,
                Item.ItemType.RING,
                Item.ItemType.RING,
                null,
                Item.ItemType.BRACELET,
                Item.ItemType.NECKLACE,
                null
        )) {
            if (desiredType == null) embed.addBlankField(true);
            for (Item item : items) {
                if (item.type == desiredType) {
                    embed.addField(Pretty.uppercaseFirst(item.type.name()), item.name, true);
                    items.remove(item);
                    break;
                }
            }
        }
        for (Item item : items) {
            if (item.type.isWeapon()) {
                embed.addField(Pretty.uppercaseFirst(item.type.name()), item.name, true);
                items.remove(item);
                break;
            }
        }
        embed.setDescription(String.format("Wynnbuilder url: https://wynnbuilder.github.io/index.html#%s", build.encodeToWynnBuilder()));

        messageBuilder.setEmbeds(embed.build());
        return messageBuilder.build();
    }

    @Override
    protected Collection<ActionRow> getNavigationRow() {
        return List.of(
                ActionRow.of(
                        getBackButton(),
                        getForwardButton(),
                        getBackBuildButton(),
                        getForwardBuildButton()
                ),
                ActionRow.of(
                        getRestartEditButton()
                )
        );
    }

    private ButtonImpl getRestartEditButton() {
        addManualSimpleButton((e) -> mainGui.resetEdit(), "resetEdit");
        return new ButtonImpl("resetEdit", "Edit Query", ButtonStyle.PRIMARY, false, null);
    }

    protected ButtonImpl getBackBuildButton() {
        addManualButton(mainGui::back, "back2");
        return new ButtonImpl("back2", "Prev Build", ButtonStyle.SECONDARY, false, null);
    }

    protected ButtonImpl getForwardBuildButton() {
        addManualButton(mainGui::forward, "next2");
        return new ButtonImpl("next2", "Next Build", ButtonStyle.PRIMARY, false, null);
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }
}

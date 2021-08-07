package apple.build.discord.commands;

import apple.build.discord.DiscordBot;
import apple.build.search.enums.ElementSkill;
import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;
import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.buttons.GuiMenu;
import apple.discord.acd.reaction.gui.ACDGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class BuildMessage extends ACDGui {
    public static final String SUBMIT_BUTTON_ID = "submit";
    public static final String MAJOR_ID_SELECTION_ID = "major_id_sel";
    private static final String BACK_BUTTON_ID = "back";
    private static final String MAJOR_ID_CLEAR = "major_id_clear";
    private BuildPhase phase = BuildPhase.ELEMENTS;
    private final List<ElementSkill> elements = new ArrayList<>();
    private final List<String> majorIds = new ArrayList<>();
    private MessageEmbed error = null;

    public BuildMessage(ACD acd, MessageChannel channel) {
        super(acd, channel);
    }

    @Override
    protected void initButtons() {
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.MINUTE_15;
    }

    @Override
    protected Message makeMessage() {
        switch (phase) {
            case ELEMENTS:
                return makeElementsMessage();
            case MAJOR_ID:
                return makeMajorIdMessage();
            case DAMAGE:
                return makeDamageMessage();
        }
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setContent("hmm");
        messageBuilder.setEmbeds(new EmbedBuilder().setTitle("nope").build());
        return messageBuilder.build();
    }

    @GuiButton(id = SUBMIT_BUTTON_ID)
    public void submit(ComponentInteraction interaction) {
        switch (phase) {
            case ELEMENTS -> elementsSubmit(interaction);
            case MAJOR_ID -> majorIdSubmit(interaction);
        }
    }

    @GuiButton(id = ElementSkill.Constants.AIR_ID)
    public void addAir(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.AIR)) {
            this.elements.add(ElementSkill.AIR);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.WATER_ID)
    public void addWater(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.WATER)) {
            this.elements.add(ElementSkill.WATER);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.EARTH_ID)
    public void addEarth(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.EARTH)) {
            this.elements.add(ElementSkill.EARTH);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.THUNDER_ID)
    public void addThunder(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.THUNDER)) {
            this.elements.add(ElementSkill.THUNDER);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.FIRE_ID)
    public void addFire(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.FIRE)) {
            this.elements.add(ElementSkill.FIRE);
        }
        editAsReply(interaction);
    }

    @GuiMenu(id = MAJOR_ID_SELECTION_ID)
    public void majorIdMenu(SelectionMenuEvent interaction) {
        majorIds.addAll(interaction.getValues());
        editAsReply(interaction);
    }

    @GuiButton(id = MAJOR_ID_CLEAR)
    public void majorIdClear(ComponentInteraction interaction) {
        majorIds.clear();
        editAsReply(interaction);
    }

    public void elementsSubmit(ComponentInteraction interaction) {
        if (this.elements.size() > 3) {
            EmbedBuilder errorBuilder = new EmbedBuilder();
            errorBuilder.setTitle(DiscordEmoji.RED_X.getEmoji() + "  A maximum of 3 elements is enforced");
            errorBuilder.setColor(DiscordBot.RED_COLOR);
            error = errorBuilder.build();
        } else {
            phase = BuildPhase.MAJOR_ID;
        }
        editAsReply(interaction);
    }

    private void majorIdSubmit(ComponentInteraction interaction) {
        phase = BuildPhase.DAMAGE;
        editAsReply(interaction);
    }

    private Message makeElementsMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Select 3 elements");
        embed.setDescription("Select up to 3 elements that the build will be based off");
        if (error == null) {
            messageBuilder.setEmbeds(embed.build());
        } else {
            messageBuilder.setEmbeds(embed.build(), error);
        }
        Collection<ButtonImpl> elementButtons = new ArrayList<>();
        for (ElementSkill element : ElementSkill.values()) {
            elementButtons.add(
                    new ButtonImpl(
                            element.name(),
                            Pretty.uppercaseFirst(element.name()),
                            elements.contains(element) ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                            false,
                            null
                    )
            );
        }

        ActionRow navigationRow = ActionRow.of(new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null));

        messageBuilder.setActionRows(ActionRow.of(elementButtons), navigationRow);
        return messageBuilder.build();
    }

    private Message makeMajorIdMessage() {
        final MessageBuilder messageBuilder = new MessageBuilder();
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Select major ids");
        embed.setAuthor("Select all the major ids you require for your build");
        embed.setDescription(majorIds.stream().map(Pretty::uppercaseFirst).collect(Collectors.joining("\n")));
        messageBuilder.setEmbeds(embed.build());
        List<SelectOption> majorIdOptions = new ArrayList<>();
        List<String> majorIdsAll = new ArrayList<>(Item.majorIdsListAll);
        majorIdsAll.sort(String::compareTo);
        for (String majorId : majorIdsAll) {
            majorIdOptions.add(
                    SelectOption.of(
                            majorId, Pretty.uppercaseFirst(majorId)
                    )
            );
        }
        ActionRow majorIdSelection = ActionRow.of(
                new SelectionMenuImpl(
                        MAJOR_ID_SELECTION_ID,
                        "Major Ids",
                        1,
                        majorIdsAll.size(),
                        false,
                        majorIdOptions
                )
        );
        ActionRow navigationRow = ActionRow.of(
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null),
                new ButtonImpl(MAJOR_ID_CLEAR, "Clear", ButtonStyle.PRIMARY, false, null)
        );
        messageBuilder.setActionRows(majorIdSelection, navigationRow);
        return messageBuilder.build();
    }

    private Message makeDamageMessage() {
        return new MessageBuilder("incomplete").build();
    }

    private enum BuildPhase {
        ELEMENTS,
        MAJOR_ID,
        DAMAGE,
        ID
    }
}

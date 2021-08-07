package apple.build.discord.commands;

import apple.build.discord.DiscordBot;
import apple.build.search.enums.ElementSkill;
import apple.build.utils.Pretty;
import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.gui.ACDGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BuildMessage extends ACDGui {
    public static final String SUBMIT_BUTTON_ID = "submit";
    private BuildPhase phase = BuildPhase.ELEMENTS;
    private List<ElementSkill> elements = new ArrayList<>();
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
        }
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setContent("hmm");
        messageBuilder.setEmbeds(new EmbedBuilder().setTitle("nope").build());
        return messageBuilder.build();
    }

    @GuiButton(id = SUBMIT_BUTTON_ID)
    public void submit(ComponentInteraction interaction) {
        switch (phase) {
            case ELEMENTS:
                elementsSubmit(interaction);
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
        return null;
    }

    private enum BuildPhase {
        ELEMENTS,
        MAJOR_ID,
        DAMAGE,
        ID
    }
}

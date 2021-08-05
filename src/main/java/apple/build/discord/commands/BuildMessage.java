package apple.build.discord.commands;

import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.gui.ACDGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;


public class BuildMessage extends ACDGui {
    private final BuildPhase phase = BuildPhase.ELEMENTS;

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
        }
        MessageBuilder messageBuilder = new MessageBuilder();
        return messageBuilder.build();
    }

    private Message makeElementsMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed1 = new EmbedBuilder();
        embed1.setTitle("Elements");
        messageBuilder.setEmbeds(embed1.build(), embed1.build());
        return messageBuilder.build();
    }

    private enum BuildPhase {
        ELEMENTS,
        MAJOR_ID,
        DAMAGE,
        ID
    }
}

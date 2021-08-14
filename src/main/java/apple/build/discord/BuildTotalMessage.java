package apple.build.discord;

import apple.build.search.Build;
import apple.build.wynncraft.items.Item;
import apple.discord.acd.ACD;
import apple.discord.acd.reaction.gui.ACDGuiPageable;
import apple.discord.acd.reaction.gui.GuiPageMessageable;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildTotalMessage extends ACDGuiPageable<GuiPageMessageable> {
    private Build build;

    public BuildTotalMessage(ACD acd, Message msg, Build build) {
        super(acd, msg);
        this.build = build;
       this. addPage(this::items);
        this.addPage(this::two);
    }

    private Message two() {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setContent("whoa" + build.toString());
        return messageBuilder.build();
    }


    private Message items() {
        MessageBuilder messageBuilder = new MessageBuilder();
        List<Item> items = build.items;
        Map<Item.ItemType, List<Item>> itemsByType = new HashMap<>();
        for (Item item : items) {
            itemsByType.compute(item.type, (it, i) -> {
                if (i == null) i = new ArrayList<>();
                i.add(item);
                return i;
            });
        }
        messageBuilder.setContent("ad");
        return messageBuilder.build();
    }

    @Override
    protected long getMillisToOld() {
        return 0;
    }
}

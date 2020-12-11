package apple.build.discord.reactions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class BuilderMessage implements ReactableMessage{
    public BuilderMessage(){

    }
    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {

    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public long getLastUpdated() {
        return 0;
    }

    @Override
    public void dealWithOld() {

    }
}

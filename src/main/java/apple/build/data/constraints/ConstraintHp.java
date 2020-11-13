package apple.build.data.constraints;

import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Armor;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ConstraintHp extends BuildConstraint {
    private final int ehp;

    public ConstraintHp(int ehp) {
        this.ehp = ehp;
    }

    @Override
    boolean internalIsValid(Collection<Item> items) {
        int hp = 0;
        for (Item item : items) {
            hp += getEhp(item);
        }
        return hp >= ehp;
    }

    private int getEhp(Item item) {
        int hp = 0;
        if (item instanceof Armor)
            hp += ((Armor) item).health;
        else if (item instanceof Accessory) {
            hp += ((Accessory) item).health;
        }
        return hp + item.ids.getOrDefault("healthBonus", 0);
    }

    @Override
    public @Nullable Item getBest(List<Item> items) {
        Item best = null;
        int bestVal = 0;
        for (Item item : items) {
            if (best == null) {
                best = item;
                bestVal = getEhp(item);
            } else {
                Integer val = getEhp(item);
                if (val > bestVal) {
                    best = item;
                    bestVal = val;
                }
            }
        }
        return best;
    }

    @Override
    public boolean contributes(Item item) {
        return getEhp(item) > 0;
    }

    @Override
    public int compare(Item item1, Item item2) {
        return getEhp(item1) - getEhp(item2);
    }
}

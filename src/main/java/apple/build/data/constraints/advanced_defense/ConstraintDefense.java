package apple.build.data.constraints.advanced_defense;

import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ConstraintDefense extends BuildConstraintAdvancedDefense {
    private final String name;
    private final String bonus;
    private final int value;

    public ConstraintDefense(String name, int value) {
        this.name = name;
        this.value = value;
        this.bonus = "bonus" + Pretty.uppercaseFirst(name);
    }

    protected boolean internalIsValid(Collection<Item> items) {
        int actualVal = 0;
        for (Item item : items) {
            actualVal += item.ids.getOrDefault(name, 0);
            if (actualVal >= value) return true;
        }
        return false;
    }

    @Nullable
    public Item getBest(List<Item> items) {
        Item best = null;
        int bestVal = 0;
        for (Item item : items) {
            if (best == null) {
                best = item;
                bestVal = best.ids.getOrDefault(name, 0);
            } else {
                Integer val = item.ids.getOrDefault(name, 0);
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
        return item.ids.getOrDefault(name, 0) > 0 || item.ids.getOrDefault(bonus, 0) > 0;
    }

}

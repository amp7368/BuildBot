package apple.build.data.constraints;

import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConstraintId extends BuildConstraint {
    private final String name;
    private final int value;

    public ConstraintId(String name, int value) {
        this.name = name;
        this.value = value;
    }


    /**
     * checks whether the items satisfy the constraint
     *
     * @param items the items to check against
     * @return true if it satisfies, otherwise false
     */
    @Override
    protected boolean internalIsValid(Collection<Item> items) {
        int actualVal = 0;
        for (Item item : items) {
            actualVal += item.ids.getOrDefault(name, 0);
            if (actualVal >= value) return true;
        }
        return false;
    }

    /**
     * gets the best item among the given items
     *
     * @param items the items to check against
     * @return the best item for this constraint
     */
    @Nullable
    @Override
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
        return item.ids.getOrDefault(name, 0) > 0;
    }

    /**
     * compares two items with this constraint
     *
     * @param item1 the first item to compare
     * @param item2 the second item to compare
     * @return positive if first is better, negative if second is better, otherwise 0
     */
    @Override
    public int compare(Item item1, Item item2) {
        return item1.ids.getOrDefault(name, 0) - item2.ids.getOrDefault(name, 0);
    }

}

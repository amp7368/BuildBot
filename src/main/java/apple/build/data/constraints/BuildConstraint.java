package apple.build.data.constraints;

import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BuildConstraint {
    /**
     * gets the best item among the given items
     *
     * @param items the items to check against
     * @return the best item for this constraint
     */
    @Nullable
    Item getBest(List<Item> items);

    boolean contributes(Item item);

    /**
     * compares two items with this constraint
     *
     * @param item1 the first item to compare
     * @param item2 the second item to compare
     * @return positive if first is better, negative if second is better, otherwise 0
     */
    int compare(Item item1, Item item2);
}

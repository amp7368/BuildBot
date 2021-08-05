package apple.build.search.constraints.general;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class BuildConstraintGeneral implements BuildConstraint {
    /**
     * checks whether the items satisfy the constraint
     *
     * @param items the items to check against
     * @return true if it satisfies, otherwise false
     */
    boolean isValid(Item... items) {
        return internalIsValid(Arrays.asList(items));
    }


    /**
     * checks whether the items satisfy the constraint
     *
     * @param items1 the items to check against
     * @param items2 the items to check against
     * @return true if it satisfies, otherwise false
     */
    public boolean isValid(Collection<Item> items1, Item... items2) {
        List<Item> items = new ArrayList<>();
        items.addAll(items1);
        items.addAll(Arrays.asList(items2));
        return internalIsValid(items);
    }

    /**
     * checks whether the items satisfy the constraint
     *
     * @param items the items to check against
     * @return true if it satisfies, otherwise false
     */
    protected abstract boolean internalIsValid(Collection<Item> items);

    /**
     * gets the best item among the given items
     *
     * @param items the items to check against
     * @return the best item for this constraint
     */
    @Nullable
    public abstract Item getBest(List<Item> items);

    public abstract boolean contributes(Item item);

    /**
     * compares two items with this constraint
     *
     * @param item1 the first item to compare
     * @param item2 the second item to compare
     * @return positive if first is better, negative if second is better, otherwise 0
     */
    public abstract int compare(Item item1, Item item2);


    /**
     * @return the database ready version of this constraint
     */
    @NotNull
    public abstract ConstraintSimplified getSimplified();
}

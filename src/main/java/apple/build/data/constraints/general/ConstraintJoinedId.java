package apple.build.data.constraints.general;

import apple.build.data.constraints.BuildConstraint;
import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.constraints.ConstraintType;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConstraintJoinedId extends BuildConstraintGeneral {
    private final List<Integer> names;
    private final int value;

    public ConstraintJoinedId(List<String> names, int value) {
        this.names = new ArrayList<>(names.size());
        for (String n : names) {
            this.names.add(Item.getIdIndex(n));
        }
        this.value = value;
    }

    public ConstraintJoinedId(String textVal, int val) {
        String[] names = textVal.split(",");
        this.names = new ArrayList<>(names.length);
        for (String n : names) {
            this.names.add(Item.getIdIndex(n));
        }
        this.value = val;
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
            for (int name : names)
                actualVal += item.getId(name);
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
        int[] bestVal = new int[names.size()];
        for (Item item : items) {
            if (best == null) {
                best = item;
                int i = 0;
                for (int name : names) {
                    bestVal[i++] = best.getId(name);
                }
            } else {
                int i = 0;
                for (int name : names) {
                    int val = item.getId(name);
                    if (val > bestVal[i]) {
                        if (i == 0) best = item;
                        bestVal[i] = val;
                    }
                    i++;
                }
            }
        }
        best = Item.makeItem(best);
        for (int i = 1; i < bestVal.length; i++)
            best.ids.put(names.get(i), bestVal[i]);
        return best;
    }

    @Override
    public boolean contributes(Item item) {
        for (int name : names)
            if (item.getId(name) > 0) return true;
        return false;
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
        int total1 = 0;
        int total2 = 0;
        for (int name : names) {
            total1 += item1.getId(name);
            total2 += item2.getId(name);
        }
        return total1 - total2;
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_JOINED_ID;
    }

    /**
     * @return the database ready version of this constraint
     */
    @NotNull
    public ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_JOINED_ID);
        simple.text = names.stream().map(Item::getIdName).collect(Collectors.joining(","));
        simple.val = value;
        return simple;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        if (obj instanceof ConstraintJoinedId) {
            ConstraintJoinedId other = (ConstraintJoinedId) obj;
            if (other.value < this.value) return false;
            for (Integer name : this.names)
                if (!other.names.contains(name)) return false;
            return true;
        }
        return false;
    }
}

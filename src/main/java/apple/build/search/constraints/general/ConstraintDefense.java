package apple.build.search.constraints.general;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.search.enums.ElementSkill;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ConstraintDefense extends BuildConstraintGeneral {
    private final int val;
    private final int percIndex;
    private final int rawIndex;
    private final ElementSkill name;

    public ConstraintDefense(ElementSkill name, int val) {
        this.percIndex = name.defensePercIndex;
        this.rawIndex = name.defenseRawIndex;
        this.name = name;
        this.val = val;
    }

    public ConstraintDefense(String text, Integer val) {
        this.name = ElementSkill.valueOf(text);
        this.percIndex = this.name.defensePercIndex;
        this.rawIndex = this.name.defenseRawIndex;
        this.val = val;

    }

    @Override
    protected boolean internalIsValid(Collection<Item> items) {
        int raw = 0;
        int perc = 0;
        for (Item item : items) {
            perc += item.getId(percIndex);
            raw += item.getId(rawIndex);
        }
        return raw * (1 + perc / 100f) >= val;
    }

    @Override
    public @Nullable Item getBest(List<Item> items) {
        Item best = null;
        int bestRawVal = 0;
        int bestPercVal = 0;
        for (Item item : items) {
            if (best == null) {
                best = item;
                bestRawVal = item.getId(rawIndex);
                bestPercVal = item.getId(percIndex);
            } else {
                int rawVal = item.getId(rawIndex);
                int percVal = item.getId(percIndex);
                if (rawVal > bestRawVal) {
                    best = item;
                    bestRawVal = rawVal;
                }
                if (percVal > bestPercVal) {
                    bestPercVal = percVal;
                }
            }
        }
        Item newItem = Item.makeItem(best);
        newItem.ids.put(percIndex, bestPercVal);
        return newItem;
    }

    @Override
    public boolean contributes(Item item) {
        return item.getId(percIndex) > 0 || item.getId(rawIndex) > 0;
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
        int percVal1 = item1.getId(percIndex);
        int rawVal1 = item1.getId(rawIndex);
        int percVal2 = item2.getId(percIndex);
        int rawVal2 = item2.getId(rawIndex);
        if (percVal1 > percVal2) {
            if (rawVal1 > rawVal2) {
                // 1 is better
                return 1;
            }
            return 0;
        } else {
            if (rawVal1 < rawVal2) {
                // 2 is better
                return -1;
            }
            return 0;
        }
    }

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_HP);
        simple.text = name.name();
        simple.val = val;
        return simple;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        if (obj instanceof ConstraintDefense) {
            ConstraintDefense other = (ConstraintDefense) obj;
            return other.name == this.name && other.val >= this.val;
        }
        return false;
    }
    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_DEFENSE;
    }

    @Override
    public boolean isExact(BuildConstraint constraint) {
        if (constraint instanceof ConstraintDefense) {
            ConstraintDefense other = (ConstraintDefense) constraint;
            return other.val == this.val && other.name == this.name;
        }
        return false;
    }
}

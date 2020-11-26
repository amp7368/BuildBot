package apple.build.data.constraints.general;

import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.constraints.ConstraintType;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.ItemIdIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ConstraintHpr extends BuildConstraintGeneral {
    private final int hpr;

    public ConstraintHpr(int hpr) {
        this.hpr = hpr;
    }

    protected boolean internalIsValid(Collection<Item> items) {
        int hprRaw = 0;
        int hprPerc = 0;
        for (Item item : items) {
            hprPerc += item.getId(ItemIdIndex.HEALTH_REGEN);
            hprRaw += item.getId(ItemIdIndex.HEALTH_REGEN_RAW);
        }
        return hprRaw * (1 + hprPerc / 100f) >= hpr;
    }

    @Override
    public @Nullable Item getBest(List<Item> items) {
        Item best = null;
        int bestHprRawVal = 0;
        int bestHprPercVal = 0;
        for (Item item : items) {
            if (best == null) {
                best = item;
                bestHprPercVal = item.getId(ItemIdIndex.HEALTH_REGEN);
                bestHprRawVal = item.getId(ItemIdIndex.HEALTH_REGEN_RAW);
            } else {
                int hprRawVal = item.getId(ItemIdIndex.HEALTH_REGEN_RAW);
                int hprPercVal = item.getId(ItemIdIndex.HEALTH_REGEN);
                if (hprRawVal > bestHprRawVal) {
                    best = item;
                    bestHprRawVal = hprRawVal;
                }
                if (hprPercVal > bestHprPercVal) {
                    bestHprPercVal = hprPercVal;
                }
            }
        }
        Item newItem = Item.makeItem(best);
        newItem.ids.put(ItemIdIndex.HEALTH_REGEN, bestHprPercVal);
        return newItem;
    }

    @Override
    public boolean contributes(Item item) {
        return item.getId(ItemIdIndex.HEALTH_REGEN_RAW) > 0 || item.getId(ItemIdIndex.HEALTH_REGEN) > 0;
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
        int hprPercVal1 = item1.getId(ItemIdIndex.HEALTH_REGEN);
        int hprRawVal1 = item1.getId(ItemIdIndex.HEALTH_REGEN_RAW);
        int hprPercVal2 = item2.getId(ItemIdIndex.HEALTH_REGEN);
        int hprRawVal2 = item2.getId(ItemIdIndex.HEALTH_REGEN_RAW);
        if (hprPercVal1 > hprPercVal2) {
            if (hprRawVal1 > hprRawVal2) {
                // 1 is better
                return 1;
            }
            return 0;
        } else {
            if (hprRawVal1 < hprRawVal2) {
                // 2 is better
                return -1;
            }
            return 0;
        }
    }

    @Override
    public @NotNull ConstraintType getType() {
        return ConstraintType.SIMPLE;
    }
    /**
     * @return the database ready version of this constraint
     */
    @NotNull
    public ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_HPR);
        simple.val = hpr;
        return simple;
    }
}

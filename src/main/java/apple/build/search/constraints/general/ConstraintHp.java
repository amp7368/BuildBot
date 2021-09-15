package apple.build.search.constraints.general;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Armor;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.ItemIdIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConstraintHp extends BuildConstraintGeneral {
    public static final int BASE_HEALTH = 103 * 5 + 5;
    private final int ehp;

    public ConstraintHp(int ehp) {
        this.ehp = ehp - BASE_HEALTH;
    }

    public ConstraintHp(String text, Integer val) {
        this.ehp = val;
    }

    @Override
    protected boolean internalIsValid(Iterable<Item> items) {
        int hp = 0;
        for (Item item : items) {
            hp += getEhp(item);
        }

        return hp >= ehp;
    }

    private int getEhp(Item item) {
        int hp = 0;
        if (item instanceof Armor armor){
            hp += armor.health;
        }
        else if (item instanceof Accessory accessory) {
            hp += accessory.health;
        }
        return hp + item.ids.getOrDefault(ItemIdIndex.HEALTH_BONUS, 0);
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
                int val = getEhp(item);
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

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_HP);
        simple.val = ehp;
        return simple;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        if (obj instanceof ConstraintHp other) {
            return other.ehp >= this.ehp;
        }
        return false;
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_HP;
    }

    @Override
    public boolean isExact(BuildConstraint constraint) {
        if (constraint instanceof ConstraintHp other) {
            return other.ehp == this.ehp;
        }
        return false;
    }
}

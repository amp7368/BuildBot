package apple.build.search.constraints.general;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.search.enums.IdNames;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConstraintMinAttackSpeed extends BuildConstraintGeneral {
    private final int val;

    public ConstraintMinAttackSpeed(int val) {
        this.val = val;
    }

    public ConstraintMinAttackSpeed(String text, Integer val) {
        this.val = val;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        if (obj instanceof ConstraintMinAttackSpeed other) {
            return other.val >= this.val;
        }
        return false;
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_MIN_ATTACK_SPEED;
    }

    @Override
    public boolean isExact(BuildConstraint constraint) {
        if (constraint instanceof ConstraintMinAttackSpeed other) {
            return other.val == this.val;
        }
        return false;
    }

    @Override
    protected boolean internalIsValid(Iterable<Item> items) {
        int attackSpeed = 0;
        for (Item item : items) {
            attackSpeed += getAttackSpeed(item);
        }
        return attackSpeed >= this.val;
    }

    @Override
    public @Nullable Item getBest(List<Item> items) {
        Item bestItem = null;
        int bestVal = -1;
        for (Item item : items) {
            int attackSpeed = getAttackSpeed(item);
            if (attackSpeed > bestVal) {
                bestVal = attackSpeed;
                bestItem = item;
            }
        }
        return bestItem;
    }

    private static int getAttackSpeed(Item item) {
        int attackSpeed = item.getId(IdNames.ATTACK_SPEED.getIdIndex());
        if (item instanceof Weapon weapon) {
            attackSpeed += weapon.attackSpeed.speed;
        }
        return attackSpeed;
    }

    @Override
    public boolean contributes(Item item) {
        return true;
    }

    @Override
    public int compare(Item item1, Item item2) {
        return getAttackSpeed(item1) - getAttackSpeed(item2);
    }


    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified constraintSimplified = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_MIN_ATTACK_SPEED);
        constraintSimplified.val = this.val;
        return constraintSimplified;
    }
}

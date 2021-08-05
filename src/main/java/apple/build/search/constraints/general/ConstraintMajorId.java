package apple.build.search.constraints.general;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ConstraintMajorId extends BuildConstraintGeneral {
    private final String majorId;

    public ConstraintMajorId(String majorId) {
        this.majorId = majorId;
    }

    public ConstraintMajorId(String text, Integer val) {
        this.majorId = text;
    }

    @Override
    protected boolean internalIsValid(Collection<Item> items) {
        for (Item item : items) {
            for (String majorId : item.majorIds) {
                if (this.majorId.equals(majorId)) return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable Item getBest(List<Item> items) {
        for (Item item : items) {
            for (String majorId : item.majorIds) {
                if (this.majorId.equals(majorId)) return item;
            }
        }
        return items.get(0);
    }

    @Override
    public boolean contributes(Item item) {
        for (String majorId : item.majorIds) {
            if (this.majorId.equals(majorId)) return true;
        }
        return false;
    }

    @Override
    public int compare(Item item1, Item item2) {
        boolean contains1 = false;
        boolean contains2 = false;
        for (String majorId : item1.majorIds) {
            if (this.majorId.equals(majorId)) contains1 = true;
        }
        for (String majorId : item2.majorIds) {
            if (this.majorId.equals(majorId)) contains2 = true;
        }
        if (contains1 == contains2) return 0;
        return contains1 ? 1 : -1;
    }

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified constraint = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_MAJOR_ID);
        constraint.text = majorId;
        return constraint;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        return obj instanceof ConstraintMajorId && ((ConstraintMajorId) obj).majorId.equals(majorId);
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_MAJOR_ID;
    }

    @Override
    public boolean isExact(BuildConstraint constraint) {
        if (constraint instanceof ConstraintMajorId) {
            ConstraintMajorId other = (ConstraintMajorId) constraint;
            return other.majorId.equals(this.majorId);
        }
        return false;
    }
}

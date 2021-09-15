package apple.build.search.constraints.advanced_damage;

import apple.build.search.BuildMath;
import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.search.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;

public class ConstraintMainDamage extends BuildConstraintAdvancedDamage {
    private final int dmg;

    public ConstraintMainDamage(int dmg) {
        this.dmg = dmg;
    }

    public ConstraintMainDamage(String text, Integer val) {
        this.dmg = val;
    }

    @Override
    public boolean isValid(DamageInput input, Weapon item) {
        return BuildMath.getDamage(input, item).dpsWithRaw() >= dmg;
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_MAIN_DMG;
    }

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_MAIN_DMG);
        simple.val = dmg;
        return simple;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        if (obj instanceof ConstraintMainDamage) {
            ConstraintMainDamage other = (ConstraintMainDamage) obj;
            return other.dmg >= this.dmg;
        }
        return false;
    }

    @Override
    public boolean isExact(BuildConstraint constraint) {
        return constraint instanceof ConstraintMainDamage && ((ConstraintMainDamage) constraint).dmg == dmg;
    }
}

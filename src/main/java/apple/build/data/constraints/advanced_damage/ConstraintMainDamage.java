package apple.build.data.constraints.advanced_damage;

import apple.build.data.BuildMath;
import apple.build.data.constraints.BuildConstraint;
import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.constraints.ConstraintType;
import apple.build.data.constraints.answers.DamageInput;
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
        return BuildMath.getDamage(input, item).dps() >= dmg;
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_MAIN_DMG;
    }

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_SPELL_COST);
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
}

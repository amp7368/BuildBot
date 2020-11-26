package apple.build.data.constraints.advanced_damage;

import apple.build.data.BuildMath;
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

    @Override
    public boolean isValid(DamageInput input, Weapon item) {
        return BuildMath.getDamage(input, item).dps() >= dmg;
    }

    @Override
    public @NotNull ConstraintType getType() {
        return ConstraintType.SIMPLE;
    }

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_SPELL_COST);
        simple.val = dmg;
        return simple;
    }
}

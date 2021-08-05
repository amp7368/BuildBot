package apple.build.search.constraints.advanced_damage;

import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.search.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;

public abstract class BuildConstraintAdvancedDamage implements BuildConstraint {
    public abstract boolean isValid(DamageInput input, Weapon item);

    /**
     * @return the database ready version of this constraint
     */
    @NotNull
    public abstract ConstraintSimplified getSimplified();
}

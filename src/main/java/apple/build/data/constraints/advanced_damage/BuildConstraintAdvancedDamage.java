package apple.build.data.constraints.advanced_damage;

import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.constraints.ConstraintType;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;

public abstract class BuildConstraintAdvancedDamage {
    public abstract boolean isValid(DamageInput input, Weapon item);

    /**
     * @return what type this is
     */
    @NotNull
    public abstract ConstraintType getType();

    /**
     * @return the database ready version of this constraint
     */
    @NotNull
    public abstract ConstraintSimplified getSimplified();
}

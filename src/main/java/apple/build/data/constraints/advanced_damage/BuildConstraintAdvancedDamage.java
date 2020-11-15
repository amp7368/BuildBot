package apple.build.data.constraints.advanced_damage;

import apple.build.data.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;

public abstract class BuildConstraintAdvancedDamage {
    public abstract boolean isValid(DamageInput input, Weapon item);
}

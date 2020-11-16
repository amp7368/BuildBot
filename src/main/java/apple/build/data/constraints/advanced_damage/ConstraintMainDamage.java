package apple.build.data.constraints.advanced_damage;

import apple.build.data.BuildMath;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;

public class ConstraintMainDamage extends BuildConstraintAdvancedDamage {
    private final int dmg;

    public ConstraintMainDamage(int dmg) {
        this.dmg = dmg;
    }

    @Override
    public boolean isValid(DamageInput input, Weapon item) {
        return BuildMath.getDamage(input, item).dps() >= dmg;
    }
}

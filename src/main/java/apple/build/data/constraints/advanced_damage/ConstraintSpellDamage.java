package apple.build.data.constraints.advanced_damage;

import apple.build.data.BuildMath;
import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;

public class ConstraintSpellDamage extends BuildConstraintAdvancedDamage {
    private final ConstraintSpellCost.Spell spell;
    private int dmgRequired;

    public ConstraintSpellDamage(ConstraintSpellCost.Spell spell, int dmgRequired) {
        this.dmgRequired = dmgRequired;
        this.spell = spell;
    }

    @Override
    public boolean isValid(DamageInput input, Weapon weapon) {
        //todo start here
        return BuildMath.getDamage(spell, input, weapon).dps() > dmgRequired;
    }
}

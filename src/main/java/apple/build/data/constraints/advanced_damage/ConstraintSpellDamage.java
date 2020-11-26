package apple.build.data.constraints.advanced_damage;

import apple.build.data.BuildMath;
import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.constraints.ConstraintType;
import apple.build.data.constraints.answers.DamageOutput;
import apple.build.data.enums.Spell;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;

public class ConstraintSpellDamage extends BuildConstraintAdvancedDamage {
    private final Spell spell;
    private int dmgRequired;

    public ConstraintSpellDamage(Spell spell, int dmgRequired) {
        this.dmgRequired = dmgRequired;
        this.spell = spell;
    }

    @Override
    public boolean isValid(DamageInput input, Weapon weapon) {
        DamageOutput damage = BuildMath.getDamage(spell, input, weapon);
        return damage.dps() > dmgRequired;
    }

    @Override
    public @NotNull ConstraintType getType() {
        return ConstraintType.TEXT_VAL;
    }
    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_SPELL_COST);
        simple.text = spell.name();
        simple.val = dmgRequired;
        return simple;
    }
}

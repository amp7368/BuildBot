package apple.build.data.constraints.advanced_damage;

import apple.build.data.BuildMath;
import apple.build.data.constraints.BuildConstraint;
import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.constraints.ConstraintType;
import apple.build.data.constraints.answers.DamageOutput;
import apple.build.data.enums.Spell;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;

public class ConstraintSpellDamage extends BuildConstraintAdvancedDamage {
    private final Spell spell;
    private final int dmgRequired;

    public ConstraintSpellDamage(Spell spell, int dmgRequired) {
        this.dmgRequired = dmgRequired;
        this.spell = spell;
    }

    public ConstraintSpellDamage(String text, Integer val) {
        this.spell = Spell.valueOf(text);
        this.dmgRequired = val;
    }

    @Override
    public boolean isValid(DamageInput input, Weapon weapon) {
        DamageOutput damage = BuildMath.getDamage(spell, input, weapon);
        return damage.dps() > dmgRequired;
    }

    @Override
    public ConstraintSimplified.ConstraintSimplifiedName getSimplifiedName() {
        return ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_SPELL_DMG;
    }

    @Override
    public @NotNull ConstraintSimplified getSimplified() {
        ConstraintSimplified simple = new ConstraintSimplified(ConstraintSimplified.ConstraintSimplifiedName.CONSTRAINT_SPELL_DMG);
        simple.text = spell.name();
        simple.val = dmgRequired;
        return simple;
    }

    @Override
    public boolean isMoreStrict(BuildConstraint obj) {
        if (obj instanceof ConstraintSpellDamage) {
            ConstraintSpellDamage other = (ConstraintSpellDamage) obj;
            return other.spell == this.spell && other.dmgRequired >= this.dmgRequired;
        }
        return false;
    }

    @Override
    public boolean isExact(BuildConstraint constraint) {
        return constraint instanceof ConstraintSpellDamage && ((ConstraintSpellDamage) constraint).dmgRequired == dmgRequired;
    }
}

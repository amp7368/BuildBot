package apple.build.data;

import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.data.constraints.answers.DamageOutput;
import apple.build.sql.GetDB;
import apple.build.sql.VerifyDB;
import apple.build.utils.Pair;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

import java.sql.SQLException;
import java.util.List;

public class BuildMath {
    /**
     * gets the percent increase that a skill point of sp gives
     *
     * @param sp the skill points invested
     * @return the percent increase
     */
    public static float getSkillImprovement(int sp) {
        sp = Math.min(150, sp);
        return Math.round((
                -0.0000000166 * Math.pow(sp, 4) + 0.0000122614 * Math.pow(sp, 3) - 0.0044972984 * Math.pow(sp, 2) + 0.9931907398 * sp + 0.0093811967
        ) * 10) / 10f; //idk what this formula is
    }

    /**
     * gets the mana cost for a particular situation
     * (will give 0 and negatives because i think that gives more info)
     *
     * @param spell        the spell with mana cost
     * @param intelligence the intelligence of the player
     * @param addedCostRaw the added spell cost of the player
     * @return the mana cost
     */
    public static int getMana(ConstraintSpellCost.Spell spell, int intelligence, int addedCostRaw) {
        return (int) Math.ceil(spell.mana * (1 - getSkillImprovement(intelligence) / 100) + addedCostRaw);
    }

    public static DamageOutput getDamage(ConstraintSpellCost.Spell spell, DamageInput input, Weapon weapon) {
        double neutralLower = weapon.damage.getKey();
        double neutralUpper = weapon.damage.getValue();
        int elementSkillsLength = ElementSkill.values().length;
        double[] elementalLower = new double[elementSkillsLength];
        double[] elementalUpper = new double[elementSkillsLength];
        int i = 0;
        // get the base damage
        for (Pair<Integer, Integer> elemental : weapon.elemental) {
            elementalLower[i] = elemental.getKey();
            elementalUpper[i++] = elemental.getValue();
        }
        // multiply by spell elemental multiplier
        double multiplier = 1;
        for (i = 0; i < elementalLower.length; i++) {
            double elementalMultiplier = spell.elemental[i];
            multiplier -= elementalMultiplier;
            elementalLower[i] += neutralLower * elementalMultiplier;
            elementalUpper[i] += neutralUpper * elementalMultiplier;
        }
        neutralLower *= multiplier;
        neutralUpper *= multiplier;

        double idBoost = 1;
        idBoost += input.spellDamage;
        idBoost += getSkillImprovement(input.strength) / 100;

        double idBoostCrit = idBoost + 1;
        double neutralLowerCrit = neutralLower;
        double neutralUpperCrit = neutralUpper;
        double[] elementalLowerCrit = new double[elementSkillsLength];
        double[] elementalUpperCrit = new double[elementSkillsLength];
        System.arraycopy(elementalLower, 0, elementalLowerCrit, 0, elementSkillsLength);
        System.arraycopy(elementalUpper, 0, elementalUpperCrit, 0, elementSkillsLength);

        neutralLower *= idBoost;
        neutralUpper *= idBoost;
        neutralLowerCrit *= idBoostCrit;
        neutralUpperCrit *= idBoostCrit;
        for (i = 0; i < elementalLower.length; i++) {
            double elementalIdBoost = Math.max(0, getSkillImprovement(input.skills[i]) / 100);
            elementalIdBoost += input.elemental[i];
            double idBoostElemental = Math.max(0, idBoost + elementalIdBoost);
            double idBoostElementalCrit = Math.max(0, idBoostCrit + elementalIdBoost);
            elementalLower[i] *= idBoostElemental;
            elementalUpper[i] *= idBoostElemental;
            elementalLowerCrit[i] *= idBoostElementalCrit;
            elementalUpperCrit[i] *= idBoostElementalCrit;
        }
        multiplier = weapon.attackSpeed.modifier() * (spell.damage);
        if (input.hawkeye && spell == ConstraintSpellCost.Spell.ARROW_STORM) {
            multiplier *= 5 / 3d;
        }
        neutralLower *= multiplier;
        neutralUpper *= multiplier;
        neutralLowerCrit *= multiplier;
        neutralUpperCrit *= multiplier;
        for (i = 0; i < elementalLower.length; i++) {
            elementalLower[i] = Math.floor(elementalLower[i] * multiplier);
            elementalUpper[i] = Math.floor(elementalUpper[i] * multiplier);
            elementalLowerCrit[i] = Math.floor(elementalLowerCrit[i] * multiplier);
            elementalUpperCrit[i] = Math.floor(elementalUpperCrit[i] * multiplier);
        }
        multiplier = input.spellDamageRaw * (spell.damage);
        if (input.hawkeye && spell == ConstraintSpellCost.Spell.ARROW_STORM) {
            multiplier *= 5 / 3d;
        }
        neutralLower += multiplier;
        neutralUpper += multiplier;
        neutralLowerCrit += multiplier;
        neutralUpperCrit += multiplier;
        return new DamageOutput(neutralLower, neutralUpper, elementalLower, elementalUpper,
                neutralLowerCrit, neutralUpperCrit, elementalLowerCrit, elementalUpperCrit,
                getSkillImprovement(input.dexterity) / 100);
    }

    public static DamageOutput getDamage(DamageInput input, Weapon weapon) {
        double neutralLower = weapon.damage.getKey();
        double neutralUpper = weapon.damage.getValue();
        int elementSkillsLength = ElementSkill.values().length;
        double[] elementalLower = new double[elementSkillsLength];
        double[] elementalUpper = new double[elementSkillsLength];
        int i = 0;
        // get the base damage
        for (Pair<Integer, Integer> elemental : weapon.elemental) {
            elementalLower[i] = elemental.getKey();
            elementalUpper[i++] = elemental.getValue();
        }
        double idBoost = 1;
        idBoost += input.mainDamage;
        idBoost += getSkillImprovement(input.strength) / 100;

        double idBoostCrit = idBoost + 1;
        double neutralLowerCrit = neutralLower;
        double neutralUpperCrit = neutralUpper;
        double[] elementalLowerCrit = new double[elementSkillsLength];
        double[] elementalUpperCrit = new double[elementSkillsLength];
        System.arraycopy(elementalLower, 0, elementalLowerCrit, 0, elementSkillsLength);
        System.arraycopy(elementalUpper, 0, elementalUpperCrit, 0, elementSkillsLength);

        neutralLower *= idBoost;
        neutralUpper *= idBoost;
        neutralLowerCrit *= idBoostCrit;
        neutralUpperCrit *= idBoostCrit;
        for (i = 0; i < elementalLower.length; i++) {
            double elementalIdBoost = Math.max(0, getSkillImprovement(input.skills[i]) / 100);
            elementalIdBoost += input.elemental[i];
            elementalLower[i] = Math.floor(elementalLower[i] * (idBoost + elementalIdBoost));
            elementalUpper[i] = Math.floor(elementalUpper[i] * (idBoost + elementalIdBoost));
            elementalLowerCrit[i] = Math.floor(elementalLowerCrit[i] * (idBoostCrit + elementalIdBoost));
            elementalUpperCrit[i] = Math.floor(elementalUpperCrit[i] * (idBoostCrit + elementalIdBoost));
        }
        neutralLower += input.mainDamageRaw;
        neutralUpper += input.mainDamageRaw;
        neutralLowerCrit += input.mainDamageRaw;
        neutralUpperCrit += input.mainDamageRaw;
        return new DamageOutput(neutralLower, neutralUpper, elementalLower, elementalUpper,
                neutralLowerCrit, neutralUpperCrit, elementalLowerCrit, elementalUpperCrit,
                getSkillImprovement(input.dexterity) / 100, input.attackSpeedModifier);

    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        VerifyDB.initialize();
        List<Item> items = GetDB.getAllItems(Item.ItemType.BOW);
        Weapon item = null;
        for (Item i : items) {
            if (i.name.equals("Divzer"))
                item = (Weapon) i;
        }
        if (item == null) return;
        DamageOutput damage = getDamage(ConstraintSpellCost.Spell.BOMB_ARROW, new DamageInput(
                .68,
                -.14,
                835,
                1745,
                new int[]{157, -580, -30, 107, 57},
                0,
                new double[]{1.02, -.65, -.65, -3.43, -3.25},
                Item.AttackSpeed.toModifier(Item.AttackSpeed.SLOW.speed)
        ), item);
        System.out.println(damage.dps());
    }
}

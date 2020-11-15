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
            double elementalMultiplier = 1 + spell.elemental[i];
            multiplier -= spell.elemental[i];
            elementalLower[i] *= elementalMultiplier;
            elementalUpper[i] *= elementalMultiplier;
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
            elementalLower[i] *= idBoost + elementalIdBoost;
            elementalUpper[i] *= idBoost + elementalIdBoost;
            elementalLowerCrit[i] *= idBoostCrit + elementalIdBoost;
            elementalUpperCrit[i] *= idBoostCrit + elementalIdBoost;
        }

        multiplier = input.attackSpeedModifier * (spell.damage);
        neutralLower *= multiplier;
        neutralUpper *= multiplier;
        neutralLowerCrit *= multiplier;
        neutralUpperCrit *= multiplier;
        for (i = 0; i < elementalLower.length; i++) {
            elementalLower[i] *= multiplier;
            elementalUpper[i] *= multiplier;
            elementalLowerCrit[i] *= multiplier;
            elementalUpperCrit[i] *= multiplier;
        }
        multiplier = input.spellDamageRaw * (spell.damage);
        neutralLower += multiplier;
        neutralUpper += multiplier;
        return new DamageOutput(neutralLower, neutralUpper, elementalLower, elementalUpper,
                neutralLowerCrit, neutralUpperCrit, elementalLowerCrit, elementalUpperCrit,
                getSkillImprovement(input.dexterity) / 100);
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        VerifyDB.initialize();
        List<Item> items = GetDB.getAllItems(Item.ItemType.WAND);
        Weapon item = null;
        for (Item i : items) {
            if (i.name.equals("Nepta Floodbringer"))
                item = (Weapon) i;
        }
        if (item == null) return;
        System.out.println(getDamage(ConstraintSpellCost.Spell.METEOR, new DamageInput(
                .93,
                .14,
                252,
                164,
                new int[]{-30, -30, 40, 130, 70},
                0,
                new double[]{-1.19, 0, -.04, .60, .44},
                4.3
        ), item).dps());
    }
}

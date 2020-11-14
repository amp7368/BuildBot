package apple.build.data;

import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;

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
}

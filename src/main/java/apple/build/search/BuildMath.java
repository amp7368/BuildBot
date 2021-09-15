package apple.build.search;

import apple.build.search.constraints.answers.DamageInput;
import apple.build.search.constraints.answers.DamageOutput;
import apple.build.search.enums.ElementSkill;
import apple.build.search.enums.Powder;
import apple.build.search.enums.Spell;
import apple.build.search.enums.SpellPart;
import apple.build.utils.Pair;
import apple.build.wynnbuilder.ServiceWynnbuilderItemDB;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

public class BuildMath {
    private static final int MAX_SKILL_POINT_ALLOCATION = 150;
    private static final Float[] skillImprovements = new Float[MAX_SKILL_POINT_ALLOCATION + 1];

    /**
     * gets the percent increase that a skill point of sp gives
     *
     * @param sp the skill points invested
     * @return the percent increase
     */
    public static float getSkillImprovement(int sp) {
        if (sp < 0) return 0;
        if (sp > MAX_SKILL_POINT_ALLOCATION) sp = MAX_SKILL_POINT_ALLOCATION;
        Float valueThere = skillImprovements[sp];
        if (valueThere == null) {
            float improvement = Math.round((
                    -0.0000000166 * Math.pow(sp, 4) + 0.0000122614 * Math.pow(sp, 3) - 0.0044972984 * Math.pow(sp, 2) + 0.9931907398 * sp + 0.0093811967
            ) * 10) / 10f; //idk what this formula is
            return skillImprovements[sp] = improvement;
        }
        return valueThere;
    }

    /**
     * gets the mana cost for a particular situation
     * (will give 0 and negatives because i think that gives more info)
     *
     * @param spell         the spell with mana cost
     * @param intelligence  the intelligence of the player
     * @param addedCostRaw  the added raw spell cost of the player
     * @param addedCostPerc the added percentage spell cost of the player
     * @return the mana cost
     */
    public static int getMana(Spell spell, int intelligence, int addedCostRaw, int addedCostPerc) {
        return (int) Math.floor(Math.ceil(spell.mana * (1 - getSkillImprovement(intelligence) / 100) + addedCostRaw) * (1 + addedCostPerc / 100f));
    }

    @NotNull
    public static DamageOutput getDamage(Spell spell, DamageInput input, Weapon weapon) {
        int elementLength = ElementSkill.values().length;
        double maxIdBoostNoSP = 0;
        double[] assignedIdBoost = new double[elementLength];
        for (ElementSkill elementSkill : ElementSkill.values()) {
            int i = elementSkill.ordinal();
            double elementalIdBoost = Math.max(0, getSkillImprovement(input.skills[i]) / 100);
            double extra = input.elemental[i];
            elementalIdBoost += extra;
            if (elementalIdBoost > maxIdBoostNoSP) maxIdBoostNoSP = elementalIdBoost;
            assignedIdBoost[i] = Math.max(0, getSkillImprovement(input.skills[i] + input.extraSkillPoints) / 100) + extra;
        }
        int skillIndex = 0;
        DamageOutput best = null;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            if (assignedIdBoost[skillIndex] > maxIdBoostNoSP) {
                DamageOutput out = getDamage(spell, input, weapon, elementSkill.getPowder());
                if (best == null || out.dpsWithRaw() > best.dpsWithRaw()) best = out;
            } else if (best == null && assignedIdBoost[skillIndex] == maxIdBoostNoSP) {
                best = getDamage(spell, input, weapon, elementSkill.getPowder());
            }
            skillIndex++;
        }
        //todo test with this
        // System.err.println("BuildMath had negative extraSkillPoints. There's room for optimizing.");
        return Objects.requireNonNullElseGet(best, () -> new DamageOutput(-1, -1,
                new double[ElementSkill.values().length], new double[ElementSkill.values().length],
                -1, -1,
                new double[ElementSkill.values().length], new double[ElementSkill.values().length],
                0, 0, new int[ElementSkill.values().length]));
    }

    @NotNull
    public static DamageOutput getDamage(Spell spell, DamageInput input, Weapon weapon, @Nullable Powder powder) {
        int elementSkillsLength = ElementSkill.values().length;


        double neutralLowerFinal = 0, neutralUpperFinal = 0, neutralLowerCritFinal = 0, neutralUpperCritFinal = 0, rawSpellFinal = 0;
        double[] elementalLowerFinal = new double[elementSkillsLength];
        double[] elementalUpperFinal = new double[elementSkillsLength];
        double[] elementalLowerCritFinal = new double[elementSkillsLength];
        double[] elementalUpperCritFinal = new double[elementSkillsLength];
        int[] skills = null;
        for (SpellPart spellPart : spell.spellParts) {
            if (spell == Spell.ARROW_STORM && input.hawkeye)
                spellPart = SpellPart.HAWKEYE;
            DamageOutput spellPartOutput = spellPartDamage(spellPart, input, weapon, powder, skills);
            if (skills == null) {
                skills = spellPartOutput.getSkills();
            }
            rawSpellFinal += spellPartOutput.raw;
            neutralLowerFinal += spellPartOutput.neutralLower;
            neutralUpperFinal += spellPartOutput.neutralUpper;
            neutralLowerCritFinal += spellPartOutput.neutralLowerCrit;
            neutralUpperCritFinal += spellPartOutput.neutralUpperCrit;
            for (int i = 0; i < elementSkillsLength; i++) {
                elementalLowerFinal[i] += spellPartOutput.elementalLower[i];
                elementalUpperFinal[i] += spellPartOutput.elementalUpper[i];
                elementalLowerCritFinal[i] += spellPartOutput.elementalLowerCrit[i];
                elementalUpperCritFinal[i] += spellPartOutput.elementalUpperCrit[i];
            }
        }

        final float critChance = skills == null ? 0 : getSkillImprovement(ElementSkill.THUNDER.getSkill(skills));
        return new DamageOutput(neutralLowerFinal, neutralUpperFinal, elementalLowerFinal, elementalUpperFinal,
                neutralLowerCritFinal, neutralUpperCritFinal, elementalLowerCritFinal, elementalUpperCritFinal,
                critChance / 100, rawSpellFinal, skills);
    }

    private static DamageOutput spellPartDamage(SpellPart spellPart, DamageInput input, Weapon weapon, Powder powder, int[] skills) {
        double neutralLowerOriginal = weapon.damage.getKey();
        double neutralUpperOriginal = weapon.damage.getValue();
        int elementSkillsLength = ElementSkill.values().length;
        // get the base damage
        double[] elementalLower = new double[elementSkillsLength];
        double[] elementalUpper = new double[elementSkillsLength];
        int i = 0;
        for (Pair<Integer, Integer> elemental : weapon.elemental) {
            elementalLower[i] = elemental.getKey();
            elementalUpper[i++] = elemental.getValue();
        }
        // multiply by spell elemental multiplier
        int totalBaseDmg = 0;
        double multiplier = 1;

        for (i = 0; i < elementSkillsLength; i++) {
            double elementalMultiplier = spellPart.elemental[i];
            multiplier -= elementalMultiplier;
            elementalLower[i] += neutralLowerOriginal * elementalMultiplier;
            elementalUpper[i] += neutralUpperOriginal * elementalMultiplier;
            totalBaseDmg += elementalLower[i] + elementalUpper[i];
        }
        multiplier = Math.max(0, multiplier);
        if (powder != null) {
            i = powder.getElement().ordinal();
            double powderMultiplier = Math.min(multiplier, powder.getPerc() * weapon.sockets);
            multiplier -= powderMultiplier;
            elementalLower[i] += neutralLowerOriginal * powderMultiplier + powder.getLower() * weapon.sockets;
            elementalUpper[i] += neutralUpperOriginal * powderMultiplier + powder.getUpper() * weapon.sockets;
        }
        double neutralLower = neutralLowerOriginal * multiplier;
        double neutralUpper = neutralUpperOriginal * multiplier;
        totalBaseDmg += neutralLowerOriginal + neutralUpperOriginal;

        // figure out where to assign the skill points
        double[] elementEffectiveness = new double[elementSkillsLength];
        for (ElementSkill elementSkill : ElementSkill.values()) {
            i = elementSkill.ordinal();
            if (elementSkill == ElementSkill.THUNDER || elementSkill == ElementSkill.EARTH) {
                elementEffectiveness[i] += 1;
            }
            elementEffectiveness[i] += ((elementalLower[i] + elementalUpper[i]) / (double) totalBaseDmg);
        }
        if (skills == null)
            skills = findMaximum(elementEffectiveness, input.skills, input.extraSkillPoints, input.extraSkillsPerElement);

        // idBoost
        double idBoost = 1 + input.spellDamage;
        idBoost = Math.max(0, idBoost);

        // strengthBoost
        float strengthBoost = 1 + getSkillImprovement(ElementSkill.EARTH.getSkill(skills)) / 100;
        double critBoost = Math.max(0, strengthBoost + 1);
        strengthBoost = Math.max(0, strengthBoost);

        double[] elementalLowerCrit = new double[elementSkillsLength];
        double[] elementalUpperCrit = new double[elementSkillsLength];
        System.arraycopy(elementalLower, 0, elementalLowerCrit, 0, elementSkillsLength);
        System.arraycopy(elementalUpper, 0, elementalUpperCrit, 0, elementSkillsLength);

        multiplier = weapon.attackSpeed.modifier() * spellPart.damage;
        neutralLower *= strengthBoost * idBoost * multiplier;
        neutralUpper *= strengthBoost * idBoost * multiplier;
        double neutralLowerCrit = neutralLower * critBoost * idBoost * multiplier;
        double neutralUpperCrit = neutralUpper * critBoost * idBoost * multiplier;
        for (i = 0; i < elementSkillsLength; i++) {
            double elementalIdBoost = Math.max(0, idBoost + Math.max(0, getSkillImprovement(skills[i]) / 100) + input.elemental[i]);
            double idBoostElemental = strengthBoost * elementalIdBoost * multiplier;
            double idBoostElementalCrit = critBoost * elementalIdBoost * multiplier;
            elementalLower[i] = Math.floor(elementalLower[i] * idBoostElemental);
            elementalUpper[i] = Math.floor(elementalUpper[i] * idBoostElemental);
            elementalLowerCrit[i] = Math.floor(elementalLowerCrit[i] * idBoostElementalCrit);
            elementalUpperCrit[i] = Math.floor(elementalUpperCrit[i] * idBoostElementalCrit);
        }
        final float critChance = getSkillImprovement(ElementSkill.THUNDER.getSkill(skills)) / 100;
        double rawSpell = input.spellDamageRaw * spellPart.damage;
        return new DamageOutput(
                neutralLower,
                neutralUpper,
                elementalLower,
                elementalUpper,
                neutralLowerCrit,
                neutralUpperCrit,
                elementalLowerCrit,
                elementalUpperCrit,
                critChance,
                rawSpell,
                skills);
    }


    private static int[] findMaximum(double[] elementEffectiveness, int[] skills, int extraSkillPoints,
                                     int[] extraSkillsPerElement) {
        if (extraSkillPoints == 0) {
            return skills;
        }
        int varCount = 0;
        for (double effectiveness : elementEffectiveness) {
            if (effectiveness != 0) varCount++;
        }
        double[] elementEffectivenessWithoutZero = new double[varCount];
        int[] skillsWithoutZero = new int[varCount];
        int[] extraSkillsPerElementWithoutZero = new int[varCount];
        int[] skillsToWithoutZero = new int[skills.length];
        int ei = 0;
        for (int i = 0; i < elementEffectiveness.length; i++) {
            double val = elementEffectiveness[i];
            if (val != 0) {
                elementEffectivenessWithoutZero[ei] = val;
                skillsWithoutZero[ei] = skills[i];
                extraSkillsPerElementWithoutZero[ei] = extraSkillsPerElement[i] + skills[i];
                skillsToWithoutZero[i] = ei++;
            } else {
                skillsToWithoutZero[i] = -1;
            }
        }
        int[] skillsMinWithoutZero = Arrays.copyOf(skillsWithoutZero, varCount);
        int extraToEach = extraSkillPoints / varCount;
        int extraLeft = extraSkillPoints % varCount;
        int atMax = 0;
        for (int i = 0; i < varCount; i++) {
            skillsWithoutZero[i] += extraToEach;
            if (skillsWithoutZero[i] > extraSkillsPerElementWithoutZero[i]) {
                atMax++;
                extraLeft += skillsWithoutZero[i] - extraSkillsPerElementWithoutZero[i];
                skillsWithoutZero[i] = extraSkillsPerElementWithoutZero[i];
            }
        }
        while (extraLeft > varCount) {
            int toDistribute = varCount - atMax;
            if (toDistribute == 0) {
                // this is probably very rare, but if it does happen, just ditch the extra skill points because they
                // literally do nothing to help
                // map the skills back to 5 index
                int[] newSkills = new int[skills.length];
                for (int i = 0; i < skills.length; i++) {
                    int index = skillsToWithoutZero[i];
                    if (index == -1) {
                        newSkills[i] = skills[i];
                    } else {
                        newSkills[i] = skillsWithoutZero[skillsToWithoutZero[i]];
                    }
                }
                return newSkills;
            }
            extraToEach = extraLeft / toDistribute;
            extraLeft = extraLeft % toDistribute;
            for (int i = 0; i < varCount; i++) {
                if (skillsWithoutZero[i] != extraSkillsPerElementWithoutZero[i]) {
                    skillsWithoutZero[i] += extraToEach;
                    if (skillsWithoutZero[i] > extraSkillsPerElementWithoutZero[i]) {
                        atMax++;
                        extraLeft += skillsWithoutZero[i] - extraSkillsPerElementWithoutZero[i];
                        skillsWithoutZero[i] = extraSkillsPerElementWithoutZero[i];
                    }
                }
            }
        }
        for (int i = 0; i < extraLeft; i++) skills[i]++;
        double[] currentIdBonus = new double[varCount];
        for (int i = 0; i < varCount; i++) {
            double extraBonus = elementEffectivenessWithoutZero[i] * getSkillImprovement(skillsWithoutZero[i]);
            currentIdBonus[i] = extraBonus;
        }
        // keep taking baby steps on the graph until you reach a maxima
        while (true) {
            int removeFromMeIndex = 0;
            double removeFromMeVal = Double.MAX_VALUE;
            for (int i = 0; i < varCount; i++) {
                if (skillsMinWithoutZero[i] != skillsWithoutZero[i]) {
                    double productivenessOfMe = currentIdBonus[i] - (elementEffectivenessWithoutZero[i] * getSkillImprovement(skillsWithoutZero[i] - 1));
                    if (productivenessOfMe < removeFromMeVal) {
                        removeFromMeVal = productivenessOfMe;
                        removeFromMeIndex = i;
                    }
                }
            }
            double[] idBonus = new double[varCount];
            for (int i = 0; i < varCount; i++) {
                idBonus[i] = elementEffectivenessWithoutZero[i] * getSkillImprovement(skillsWithoutZero[i] + 1);
            }
            double topDifferenceVal = -1;
            int topDifferenceIndex = 0;
            for (int i = 0; i < varCount; i++) {
                double difference = idBonus[i] - currentIdBonus[i];
                if (difference > topDifferenceVal) {
                    topDifferenceIndex = i;
                    topDifferenceVal = difference;
                }
            }
            if (topDifferenceVal <= 0)
                break;
            currentIdBonus = idBonus;
            skillsWithoutZero[removeFromMeIndex]--;
            skillsWithoutZero[topDifferenceIndex]++;
        }
        // map the skills back to 5 index
        int[] newSkills = new int[skills.length];
        for (int i = 0; i < skills.length; i++) {
            int index = skillsToWithoutZero[i];
            if (index == -1) {
                newSkills[i] = skills[i];
            } else {
                newSkills[i] = skillsWithoutZero[skillsToWithoutZero[i]];
            }
        }
        return newSkills;
    }

    public static DamageOutput getDamage(DamageInput input, Weapon weapon) {
        int elementLength = ElementSkill.values().length;
        double maxIdBoostNoSP = 0;
        double[] assignedIdBoost = new double[elementLength];
        int i = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            double elementalIdBoost = Math.max(0, getSkillImprovement(input.skills[i]) / 100);
            double extra = input.elemental[i];
            if (elementSkill == ElementSkill.THUNDER || elementSkill == ElementSkill.EARTH)
                extra++;
            elementalIdBoost += extra;
            if (elementalIdBoost > maxIdBoostNoSP) maxIdBoostNoSP = elementalIdBoost;
            assignedIdBoost[i] = Math.max(0, getSkillImprovement(input.skills[i] + input.extraSkillPoints) / 100) + extra;
            i++;
        }
        int skillIndex = 0;
        DamageOutput best = null;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            if (assignedIdBoost[skillIndex] > maxIdBoostNoSP) {
                DamageOutput out = getDamage(input, weapon, elementSkill.getPowder());
                if (best == null || out.dpsWithRaw() > best.dpsWithRaw()) best = out;
            } else if (best == null && assignedIdBoost[skillIndex] == maxIdBoostNoSP) {
                best = getDamage(input, weapon, elementSkill.getPowder());
            }
            skillIndex++;
        }
        if (best == null) {
            System.err.println("BuildMath had negative extraSkillPoints. There's room for optimizing.");
            return new DamageOutput(-1, -1,
                    new double[ElementSkill.values().length], new double[ElementSkill.values().length],
                    -1, -1,
                    new double[ElementSkill.values().length], new double[ElementSkill.values().length],
                    0, 0, new int[ElementSkill.values().length]);
        }
        return best;
    }

    public static DamageOutput getDamage(DamageInput input, Weapon weapon, Powder powder) {
        double neutralLower = weapon.damage.getKey();
        double neutralUpper = weapon.damage.getValue();
        int elementSkillsLength = ElementSkill.values().length;
        double[] elementalLower = new double[elementSkillsLength];
        double[] elementalUpper = new double[elementSkillsLength];
        int i = 0;
        int totalBaseDmg = 0;
        // get the base damage
        for (Pair<Integer, Integer> elemental : weapon.elemental) {
            elementalLower[i] = elemental.getKey();
            elementalUpper[i] = elemental.getValue();
            totalBaseDmg += elementalLower[i] + elementalUpper[i];
            i++;
        }
        // multiply by spell elemental multiplier
        double multiplier = 1;

        if (powder != null) {
            i = powder.getElement().ordinal();
            double powderMultiplier = Math.min(multiplier, powder.getPerc() * weapon.sockets);
            multiplier -= powderMultiplier;
            elementalLower[i] += powder.getLower() * weapon.sockets;
            elementalUpper[i] += powder.getUpper() * weapon.sockets;
            elementalLower[i] += neutralLower * powderMultiplier;
            elementalUpper[i] += neutralUpper * powderMultiplier;
        }
        neutralLower *= Math.max(0, multiplier);
        neutralUpper *= Math.max(0, multiplier);
        totalBaseDmg += neutralLower + neutralUpper;

        // figure out where to assign the skill points
        double[] elementEffectiveness = new double[elementSkillsLength];
        i = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            if (elementSkill == ElementSkill.THUNDER || elementSkill == ElementSkill.EARTH) {
                elementEffectiveness[i] = 1 + ((elementalLower[i] + elementalUpper[i]) / (double) totalBaseDmg);
            } else {
                elementEffectiveness[i] = ((elementalLower[i] + elementalUpper[i]) / (double) totalBaseDmg);
            }
            i++;
        }
        int[] skills = findMaximum(elementEffectiveness, input.skills, input.extraSkillPoints, input.extraSkillsPerElement);

        double idBoost = 1 + input.mainDamage;
        idBoost += getSkillImprovement(ElementSkill.EARTH.getSkill(skills)) / 100f;

        double idBoostCrit = Math.max(0, idBoost + 1);
        idBoost = Math.max(0, idBoost);
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
            double elementalIdBoost = 1 + Math.max(0, getSkillImprovement(input.skills[i]) / 100);
            elementalIdBoost += input.elemental[i];
            elementalLower[i] = Math.floor(elementalLower[i] * (idBoost * elementalIdBoost));
            elementalUpper[i] = Math.floor(elementalUpper[i] * (idBoost * elementalIdBoost));
            elementalLowerCrit[i] = Math.floor(elementalLowerCrit[i] * (idBoostCrit * elementalIdBoost));
            elementalUpperCrit[i] = Math.floor(elementalUpperCrit[i] * (idBoostCrit * elementalIdBoost));
        }
        return new DamageOutput(neutralLower, neutralUpper, elementalLower, elementalUpper,
                neutralLowerCrit, neutralUpperCrit, elementalLowerCrit, elementalUpperCrit,
                getSkillImprovement(ElementSkill.THUNDER.getSkill(skills)) / 100, input.mainDamageRaw, input.attackSpeedModifier, skills);

    }

    public static void main(String[] args) throws SQLException {
        ServiceWynnbuilderItemDB.callWynnbuilderToGetItemDB(false);
        Weapon item = (Weapon) Item.getItem("Thrundacrack");
        if (item == null) return;
        final DamageInput damageInput = new DamageInput(
                -.07,
                -.28,
                241,
                0,
                new int[]{150, 69, -25, -24, 60},
                0,
                new int[]{0, 0, 0, 0, 0},
                new double[]{1.15, .1, -.21, .89, .13},
                Item.AttackSpeed.VERY_SLOW.modifier()
        );
        DamageOutput damage = getDamage(Spell.UPPERCUT, damageInput, item, Powder.THUNDER);
        DamageOutput subDmg = spellPartDamage(SpellPart.PART1_UPPERCUT, damageInput, item, Powder.THUNDER, null);
        subDmg.dpsNoRaw();
        System.out.println(subDmg.dpsNoRaw());
        System.out.println(damage);
        System.out.println(damage.dpsWithRaw());
    }
}

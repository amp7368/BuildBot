package apple.build.data;

import apple.build.BuildMain;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.data.constraints.answers.DamageOutput;
import apple.build.data.enums.ElementSkill;
import apple.build.data.enums.Powder;
import apple.build.data.enums.Spell;
import apple.build.sql.GetDB;
import apple.build.utils.Pair;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BuildMath {
    private static final int MAX_SKILL_POINT_ALLOCATION = 150;

    /**
     * gets the percent increase that a skill point of sp gives
     *
     * @param sp the skill points invested
     * @return the percent increase
     */
    public static float getSkillImprovement(int sp) {
        if (sp < 0) return 0;
        if (sp > MAX_SKILL_POINT_ALLOCATION) sp = MAX_SKILL_POINT_ALLOCATION;
        return Math.round((
                -0.0000000166 * Math.pow(sp, 4) + 0.0000122614 * Math.pow(sp, 3) - 0.0044972984 * Math.pow(sp, 2) + 0.9931907398 * sp + 0.0093811967
        ) * 10) / 10f; //idk what this formula is
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
                DamageOutput out = getDamage(spell, input, weapon, elementSkill.getPowder());
                if (best == null || out.dps() > best.dps()) best = out;
            } else if (best == null && assignedIdBoost[skillIndex] == maxIdBoostNoSP) {
                best = getDamage(spell, input, weapon, elementSkill.getPowder());
            }
            skillIndex++;
        }
        if (best == null) {
            System.err.println("BuildMath had negative extraSkillPoints. There's room for optimizing.");
            return new DamageOutput(-1, -1,
                    new double[ElementSkill.values().length], new double[ElementSkill.values().length],
                    -1, -1,
                    new double[ElementSkill.values().length], new double[ElementSkill.values().length],
                    0, 0);
        }
        return best;
    }

    @NotNull
    public static DamageOutput getDamage(Spell spell, DamageInput input, Weapon weapon, @Nullable Powder powder) {
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
        int totalBaseDmg = 0;

        for (i = 0; i < elementSkillsLength; i++) {
            double elementalMultiplier = spell.elemental[i];
            multiplier -= elementalMultiplier;
            elementalLower[i] += neutralLower * elementalMultiplier;
            elementalUpper[i] += neutralUpper * elementalMultiplier;
            totalBaseDmg += elementalLower[i] + elementalUpper[i];
        }
        if (powder != null) {
            i = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                if (powder.getElement() == elementSkill) {
                    double powderMultiplier = Math.min(multiplier, powder.getPerc() * weapon.sockets);
                    multiplier -= powderMultiplier;
                    elementalLower[i] += powder.getLower() * weapon.sockets;
                    elementalUpper[i] += powder.getUpper() * weapon.sockets;
                    elementalLower[i] += neutralUpper * powderMultiplier;
                    elementalUpper[i] += neutralUpper * powderMultiplier;
                    break;
                }
                i++;
            }
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

        // idBoost
        double idBoost = 1;
        idBoost += input.spellDamage;
        idBoost += getSkillImprovement(ElementSkill.EARTH.getSkill(skills) / 100);

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
            double elementalIdBoost = Math.max(0, getSkillImprovement(skills[i]) / 100) + input.elemental[i];
            double idBoostElemental = Math.max(0, idBoost + elementalIdBoost);
            double idBoostElementalCrit = Math.max(0, idBoostCrit + elementalIdBoost);
            elementalLower[i] *= idBoostElemental;
            elementalUpper[i] *= idBoostElemental;
            elementalLowerCrit[i] *= idBoostElementalCrit;
            elementalUpperCrit[i] *= idBoostElementalCrit;
        }
        multiplier = weapon.attackSpeed.modifier() * (spell.damage);
        if (input.hawkeye && spell == Spell.ARROW_STORM) {
            multiplier *= 5 / 3d;
        }
        neutralLower = Math.floor(neutralLower * multiplier);
        neutralUpper = Math.floor(neutralUpper * multiplier);
        neutralLowerCrit = Math.floor(neutralLowerCrit * multiplier);
        neutralUpperCrit = Math.floor(neutralUpperCrit * multiplier);
        for (i = 0; i < elementalLower.length; i++) {
            elementalLower[i] = Math.floor(elementalLower[i] * multiplier);
            elementalUpper[i] = Math.floor(elementalUpper[i] * multiplier);
            elementalLowerCrit[i] = Math.floor(elementalLowerCrit[i] * multiplier);
            elementalUpperCrit[i] = Math.floor(elementalUpperCrit[i] * multiplier);
        }
        double rawSpell = input.spellDamageRaw * (spell.damage);
        if (input.hawkeye && spell == Spell.ARROW_STORM) {
            rawSpell *= 5 / 3d;
        }
        return new DamageOutput(neutralLower, neutralUpper, elementalLower, elementalUpper,
                neutralLowerCrit, neutralUpperCrit, elementalLowerCrit, elementalUpperCrit,
                getSkillImprovement(input.dexterity) / 100, rawSpell);
    }

    private static final AtomicInteger test = new AtomicInteger();

    private static int[] findMaximum(double[] elementEffectiveness, int[] skills, int extraSkillPoints, int[] extraSkillsPerElement) {
        int id = test.getAndIncrement();
//        System.out.println(id + " enter");
        if (extraSkillPoints == 0) {
//            System.out.println(id + " exit");
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
                extraSkillsPerElementWithoutZero[ei] = extraSkillsPerElement[i];
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
//                System.out.println(id + " exit");
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
//        System.out.println(id + " exit");
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
                if (best == null || out.dps() > best.dps()) best = out;
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
                    0, 0);
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
        // get the base damage
        for (Pair<Integer, Integer> elemental : weapon.elemental) {
            elementalLower[i] = elemental.getKey();
            elementalUpper[i++] = elemental.getValue();
        }
        // multiply by spell elemental multiplier
        double multiplier = 1;
        int totalBaseDmg = 0;

        if (powder != null) {
            i = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                if (powder.getElement() == elementSkill) {
                    double powderMultiplier = Math.min(multiplier, powder.getPerc() * weapon.sockets);
                    multiplier -= powderMultiplier;
                    elementalLower[i] += powder.getLower() * weapon.sockets;
                    elementalUpper[i] += powder.getUpper() * weapon.sockets;
                    elementalLower[i] += neutralUpper * powderMultiplier;
                    elementalUpper[i] += neutralUpper * powderMultiplier;
                    break;
                }
                i++;
            }
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
        return new DamageOutput(neutralLower, neutralUpper, elementalLower, elementalUpper,
                neutralLowerCrit, neutralUpperCrit, elementalLowerCrit, elementalUpperCrit,
                getSkillImprovement(input.dexterity) / 100, input.mainDamageRaw, input.attackSpeedModifier);

    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        BuildMain.initialize();
        List<Item> items = GetDB.getAllItems(Item.ItemType.BOW);
        Weapon item = null;
        for (Item i : items) {
            if (i.name.equals("Divzer"))
                item = (Weapon) i;
        }
        if (item == null) return;
        DamageOutput damage = getDamage(new DamageInput(
                0,
                0,
                445,
                945,
                new int[]{152, 0, 0, 0, 0},
                0,
                new int[]{200, 100, 100, 100, 100},
                new double[]{0, 0, 0, 0, 0},
                Item.AttackSpeed.toModifier(Item.AttackSpeed.SUPER_FAST.speed)
        ), item);
        System.out.println(damage.dps());
    }
}

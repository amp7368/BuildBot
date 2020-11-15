package apple.build.data;

import apple.build.data.constraints.advanced_damage.BuildConstraintAdvancedDamage;
import apple.build.data.constraints.advanced_skill.BuildConstraintAdvancedSkills;
import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.data.constraints.answers.DamageInput;
import apple.build.data.constraints.answers.DamageOutput;
import apple.build.data.constraints.general.BuildConstraintGeneral;
import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

import java.math.BigInteger;
import java.util.*;

public class BuildGenerator {
    private final int layer;
    private List<Item>[] allItems;
    private final List<BuildGenerator> subGenerators = new ArrayList<>();
    private static long test = System.currentTimeMillis();
    private final List<BuildConstraintGeneral> constraints;
    private final List<BuildConstraintAdvancedSkills> constraintsAdvancedSkill;
    private final List<BuildConstraintAdvancedDamage> constraintsAdvancedDamage;

    /**
     * makes a generator for every build possible with the given items
     *
     * @param allItems the items to build with (weapon must be last)
     */
    public BuildGenerator(List<Item>[] allItems) {
        this.allItems = allItems;
        this.layer = 0;
        this.constraints = new ArrayList<>();
        this.constraintsAdvancedSkill = new ArrayList<>();
        this.constraintsAdvancedDamage = new ArrayList<>();
    }

    private BuildGenerator(List<Item>[] subItems, List<BuildConstraintGeneral> constraints,
                           List<BuildConstraintAdvancedSkills> constraintsAdvancedSkill,
                           List<BuildConstraintAdvancedDamage> constraintsAdvancedDamage, int layer) {
        this.allItems = subItems;
        this.constraints = constraints;
        this.constraintsAdvancedSkill = constraintsAdvancedSkill;
        this.constraintsAdvancedDamage = constraintsAdvancedDamage;
        this.layer = layer;
    }

    public void addConstraint(BuildConstraintGeneral constraint) {
        this.constraints.add(constraint);
    }

    public void addConstraint(BuildConstraintAdvancedSkills constraint) {
        this.constraintsAdvancedSkill.add(constraint);
    }

    public void addConstraint(BuildConstraintAdvancedDamage constraint) {
        this.constraintsAdvancedDamage.add(constraint);
    }

    /**
     * generates for the top level
     */
    public void generate(Set<ElementSkill> archetype) {
        filterOnBadArchetype(archetype);
        if (isFail()) return;
        filterOnBadContribution();
        if (isFail()) return;
        filterOnConstraints();
        if (isFail()) return;
        filterOnAdvancedSkillConstraints();
        if (isFail()) return;
//        filterOnTranslationConstraints();
//        if (isFail()) return;
        breakApart();
        int i = 0;
        System.out.println(size());
        System.out.println("time " + (System.currentTimeMillis() - test) + " | " + i++ + "/" + size());
        for (BuildGenerator generator : subGenerators) {
            long now = System.currentTimeMillis();
            generator.generate(archetype, 9);
            System.out.println("time " + (now - test) + " | " + i++ + "/" + size());
        }
        subGenerators.removeIf(generator -> generator.size().equals(BigInteger.ZERO));

    }


    /**
     * filters item pool as much as possible, then generates all possible builds
     */
    public void generate(Set<ElementSkill> archetype, int layerToStop) {
        if (layer == layerToStop) return;
        filterOnConstraints();
        filterOnAdvancedSkillConstraints();
        filterWeaponOnAdvancedDamageConstraints();
        filterOnSkillReqs(archetype);
        if (isFail()) return;
        breakApart();
        subGenerators.forEach(buildGenerator -> buildGenerator.generate(archetype, layerToStop));
        subGenerators.removeIf(generator -> generator.size().equals(BigInteger.ZERO));
    }

    /**
     * filters item pool based on whether the item is possible given the requirements of damage
     */
    private void filterWeaponOnAdvancedDamageConstraints() {
        int size = allItems.length;
        int elementSize = ElementSkill.values().length;
        int[] spellDmgAll = new int[size];
        int[] spellDmgRawAll = new int[size];
        int[] mainDmgAll = new int[size];
        int[] mainDmgRawAll = new int[size];
        double[] attackSpeedModifierAll = new double[size];

        int[][] elementalDamageAll = new int[size][elementSize];
        int[][] skillsAll = new int[size][elementSize];
        int[][] requiredSkillsAll = new int[size][elementSize];
        int weaponIndex = size - 1;
        // skip weapon
        for (int typePieceIndex = 0; typePieceIndex < weaponIndex; typePieceIndex++) {
            // find max of all these
            int spellDmg = 0;
            int spellDmgRaw = 0;
            int mainDmg = 0;
            int mainDmgRaw = 0;
            int attackSpeed = 0;

            for (Item item : allItems[typePieceIndex]) {
                spellDmg = Math.max(spellDmg, item.getId("spellDamage"));
                spellDmgRaw = Math.max(spellDmgRaw, item.getId("spellDamageRaw"));
                mainDmg = Math.max(mainDmg, item.getId("damageBonus"));
                mainDmgRaw = Math.max(mainDmgRaw, item.getId("damageBonusRaw"));
                attackSpeed = Math.max(attackSpeed, item.getId("attackSpeedBonus"));
            }
            spellDmgAll[typePieceIndex] = spellDmg;
            spellDmgRawAll[typePieceIndex] = spellDmgRaw;
            mainDmgAll[typePieceIndex] = mainDmg;
            mainDmgRawAll[typePieceIndex] = mainDmgRaw;
            attackSpeedModifierAll[typePieceIndex] = attackSpeed;
        }
        // don't combine vvv and ^^^ for optimization
        for (int typePieceIndex = 0; typePieceIndex < weaponIndex; typePieceIndex++) {
            // find max of all these
            int[] elementalDamage = elementalDamageAll[typePieceIndex];
            int[] skills = skillsAll[typePieceIndex];
            int[] requiredSkills = requiredSkillsAll[typePieceIndex];

            int elementIndex = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                for (Item item : allItems[typePieceIndex]) {
                    elementalDamage[elementIndex] = Math.max(elementalDamage[elementIndex],
                            item.getId("bonus" + Pretty.uppercaseFirst(elementSkill.name().toLowerCase() + "Damage")));
                    elementalDamage[elementIndex] = Math.max(elementalDamage[elementIndex],
                            item.getId("bonus" + Pretty.uppercaseFirst(elementSkill.name().toLowerCase() + "Damage")));
                }
                skills[elementIndex] = BuildUtils.bestSkillPoints(elementSkill, allItems[typePieceIndex]);
                requiredSkills[elementIndex] = BuildUtils.bestSkillReqs(elementSkill, allItems[typePieceIndex]);
                elementIndex++;
            }
        }

        int spellDmg = 0;
        int spellDmgRaw = 0;
        int mainDmg = 0;
        int mainDmgRaw = 0;
        int attackSpeed = 0;
        int[] elementalDamage = new int[elementSize];
        int[] skills = new int[elementSize];
        int[] requiredSkills = new int[elementSize];
        for (int i = 0; i < size; i++) {
            if (weaponIndex == i) continue;
            spellDmg += spellDmgAll[i];
            spellDmgRaw += spellDmgRawAll[i];
            mainDmg += mainDmgAll[i];
            mainDmgRaw += mainDmgRawAll[i];
            attackSpeed += attackSpeedModifierAll[i];
            for (int j = 0; j < elementSize; j++) {
                elementalDamage[j] += elementalDamageAll[i][j];
                skills[j] += skillsAll[i][j];
                requiredSkills[j] = Math.max(requiredSkillsAll[i][j], requiredSkills[j]);
            }
        }
        int finalSpellDmg = spellDmg;
        int finalSpellDmgRaw = spellDmgRaw;
        int finalMainDmg = mainDmg;
        int finalMainDmgRaw = mainDmgRaw;
        int finalAttackSpeed = attackSpeed;
        allItems[weaponIndex].removeIf(item -> {
            // make sure it's read only for the arrays
            int extraSkillPoints = Item.SKILLS_FOR_PLAYER;
            int[] mySkills = new int[elementSize];
            double[] myElemental = new double[elementSize];

            int i = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                int requiredSkill = Math.max(requiredSkills[i], item.getRequiredSkill(elementSkill));
                int skill = skills[i];
                if (requiredSkill != 0 && requiredSkill > skill) {
                    extraSkillPoints -= requiredSkill - skill;
                    mySkills[i] = requiredSkill;
                } else {
                    mySkills[i] = skill;
                }
                mySkills[i] += item.getSkill(elementSkill); // do this after because weapon skill doesn't help with reqs
                myElemental[i] = ((double) (elementalDamage[i] + item.getId("bonus" + Pretty.uppercaseFirst(elementSkill.name().toLowerCase() + "Damage")))) / 100d;
                i++;
            }
            int mySpellDmg = finalSpellDmg;
            int mySpellDmgRaw = finalSpellDmgRaw;
            int myMainDmg = finalMainDmg;
            int myMainDmgRaw = finalMainDmgRaw;
            int myAttackSpeed = finalAttackSpeed;
            mySpellDmg += item.getId("spellDamage");
            mySpellDmgRaw += item.getId("spellDamageRaw");
            myMainDmg += item.getId("damageBonus");
            myMainDmgRaw += item.getId("damageBonusRaw");
            myAttackSpeed += item.getId("attackSpeedBonus");
            myAttackSpeed += ((Weapon) item).attackSpeed.speed;
            double myFinalSpellDmg = mySpellDmg / 100.0;
            double myFinalMainDmg = myMainDmg / 100.0;
            DamageInput input = new DamageInput(myFinalSpellDmg, myFinalMainDmg, mySpellDmgRaw, myMainDmgRaw, mySkills, extraSkillPoints, myElemental, Item.AttackSpeed.toModifier(myAttackSpeed));
            for (BuildConstraintAdvancedDamage constraint : constraintsAdvancedDamage) {
                if (!constraint.isValid(input, (Weapon) item)) return true;
            }
            return false;
        });
    }

    public BigInteger size() {
        BigInteger combinationsCount;
        if (allItems.length == 0) {
            combinationsCount = BigInteger.ZERO;
        } else {
            combinationsCount = BigInteger.ONE;
            for (List<Item> type : allItems) {
                combinationsCount = combinationsCount.multiply(BigInteger.valueOf(type.size()));
            }
        }
        for (BuildGenerator generator : subGenerators) {
            combinationsCount = combinationsCount.add(generator.size());
        }
        return combinationsCount;
    }

    private void breakApart() {
        int pieceIndex = -1;
        for (int i = 0; i < allItems.length; i++) {
            if (allItems[i].size() != 1) {
                pieceIndex = i;
                break;
            }
        }
        if (pieceIndex == -1) return;
        List<Item> items = allItems[pieceIndex];
        for (Item chosenItem : items) {
            List<Item>[] subItems = new List[allItems.length];
            for (int subIndex = 0; subIndex < subItems.length; subIndex++) {
                if (subIndex == pieceIndex) {
                    List<Item> smallList = new ArrayList<>(1);
                    smallList.add(chosenItem);
                    subItems[subIndex] = smallList;
                } else {
                    subItems[subIndex] = new ArrayList<>(allItems[subIndex]);
                }
            }
            subGenerators.add(new BuildGenerator(subItems, constraints, constraintsAdvancedSkill, constraintsAdvancedDamage, layer + 1));
        }
        allItems = new List[0];
    }

    /**
     * filters item pool based on whether with the best skill points available, make the build possible
     * (keep in mind this doesn't care about ordering of items. only that the skill points of the item being tested dont contribute)
     *
     * @param archetype the archetype we're making
     */
    private void filterOnSkillReqs(Set<ElementSkill> archetype) {
        int size = allItems.length;
        int elementSize = ElementSkill.values().length;
        int[][] skillsAll = new int[elementSize][size];
        int[][] requiredSkillsAll = new int[elementSize][size];

        for (int pieceIndex = 0; pieceIndex < size; pieceIndex++) {
            int i = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                skillsAll[i][pieceIndex] = BuildUtils.bestSkillPoints(elementSkill, allItems[pieceIndex]);
                requiredSkillsAll[i][pieceIndex] = BuildUtils.bestSkillReqs(elementSkill, allItems[pieceIndex]);
                i++;
            }
        }

        int[] requiredSkills = new int[elementSize];
        for (int i = 0; i < elementSize; i++) {
            for (int j = 0; j < size; j++) {
                requiredSkills[i] = Math.max(requiredSkills[i], requiredSkillsAll[i][j]);
            }
        }
        for (int pieceIndex = 0; pieceIndex < size; pieceIndex++) {
            int[] skills = new int[elementSize];
            for (int i = 0; i < size; i++) {
                if (i == pieceIndex) continue;
                for (int j = 0; j < elementSize; j++) {
                    skills[j] += skillsAll[j][i];
                }
            }
            allItems[pieceIndex].removeIf(item -> item.isSkillImpossible(
                    requiredSkills,
                    skills,
                    archetype
            ));
        }
    }

    /**
     * remove items that don't fit our archetype
     *
     * @param archetype the archetype we're fitting
     */
    private void filterOnBadArchetype(Set<ElementSkill> archetype) {
        for (List<Item> items : allItems) {
            Iterator<Item> itemIterator = items.iterator();
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                if (item.strength != 0) {
                    if (!archetype.contains(ElementSkill.EARTH)) {
                        itemIterator.remove();
                        continue;
                    }
                }
                if (item.agility != 0) {
                    if (!archetype.contains(ElementSkill.AIR)) {
                        itemIterator.remove();
                        continue;
                    }
                }
                if (item.intelligence != 0) {
                    if (!archetype.contains(ElementSkill.WATER)) {
                        itemIterator.remove();
                        continue;
                    }
                }
                if (item.defense != 0) {
                    if (!archetype.contains(ElementSkill.FIRE)) {
                        itemIterator.remove();
                        continue;
                    }
                }
                if (item.dexterity != 0) {
                    if (!archetype.contains(ElementSkill.THUNDER)) {
                        itemIterator.remove();
                        continue;
                    }
                }
            }
        }
    }

    /**
     * filters item pool based on if the item is possible given that all the other items would be optimal for the constraint
     */
    private void filterOnBadContribution() {
        for (List<Item> items : allItems) {
            items.removeIf(item -> {
                for (BuildConstraintGeneral constraint : constraints) {
                    if (constraint.contributes(item)) return false;
                }
                for (BuildConstraintAdvancedSkills constraint : constraintsAdvancedSkill) {
                    if (constraint.contributes(item)) return false;
                }
                if (BuildUtils.contributesToUseful(item)) return false;
                return true;
            });
        }
    }

    /**
     * filters items based on ranking the best items for this build
     * and if the item doesn't contribute to making it a "better build" ditch it
     */
    private void filterOnTranslationConstraints() {
        // if you have hpr 100, and speed 10, and then you have hpr 90, and speed 9,
        // you should ditch the 90,9 if you're only comparing on hpr and speed
        for (List<Item> items : allItems) {
            Set<Item> badItems = new HashSet<>();
            for (Item asBest : items) {
                for (Item item : items) {
                    if (!item.equals(asBest) && !badItems.contains(item)) {
                        boolean isCool = false;
                        for (BuildConstraintGeneral constraint : constraints) {
                            if (constraint.compare(asBest, item) <= 0) {
                                isCool = true;
                                break;
                            }
                        }
                        if (!isCool) badItems.add(item);
                    }
                }
            }
            items.removeAll(badItems);
        }
    }

    /**
     * filters item pool based on if the item is possible given that all the other items would be optimal for the constraint
     */
    private void filterOnConstraints() {
        for (BuildConstraintGeneral constraint : constraints) {
            for (int optimizingIndex = 0; optimizingIndex < allItems.length; optimizingIndex++) {
                List<Item> bestItems = new ArrayList<>();
                for (int index = 0; index < allItems.length; index++) {
                    if (optimizingIndex != index) {
                        Item bestItem = constraint.getBest(allItems[index]);
                        bestItems.add(bestItem);
                    }
                }
                allItems[optimizingIndex].removeIf(item -> !constraint.isValid(bestItems, item));
                if (allItems[optimizingIndex].isEmpty())
                    return;
            }
        }
    }

    /**
     * filters item pool based on if the item is possible given that all the other items would be optimal for the constraint
     */
    private void filterOnAdvancedSkillConstraints() {
        int elementSize = ElementSkill.values().length;
        int[] skillsAll = new int[elementSize];
        int[] requiredSkillsAll = new int[elementSize];
        for (List<Item> allItem : allItems) {
            int i = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                skillsAll[i] += BuildUtils.bestSkillPoints(elementSkill, allItem);
                requiredSkillsAll[i] = Math.max(BuildUtils.bestSkillReqs(elementSkill, allItem), requiredSkillsAll[i]);
                i++;
            }
        }
        int extraPoints = Item.SKILLS_FOR_PLAYER;
        for (int i = 0; i < skillsAll.length; i++) {
            if (requiredSkillsAll[i] != 0) {
                int sub = requiredSkillsAll[i] - skillsAll[i];
                if (sub > 0) {
                    skillsAll[i] = requiredSkillsAll[i];
                    extraPoints -= sub;
                }
            }
        }
        int finalExtraPoints = extraPoints;
        //todo it's possible to narrow down further
        // by recalculating skillsAll and requiredSkillsAll for each item using a double array and skipping the current item

        for (BuildConstraintAdvancedSkills constraint : constraintsAdvancedSkill) {
            for (int optimizingIndex = 0; optimizingIndex < allItems.length; optimizingIndex++) {
                List<Item> bestItems = new ArrayList<>();
                for (int index = 0; index < allItems.length; index++) {
                    if (optimizingIndex != index) {
                        Item bestItem = constraint.getBest(allItems[index]);
                        if (bestItem == null) return;
                        bestItems.add(bestItem);
                    }
                }
                allItems[optimizingIndex].removeIf(item -> !constraint.isValid(skillsAll, finalExtraPoints, bestItems, item));
                if (allItems[optimizingIndex].isEmpty())
                    return;
            }
        }
    }

    private boolean isFail() {
        for (List<Item> i : allItems) {
            if (i.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public List<Build> getBuilds() {
        if (allItems.length == 0) {
            List<Build> builds = new ArrayList<>();
            for (BuildGenerator generator : subGenerators) {
                builds.addAll(generator.getBuilds());
            }
            return builds;
        } else {
            return Collections.singletonList(new Build(allItems));
        }
    }

    private boolean c() {
        for (List<Item> items : allItems) {
            if (!d(items)) return false;
        }
        return true;
    }

    private boolean d(List<Item> items) {
        Set<String> set = new HashSet<>() {{
            add("Nighthawk");
            add("Boreal-Patterned Aegis");
            add("Cinderchain");
            add("Sine");
            add("Diamond Static Ring");
            add("Diamond Hydro Bracelet");
            add("Tenuto");
            add("Divzer");
        }};
        for (Item item : items) {
            if (set.contains(item.name)) return true;
        }
        return false;
    }
}

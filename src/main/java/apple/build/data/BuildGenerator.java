package apple.build.data;

import apple.build.data.constraints.advanced_skill.BuildConstraintAdvancedSkills;
import apple.build.data.constraints.general.BuildConstraintGeneral;
import apple.build.wynncraft.items.Item;

import java.math.BigInteger;
import java.util.*;

public class BuildGenerator {
    private final int layer;
    private List<Item>[] allItems;
    private final List<BuildGenerator> subGenerators = new ArrayList<>();
    private static long test = System.currentTimeMillis();
    private final List<BuildConstraintGeneral> constraints;
    private final List<BuildConstraintAdvancedSkills> constraintsAdvanced;

    public BuildGenerator(List<Item>[] allItems) {
        this.allItems = allItems;
        this.layer = 0;
        this.constraints = new ArrayList<>();
        this.constraintsAdvanced = new ArrayList<>();
    }

    private BuildGenerator(List<Item>[] subItems, List<BuildConstraintGeneral> constraints, List<BuildConstraintAdvancedSkills> constraintsAdvanced, int layer) {
        this.allItems = subItems;
        this.constraints = constraints;
        this.constraintsAdvanced = constraintsAdvanced;
        this.layer = layer;
    }

    public void addConstraint(BuildConstraintGeneral constraint) {
        this.constraints.add(constraint);
    }

    public void addConstraint(BuildConstraintAdvancedSkills advancedConstraint) {
        this.constraintsAdvanced.add(advancedConstraint);
    }

    /**
     * generates for the top level
     */
    public void generate(Set<ElementSkill> archetype) {
        filterOnBadContribution();
        if (isFail()) return;
        filterOnConstraints();
        if (isFail()) return;
        filterOnAdvancedConstraints();
        if (isFail()) return;
        filterOnTranslationConstraints();
        if (isFail()) return;
        breakApart();
        int i = 0;
        for (BuildGenerator generator : subGenerators) {
            long now = System.currentTimeMillis();
            generator.generate(archetype, 9);
            System.out.println("time " + (now - test) + " | " + i++ + "/" + subGenerators.size());
        }
    }

    /**
     * filters item pool as much as possible, then generates all possible builds
     */
    public void generate(Set<ElementSkill> archetype, int layerToStop) {
        if (layer == layerToStop) return;
        filterOnConstraints();
        filterOnAdvancedConstraints();
        filterOnSkillReqs(archetype);
        if (isFail()) return;
        breakApart();
        subGenerators.forEach(buildGenerator -> buildGenerator.generate(archetype, layerToStop));
        subGenerators.removeIf(generator -> generator.size().equals(BigInteger.ZERO));
    }

    private boolean c() {
        boolean t = false;
        for (List<Item> item : allItems) {
            for (Item item1 : item) {
                if (d(item1)) {
                    t = true;
                    break;
                }
            }
            if (!t) {
                return false;
            }
            t = false;
        }
        return true;
    }

    private boolean d(Item item1) {
        return (item1.name.equals("Ornate Shadow Cowl") ||
                item1.name.equals("Hetusol") ||
                item1.name.equals("Ophiuchus") ||
                item1.name.equals("Gaea-Hewn Boots") ||
                item1.name.equals("Diamond Hydro Ring") ||
                item1.name.equals("Yang") ||
                item1.name.equals("Dragon$s Eye Bracelet") ||
                item1.name.equals("Diamond Hydro Necklace") ||
                item1.name.equals("Nepta Floodbringer"));
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
            subGenerators.add(new BuildGenerator(subItems, constraints, constraintsAdvanced, layer + 1));
        }
        allItems = new List[0];
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
                for (BuildConstraintAdvancedSkills constraint : constraintsAdvanced) {
                    if (constraint.contributes(item)) return false;
                }
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
    private void filterOnAdvancedConstraints() {
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

        for (BuildConstraintAdvancedSkills constraint : constraintsAdvanced) {
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
}

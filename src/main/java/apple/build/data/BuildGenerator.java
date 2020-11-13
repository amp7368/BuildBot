package apple.build.data;

import apple.build.data.constraints.BuildConstraint;
import apple.build.wynncraft.items.Item;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BuildGenerator {
    private final int layer;
    private List<Item>[] allItems;
    private List<BuildGenerator> subGenerators = new ArrayList<>();
    private static long test = System.currentTimeMillis();
    private List<BuildConstraint> constraints = new ArrayList<>();
    private static AtomicInteger counter = new AtomicInteger(0);

    public BuildGenerator(List<Item>[] allItems) {
        this.allItems = allItems;
        this.layer = 0;
    }

    private BuildGenerator(List<Item>[] subItems, List<BuildConstraint> constraints, int layer) {
        this.allItems = subItems;
        this.constraints = constraints;
        this.layer = layer;
    }

    public void addConstraint(BuildConstraint constraint) {
        this.constraints.add(constraint);
    }

    /**
     * generates for the top level
     */
    public void generate(Set<ElementSkill> archetype) {
        filterOnBadContribution();
        if (isFail()) return;
        filterOnConstraints();
        if (isFail()) return;
        filterOnTranslationConstraints();
        if (isFail()) return;
        breakApart();
        int i = 0;
        for (BuildGenerator generator : subGenerators) {
            long now = System.currentTimeMillis();
            generator.generate(archetype, 4);
            System.out.println("time " + (now - test) + " | " + i++ + "/" + subGenerators.size());
        }
    }

    /**
     * filters item pool as much as possible, then generates all possible builds
     */
    public void generate(Set<ElementSkill> archetype, int layerToStop) {
        if (layer == layerToStop) return;
        filterOnConstraints();
        if (layer % 2 == 1 || layerToStop - 1 == layer)
            filterOnSkillReqs(archetype);
        if (isFail()) return;
        breakApart();
        subGenerators.forEach(buildGenerator -> buildGenerator.generate(archetype, layerToStop));
        subGenerators.removeIf(generator -> generator.size().equals(BigInteger.ZERO));
    }

    /**
     * filters item pool based on whether with the best skill points available, make the build possible
     * (keep in mind this doesn't care about ordering of items. only that the skill points of the item being tested dont contribute)
     *
     * @param archetype the archetype we're making
     */
    private void filterOnSkillReqs(Set<ElementSkill> archetype) {
        int size = allItems.length;
        int[] thunderAll = new int[size];
        int[] airAll = new int[size];
        int[] earthAll = new int[size];
        int[] waterAll = new int[size];
        int[] fireAll = new int[size];
        int[] requiredThunderAll = new int[size];
        int[] requiredAirAll = new int[size];
        int[] requiredEarthAll = new int[size];
        int[] requiredWaterAll = new int[size];
        int[] requiredFireAll = new int[size];

        for (int pieceIndex = 0; pieceIndex < size; pieceIndex++) {
            thunderAll[pieceIndex] = BuildUtils.bestSkillPoints(ElementSkill.THUNDER, allItems[pieceIndex]);
            airAll[pieceIndex] = BuildUtils.bestSkillPoints(ElementSkill.AIR, allItems[pieceIndex]);
            earthAll[pieceIndex] = BuildUtils.bestSkillPoints(ElementSkill.EARTH, allItems[pieceIndex]);
            waterAll[pieceIndex] = BuildUtils.bestSkillPoints(ElementSkill.WATER, allItems[pieceIndex]);
            fireAll[pieceIndex] = BuildUtils.bestSkillPoints(ElementSkill.FIRE, allItems[pieceIndex]);
            requiredThunderAll[pieceIndex] = BuildUtils.bestSkillReqs(ElementSkill.THUNDER, allItems[pieceIndex]);
            requiredAirAll[pieceIndex] = BuildUtils.bestSkillReqs(ElementSkill.AIR, allItems[pieceIndex]);
            requiredEarthAll[pieceIndex] = BuildUtils.bestSkillReqs(ElementSkill.EARTH, allItems[pieceIndex]);
            requiredWaterAll[pieceIndex] = BuildUtils.bestSkillReqs(ElementSkill.WATER, allItems[pieceIndex]);
            requiredFireAll[pieceIndex] = BuildUtils.bestSkillReqs(ElementSkill.FIRE, allItems[pieceIndex]);
        }

        for (int pieceIndex = 0; pieceIndex < size; pieceIndex++) {
            int thunder = 0;
            int air = 0;
            int earth = 0;
            int water = 0;
            int fire = 0;
            for (int i = 0; i < thunderAll.length; i++) {
                if (i == pieceIndex) continue;
                thunder += thunderAll[i];
                air += airAll[i];
                earth += earthAll[i];
                water += waterAll[i];
                fire += fireAll[i];
            }
            int finalThunder = thunder;
            int finalAir = air;
            int finalEarth = earth;
            int finalWater = water;
            int finalFire = fire;

            int thunderReq = 0;
            int airReq = 0;
            int earthReq = 0;
            int waterReq = 0;
            int fireReq = 0;
            for (int i = 0; i < thunderAll.length; i++) {
                thunderReq = Math.max(requiredThunderAll[i], thunderReq);
                airReq = Math.max(requiredAirAll[i], airReq);
                earthReq = Math.max(requiredEarthAll[i], earthReq);
                waterReq = Math.max(requiredWaterAll[i], waterReq);
                fireReq = Math.max(requiredFireAll[i], fireReq);
            }
            int finalThunderReq = thunderReq;
            int finalAirReq = airReq;
            int finalEarthReq = earthReq;
            int finalWaterReq = waterReq;
            int finalFireReq = fireReq;
            allItems[pieceIndex].removeIf(item -> item.isSkillImpossible(
                    finalThunderReq,
                    finalAirReq,
                    finalEarthReq,
                    finalWaterReq,
                    finalFireReq,
                    finalThunder,
                    finalAir,
                    finalEarth,
                    finalWater,
                    finalFire,
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
            subGenerators.add(new BuildGenerator(subItems, constraints, layer + 1));
        }
        allItems = new List[0];
    }

    /**
     * filters item pool based on if the item is possible given that all the other items would be optimal for the constraint
     */
    private void filterOnBadContribution() {
        for (List<Item> items : allItems) {
            items.removeIf(item -> {
                for (BuildConstraint constraint : constraints) {
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
                        for (BuildConstraint constraint : constraints) {
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
        for (BuildConstraint constraint : constraints) {
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

    private boolean isFail() {
        for (List<Item> i : allItems) {
            if (i.isEmpty()) {
//                System.out.println("failed");
                return true;
            }
        }
        return false;
    }
}

package apple.build.search;

import apple.build.Preindexing;
import apple.build.search.constraints.BuildConstraint;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.search.constraints.advanced_damage.BuildConstraintAdvancedDamage;
import apple.build.search.constraints.advanced_skill.BuildConstraintAdvancedSkills;
import apple.build.search.constraints.answers.DamageInput;
import apple.build.search.constraints.answers.SkillReqAnswer;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.BuildConstraintGeneral;
import apple.build.search.enums.ElementSkill;
import apple.build.sql.PreFilter;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.ItemIdIndex;
import apple.build.wynncraft.items.Weapon;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BuildGenerator {
    private static final long TOO_LONG_THRESHOLD = 3 * 1000;
    private final int layer;
    private ArrayList<Item>[] allItems;
    private ArrayList<BuildGenerator> subGenerators = new ArrayList<>();
    private final BuildGeneratorSettings settings;
    private Set<Build> extraBuilds = null;

    private boolean shouldSaveResult;
    private GenerationPhase phase = GenerationPhase.START;
    private boolean startedWithExactMatch;
    private int topLayerIndex;
    private int initalSubGeneratorsSize = Integer.MAX_VALUE;
    private long timeToCompute = 0;
    private boolean overrideShouldSave = false;

    /**
     * makes a generator for every build possible with the given items
     *
     * @param allItems the items to build with (weapon must be last)
     */
    public BuildGenerator(ArrayList<Item>[] allItems, Set<ElementSkill> archetype) {
        this.allItems = allItems;
        this.layer = 0;
        this.settings = new BuildGeneratorSettings(archetype);
    }

    private BuildGenerator(ArrayList<Item>[] subItems, BuildGeneratorSettings settings, int layer) {
        this.allItems = subItems;
        this.settings = settings;
        this.layer = layer;
    }

    public void addConstraint(BuildConstraintGeneral constraint) {
        this.settings.addConstraint(constraint);
    }

    public void addConstraint(BuildConstraintAdvancedSkills constraint) {
        this.settings.addConstraint(constraint);
    }

    public void addConstraint(BuildConstraintAdvancedDamage constraint) {
        this.settings.addConstraint(constraint);
    }

    public void addConstraint(BuildConstraintExclusion constraint) {
        this.settings.addConstraint(constraint);
    }

    public List<ConstraintSimplified> getSimplifiedConstraints() {
        List<ConstraintSimplified> simples = new ArrayList<>();
        for (BuildConstraint constraint : settings.getConstraintsAll())
            simples.add(constraint.getSimplified());
        return simples;
    }

    public List<BuildConstraint> getConstraints() {
        return settings.getAllConstraints();
    }

    public ExitType runFor(long desiredMillisToRun, long maxMillisToRun, Consumer<Double> onUpdate) {
        this.settings.getDesiredTimeToStop().set(System.currentTimeMillis() + desiredMillisToRun);
        this.settings.getMaxTimeToStop().set(System.currentTimeMillis() + maxMillisToRun);
        return this.generateTopLevel(onUpdate);
    }

    /**
     * generates for the top level
     *
     * @param onUpdate onProgress give progress to onUpdate
     */
    private ExitType generateTopLevel(Consumer<Double> onUpdate) {
        long timingStart = System.currentTimeMillis();
        if (phase == GenerationPhase.START) {
            if (isFail()) return ExitType.COMPLETE;
            if (overrideShouldSave)
                startedWithExactMatch = true;
            else
                startedWithExactMatch = PreFilter.filterItemPool(this, allItems[allItems.length - 1].get(0).type);
            if (isFail()) return ExitType.COMPLETE;
            filterOnBadArchetype();
            if (isFail()) return ExitType.COMPLETE;
            filterOnConstraints();
            if (isFail()) return ExitType.COMPLETE;
            filterOnAdvancedSkillConstraints();
            if (isFail()) return ExitType.COMPLETE;
            breakApart();

            subGenerators.removeIf(BuildGenerator::filterLower);
            if (subGenerators.isEmpty()) return ExitType.COMPLETE;
            this.initalSubGeneratorsSize = subGenerators.size();
            phase = GenerationPhase.SUB_GENERATORS;
            if (System.currentTimeMillis() >= this.settings.getDesiredTimeToStop().get()) {
                timeToCompute += System.currentTimeMillis() - timingStart;
                return ExitType.INCOMPLETE;
            }
        }
        if (phase == GenerationPhase.SUB_GENERATORS) {
            int layerIndex = 0;
            for (BuildGenerator subGenerator : subGenerators) {
                ExitType exit = subGenerator.generateLowerLevel();
                if (exit != ExitType.COMPLETE) {
                    if (exit == ExitType.HARD_TIMEOUT && topLayerIndex == 0) {
                        exit = ExitType.IMPOSSIBLE;
                    }
                    timeToCompute += System.currentTimeMillis() - timingStart;
                    return exit;
                }
                if (System.currentTimeMillis() >= this.settings.getDesiredTimeToStop().get())
                    return ExitType.INCOMPLETE;
                int iValue = topLayerIndex = Math.max(topLayerIndex, ++layerIndex);
                onUpdate.accept(progress());
            }
            phase = GenerationPhase.FINISH_SUB_GENERATORS;
        }
        subGenerators.removeIf(BuildGenerator::isEmpty);
        timeToCompute += System.currentTimeMillis() - timingStart;
        shouldSaveResult = !startedWithExactMatch && timeToCompute > TOO_LONG_THRESHOLD;
        getBuildsAll();
        phase = GenerationPhase.FINISHED;
        return ExitType.COMPLETE;
    }


    /**
     * filters item pool as much as possible, then generates all possible builds
     */
    public ExitType generateLowerLevel() {
        if (phase == GenerationPhase.START) {
            if (isFail()) return ExitType.COMPLETE;
            Thread.currentThread().setPriority(Math.min(layer + 2, 10));

            breakApart();
            subGenerators.removeIf(BuildGenerator::filterLower);
            if (subGenerators.isEmpty()) return ExitType.COMPLETE;
            subGenerators.trimToSize();
            phase = GenerationPhase.SUB_GENERATORS;
        }
        if (phase == GenerationPhase.SUB_GENERATORS) {
            int threadsAvailable = GeneratorThreadPool.getThreadsAvailable();
            if (threadsAvailable <= 1) {
                for (BuildGenerator buildGenerator : subGenerators) {
                    ExitType exit = buildGenerator.generateLowerLevel();
                    if (exit != ExitType.COMPLETE) return exit;
                    if (GeneratorThreadPool.shouldStop(this.settings.getMaxTimeToStop())) {
                        return ExitType.HARD_TIMEOUT;
                    }
                }
            } else {
                ExitType exit = new GeneratorThreadPool(subGenerators, BuildGenerator::generateLowerLevel, threadsAvailable, this.settings.getMaxTimeToStop()).waitForCompletion();
                if (exit != ExitType.COMPLETE) return exit;
            }
            phase = GenerationPhase.FINISH_SUB_GENERATORS;
        }
        if (phase == GenerationPhase.FINISH_SUB_GENERATORS) {
            subGenerators.removeIf(BuildGenerator::isEmpty);
            if (subGenerators.isEmpty()) return ExitType.COMPLETE;
            subGenerators.trimToSize();
            if (size().compareTo(BigInteger.valueOf(100)) < 0) {
                Collection<Build> builds = getBuildsAll();
                if (!builds.isEmpty()) {
                    if (extraBuilds == null) extraBuilds = new HashSet<>();
                    extraBuilds.addAll(builds);
                }
                subGenerators = new ArrayList<>(0);
            }
            phase = GenerationPhase.FINISHED;
        }
        return ExitType.COMPLETE;
    }

    private boolean filterLower() {
        if (size().compareTo(BigInteger.valueOf(100)) < 0) {
            Collection<Build> builds = getBuildsAll();
            if (!builds.isEmpty()) {
                if (extraBuilds == null) extraBuilds = new HashSet<>();
                extraBuilds.addAll(builds);
            }
            subGenerators = new ArrayList<>(0);
            allItems = new ArrayList[0];
            return false;
        }
        filterOnExclusion();
        if (isFail()) return true;
        filterOnConstraints();
        if (isFail()) return true;
        filterOnAdvancedSkillConstraints();
        if (isFail()) return true;
        filterWeaponOnAdvancedDamageConstraintsFirstPass();
        if (isFail()) return true;
        filterOnSkillReqsFirstPass(this.settings.getArchetype());
        return isFail();
    }

    private void filterOnExclusion() {
        List<Item> knownItems = new ArrayList<>();
        int start = 0;
        for (; start < allItems.length; start++) {
            List<Item> items = allItems[start];
            if (items.size() == 1) {
                knownItems.add(items.get(0));
            } else {
                break;
            }
        }
        for (BuildConstraintExclusion exclusion : settings.getConstraintsExclusion()) {
            for (int i = start; i < allItems.length; i++)
                exclusion.filter(allItems[i], knownItems);
        }
    }

    private boolean filterOnExclusion(Build build) {
        for (BuildConstraintExclusion exclusion : settings.getConstraintsExclusion()) {
            if (!exclusion.isValid(build.items)) return true;
        }
        return false;
    }

    /**
     * do all filters that require defined items to check
     */
    private boolean finalLayerFilter(Build build) {
        if (filterOnSkillReqsSecondPass(build)) return true;
        if (filterWeaponOnAdvancedDamageConstraintsSecondPass(build)) return true;
        if (filterOnConstraints(build)) return true;
        if (filterOnConstraintsAdvancedSkill(build)) return true;
        return filterOnExclusion(build);
    }

    private boolean filterOnConstraintsAdvancedSkill(Build build) {
        for (BuildConstraintAdvancedSkills constaint : settings.getConstraintsAdvancedSkill()) {
            if (!constaint.isValid(build.skills, build.extraSkillPoints, build.extraSkillPerElement, build.items))
                return true;
        }
        return false;
    }


    private boolean filterWeaponOnAdvancedDamageConstraintsSecondPass(Build build) {
        int spellDmg = 0;
        int mainDmg = 0;
        int spellDmgRaw = 0;
        int mainDmgRaw = 0;
        int[] skills = build.skills;
        int[] elemental = new int[ElementSkill.values().length];
        int attackSpeed = 0;
        int extraSkillPoints = build.extraSkillPoints;
        int[] extraSkillPerElement = build.extraSkillPerElement;
        for (Item item : build.items) {
            spellDmg += item.getId(ItemIdIndex.SPELL_DAMAGE);
            mainDmg += item.getId(ItemIdIndex.DAMAGE_BONUS);
            spellDmgRaw += item.getId(ItemIdIndex.SPELL_DAMAGE_RAW);
            mainDmgRaw += item.getId(ItemIdIndex.DAMAGE_BONUS_RAW);
            int i = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                elemental[i++] += item.getId(elementSkill.getDamageIdIndex());
            }
            attackSpeed += item.getId(ItemIdIndex.ATTACK_SPEED_BONUS);
        }

        Weapon weapon =  build.getWeapon();
        attackSpeed += weapon.attackSpeed.speed;
        double[] elementalPrecise = new double[elemental.length];
        for (int i = 0; i < elemental.length; i++) {
            elementalPrecise[i] = elemental[i] / 100d;
        }
        DamageInput input = new DamageInput(spellDmg / 100d, mainDmg / 100d, spellDmgRaw, mainDmgRaw, skills, extraSkillPoints, extraSkillPerElement, elementalPrecise, Item.AttackSpeed.toModifier(attackSpeed));
        if (build.getHawkeye()) {
            input.setHawkeye(true);
        }
        for (BuildConstraintAdvancedDamage constraint : settings.getConstraintsAdvancedDamage()) {
            if (!constraint.isValid(input, weapon)) return true;
        }
        return false;
    }

    /**
     * filters item pool based on whether the item is possible given the requirements of damage
     */
    private void filterWeaponOnAdvancedDamageConstraintsFirstPass() {
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
        boolean hawkeye = false;
        List<Item> weapons = allItems[weaponIndex];
        if (!weapons.isEmpty() && weapons.get(0).type == Item.ItemType.BOW)
            for (Item item : allItems[0]) {
                if (Build.isHawkeye(item)) {
                    hawkeye = true;
                    break;
                }
            }
        // skip weapon
        for (int typePieceIndex = 0; typePieceIndex < weaponIndex; typePieceIndex++) {
            // find max of all these
            int spellDmg = 0;
            int spellDmgRaw = 0;
            int mainDmg = 0;
            int mainDmgRaw = 0;
            int attackSpeed = 0;

            for (Item item : allItems[typePieceIndex]) {
                spellDmg = Math.max(spellDmg, item.getId(ItemIdIndex.SPELL_DAMAGE));
                spellDmgRaw = Math.max(spellDmgRaw, item.getId(ItemIdIndex.SPELL_DAMAGE_RAW));
                mainDmg = Math.max(mainDmg, item.getId(ItemIdIndex.DAMAGE_BONUS));
                mainDmgRaw = Math.max(mainDmgRaw, item.getId(ItemIdIndex.DAMAGE_BONUS_RAW));
                attackSpeed = Math.max(attackSpeed, item.getId(ItemIdIndex.ATTACK_SPEED_BONUS));
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
                            item.getId(elementSkill.getDamageIdIndex()));
                    elementalDamage[elementIndex] = Math.max(elementalDamage[elementIndex],
                            item.getId(elementSkill.getDamageIdIndex()));
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
        boolean finalHawkeye = hawkeye;
        allItems[weaponIndex].removeIf(item -> {
            // make sure it's read only for the arrays
            int extraSkillPoints = Item.SKILLS_FOR_PLAYER;
            int[] extraSkillsPerElement = new int[elementSize];
            Arrays.fill(extraSkillsPerElement, Item.SKILLS_PER_ELEMENT);
            int[] mySkills = new int[elementSize];
            double[] myElemental = new double[elementSize];

            int i = 0;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                int requiredSkill = Math.max(requiredSkills[i], item.getRequiredSkill(elementSkill));
                int skill = skills[i];
                if (requiredSkill != 0 && requiredSkill > skill) {
                    int difference = requiredSkill - skill;
                    extraSkillPoints -= difference;
                    extraSkillsPerElement[i] -= difference;
                    mySkills[i] = requiredSkill;
                } else {
                    mySkills[i] = skill;
                }
                mySkills[i] += item.getSkill(elementSkill); // do this after because weapon skill doesn't help with reqs
                myElemental[i] = ((double) (elementalDamage[i] + item.getId(elementSkill.getDamageIdIndex()))) / 100d;
                i++;
            }
            if (extraSkillPoints < 0) return true; // return a failed skill point test
            int mySpellDmg = finalSpellDmg;
            int mySpellDmgRaw = finalSpellDmgRaw;
            int myMainDmg = finalMainDmg;
            int myMainDmgRaw = finalMainDmgRaw;
            int myAttackSpeed = finalAttackSpeed;
            mySpellDmg += item.getId(ItemIdIndex.SPELL_DAMAGE);
            mySpellDmgRaw += item.getId(ItemIdIndex.SPELL_DAMAGE_RAW);
            myMainDmg += item.getId(ItemIdIndex.DAMAGE_BONUS);
            myMainDmgRaw += item.getId(ItemIdIndex.DAMAGE_BONUS_RAW);
            myAttackSpeed += item.getId(ItemIdIndex.ATTACK_SPEED_BONUS);
            myAttackSpeed += ((Weapon) item).attackSpeed.speed;
            double myFinalSpellDmg = mySpellDmg / 100.0;
            double myFinalMainDmg = myMainDmg / 100.0;
            DamageInput input = new DamageInput(myFinalSpellDmg, myFinalMainDmg, mySpellDmgRaw, myMainDmgRaw, mySkills, extraSkillPoints, extraSkillsPerElement, myElemental, Item.AttackSpeed.toModifier(myAttackSpeed));
            if (finalHawkeye)
                input.setHawkeye(true);
            for (BuildConstraintAdvancedDamage constraint : settings.getConstraintsAdvancedDamage()) {
                if (!constraint.isValid(input, (Weapon) item)) return true;
            }
            return false;
        });
    }

    public boolean isEmpty() {
        if (allItems.length != 0) {
            boolean shouldStop = true;
            for (List<Item> type : allItems) {
                if (type.isEmpty()) {
                    shouldStop = false;
                    break;
                }
            }
            if (shouldStop) return false;
        }
        if (extraBuilds != null && !extraBuilds.isEmpty()) {
            return false;
        }
        for (BuildGenerator generator : subGenerators) {
            if (!generator.isEmpty()) return false;
        }
        return true;
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
        if (extraBuilds != null)
            combinationsCount = combinationsCount.add(BigInteger.valueOf(extraBuilds.size()));
        return combinationsCount;
    }

    private void breakApart() {
        int pieceIndex = -1;
        int pieceCount = -1;
        int length = allItems.length;
        int weaponIndex = length - 1;
        for (int i = 0; i < weaponIndex; i++) {
            int size = allItems[i].size();
            if (size != 1) {
                if (size < pieceCount || pieceIndex == -1) {
                    pieceIndex = i;
                    pieceCount = size;
                }
            }
        }
        if (pieceIndex == -1) {
            if (allItems[weaponIndex].size() != 1)
                pieceIndex = weaponIndex;
            else
                return;
        }
        List<Item> items = allItems[pieceIndex];
        for (Item chosenItem : items) {
            ArrayList<Item>[] subItems = new ArrayList[length];
            for (int subIndex = 0; subIndex < length; subIndex++) {
                if (subIndex == pieceIndex) {
                    ArrayList<Item> smallList = new ArrayList<>(1);
                    smallList.add(chosenItem);
                    subItems[subIndex] = smallList;
                } else {
                    subItems[subIndex] = new ArrayList<>(allItems[subIndex]);
                }
            }
            subGenerators.add(new BuildGenerator(
                    subItems,
                    settings,
                    layer + 1));
        }
        allItems = new ArrayList[0];
    }

    /**
     * filters item pool based on whether with the best skill points available, make the build possible
     * (keep in mind this doesn't care about ordering of items. only that the skill points of the item being tested dont contribute)
     *
     * @param archetype the archetype we're making
     */
    private void filterOnSkillReqsFirstPass(Set<ElementSkill> archetype) {
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

    private boolean filterOnSkillReqsSecondPass(Build build) {
        int elementLength = ElementSkill.values().length;
        List<Item> group1 = new ArrayList<>();
        List<Item> group2Unsorted = new ArrayList<>();
        List<Item> group3 = new ArrayList<>();
        int weaponIndex = build.items.size() - 1;
        for (int i = 0; i < weaponIndex; i++) {
            Item item = build.items.get(i);
            if (item.strength == 0 && item.dexterity == 0 && item.agility == 0 && item.intelligence == 0 && item.defense == 0) {
                // group 1
                group1.add(item);
            } else {
                // group 2 or 3
                boolean isGroup2 = false;
                for (ElementSkill elementSkill : ElementSkill.values()) {
                    if (item.getSkill(elementSkill) != 0) {
                        isGroup2 = true;
                    }
                }
                if (isGroup2) group2Unsorted.add(item);
                else group3.add(item);
            }
        }
        Item weapon = build.items.get(weaponIndex);
        int[] skills = new int[elementLength];
        for (Item item : group1) {
            skills[0] += item.getSkill(ElementSkill.THUNDER);
            skills[1] += item.getSkill(ElementSkill.AIR);
            skills[2] += item.getSkill(ElementSkill.EARTH);
            skills[3] += item.getSkill(ElementSkill.WATER);
            skills[4] += item.getSkill(ElementSkill.FIRE);
        }
        if (group2Unsorted.isEmpty()) {
            // check it once to see if it works
            SkillReqAnswer response = passesSkillReqSecondPass(skills, new Item[0], group3, weapon);
            if (response.valid) {
                int[] realSkills = convertFromHardcodeSkills(response);
                group3.add(weapon);
                build.addOrdering(new Item[0], group3, realSkills, response.extraSkillPoints, response.extraSkillPerElement);
                return false;
            }
        }
        Item[] items = new Item[group2Unsorted.size()];
        group2Unsorted.toArray(items);
        int length = items.length;
        int[] c = new int[length];
        int i = 0;
        while (i < length) {
            if (c[i] < i) {
                if (i % 2 == 0) {
                    Item i1 = items[0];
                    items[0] = items[i];
                    items[i] = i1;
                } else {
                    int index = c[i];
                    Item i1 = items[index];
                    items[index] = items[i];
                    items[i] = i1;
                }
                SkillReqAnswer response = passesSkillReqSecondPass(skills, items, group3, weapon);
                if (response.valid) {
                    int[] realSkills = convertFromHardcodeSkills(response);
                    group3.add(weapon);
                    build.addOrdering(items, group3, realSkills, response.extraSkillPoints, response.extraSkillPerElement);
                    return false;
                }
                c[i]++;
                i = 0;
            } else {
                c[i] = 0;
                i++;
            }
        }
        // this is impossible if we didn't return already
        return true;
    }

    private int[] convertFromHardcodeSkills(SkillReqAnswer response) {
        int[] realSkills = new int[ElementSkill.values().length];
        int j = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            switch (elementSkill) {
                case THUNDER -> realSkills[j] = response.mySkills[0];
                case AIR -> realSkills[j] = response.mySkills[1];
                case EARTH -> realSkills[j] = response.mySkills[2];
                case WATER -> realSkills[j] = response.mySkills[3];
                case FIRE -> realSkills[j] = response.mySkills[4];
            }
            j++;
        }
        return realSkills;
    }


    private SkillReqAnswer passesSkillReqSecondPass(int[] skills, Item[] group2, List<Item> group3, Item weapon) {
        int[] mySkills = Arrays.copyOf(skills, skills.length);
        int extraSkillPoints = Item.SKILLS_FOR_PLAYER;
        int[] extraSkillPerElement = new int[skills.length];
        Arrays.fill(extraSkillPerElement, Item.SKILLS_PER_ELEMENT);
        for (int group2Index = 0; group2Index < group2.length; group2Index++) {
            Item item = group2[group2Index];
            extraSkillPoints = helperPassesSkillReqSecondPass(item, mySkills, extraSkillPerElement, extraSkillPoints);
            if (extraSkillPoints < 0) return new SkillReqAnswer(false, null, 0, null);
            int i = 0;
            boolean badSkills = false;
            for (ElementSkill elementSkill : ElementSkill.values()) {
                int skill = item.getSkill(elementSkill);
                if (skill < 0) badSkills = true;
                mySkills[i++] += skill;
            }
            if (badSkills) {
                // check all the previous items
                for (i = 0; i < group2Index; i++) {
                    extraSkillPoints = helperPassesSkillReqSecondPass(group2[i], mySkills, extraSkillPerElement, extraSkillPoints);
                    if (extraSkillPoints < 0) return new SkillReqAnswer(false, null, 0, null);
                }
            }
        }
        extraSkillPoints = helperPassesSkillReqSecondPass(weapon, mySkills, extraSkillPerElement, extraSkillPoints);
        if (extraSkillPoints < 0) return new SkillReqAnswer(false, null, 0, null);
        int i = 0;
        boolean badSkills = false;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            int skill = weapon.getSkill(elementSkill);
            if (skill < 0) badSkills = true;
            mySkills[i++] += skill;
        }
        for (Item item : group3) {
            extraSkillPoints = helperPassesSkillReqSecondPass(item, mySkills, extraSkillPerElement, extraSkillPoints);
            if (extraSkillPoints < 0) return new SkillReqAnswer(false, null, 0, null);
        }
        if (badSkills) {
            // check all the previous items
            for (i = 0; i < group2.length; i++) {
                extraSkillPoints = helperPassesSkillReqSecondPass(group2[i], mySkills, extraSkillPerElement, extraSkillPoints);
                if (extraSkillPoints < 0) return new SkillReqAnswer(false, null, 0, null);
            }
        }
        for (int skill : extraSkillPerElement) {
            if (skill < 0) return new SkillReqAnswer(false, null, 0, null);
        }
        return new SkillReqAnswer(true, mySkills, extraSkillPoints, extraSkillPerElement);
    }

    private int helperPassesSkillReqSecondPass(Item item, int[] mySkills, int[] extraSkillPerElement, int extraSkillPoints) {
        int req, difference;
        req = item.dexterity;
        if (req != 0) {
            difference = req - mySkills[0];
            if (difference > 0) {
                extraSkillPoints -= difference;
                extraSkillPerElement[0] -= difference;
                mySkills[0] = req;
            }
        }
        req = item.agility;
        if (req != 0) {
            difference = req - mySkills[1];
            if (difference > 0) {
                extraSkillPoints -= difference;
                extraSkillPerElement[1] -= difference;
                mySkills[1] = req;
            }
        }
        req = item.strength;
        if (req != 0) {
            difference = req - mySkills[2];
            if (difference > 0) {
                extraSkillPoints -= difference;
                extraSkillPerElement[2] -= difference;
                mySkills[2] = req;
            }
        }
        req = item.intelligence;
        if (req != 0) {
            difference = req - mySkills[3];
            if (difference > 0) {
                extraSkillPoints -= difference;
                extraSkillPerElement[3] -= difference;
                mySkills[3] = req;
            }
        }
        req = item.defense;
        if (req != 0) {
            difference = req - mySkills[4];
            if (difference > 0) {
                extraSkillPoints -= difference;
                extraSkillPerElement[4] -= difference;
                mySkills[4] = req;
            }
        }
        return extraSkillPoints;
    }

    /**
     * remove items that don't fit our archetype
     */
    private void filterOnBadArchetype() {
        Set<ElementSkill> archetype = settings.getArchetype();
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
                for (BuildConstraintGeneral constraint : settings.getConstraintsGeneral()) {
                    if (constraint.contributes(item)) return false;
                }
                for (BuildConstraintAdvancedSkills constraint : settings.getConstraintsAdvancedSkill()) {
                    if (constraint.contributes(item)) return false;
                }
                return !BuildUtils.contributesToUseful(item);
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
                        for (BuildConstraintGeneral constraint : settings.getConstraintsGeneral()) {
                            if (constraint.compare(asBest, item) <= 0) {
                                isCool = true;
                                break;
                            }
                        }
//                        for(BuildConstraintAdvancedDefense constraint:constraintsAdvancedDefense){
//                            if(constraint.compare(asBest,item));
//                        }
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
        for (BuildConstraintGeneral constraint : settings.getConstraintsGeneral()) {
            for (int optimizingIndex = 0; optimizingIndex < allItems.length; optimizingIndex++) {
                List<Item> bestItems = new ArrayList<>(allItems.length);
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

    private boolean filterOnConstraints(Build build) {
        for (BuildConstraintGeneral constraint : settings.getConstraintsGeneral()) {
            if (!constraint.isValid(build.items)) {
                return true;
            }
        }
        return false;
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
        int[] skillsExtraPerElement = new int[elementSize];
        Arrays.fill(skillsExtraPerElement, Item.SKILLS_PER_ELEMENT);
        for (int i = 0; i < skillsAll.length; i++) {
            if (requiredSkillsAll[i] != 0) {
                int diff = requiredSkillsAll[i] - skillsAll[i];
                if (diff > 0) {
                    skillsAll[i] = requiredSkillsAll[i];
                    skillsExtraPerElement[i] -= diff;
                    extraPoints -= diff;
                }
            }
        }
        int finalExtraPoints = extraPoints;
        //todo it's possible to narrow down further
        // by recalculating skillsAll and requiredSkillsAll for each item using a double array and skipping the current item

        for (BuildConstraintAdvancedSkills constraint : settings.getConstraintsAdvancedSkill()) {
            for (int optimizingIndex = 0; optimizingIndex < allItems.length; optimizingIndex++) {
                List<Item> bestItems = new ArrayList<>();
                for (int index = 0; index < allItems.length; index++) {
                    if (optimizingIndex != index) {
                        Item bestItem = constraint.getBest(allItems[index]);
                        if (bestItem == null) return;
                        bestItems.add(bestItem);
                    }
                }
                allItems[optimizingIndex].removeIf(item -> !constraint.isValid(skillsAll, finalExtraPoints, skillsExtraPerElement, bestItems, item));
                if (allItems[optimizingIndex].isEmpty())
                    return;
            }
        }
    }

    private boolean isFail() {
        if (allItems.length == 0) return true;
        for (List<Item> i : allItems) {
            if (i.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public Collection<Build> getBuildsAll() {
        Collection<Build> builds = getBuilds(this::finalLayerFilter);
        if (!builds.isEmpty()) {
            if (extraBuilds == null) extraBuilds = new HashSet<>();
            extraBuilds.addAll(builds);
        }
        allItems = new ArrayList[0];
        subGenerators = new ArrayList<>(0);
        if (shouldSaveResult && !overrideShouldSave) {
            shouldSaveResult = false;
            Preindexing.saveResult(this);
        }
        return builds;
    }

    public Collection<Build> getBuilds(Predicate<Build> filter) {
        if (allItems.length == 0) {
            Set<Build> builds = new HashSet<>();
            for (BuildGenerator generator : subGenerators) {
                builds.addAll(generator.getBuilds(filter));
            }
            if (extraBuilds != null) {
                extraBuilds.removeIf(filter);
                builds.addAll(extraBuilds);
            }
            return builds;
        } else {
            return new ArrayList<>(Build.makeBuilds(allItems, filter));
        }
    }

    public Set<Item> getItemsInBuilds() {
        Collection<Build> builds = getBuildsAll();
        if (builds.size() != 0) {
            Set<Item> items = new HashSet<>();
            for (Build build : builds) {
                items.addAll(build.items);
            }
            return items;
        }
        return null;
    }

    public Set<ElementSkill> getArchetype() {
        return settings.getArchetype();
    }

    public void refineItemPoolTo(Map<Item.ItemType, List<String>> results) {
        for (ArrayList<Item> items : allItems) {
            List<String> refineTo = results.get(items.get(0).type);
            if (refineTo == null) {
                allItems = new ArrayList[0];
                return;
            }
            items.removeIf(item -> !refineTo.contains(item.name));
            items.trimToSize();
        }
    }

    public boolean isWorking() {
        return phase != GenerationPhase.FINISHED;
    }

    public double progress() {
        return topLayerIndex / (double) initalSubGeneratorsSize;
    }

    public void setOverrideSave(boolean shouldSave) {
        overrideShouldSave = shouldSave;
    }

    private enum GenerationPhase {
        START,
        SUB_GENERATORS,
        FINISH_SUB_GENERATORS,
        FINISHED
    }

    public enum ExitType {
        INCOMPLETE,
        HARD_TIMEOUT,
        IMPOSSIBLE,
        COMPLETE
    }
}

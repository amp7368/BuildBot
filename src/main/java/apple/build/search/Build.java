package apple.build.search;

import apple.build.search.constraints.answers.DamageInput;
import apple.build.search.constraints.answers.DamageOutput;
import apple.build.search.enums.ElementSkill;
import apple.build.search.enums.IdNames;
import apple.build.search.enums.Spell;
import apple.build.wynnbuilder.ServiceWynnbuilderItemDB;
import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.ItemIdIndex;
import apple.build.wynncraft.items.Weapon;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Build {

    private static final int MAX_BUILDS = 600;
    private static final List<Item.ItemType> WYNNBUILDER_ITEM_ORDER = List.of(
            Item.ItemType.HELMET,
            Item.ItemType.CHESTPLATE,
            Item.ItemType.LEGGINGS,
            Item.ItemType.BOOTS,
            Item.ItemType.RING,
            Item.ItemType.BRACELET,
            Item.ItemType.NECKLACE,
            Item.ItemType.BOW,
            Item.ItemType.WAND,
            Item.ItemType.SPEAR,
            Item.ItemType.DAGGER,
            Item.ItemType.RELIK
    );
    private static final int MAX_WYNN_LEVEL = 106;
    public final List<Item> items;
    public final List<Integer> ordering = new ArrayList<>();
    public int[] skills = null;
    public int extraSkillPoints = -1;
    public int[] extraSkillPerElement = null;
    private Weapon weapon = null;
    private DamageInput damageInput = null;
    private DamageOutput mainDmg = null;
    private HashMap<Spell, DamageOutput> spellDmg = null;

    public Build(List<Item> chosen) {
        items = chosen;
    }

    public static Collection<Build> makeBuilds(List<Item>[] allItems, Predicate<Build> filter) {
        Set<Build> builds = new HashSet<>();
        List<Item> chosen = new ArrayList<>();
        int i = 0;
        for (List<Item> items : allItems) {
            if (items.size() == 1) {
                chosen.add(items.get(0));
            } else {
                makeBuilds(chosen, allItems, builds, i, filter);
                return builds;
            }
            i++;
        }
        return Collections.singletonList(new Build(chosen));
    }

    private static void makeBuilds(List<Item> chosen, List<Item>[] allItems, Set<Build> builds, int indexAt, Predicate<Build> filter) {
        if (indexAt == allItems.length) {
            Build build = new Build(chosen);
            if (!filter.test(build))
                builds.add(build);
            return;
        }
        for (Item item : allItems[indexAt]) {
            List<Item> subChosen = new ArrayList<>(chosen);
            subChosen.add(item);
            makeBuilds(subChosen, allItems, builds, indexAt + 1, filter);
            if (builds.size() > MAX_BUILDS) break;
        }
    }

    public void addOrdering(Item[] group2, List<Item> group3, int[] mySkills, int extraSkillPoints, int[] extraSkillPerElement) {
        int itemsFound = 0;
        this.extraSkillPoints = extraSkillPoints;
        this.extraSkillPerElement = extraSkillPerElement;
        this.skills = mySkills;
        int itemsToFind = items.size() - group2.length - group3.size();
        if (itemsToFind != 0) {
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                if (!group3.contains(item)) {
                    boolean contains = false;
                    for (Item item1 : group2) {
                        if (item1.equals(item)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        ordering.add(i);
                        itemsFound++;
                        if (itemsFound == itemsToFind) break;
                    }
                }
            }
        }
        for (Item item : group2) {
            int i = 0;
            for (Item item1 : items) {
                if (item.equals(item1)) {
                    ordering.add(i);
                    break;
                }
            }
        }
        for (Item item : group3) {
            int i = 0;
            for (Item item1 : items) {
                if (item.equals(item1)) {
                    ordering.add(i);
                    break;
                }
            }
        }
    }

    public boolean getHawkeye() {
        return isHawkeye(items.get(0)) && items.get(items.size() - 1).type == Item.ItemType.BOW;
    }

    public static boolean isHawkeye(Item item) {
        for (String majorId : item.majorIds) {
            if (majorId.equals("HAWKEYE")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return items.stream().map(item -> {
            String s = item.toString();
            int l = Math.max(0, 20 - s.length());
            return " ".repeat(l) + s;
        }).collect(Collectors.joining(", "));
    }

    @Override
    public int hashCode() {
        StringBuilder s = new StringBuilder();
        long ringAddition = 0;
        for (Item item : items) {
            if (item.type == Item.ItemType.RING) {
                ringAddition += item.hashCode();
            } else {
                s.append(item.name);
            }
        }
        return (int) ((((long) s.toString().hashCode()) + ringAddition) % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Build otherBuild) {
            Iterator<Item> mine = items.iterator();
            Iterator<Item> other = otherBuild.items.iterator();
            while (mine.hasNext()) {
                if (!other.hasNext()) return false;
                Item mr1 = mine.next();
                Item or1 = other.next();
                if (mr1.type == Item.ItemType.RING) {
                    if (mr1.equals(or1)) {
                        if (mine.hasNext() && other.hasNext()) {
                            if (!mine.next().equals(other.next())) return false;
                        }
                        continue;
                    }
                    if (mine.hasNext() && other.hasNext()) {
                        Item mr2 = mine.next();
                        Item or2 = other.next();
                        if (!mr1.equals(or2) || !or1.equals(mr2)) {
                            return false;
                        }
                    }
                } else {
                    if (!mr1.equals(or1)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public List<Item> getItemsInOrder(List<Item.ItemType> itemTypes) {
        List<Item> itemsSorted = new ArrayList<>();
        List<Item> itemsCopy = new ArrayList<>(items);
        for (Item.ItemType itemType : itemTypes) {
            for (Item item : itemsCopy) {
                if (item.type == itemType) {
                    itemsSorted.add(item);
                }
            }
        }
        return itemsSorted;
    }

    public DamageOutput getMainDamage() {
        if (mainDmg == null) {
            verifyDamageInput();
            return this.mainDmg = BuildMath.getDamage(damageInput, getWeapon());
        }
        return mainDmg;
    }

    public DamageOutput getSpellDamage(Spell spell) {
        if (spellDmg == null) {
            verifyDamageInput();
            spellDmg = new HashMap<>();
        }
        return spellDmg.computeIfAbsent(spell, (s) -> BuildMath.getDamage(s, damageInput, getWeapon()));
    }

    public void verifyDamageInput() {
        if (this.damageInput == null) {
            this.damageInput = new DamageInput(getSpellDmgId(),
                    getMainDmgId(),
                    getSpellDmgRaw(),
                    getMainDmgRaw(),
                    skills,
                    extraSkillPoints,
                    extraSkillPerElement,
                    getElemental(),
                    getAttackSpeed().modifier()
            );
        }
    }

    private double[] getElemental() {
        double[] elemental = new double[ElementSkill.values().length];
        int i = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            elemental[i++] = getId(elementSkill.getDamageIdIndex());
        }
        for (i = 0; i < elemental.length; i++) {
            elemental[i] /= 100d;
        }
        return elemental;
    }

    private Item.AttackSpeed getAttackSpeed() {
        int attackSpeed = 0;
        for (Item item : items) {
            attackSpeed += item.getId(IdNames.ATTACK_SPEED.getIdIndex());
            if (item instanceof Weapon weapon) {
                attackSpeed += weapon.attackSpeed.speed;
            }
        }
        return Item.AttackSpeed.from(attackSpeed);
    }

    private int getMainDmgRaw() {
        return getId(ItemIdIndex.DAMAGE_BONUS_RAW);
    }

    private int getSpellDmgRaw() {
        return getId(ItemIdIndex.SPELL_DAMAGE_RAW);
    }

    private double getMainDmgId() {
        return getId(ItemIdIndex.DAMAGE_BONUS) / 100d;
    }

    private double getSpellDmgId() {
        return getId(ItemIdIndex.SPELL_DAMAGE) / 100d;
    }

    private int getId(int idIndex) {
        int id = 0;
        for (Item item : items) {
            id += item.getId(idIndex);
        }
        return id;
    }

    public Weapon getWeapon() {
        if (this.weapon == null) {
            for (Item item : this.items) {
                if (item instanceof Weapon weapon) {
                    this.weapon = weapon;
                }
            }
        }
        return this.weapon;
    }

    private final static String digitsStr =
            //   0       8       16      24      32      40      48      56     63
            //   v       v       v       v       v       v       v       v      v
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+-";
    private final static char[] digits = digitsStr.toCharArray();
    private final static String[] skillpoint_order = new String[]{"str", "dex", "int", "def", "agi"};
    private static final int POWDER_NOTHING = 0;

    private static String fromIntN(int int32, int n) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i++) {
            result.insert(0, digits[int32 & 0x3f]);
            int32 >>= 6;
        }
        return result.toString();
    }

    public String encodeToWynnBuilder() {
        StringBuilder build_string = new StringBuilder("4_");

        // items
        for (Item item : getItemsInOrder(WYNNBUILDER_ITEM_ORDER)) {
            build_string.append(fromIntN(ServiceWynnbuilderItemDB.getItemId(item), 3));
        }

        // skillpoints
        for (String skp : skillpoint_order) {
            build_string.append(fromIntN(0, 2)); // 0 skill points currently
        }

        // level
        build_string.append(fromIntN(MAX_WYNN_LEVEL, 2));

        // powders
        for (Item item : getItemsInOrder(WYNNBUILDER_ITEM_ORDER)) {
            if (item instanceof Accessory) continue;
            int n_bits = 0; //adding no powders
            build_string.append(fromIntN(n_bits, 1)); // Hard cap of 378 powders.
            // Slice copy.
            List<Object> powderset = new ArrayList<>();
            while (!powderset.isEmpty()) {
                List<Object> firstSix = new ArrayList<>(powderset.subList(0, Math.min(powderset.size(), 6)));
                Collections.reverse(firstSix);
                int powder_hash = 0;
                for (Object powder : firstSix) {
                    powder_hash = (powder_hash << 5) + 1 + POWDER_NOTHING; // LSB will be extracted first.
                }
                build_string.append(fromIntN(powder_hash, 5));
                powderset = powderset.size() <= 6 ? Collections.emptyList() : powderset.subList(6, powderset.size());
            }
        }

        return build_string.toString();
//        build_string += fromIntN(player_build.level, 2);
//        for (const _powderset of player_build.powders){
//            let n_bits = Math.ceil(_powderset.length / 6);
//            build_string += fromIntN(n_bits, 1); // Hard cap of 378 powders.
//             Slice copy.
//            let powderset = _powderset.slice();
//            while (powderset.length != 0) {
//                let firstSix = powderset.slice(0, 6).reverse();
//                let powder_hash = 0;
//                for (const powder of firstSix){
//                    powder_hash = (powder_hash << 5) + 1 + powder; // LSB will be extracted first.
//                }
//                build_string += fromIntN(powder_hash, 5);
//                powderset = powderset.slice(6);
//            }
//        }
    }
}

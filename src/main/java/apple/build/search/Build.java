package apple.build.search;

import apple.build.wynnbuilder.ServiceWynnbuilderItemDB;
import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Item;

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
        int i = 0;
        long ringAddition = 0;
        for (Item item : items) {
            if (i == 4 || i == 5) {
                ringAddition += item.hashCode();
            } else {
                s.append(item.name);
            }
            i++;
        }
        return (int) ((((long) s.toString().hashCode()) + ringAddition) % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Build) {
            Build otherBuild = (Build) obj;
            Iterator<Item> mine = items.iterator();
            Iterator<Item> other = otherBuild.items.iterator();
            int i = 0;
            while (mine.hasNext()) {
                if (!other.hasNext()) return false;
                if (i == 4) {
                    Item mr1 = mine.next();
                    Item or1 = other.next();
                    if (mr1.equals(or1)) {
                        continue;
                    }
                    if (mine.hasNext() && other.hasNext()) {
                        Item mr2 = mine.next();
                        Item or2 = other.next();
                        if (!mr1.equals(or2) || !or1.equals(mr2)) {
                            return false;
                        }
                        i++;
                    }
                } else {
                    if (!mine.next().equals(other.next())) return false;
                }
                i++;
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

    private final static String digitsStr =
            //   0       8       16      24      32      40      48      56     63
            //   v       v       v       v       v       v       v       v      v
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+-";
    private final static char[] digits = digitsStr.toCharArray();
    private final static String[] skillpoint_order = new String[]{"str", "dex", "int", "def", "agi"};
    private static final int POWDER_NOTHING = 0;

    public static String fromIntN(int int32, int n) {
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

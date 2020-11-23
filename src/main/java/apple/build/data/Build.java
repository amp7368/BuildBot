package apple.build.data;

import apple.build.wynncraft.items.Item;

import java.util.*;
import java.util.stream.Collectors;

public class Build {

    public final List<Item> items;
    public final List<Integer> ordering = new ArrayList<>();
    public int[] skills = null;
    public int extraSkillPoints = -1;
    public int[] extraSkillPerElement = null;

    public Build(List<Item> chosen) {
        items = chosen;
    }

    public static List<Build> makeBuilds(List<Item>[] allItems) {
        List<Build> builds = new ArrayList<>();
        List<Item> chosen = new ArrayList<>();
        int i = 0;
        for (List<Item> items : allItems) {
            if (items.size() == 1) {
                chosen.add(items.get(0));
            } else {
                makeBuilds(chosen, allItems, builds, i);
                return builds;
            }
            i++;
        }
        return Collections.singletonList(new Build(chosen));
    }

    private static void makeBuilds(List<Item> chosen, List<Item>[] allItems, List<Build> builds, int indexAt) {
        if (indexAt == allItems.length) {
            builds.add(new Build(chosen));
            return;
        }
        for (Item item : allItems[indexAt]) {
            List<Item> subChosen = new ArrayList<>(chosen);
            subChosen.add(item);
            makeBuilds(subChosen, allItems, builds, indexAt + 1);
        }
    }

    @Override
    public String toString() {
        return items.stream().map(item -> {
            String s = item.toString();
            int l = Math.max(0, 20 - s.length());
            return " ".repeat(l) + s;
        }).collect(Collectors.joining(", "));
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
}

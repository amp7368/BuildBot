package apple.build.data;

import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Build {

    public final List<Item> items = new ArrayList<>();
    public final List<Integer> ordering = new ArrayList<>();
    public int[] skills = null;
    public int extraSkillPoints = -1;

    public Build(List<Item>[] allItems) {
        for (List<Item> allItem : allItems) {
            items.add(allItem.get(0));
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

    public void addOrdering(Item[] group2, List<Item> group3, int[] mySkills, int extraSkillPoints) {
        int itemsFound = 0;
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
        this.extraSkillPoints = extraSkillPoints;
        this.skills = mySkills;
    }

    public boolean getHawkeye() {
        boolean hawkEye = false;
        for (String majorId : items.get(0).majorIds) {
            if (majorId.equals("HAWKEYE")) {
                hawkEye = true;
                break;
            }
        }
        return hawkEye && items.get(items.size() - 1).type == Item.ItemType.BOW;
    }
}

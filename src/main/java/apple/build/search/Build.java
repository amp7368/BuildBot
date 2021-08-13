package apple.build.search;

import apple.build.wynncraft.items.Item;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Build {

    private static final int MAX_BUILDS = 600;
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
}

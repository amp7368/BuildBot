package apple.build.data;

import apple.build.wynncraft.items.Item;

import java.util.List;

public class BuildUtils {
    public static int bestSkillPoints(ElementSkill element, List<Item> items) {
        int bestSkill = Integer.MIN_VALUE;
        for (Item item : items) {
            switch (element) {
                case THUNDER:
                    bestSkill = Math.max(item.ids.getOrDefault("dexterityPoints", 0), bestSkill);
                    break;
                case AIR:
                    bestSkill = Math.max(item.ids.getOrDefault("agilityPoints", 0), bestSkill);
                    break;
                case EARTH:
                    bestSkill = Math.max(item.ids.getOrDefault("strengthPoints", 0), bestSkill);
                    break;
                case WATER:
                    bestSkill = Math.max(item.ids.getOrDefault("intelligencePoints", 0), bestSkill);
                    break;
                case FIRE:
                    bestSkill = Math.max(item.ids.getOrDefault("defensePoints", 0), bestSkill);
                    break;
            }
        }
        return bestSkill;
    }

    public static int bestSkillReqs(ElementSkill element, List<Item> items) {
        int bestReqs = Integer.MAX_VALUE;
        for (Item item : items) {
            switch (element) {
                case THUNDER:
                    bestReqs = Math.min(item.dexterity, bestReqs);
                    break;
                case AIR:
                    bestReqs = Math.min(item.agility, bestReqs);
                    break;
                case EARTH:
                    bestReqs = Math.min(item.strength, bestReqs);
                    break;
                case WATER:
                    bestReqs = Math.min(item.intelligence, bestReqs);
                    break;
                case FIRE:
                    bestReqs = Math.min(item.defense, bestReqs);
                    break;
            }
        }
        return bestReqs;
    }
}

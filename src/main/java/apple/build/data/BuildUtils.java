package apple.build.data;

import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuildUtils {
    private static final Set<String> usefulStats = new HashSet<>() {{
        add("spellDamage");
        add("spellDamageRaw");
        add("damageBonus");
        add("damageBonusRaw");
        add("agilityPoints");
        add("defensePoints");
        add("intelligencePoints");
        add("strengthPoints");
        add("dexterityPoints");
        for (ElementSkill elementSkill : ElementSkill.values())
            add("bonus" + Pretty.uppercaseFirst(elementSkill.name().toLowerCase()) + "Damage");
    }};

    public static int bestSkillPoints(ElementSkill element, List<Item> items) {
        int bestSkill = Integer.MIN_VALUE;
        switch (element) {
            case THUNDER:
                for (Item item : items) {
                    bestSkill = Math.max(item.ids.getOrDefault("dexterityPoints", 0), bestSkill);
                }
                break;
            case AIR:
                for (Item item : items) {
                    bestSkill = Math.max(item.ids.getOrDefault("agilityPoints", 0), bestSkill);
                }
                break;
            case EARTH:
                for (Item item : items) {
                    bestSkill = Math.max(item.ids.getOrDefault("strengthPoints", 0), bestSkill);
                }
                break;
            case WATER:
                for (Item item : items) {
                    bestSkill = Math.max(item.ids.getOrDefault("intelligencePoints", 0), bestSkill);
                }
                break;
            case FIRE:
                for (Item item : items) {
                    bestSkill = Math.max(item.ids.getOrDefault("defensePoints", 0), bestSkill);
                }
                break;
        }
        return bestSkill;
    }

    public static int bestSkillReqs(ElementSkill element, List<Item> items) {
        int bestReqs = Integer.MAX_VALUE;
        switch (element) {
            case THUNDER:
                for (Item item : items) {
                    bestReqs = Math.min(item.dexterity, bestReqs);
                }
                break;
            case AIR:
                for (Item item : items) {
                    bestReqs = Math.min(item.agility, bestReqs);
                }
                break;
            case EARTH:
                for (Item item : items) {
                    bestReqs = Math.min(item.strength, bestReqs);
                }
                break;
            case WATER:
                for (Item item : items) {
                    bestReqs = Math.min(item.intelligence, bestReqs);
                }
                break;
            case FIRE:
                for (Item item : items) {
                    bestReqs = Math.min(item.defense, bestReqs);
                }
                break;
        }
        return bestReqs;
    }

    public static boolean contributesToUseful(Item item) {
        for (String id : usefulStats) {
            if (item.ids.containsKey(id)) return true;
        }
        return false;
    }
}

package apple.build.wynncraft.items;

import apple.build.data.ElementSkill;
import apple.build.sql.GetSql;
import apple.build.utils.OneToOneMap;
import apple.build.utils.Pair;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Item {
    public static final int SKILLS_FOR_PLAYER = 200;
    public static final int SKILLS_PER_ELEMENT = 100;
    private static final Set<String> UNROLLABLE = new HashSet<>() {{
        add("thunderDefense");
        add("airDefense");
        add("earthDefense");
        add("waterDefense");
        add("fireDefense");
        add("dexterityPoints");
        add("agilityPoints");
        add("strengthPoints");
        add("defensePoints");
        add("intelligencePoints");
    }};
    private static final OneToOneMap<String, Integer> idNameToUid = new OneToOneMap<>();
    private static int currentUid = 0;
    public final Map<Integer, Integer> ids = new HashMap<>();
    public final String name;
    public final String displayName;
    public final String tier;
    public final int strength;
    public final int dexterity;
    public final int intelligence;
    public final int agility;
    public final int defense;
    public final Integer sockets;
    public final String dropType;
    public final @Nullable String restrictions;
    public final @Nullable String set;
    public final @Nullable String addedLore;
    public final @Nullable String material;
    public final @Nullable String quest;
    public final @Nullable ClassType classRequirement;
    public final String[] majorIds;
    public final boolean identified;
    public final ItemType type;
    public final int level;

    protected Item(Item other) {
        this.ids.putAll(other.ids);
        this.name = other.name;
        this.displayName = other.displayName;
        this.tier = other.tier;
        this.strength = other.strength;
        this.dexterity = other.dexterity;
        this.intelligence = other.intelligence;
        this.agility = other.agility;
        this.defense = other.defense;
        this.sockets = other.sockets;
        this.dropType = other.dropType;
        this.restrictions = other.restrictions;
        this.set = other.set;
        this.addedLore = other.addedLore;
        this.material = other.material;
        this.quest = other.quest;
        this.classRequirement = other.classRequirement;
        this.majorIds = Arrays.copyOf(other.majorIds, other.majorIds.length);
        this.identified = other.identified;
        this.type = other.type;
        this.level = other.level;
    }

    public Item(Map<String, Integer> ids, String name, String displayName, int level, int strength, int dexterity, int intelligence, int agility, int defense, String tier, Integer sockets, String dropType,
                @Nullable String restrictions, @Nullable String set, @Nullable String addedLore, @Nullable String material,
                @Nullable String quest, @Nullable ClassType classRequirement, String[] majorIds, boolean identified, ItemType type) {
        for (Map.Entry<String, Integer> entry : ids.entrySet()) {

            Integer uid = idNameToUid.getFromKey(entry.getKey());
            if (uid == null) {
                uid = currentUid++;
                idNameToUid.put(entry.getKey(), uid);
            }
            this.ids.put(uid, entry.getValue());
        }

        this.name = name;
        this.displayName = displayName;
        this.level = level;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.agility = agility;
        this.defense = defense;
        this.tier = tier;
        this.sockets = sockets;
        this.dropType = dropType;
        this.restrictions = restrictions;
        this.set = set;
        this.addedLore = addedLore;
        this.material = material;
        this.quest = quest;
        this.classRequirement = classRequirement;
        this.majorIds = majorIds;
        this.identified = identified;
        this.type = type;
    }

    public Item(ResultSet response, ItemType itemType) throws SQLException {
        this.name = GetSql.convertFromSql(response.getString("name"));
        this.displayName = GetSql.convertFromSql(response.getString("displayName"));
        this.tier = response.getString("tier");
        this.sockets = response.getInt("sockets");
        this.strength = response.getInt("strength");
        this.dexterity = response.getInt("dexterity");
        this.intelligence = response.getInt("intelligence");
        this.agility = response.getInt("agility");
        this.defense = response.getInt("defense");
        this.dropType = response.getString("dropType");
        this.restrictions = response.getString("restrictions");
        this.set = response.getString("setString");
        this.addedLore = GetSql.convertFromSql(response.getString("addedLore"));
        this.material = response.getString("material");
        this.quest = GetSql.convertFromSql(response.getString("quest"));
        String classRequirementTemp = response.getString("classRequirement");
        this.classRequirement = classRequirementTemp == null ? null : ClassType.valueOf(classRequirementTemp);
        String majorIdsTemp = response.getString("majorIds");
        this.majorIds = majorIdsTemp == null ? null : majorIdsTemp.split(",");
        this.identified = response.getBoolean("identified");
        this.level = response.getInt("level");
        this.type = itemType;
    }

    public static Item makeItem(Item other) {
        if (other instanceof Accessory) {
            return new Accessory((Accessory) other);
        } else if (other instanceof Armor) {
            return new Armor((Armor) other);
        } else if (other instanceof Weapon) {
            return new Weapon((Weapon) other);
        }
        return new Item(other);
    }

    public static Item makeItem(Map<String, Object> itemMap) {
        String name = (String) itemMap.remove("name");
        String displayName = (String) itemMap.remove("displayName");
        String tier = (String) itemMap.remove("tier");
        int sockets = (Integer) itemMap.remove("sockets");
        Object healthTempObject = itemMap.remove("health");
        int strength = (Integer) itemMap.remove("strength");
        int dexterity = (Integer) itemMap.remove("dexterity");
        int intelligence = (Integer) itemMap.remove("intelligence");
        int agility = (Integer) itemMap.remove("agility");
        int defense = (Integer) itemMap.remove("defense");

        int health;
        if (healthTempObject == null)
            health = 0;
        else {
            String healthTemp = healthTempObject.toString();
            health = Integer.parseInt(healthTemp);
        }
        String dropType = (String) itemMap.remove("dropType");

        Object restrictionsTemp = itemMap.remove("restrictions");
        String restrictions = (restrictionsTemp == JSONObject.NULL) ? null : (String) restrictionsTemp;
        Object setTemp = itemMap.remove("set");
        String set = (setTemp == JSONObject.NULL) ? null : (String) setTemp;
        Object addedLoreTemp = itemMap.remove("addedLore");
        String addedLore = (addedLoreTemp == JSONObject.NULL) ? null : (String) addedLoreTemp;
        Object materialTemp = itemMap.remove("material");
        String material = (materialTemp == JSONObject.NULL) ? null : (String) materialTemp;
        Object questTemp = itemMap.remove("quest");
        String quest = (questTemp == JSONObject.NULL) ? null : (String) questTemp;
        Object classRequirementTemp = itemMap.remove("classRequirement");
        ClassType classRequirement = (classRequirementTemp == JSONObject.NULL) ? null : ClassType.valueOf(classRequirementTemp.toString().toUpperCase());

        String category = (String) itemMap.remove("category");
        String armorTypeTemp = null;
        ItemType typeTemp;
        Pair<Integer, Integer> thunderDamageTemp = null;
        Pair<Integer, Integer> airDamageTemp = null;
        Pair<Integer, Integer> waterDamageTemp = null;
        Pair<Integer, Integer> earthDamageTemp = null;
        Pair<Integer, Integer> fireDamageTemp = null;
        Pair<Integer, Integer> damageTemp = null;
        AttackSpeed attackSpeed = null;
        switch (category) {
            case "armor":
                itemMap.remove("armorColor");
                armorTypeTemp = itemMap.remove("armorType").toString();
                typeTemp = ItemType.valueOf(((String) itemMap.remove("type")).toUpperCase());
                break;
            case "weapon":
                Object attackSpeedTemp = itemMap.remove("attackSpeed");
                attackSpeed = (attackSpeedTemp == JSONObject.NULL) ? null : AttackSpeed.valueOf(attackSpeedTemp.toString().toUpperCase());

                typeTemp = ItemType.valueOf(((String) itemMap.remove("type")).toUpperCase());
                String[] dmg = ((String) itemMap.remove("thunderDamage")).split("-");
                thunderDamageTemp = new Pair<>(Integer.parseInt(dmg[0]), Integer.parseInt(dmg[1]));
                dmg = ((String) itemMap.remove("airDamage")).split("-");
                airDamageTemp = new Pair<>(Integer.parseInt(dmg[0]), Integer.parseInt(dmg[1]));
                dmg = ((String) itemMap.remove("waterDamage")).split("-");
                waterDamageTemp = new Pair<>(Integer.parseInt(dmg[0]), Integer.parseInt(dmg[1]));
                dmg = ((String) itemMap.remove("earthDamage")).split("-");
                earthDamageTemp = new Pair<>(Integer.parseInt(dmg[0]), Integer.parseInt(dmg[1]));
                dmg = ((String) itemMap.remove("fireDamage")).split("-");
                fireDamageTemp = new Pair<>(Integer.parseInt(dmg[0]), Integer.parseInt(dmg[1]));
                dmg = ((String) itemMap.remove("damage")).split("-");
                damageTemp = new Pair<>(Integer.parseInt(dmg[0]), Integer.parseInt(dmg[1]));
                break;
            case "accessory":
                typeTemp = ItemType.valueOf(((String) itemMap.remove("accessoryType")).toUpperCase());
                break;
            default:
                typeTemp = ItemType.UNKNOWN;
        }
        String[] majorIdsTemp;
        if (itemMap.containsKey("majorIds")) {
            JSONArray major = (JSONArray) itemMap.remove("majorIds");
            int i = 0;
            majorIdsTemp = new String[major.length()];
            for (Object id : major) {
                majorIdsTemp[i++] = id.toString();
            }
        } else majorIdsTemp = new String[0];
        boolean identified = itemMap.containsKey("identified") && (Boolean) itemMap.remove("identified");

        String levelTemp = itemMap.remove("level").toString();
        int level = Integer.parseInt(levelTemp);
        itemMap.remove("skin");
        Map<String, Integer> idsTemp = new HashMap<>();
        for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
            idsTemp.put(entry.getKey(), (Integer) entry.getValue());
        }
        switch (typeTemp) {
            case HELMET:
            case CHESTPLATE:
            case LEGGINGS:
            case BOOTS:
                return new Armor(idsTemp, name, displayName, level, strength, dexterity, intelligence, agility, defense, tier, sockets, dropType, restrictions, set, addedLore, material, quest, classRequirement, majorIdsTemp, identified, typeTemp, armorTypeTemp, health);
            case WAND:
            case DAGGER:
            case SPEAR:
            case BOW:
            case RELIK:
                return new Weapon(idsTemp, name, displayName, level, strength, dexterity, intelligence, agility, defense, tier, sockets, dropType, restrictions, set, addedLore, material, quest, classRequirement, majorIdsTemp, identified, typeTemp, attackSpeed, thunderDamageTemp, airDamageTemp, waterDamageTemp, earthDamageTemp, fireDamageTemp, damageTemp);
            case RING:
            case BRACELET:
            case NECKLACE:
                return new Accessory(idsTemp, name, displayName, level, strength, dexterity, intelligence, agility, defense, tier, sockets, dropType, restrictions, set, addedLore, material, quest, classRequirement, majorIdsTemp, identified, typeTemp, health);
            default:
                throw new RuntimeException("Unknown type " + typeTemp);
        }
    }

    public static int getIdIndex(String idName) {
        return idNameToUid.getFromKey(idName);
    }

    public static String getIdName(Integer index) {
        return idNameToUid.getFromVal(index);
    }


    public void addIds(ResultSet response) throws SQLException {
        if (!response.isClosed())
            while (response.next()) {
                String idName = response.getString("id_name");
                Integer uid = idNameToUid.getFromKey(idName);
                if (uid == null) {
                    uid = currentUid++;
                    idNameToUid.put(idName, uid);
                }
                this.ids.put(uid, response.getInt("value"));
            }
    }

    public void roll(double negativeRoll, double positiveRoll) {
        if (identified) return;
        for (Map.Entry<Integer, Integer> entry : ids.entrySet()) {
            if (!UNROLLABLE.contains(idNameToUid.getFromVal(entry.getKey()))) {
                int value = entry.getValue();
                if (value < 0) {
                    value = (int) Math.round(value * negativeRoll);
                } else {
                    value = (int) Math.round(value * positiveRoll);
                }
                entry.setValue(value);
            }
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Item && ((Item) obj).name.equals(name);
    }


    public boolean isSkillImpossible(int[] reqs, int[] skills, Set<ElementSkill> archetype) {
        int skillsLeft = SKILLS_FOR_PLAYER;
        int i = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            if (reqs[i] != 0) {
                int myReq = reqs[i] - skills[i];
                if (myReq > 0) {
                    if (!archetype.contains(elementSkill))
                        return true; // return fails for combinations that aren't in our archetype
                    skillsLeft -= myReq;
                }
            }
            i++;
        }
        return skillsLeft < 0;
    }

    public int getId(int idName) {
        return ids.getOrDefault(idName, 0);
    }

    public int getRequiredSkill(ElementSkill elementSkill) {
        switch (elementSkill) {
            case THUNDER:
                return dexterity;
            case AIR:
                return agility;
            case EARTH:
                return strength;
            case WATER:
                return intelligence;
            case FIRE:
                return defense;
        }
        return 0;
    }

    public int getSkill(ElementSkill elementSkill) {
        switch (elementSkill) {
            case THUNDER:
                return getId(ItemIdIndex.DEXTERITY_POINTS);
            case AIR:
                return getId(ItemIdIndex.AGILITY_POINTS);
            case EARTH:
                return getId(ItemIdIndex.STRENGTH_POINTS);
            case WATER:
                return getId(ItemIdIndex.INTELLIGENCE_POINTS);
            case FIRE:
                return getId(ItemIdIndex.DEFENSE_POINTS);
        }
        return 0;
    }

    @Override
    public String toString() {
        return displayName == null ? name : displayName.equals("null") ? name : displayName;
    }

    public enum ItemType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        WAND,
        DAGGER,
        SPEAR,
        BOW,
        RELIK,
        RING,
        BRACELET,
        NECKLACE,
        UNKNOWN
    }

    public enum ClassType {
        MAGE,
        ASSASSIN,
        WARRIOR,
        ARCHER,
        SHAMAN
    }

    public enum AttackSpeed {
        SUPER_SLOW(0),
        VERY_SLOW(1),
        SLOW(2),
        NORMAL(3),
        FAST(4),
        VERY_FAST(5),
        SUPER_FAST(6);

        public static final int MAX_SPEED = 6;
        public int speed;
        private static final Map<Integer, Double> modifierMap = new HashMap<>() {
            {
                put(0, 0.51);
                put(1, 0.83);
                put(2, 1.5);
                put(3, 2.05);
                put(4, 2.5);
                put(5, 3.1);
                put(6, 4.3);
            }
        };

        AttackSpeed(int speed) {
            this.speed = speed;
        }

        public static double toModifier(int myAttackSpeed) {
            return modifierMap.get(Math.max(0, Math.min(myAttackSpeed, MAX_SPEED)));
        }

        public double modifier() {
            return toModifier(speed);
        }
    }
}

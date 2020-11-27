package apple.build.sql.itemdb;

import apple.build.data.enums.ElementSkill;
import apple.build.sql.GetSql;
import apple.build.utils.Pair;
import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Armor;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

public class GetItemSql extends GetSql {
    public static String insertItem(Weapon item) {
        Pair<Integer, Integer> thunderDamage = null;
        Pair<Integer, Integer> airDamage = null;
        Pair<Integer, Integer> waterDamage = null;
        Pair<Integer, Integer> earthDamage = null;
        Pair<Integer, Integer> fireDamage = null;
        int i = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            switch (elementSkill) {
                case THUNDER:
                    thunderDamage = item.elemental[i];
                    break;
                case AIR:
                    airDamage = item.elemental[i];
                    break;
                case EARTH:
                    earthDamage = item.elemental[i];
                    break;
                case WATER:
                    waterDamage = item.elemental[i];
                    break;
                case FIRE:
                    fireDamage = item.elemental[i];
                    break;
            }
            i++;
        }


        return String.format("INSERT INTO %s " +
                        "VALUES ('%s','%s',%d,%d,%d,%d,%d,%d,'%s',%d,'%s',%s,%s,%s,%s,%s,%s,'%s',%b,'%s',%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d);",
                item.type.name().toLowerCase(),
                convertToSql(item.name),
                convertToSql(item.displayName),
                item.level,
                item.strength,
                item.dexterity,
                item.intelligence,
                item.agility,
                item.defense,
                item.tier,
                item.sockets,
                item.dropType,
                convertToNullable(item.restrictions),
                convertToNullable(item.set),
                convertToNullable(convertToSql(item.addedLore)),
                convertToNullable(item.material),
                convertToNullable(convertToSql(item.quest)),
                item.classRequirement == null ? null : "'" + item.classRequirement.name() + "'",
                item.majorIds == null || item.majorIds.length == 0 ? null : String.join(",", item.majorIds),
                item.identified,
                item.attackSpeed.name(),
                thunderDamage.getValue(),
                airDamage.getValue(),
                waterDamage.getValue(),
                earthDamage.getValue(),
                fireDamage.getValue(),
                thunderDamage.getKey(),
                airDamage.getKey(),
                waterDamage.getKey(),
                earthDamage.getKey(),
                fireDamage.getKey(),
                item.damage.getValue(),
                item.damage.getKey()
        );
    }

    public static String insertItem(Armor item) {
        return String.format("INSERT INTO %s " +
                        "VALUES ('%s','%s',%d,%d,%d,%d,%d,%d,'%s',%d,'%s',%s,%s,%s,%s,%s,%s,'%s',%b,'%s',%d);",
                item.type.name().toLowerCase(),
                convertToSql(item.name),
                convertToSql(item.displayName),
                item.level,
                item.strength,
                item.dexterity,
                item.intelligence,
                item.agility,
                item.defense,
                item.tier,
                item.sockets,
                item.dropType,
                convertToNullable(item.restrictions),
                convertToNullable(item.set),
                convertToNullable(convertToSql(item.addedLore)),
                convertToNullable(item.material),
                convertToNullable(convertToSql(item.quest)),
                item.classRequirement == null ? null : "'" + item.classRequirement.name() + "'",
                String.join(",", item.majorIds),
                item.identified,
                item.armorType,
                item.health);
    }

    public static String insertItem(Accessory item) {
        return String.format("INSERT INTO %s " +
                        "VALUES ('%s','%s',%d,%d,%d,%d,%d,%d,'%s',%d,'%s',%s,%s,%s,%s,%s,%s,'%s',%b,%d);",
                item.type.name().toLowerCase(),
                convertToSql(item.name),
                convertToSql(item.displayName),
                item.level,
                item.strength,
                item.dexterity,
                item.intelligence,
                item.agility,
                item.defense,
                item.tier,
                item.sockets,
                item.dropType,
                convertToNullable(item.restrictions),
                convertToNullable(item.set),
                convertToNullable(convertToSql(item.addedLore)),
                convertToNullable(item.material),
                convertToNullable(convertToSql(item.quest)),
                item.classRequirement == null ? null : "'" + item.classRequirement.name() + "'",
                String.join(",", item.majorIds),
                item.identified,
                item.health);
    }

    public static String existsItem(String type, String name) {
        return String.format("select count(*) from %s where name = '%s';", type, convertToSql(name));
    }

    public static String getAllItems(Item.ItemType itemType) {
        return "SELECT * FROM " + itemType.name().toLowerCase();
    }

    public static String insertId(String name, String id, int value) {
        return String.format("INSERT INTO ids VALUES ('%s','%s',%d);", convertToSql(name), convertToSql(id), value);
    }

    public static String getAllIds(String item) {
        return String.format("SELECT * FROM ids WHERE name = '%s'", convertToSql(item));
    }
}

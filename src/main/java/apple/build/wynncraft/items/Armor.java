package apple.build.wynncraft.items;

import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Armor extends Item {
    public final int health;
    public String armorType;

    public Armor(Map<String, Integer> ids, String name, String displayName, int level, int strength, int dexterity, int intelligence, int agility, int defense, String tier, Integer sockets, String dropType,
                 @Nullable String restrictions, @Nullable String set, @Nullable String addedLore, @Nullable String material,
                 @Nullable String quest, @Nullable ClassType classRequirement, String[] majorIds, boolean identified,
                 ItemType type, String armorType, int health) {
        super(ids, name, displayName, level, strength, dexterity, intelligence, agility, defense, tier, sockets, dropType, restrictions, set, addedLore, material, quest, classRequirement, majorIds, identified, type);
        this.armorType = armorType;
        this.health = health;
    }

    public Armor(ResultSet response, ItemType itemType) throws SQLException {
        super(response, itemType);
        this.armorType = response.getString("armorType");
        this.health = response.getInt("health");
    }
}

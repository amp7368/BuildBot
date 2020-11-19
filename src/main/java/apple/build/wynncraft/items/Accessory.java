package apple.build.wynncraft.items;

import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Accessory extends Item {
    public final int health;

    public Accessory(Map<String, Integer> ids, String name, String displayName, int level, int strength, int dexterity, int intelligence, int agility,int defense, String tier, Integer sockets, String dropType, @Nullable String restrictions, @Nullable String set, @Nullable String addedLore, @Nullable String material, @Nullable String quest, @Nullable ClassType classRequirement, String[] majorIds, boolean identified, ItemType type, int health) {
        super(ids, name, displayName, level, strength, dexterity, intelligence, agility, defense, tier, sockets, dropType, restrictions, set, addedLore, material, quest, classRequirement, majorIds, identified, type);
        this.health = health;
    }

    public Accessory(ResultSet response, ItemType itemType) throws SQLException {
        super(response, itemType);
        health = response.getInt("health");
    }

    public Accessory(Accessory other) {
        super(other);
        this.health = other.health;
    }
}

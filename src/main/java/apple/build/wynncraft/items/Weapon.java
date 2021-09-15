package apple.build.wynncraft.items;

import apple.build.search.enums.ElementSkill;
import apple.build.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class Weapon extends Item {
    public final AttackSpeed attackSpeed;
    public final Pair<Integer, Integer>[] elemental;
    public final Pair<Integer, Integer> damage;

    public Weapon(Map<String, Integer> ids, String name, String displayName, int level, int strength, int dexterity, int intelligence, int agility, int defense, String tier, int sockets, String dropType, String restrictions, String set, String addedLore, String material, String quest, ClassType classRequirement, String[] majorIds, boolean identified, ItemType type, AttackSpeed attackSpeed, Pair<Integer, Integer> thunderDamage, Pair<Integer, Integer> airDamage, Pair<Integer, Integer> waterDamage, Pair<Integer, Integer> earthDamage, Pair<Integer, Integer> fireDamage, Pair<Integer, Integer> damage) {
        super(ids, name, displayName, level, strength, dexterity, intelligence, agility, defense, tier, sockets, dropType, restrictions, set, addedLore, material, quest, classRequirement, majorIds, identified, type);
        this.elemental = new Pair[5];
        this.attackSpeed = attackSpeed;
        int i = 0;
        for (ElementSkill element : ElementSkill.values()) {
            switch (element) {
                case THUNDER -> elemental[i] = thunderDamage;
                case AIR -> elemental[i] = airDamage;
                case EARTH -> elemental[i] = earthDamage;
                case WATER -> elemental[i] = waterDamage;
                case FIRE -> elemental[i] = fireDamage;
            }
            i++;
        }
        this.damage = damage;
    }

    public Weapon(ResultSet response, ItemType itemType) throws SQLException {
        super(response, itemType);
        this.attackSpeed = AttackSpeed.valueOf(response.getString("attackSpeed"));
        this.elemental = new Pair[5];
        int i = 0;
        for (ElementSkill element : ElementSkill.values()) {
            switch (element) {
                case THUNDER -> elemental[i] = new Pair<>(response.getInt("thunderDamageLower"), response.getInt("thunderDamageUpper"));
                case AIR -> elemental[i] = new Pair<>(response.getInt("airDamageLower"), response.getInt("airDamageUpper"));
                case EARTH -> elemental[i] = new Pair<>(response.getInt("earthDamageLower"), response.getInt("earthDamageUpper"));
                case WATER -> elemental[i] = new Pair<>(response.getInt("waterDamageLower"), response.getInt("waterDamageUpper"));
                case FIRE -> elemental[i] = new Pair<>(response.getInt("fireDamageLower"), response.getInt("fireDamageUpper"));
            }
            i++;
        }
        this.damage = new Pair<>(response.getInt("damageLower"), response.getInt("damageUpper"));
    }

    public Weapon(Weapon other) {
        super(other);
        this.attackSpeed = other.attackSpeed;
        this.elemental = Arrays.copyOf(other.elemental, other.elemental.length);
        this.damage = other.damage;
    }
}

package apple.build.wynncraft.items;

import apple.build.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Weapon extends Item {

    public final AttackSpeed attackSpeed;
    public final Pair<Integer, Integer> thunderDamage;
    public final Pair<Integer, Integer> airDamage;
    public final Pair<Integer, Integer> waterDamage;
    public final Pair<Integer, Integer> earthDamage;
    public final Pair<Integer, Integer> fireDamage;
    public final Pair<Integer, Integer> damage;

    public Weapon(Map<String, Integer> ids, String name, String displayName, int level, int strength, int dexterity, int intelligence, int agility,int defense, String tier, int sockets, String dropType, String restrictions, String set, String addedLore, String material, String quest, ClassType classRequirement, String[] majorIds, boolean identified, ItemType type, AttackSpeed attackSpeed, Pair<Integer, Integer> thunderDamage, Pair<Integer, Integer> airDamage, Pair<Integer, Integer> waterDamage, Pair<Integer, Integer> earthDamage, Pair<Integer, Integer> fireDamage, Pair<Integer, Integer> damage) {
        super(ids, name, displayName, level,  strength, dexterity, intelligence, agility, defense,tier, sockets, dropType, restrictions, set, addedLore, material, quest, classRequirement, majorIds, identified, type);
        this.attackSpeed = attackSpeed;
        this.thunderDamage = thunderDamage;
        this.waterDamage = waterDamage;
        this.airDamage = airDamage;
        this.earthDamage = earthDamage;
        this.fireDamage = fireDamage;
        this.damage = damage;
    }

    public Weapon(ResultSet response, ItemType itemType) throws SQLException {
        super(response, itemType);
        this.attackSpeed = AttackSpeed.valueOf(response.getString("attackSpeed"));
        this.thunderDamage = new Pair<>(response.getInt("thunderDamageLower"), response.getInt("thunderDamageUpper"));
        this.airDamage = new Pair<>(response.getInt("airDamageLower"), response.getInt("airDamageUpper"));
        this.waterDamage = new Pair<>(response.getInt("waterDamageLower"), response.getInt("waterDamageUpper"));
        this.earthDamage = new Pair<>(response.getInt("earthDamageLower"), response.getInt("earthDamageUpper"));
        this.fireDamage = new Pair<>(response.getInt("fireDamageLower"), response.getInt("fireDamageUpper"));
        this.damage = new Pair<>(response.getInt("damageLower"), response.getInt("damageUpper"));
    }
}

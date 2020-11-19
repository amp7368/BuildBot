package apple.build;

import apple.build.data.Build;
import apple.build.data.BuildGenerator;
import apple.build.data.constraints.advanced_defense.ConstraintDefense;
import apple.build.data.constraints.filter.BuildConstraintExclusion;
import apple.build.data.ElementSkill;
import apple.build.data.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.data.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.data.constraints.general.ConstraintHp;
import apple.build.data.constraints.general.ConstraintId;
import apple.build.data.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.sql.GetDB;
import apple.build.sql.VerifyDB;
import apple.build.wynncraft.items.Item;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class BuildMain {

    public static final double NEGATIVE_MAX_ROLL = 0.7;
    public static final double POSITIVE_MAX_ROLL = 1.3;

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        System.out.println("Starting BuildBot");
        VerifyDB.initialize();
        System.out.println("Opened database successfully");
//        GetItems.getItems();
//        System.out.println("Inserted items");

        combinations();
        System.out.println("done");
    }

    private static void combinations() throws SQLException {
        List<Item> helmets = GetDB.getAllItems(Item.ItemType.HELMET);
        helmets.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> chestplates = GetDB.getAllItems(Item.ItemType.CHESTPLATE);
        chestplates.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> leggings = GetDB.getAllItems(Item.ItemType.LEGGINGS);
        leggings.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> boots = GetDB.getAllItems(Item.ItemType.BOOTS);
        boots.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> rings = GetDB.getAllItems(Item.ItemType.RING);
        rings.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> bracelets = GetDB.getAllItems(Item.ItemType.BRACELET);
        bracelets.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        List<Item> necklaces = GetDB.getAllItems(Item.ItemType.NECKLACE);
        necklaces.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        necklaces.sort((anotherString2, anotherString) -> String.CASE_INSENSITIVE_ORDER.compare(anotherString.name, anotherString2.name));
        List<Item> weapon = GetDB.getAllItems(Item.ItemType.DAGGER);
        weapon.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
        Item mine = null;
        String name = "tenut";
        for (Item i : necklaces) {
            if (i.name.toLowerCase().contains(name) || i.displayName.toLowerCase().contains(name)) {
                mine = i;
                break;
            }
        }
        int a = 3;
        helmets.removeIf(item -> item.level < 80);
        chestplates.removeIf(item -> item.level < 80);
        leggings.removeIf(item -> item.level < 80);
        boots.removeIf(item -> item.level < 80);

//        helmets.removeIf(item -> !item.name.equals("Sizzling Shawl"));
//        chestplates.removeIf(item -> !item.name.equals("Boreal-Patterned Aegis"));
//        leggings.removeIf(item -> !item.name.equals("Leggings of Desolation"));
//        boots.removeIf(item -> !item.name.equals("Stardew"));
//        rings.removeIf(item -> !item.name.equals("Gold Static Ring") && !item.name.equals("Diamond Static Ring"));
//        bracelets.removeIf(item -> !item.name.equals("Diamond Static Bracelet"));
//        necklaces.removeIf(item -> !item.name.equals("Tenuto"));
//        weapon.removeIf(item -> !item.name.equals("Divzer"));
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, weapon};
        BuildGenerator builds = new BuildGenerator(allItems);

        long start = System.currentTimeMillis();
        test(builds);
        finish(builds, start);
    }

    private static void test(BuildGenerator builds) {
        builds.addConstraint(new ConstraintId("damageBonus", 99));
        builds.addConstraint(new ConstraintId("damageBonusRaw", 718));
        builds.addConstraint(new ConstraintId("bonusThunderDamage", 143));
        builds.addConstraint(new ConstraintId("bonusEarthDamage", 143));
        builds.addConstraint(new ConstraintId("bonusAirDamage", 90));
        builds.addConstraint(new ConstraintId("attackSpeedBonus", 6));
        builds.addConstraint(new ConstraintId("lifeSteal", 0));
        builds.addConstraint(new ConstraintHp(8400));
        builds.addConstraint(new ConstraintMainDamage(39000));
        for (BuildConstraintExclusion constraint : BuildConstraintExclusion.all)
            builds.addConstraint(constraint);
        builds.generate(new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.THUNDER);
            add(ElementSkill.AIR);
        }});
    }

    private static void highMeleeAssasin(BuildGenerator builds) {
        builds.addConstraint(new ConstraintId("damageBonus", 99));
        builds.addConstraint(new ConstraintId("damageBonusRaw", 718));
        builds.addConstraint(new ConstraintId("bonusThunderDamage", 143));
        builds.addConstraint(new ConstraintId("bonusEarthDamage", 143));
        builds.addConstraint(new ConstraintId("bonusAirDamage", 90));
        builds.addConstraint(new ConstraintId("attackSpeedBonus", 6));
        builds.addConstraint(new ConstraintId("lifeSteal", 0));
        builds.addConstraint(new ConstraintHp(8400));
        builds.addConstraint(new ConstraintMainDamage(39000));
        for (BuildConstraintExclusion constraint : BuildConstraintExclusion.all)
            builds.addConstraint(constraint);
        long start = System.currentTimeMillis();
        builds.generate(new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.THUNDER);
            add(ElementSkill.AIR);
        }});
    }
    private static void setArcherDivzer(BuildGenerator builds) {
        builds.addConstraint(new ConstraintId("manaRegen", 0));
        builds.addConstraint(new ConstraintId("manaSteal", 14));
        builds.addConstraint(new ConstraintId("spellDamage", 68));
        builds.addConstraint(new ConstraintId("spellDamageRaw", 635));
        builds.addConstraint(new ConstraintId("damageBonusRaw", 1745));
        builds.addConstraint(new ConstraintId("bonusThunderDamage", 102));
        builds.addConstraint(new ConstraintId("attackSpeedBonus", -4));
        builds.addConstraint(new ConstraintDefense(ElementSkill.EARTH, -100));
        builds.addConstraint(new ConstraintDefense(ElementSkill.THUNDER, 550));
        builds.addConstraint(new ConstraintDefense(ElementSkill.WATER, 200));
        builds.addConstraint(new ConstraintDefense(ElementSkill.FIRE, 450));
        builds.addConstraint(new ConstraintDefense(ElementSkill.AIR, 0));
        builds.addConstraint(new ConstraintHp(12500));
        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spell.ARROW_STORM, 1));
        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spell.BOMB_ARROW, 2));
        builds.addConstraint(new ConstraintSpellDamage(ConstraintSpellCost.Spell.ARROW_STORM, 38500));
        builds.addConstraint(new ConstraintSpellDamage(ConstraintSpellCost.Spell.BOMB_ARROW, 15500));
        builds.addConstraint(new ConstraintMainDamage(4100));
        for (BuildConstraintExclusion constraint : BuildConstraintExclusion.all)
            builds.addConstraint(constraint);
        long start = System.currentTimeMillis();
        builds.generate(new HashSet<>() {{
            add(ElementSkill.WATER);
            add(ElementSkill.THUNDER);
            add(ElementSkill.FIRE);
        }});
    }

    private static void finish(BuildGenerator builds, long start) {
        int i = 0;
        for (Build build : builds.getBuilds()) {
            if (i++ == 400) break;
            System.out.println(build.toString());
        }
        System.out.println("Total time: " + (System.currentTimeMillis() - start) + " || Size: " + builds.size());
    }
}

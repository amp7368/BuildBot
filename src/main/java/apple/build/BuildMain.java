package apple.build;

import apple.build.data.Build;
import apple.build.data.BuildGenerator;
import apple.build.data.ElementSkill;
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
        List<Item> bows = GetDB.getAllItems(Item.ItemType.BOW);
        bows.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));
//        helmets.removeIf(item -> item.level < 80);
//        chestplates.removeIf(item -> item.level < 80);
//        leggings.removeIf(item -> item.level < 80);
//        boots.removeIf(item -> item.level < 80);
        helmets.removeIf(item -> !item.name.equals("Nighthawk"));
//        chestplates.removeIf(item -> !item.name.equals("Boreal-Patterned Aegis"));
        leggings.removeIf(item -> !item.name.equals("Cinderchain"));
        boots.removeIf(item -> !item.name.equals("Sine"));
//        rings.removeIf(item -> !item.name.equals("Diamond Static Ring") && !item.name.equals("Diamond Static Ring"));
        bracelets.removeIf(item -> !item.name.equals("Diamond Hydro Bracelet"));
        necklaces.removeIf(item -> !item.name.equals("Tenuto"));
        bows.removeIf(item -> !item.name.equals("Divzer"));
//        rings.removeIf(item -> item.level < 60);
//        bracelets.removeIf(item -> item.level < 60);
//        necklaces.removeIf(item -> item.level < 60);
//        wands.removeIf(item -> item.level < 93);
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, bows};

        BuildGenerator builds = new BuildGenerator(allItems);
        builds.addConstraint(new ConstraintId("manaSteal", 14));
        builds.addConstraint(new ConstraintId("spellDamage", 68));
        builds.addConstraint(new ConstraintId("spellDamageRaw", 835));
        builds.addConstraint(new ConstraintId("bonusThunderDamage", 102));
//        builds.addConstraint(new ConstraintDefense("waterDefense", 800));
//        builds.addConstraint(new ConstraintDefense("fireDefense", 600));
//        builds.addConstraint(new ConstraintDefense("earthDefense", 0));
//        builds.addConstraint(new ConstraintDefense("airDefense", 0));
//        builds.addConstraint(new ConstraintId("attackSpeedBonus", -4));
        builds.addConstraint(new ConstraintHp(12000));
        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spell.ARROW_STORM, 1));
        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spell.BOMB_ARROW, 2));
//        builds.addConstraint(new ConstraintSpellDamage(ConstraintSpellCost.Spell.BOMB_ARROW, 15000));
//        builds.addConstraint(new ConstraintSpellDamage(ConstraintSpellCost.Spell.ARROW_STORM, 38000));
        long start = System.currentTimeMillis();

        System.out.println(builds.size() + " || " + (System.currentTimeMillis() - start));
        builds.generate(new HashSet<>() {{
            add(ElementSkill.THUNDER);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        System.out.println(builds.size() + " || " + (System.currentTimeMillis() - start));
        for (Build build : builds.getBuilds()) {
            System.out.println(build.toString());
        }
    }
}

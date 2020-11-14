package apple.build;

import apple.build.data.BuildGenerator;
import apple.build.data.ElementSkill;
import apple.build.data.constraints.advanced_defense.ConstraintDefense;
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
        List<Item> wands = GetDB.getAllItems(Item.ItemType.WAND);
        wands.forEach(item -> item.roll(NEGATIVE_MAX_ROLL, POSITIVE_MAX_ROLL));

        helmets.removeIf(item -> item.level < 80);
        chestplates.removeIf(item -> item.level < 80);
        leggings.removeIf(item -> item.level < 80);
        boots.removeIf(item -> item.level < 80);
        helmets.removeIf(item -> !item.name.equals("Ornate Shadow Cowl"));
        chestplates.removeIf(item -> !item.name.equals("Hetusol"));
        leggings.removeIf(item -> !item.name.equals("Ophiuchus"));
        boots.removeIf(item -> !item.name.equals("Gaea-Hewn Boots"));
        rings.removeIf(item -> !item.name.equals("Yang") && !item.name.equals("Diamond Hydro Ring"));
        bracelets.removeIf(item -> !item.name.equals("Dragon$s Eye Bracelet"));
        necklaces.removeIf(item -> !item.name.equals("Diamond Hydro Necklace"));
        wands.removeIf(item -> !item.name.equals("Nepta Floodbringer"));
//        rings.removeIf(item -> item.level < 80);
//        bracelets.removeIf(item -> item.level < 80);
//        necklaces.removeIf(item -> item.level < 80);
        wands.removeIf(item -> item.level < 80);
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, wands};

        BuildGenerator builds = new BuildGenerator(allItems);
        builds.addConstraint(new ConstraintId("manaRegen", 13));
        builds.addConstraint(new ConstraintId("spellDamage", 90));
        builds.addConstraint(new ConstraintId("spellDamageRaw", 250));
        builds.addConstraint(new ConstraintId("bonusWaterDamage", 60));
//        builds.addConstraint(new ConstraintDefense("waterDefense", 800));
//        builds.addConstraint(new ConstraintDefense("fireDefense", 600));
//        builds.addConstraint(new ConstraintDefense("earthDefense", 0));
//        builds.addConstraint(new ConstraintDefense("airDefense", 0));
//        builds.addConstraint(new ConstraintDefense("thunderDefense", -150));
        builds.addConstraint(new ConstraintHp(16000));
        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spell.METEOR, 1));
//        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spell.METEOR, 11500));
        builds.generate(new HashSet<>() {{
            add(ElementSkill.EARTH);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        System.out.println(builds.size());

    }
}

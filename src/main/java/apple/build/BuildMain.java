package apple.build;

import apple.build.data.BuildGenerator;
import apple.build.data.ElementSkill;
import apple.build.data.constraints.ConstraintHp;
import apple.build.data.constraints.ConstraintId;
import apple.build.data.constraints.ConstraintSpellCost;
import apple.build.sql.GetDB;
import apple.build.sql.VerifyDB;
import apple.build.utils.Pretty;
import apple.build.wynncraft.GetItems;
import apple.build.wynncraft.items.Item;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
        rings.removeIf(item -> item.level < 80);
        bracelets.removeIf(item -> item.level < 80);
        necklaces.removeIf(item -> item.level < 80);
        wands.removeIf(item -> item.level < 80);
        List[] allItems = {helmets, chestplates, leggings, boots, new ArrayList<>(rings), rings, bracelets, necklaces, wands};
//        BigInteger combinationTries = BigInteger.valueOf(1);
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(helmets.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(chestplates.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(leggings.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(boots.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(rings.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(rings.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(bracelets.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(necklaces.size()));
//        combinationTries = combinationTries.multiply(BigInteger.valueOf(bows.size()));
//        System.out.println(Pretty.commas(combinationTries.toString()));

        BuildGenerator builds = new BuildGenerator(allItems);
        builds.addConstraint(new ConstraintId("manaRegen", 13));
        builds.addConstraint(new ConstraintId("spellDamage", 90));
        builds.addConstraint(new ConstraintId("spellDamageRaw", 250));
        builds.addConstraint(new ConstraintId("bonusWaterDamage", 60));
        builds.addConstraint(new ConstraintId("waterDefense", 800));
        builds.addConstraint(new ConstraintId("fireDefense", 600));
        builds.addConstraint(new ConstraintId("earthDefense", 0));
        builds.addConstraint(new ConstraintId("airDefense", 0));
        builds.addConstraint(new ConstraintId("thunderDefense", -150));
        builds.addConstraint(new ConstraintHp(16000));
        builds.addConstraint(new ConstraintSpellCost(ConstraintSpellCost.Spells.METEOR));
        builds.generate(new HashSet<>() {{
            add(ElementSkill.THUNDER);
            add(ElementSkill.WATER);
            add(ElementSkill.FIRE);
        }});
        System.out.println(builds.size());


    }
}

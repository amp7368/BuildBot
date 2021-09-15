package apple.build.search.enums;

import apple.build.wynncraft.items.Item;

import java.util.List;
import java.util.function.Supplier;

import static apple.build.search.enums.Spell.*;

public enum WynnClass {
    ARCHER(Item.ItemType.BOW, ARROW_STORM, ESCAPE, BOMB_ARROW, ARROW_SHIELD),
    WARRIOR(Item.ItemType.SPEAR, BASH, CHARGE, UPPERCUT, WAR_SCREAM),
    SHAMAN(Item.ItemType.RELIK, TOTEM, HAUL, AURA, UPROOT),
    MAGE(Item.ItemType.WAND, HEAL, TELEPORT, METEOR, ICE_SNAKE),
    ASSASSIN(Item.ItemType.DAGGER, SPIN_ATTACK, VANISH, MULTIHIT, SMOKE_BOMB);

    private final Item.ItemType weaponSupplier;
    private final Spell[] spells;

    WynnClass(Item.ItemType weaponSupplier, Spell... spells) {
        this.weaponSupplier = weaponSupplier;
        this.spells = new Spell[spells.length];
        for (Spell spell : spells) {
            this.spells[spell.spellNum - 1] = spell;
        }
    }

    public List<Item> getWeapons() {
        return Item.getItems(weaponSupplier);
    }

    public Spell[] getSpells() {
        return this.spells;
    }

    public Item.ItemType getWeaponItemType() {
        return weaponSupplier;
    }

    public static class Constants {
        public static final String ARCHER_ID = "archer";
        public static final String WARRIOR_ID = "warrior";
        public static final String SHAMAN_ID = "shaman";
        public static final String MAGE_ID = "mage";
        public static final String ASSASSIN_ID = "assassin";
    }
}

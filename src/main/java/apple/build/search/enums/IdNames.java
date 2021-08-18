package apple.build.search.enums;

import apple.build.wynncraft.items.Item;

public enum IdNames {
    MANA_STEAL("manaSteal"),
    MANA_REGEN("manaRegen"),
    RAW_SPELL_DMG("spellDamageRaw"),
    MAIN_ATTACK_DMG("damageBonusRaw"),
    ATTACK_SPEED("attackSpeedBonus");

    private final String idName;
    private int idIndex = -1;

    IdNames(String idName) {
        this.idName = idName;
    }

    public String getIdName() {
        return idName;
    }

    public int getIdIndex() {
        if (this.idIndex < 0) this.idIndex = Item.getIdIndex(idName);
        return idIndex;
    }
}

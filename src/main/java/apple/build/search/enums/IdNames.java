package apple.build.search.enums;

public enum IdNames {
    MANA_STEAL("manaSteal"),
    MANA_REGEN("manaRegen"),
    RAW_SPELL_DMG("spellDamageRaw"),
    MAIN_ATTACK_DMG("damageBonusRaw");

    private final String idName;

    IdNames(String idName) {
        this.idName = idName;
    }

    public String getIdName() {
        return idName;
    }
}

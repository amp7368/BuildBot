package apple.build.search.enums;

public enum IdNames {
    MANA_STEAL("manaSteal"),
    MANA_REGEN("manaRegen");

    private final String idName;

    IdNames(String idName) {
        this.idName = idName;
    }

    public String getIdName() {
        return idName;
    }
}

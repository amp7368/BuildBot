package apple.build.search.enums;

import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ElementSkill {
    THUNDER("dexterity", Constants.THUNDER_ID),
    AIR("agility", Constants.AIR_ID),
    EARTH("strength", Constants.EARTH_ID),
    WATER("intelligence", Constants.WATER_ID),
    FIRE("defense", Constants.FIRE_ID);
    public String skill;
    private int damageIdIndex;
    private int defenseRawIndex;
    private int defensePercIndex;
    private boolean wasIdIndexSet = false;
    public String id;

    ElementSkill(String skill, String id) {
        this.skill = skill;
        this.id = id;
    }

    public static ElementSkill[] orderedElements() {
        return List.of(EARTH, THUNDER, WATER, FIRE, AIR).toArray(new ElementSkill[0]);
    }

    public Powder getPowder() {
        return Powder.valueOf(name());
    }

    public int getSkill(int[] skills) {
        int i = 0;
        for (ElementSkill element : values()) {
            if (element == this) {
                return skills[i];
            }
            i++;
        }
        return -1;
    }

    public int getDamageIdIndex() {
        if (!wasIdIndexSet) {
            wasIdIndexSet = true;
            setIdIndex();
        }
        return damageIdIndex;
    }

    public int getDefenseRawIndex() {
        if (!wasIdIndexSet) {
            wasIdIndexSet = true;
            setIdIndex();
        }
        return defenseRawIndex;
    }

    public int getDefensePercIndex() {
        if (!wasIdIndexSet) {
            wasIdIndexSet = true;
            setIdIndex();
        }
        return defensePercIndex;
    }

    private void setIdIndex() {
        this.damageIdIndex = Item.getIdIndex(getDamageId());
        this.defenseRawIndex = Item.getIdIndex(getDefenseId());
        this.defensePercIndex = Item.getIdIndex(getBonusDefenseId());
    }

    @NotNull
    public String getBonusDefenseId() {
        return "bonus" + Pretty.uppercaseFirst(name().toLowerCase()) + "Defense";
    }

    @NotNull
    public String getDefenseId() {
        return name().toLowerCase() + "Defense";
    }

    @NotNull
    public String getDamageId() {
        return "bonus" + Pretty.uppercaseFirst(name().toLowerCase()) + "Damage";
    }

    @NotNull
    public String prettyName() {
        return Pretty.uppercaseFirst(name());
    }

    public static class Constants {
        public static final String THUNDER_ID = "THUNDER";
        public static final String AIR_ID = "AIR";
        public static final String EARTH_ID = "EARTH";
        public static final String WATER_ID = "WATER";
        public static final String FIRE_ID = "FIRE";
    }
}

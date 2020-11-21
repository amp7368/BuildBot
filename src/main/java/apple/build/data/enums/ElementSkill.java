package apple.build.data.enums;

import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;

public enum ElementSkill {
    THUNDER("dexterity"),
    AIR("agility"),
    EARTH("strength"),
    WATER("intelligence"),
    FIRE("defense");
    public String skill;
    public int damageIdIndex;
    public int defenseRawIndex;
    public int defensePercIndex;

    ElementSkill(String skill) {
        this.skill = skill;
        this.damageIdIndex = Item.getIdIndex("bonus" + Pretty.uppercaseFirst(name().toLowerCase() + "Damage"));
        this.defenseRawIndex = Item.getIdIndex(name().toLowerCase() + "Defense");
        this.defensePercIndex = Item.getIdIndex("bonus" + Pretty.uppercaseFirst(name().toLowerCase()) + "Defense");
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
}

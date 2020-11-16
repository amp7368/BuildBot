package apple.build.data;

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

    ElementSkill(String skill) {
        this.skill = skill;
        this.damageIdIndex = Item.getIdIndex("bonus" + Pretty.uppercaseFirst(name().toLowerCase() + "Damage"));
    }
}

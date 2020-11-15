package apple.build.data;

public enum ElementSkill {
    THUNDER("dexterity"),
    AIR("agility"),
    EARTH("strength"),
    WATER("intelligence"),
    FIRE("defense");
    public String skill;

    ElementSkill(String skill) {
        this.skill = skill;
    }
}

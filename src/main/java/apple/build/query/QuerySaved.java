package apple.build.query;

import apple.build.discord.BuildQueryMessage;
import apple.build.search.constraints.ConstraintSimplified;
import apple.build.search.constraints.general.ConstraintId;
import apple.build.search.enums.ElementSkill;
import apple.build.search.enums.WynnClass;
import apple.build.wynncraft.items.Item;
import apple.discord.acd.ACD;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.File;
import java.util.*;

public class QuerySaved {
    public String id = UUID.randomUUID().toString();

    public ElementSkill[] elements;
    public String[] majorIds;
    public WynnClass wynnClass;
    public Integer[] spellDmg;
    public Integer rawMainDmg;
    public Integer mainDmg;
    public Integer health;
    public Integer healthRegen;
    public Integer[] spellCost;
    public Integer[] defense;
    public HashMap<String, ConstraintSimplified> idsConstraints;
    public Integer rawSpellDmg;
    public Item.AttackSpeed attackSpeed;

    public QuerySaved() {
    }


    public QuerySaved(BuildQueryMessage buildQueryMessage) {
        this.elements = buildQueryMessage.getElements().toArray(new ElementSkill[0]);
        this.majorIds = buildQueryMessage.getMajorIds().toArray(new String[0]);
        this.wynnClass = buildQueryMessage.getWynnClass();
        this.spellDmg = buildQueryMessage.getSpellDmg();
        this.rawMainDmg = buildQueryMessage.getRawMainDmg();
        this.mainDmg = buildQueryMessage.getMainDmg();
        this.health = buildQueryMessage.getHealth();
        this.healthRegen = buildQueryMessage.getHealthRegen();
        this.spellCost = buildQueryMessage.getSpellCost();
        this.defense = buildQueryMessage.getDefense();
        this.idsConstraints = new HashMap<>();
        for (Map.Entry<String, ConstraintId> constraint : buildQueryMessage.getIdsConstraints().entrySet()) {
            this.idsConstraints.put(constraint.getKey(), constraint.getValue().getSimplified());
        }
        this.rawSpellDmg = buildQueryMessage.getRawSpellDmg();
        this.attackSpeed = buildQueryMessage.getAttackSpeed();
    }

    public File getFile(File fileToSave) {
        return getFile(fileToSave, id);
    }

    public static File getFile(File fileToSave, String id) {
        return new File(new File(fileToSave, "saved"), id + ".json");
    }

    public String getId() {
        return id;
    }

    public List<ElementSkill> getElements() {
        return new ArrayList<>(List.of(elements));
    }

    public List<String> getMajorIds() {
        return new ArrayList<>(List.of(majorIds));
    }

    public WynnClass getWynnClass() {
        return wynnClass;
    }

    public Integer[] getSpellDmg() {
        return spellDmg;
    }

    public Integer getRawMainDmg() {
        return rawMainDmg;
    }

    public Integer getMainDmg() {
        return mainDmg;
    }

    public Integer getHealth() {
        return health;
    }

    public Integer getHealthRegen() {
        return healthRegen;
    }

    public Integer[] getSpellCost() {
        return spellCost;
    }

    public Integer[] getDefense() {
        return defense;
    }

    public Map<String, ConstraintId> getIdsConstraints() {
        Map<String, ConstraintId> ids = new HashMap<>();
        for (Map.Entry<String, ConstraintSimplified> constraint : idsConstraints.entrySet()) {
            ids.put(constraint.getKey(), new ConstraintId(constraint.getValue().text, constraint.getValue().val));
        }
        return ids;
    }

    public Integer getRawSpellDmg() {
        return rawSpellDmg;
    }

    public Item.AttackSpeed getAttackSpeed() {
        return attackSpeed;
    }

    public BuildQueryMessage toQueryMessage(ACD acd, Member member, MessageChannel channel) {
        return new BuildQueryMessage(acd, member, channel, this);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuerySaved other) {
            return id.equals(other.id);
        }
        return false;
    }
}

package apple.build.discord;

import apple.build.query.QuerySaved;
import apple.build.query.QuerySavingService;
import apple.build.search.Build;
import apple.build.search.BuildGenerator;
import apple.build.search.GeneratorManager;
import apple.build.search.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.search.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.search.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.*;
import apple.build.search.enums.ElementSkill;
import apple.build.search.enums.IdNames;
import apple.build.search.enums.Spell;
import apple.build.search.enums.WynnClass;
import apple.build.utils.Pretty;
import apple.build.wynncraft.items.Item;
import apple.discord.acd.ACD;
import apple.discord.acd.MillisTimeUnits;
import apple.discord.acd.reaction.DiscordEmoji;
import apple.discord.acd.reaction.buttons.GuiButton;
import apple.discord.acd.reaction.buttons.GuiManualButton;
import apple.discord.acd.reaction.buttons.GuiMenu;
import apple.discord.acd.reaction.gui.ACDGui;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static apple.build.search.enums.WynnClass.Constants.*;


public class BuildQueryMessage extends ACDGui {
    // component ids
    public static final String SUBMIT_BUTTON_ID = "submit";
    public static final String MAJOR_ID_SELECTION_ID = "major_id_sel";
    private static final String BACK_BUTTON_ID = "back";
    private static final String MAJOR_ID_CLEAR = "major_id_clear";
    private static final String SPELL_DMG_ID_BIG_UP = "spell_dmg_big_up";
    private static final String SPELL_DMG_ID_UP = "spell_dmg_up";
    private static final String SPELL_DMG_ID_BIG_DOWN = "spell_dmg_down";
    private static final String SPELL_DMG_ID_DOWN = "spell_dmg_big_down";
    private static final String RAW_SPELL_DMG_ID_BIG_UP = "raw_spell_dmg_big_up";
    private static final String RAW_SPELL_DMG_ID_UP = "raw_spell_dmg_up";
    private static final String RAW_SPELL_DMG_ID_BIG_DOWN = "raw_spell_dmg_down";
    private static final String RAW_SPELL_DMG_ID_DOWN = "raw_spell_dmg_big_down";
    private static final String MAIN_DMG_ID_BIG_UP = "main_dmg_big_up";
    private static final String MAIN_DMG_ID_UP = "main_dmg_up";
    private static final String MAIN_DMG_ID_BIG_DOWN = "main_dmg_down";
    private static final String MAIN_DMG_ID_DOWN = "main_dmg_big_down";
    private static final String RAW_MAIN_DMG_ID_BIG_UP = "raw_main_dmg_big_up";
    private static final String RAW_MAIN_DMG_ID_UP = "raw_main_dmg_up";
    private static final String RAW_MAIN_DMG_ID_BIG_DOWN = "raw_main_dmg_down";
    private static final String RAW_MAIN_DMG_ID_DOWN = "raw_main_dmg_big_down";
    private static final String EDIT_HP_ID = "edit_hp";
    private static final String EDIT_HPR_ID = "edit_hpr";
    private static final String EDIT_MS_ID = "edit_mana_steal";
    private static final String EDIT_MR_ID = "edit_mana_regen";
    private static final String EDIT_DEFENSE_ID = "edit_defense";
    private static final String EDIT_SPELL_COST_ID = "edit_spell_cost";
    private static final String EDIT_SOMETHING_UP_LOTS = "something_up_lots";
    private static final String EDIT_SOMETHING_UP = "something_up";
    private static final String EDIT_SOMETHING_DOWN = "something_down";
    private static final String EDIT_SOMETHING_DOWN_LOTS = "something_down_lots";
    private static final String EDIT_ID_ID = "edit_id";
    private static final String ADD_ID_MENU_ID = "add_id_menu";
    private static final String CLEAR_ID = "clear";
    private static final String BACK_ID_BUTTON_ID = "id_menu_back";
    private static final String FORWARD_ID_BUTTON_ID = "id_menu_forward";
    private static final String CANCEL_BUILD = "cancel";
    private static final String REFRESH = "refresh";
    private static final String EDIT_ATTACK_SPEED_MIN = "edit_attack_speed_min";
    private static final String SAVE_BUTTON_ID = "save_query";
    private static final String EDIT_RAW_SPELL = "edit_raw_spell_page";

    // constants for default modifiers
    private static final int SPELL_DMG_BIG_INCREMENT = 1000;
    private static final int SPELL_DMG_INCREMENT = 100;
    private static final int RAW_SPELL_DMG_BIG_INCREMENT = 1000;
    private static final int RAW_SPELL_DMG_INCREMENT = 100;

    // metadata about the BuildMessage
    private BuildPhase phase = BuildPhase.ELEMENTS;
    private final List<SubPhase> subPages = new ArrayList<>();
    private MessageEmbed error = null;
    private int idPage = 0;
    private QuerySaved querySaved = null;
    private UUID taskUUID;
    private BuildGenerator generator = null;
    private double progress = 0;
    private BuildShowListMessage buildsGui = null;
    private GeneratorManager.TaskType priority = GeneratorManager.TaskType.PRIMARY;
    private int placeInLine;
    private final ACD acd;
    private final Member member;

    // fields for the actual build
    private List<ElementSkill> elements = new ArrayList<>();
    private List<String> majorIds = new ArrayList<>();
    private WynnClass wynnClass = WynnClass.MAGE;
    private Integer[] spellDmg = new Integer[wynnClass.getSpells().length];
    private Integer rawMainDmg = null;
    private Integer mainDmg = null;
    private Integer health = 0;
    private Integer healthRegen = null;
    private Integer[] spellCost = new Integer[wynnClass.getSpells().length];
    private Integer[] defense = new Integer[ElementSkill.values().length];
    private Map<String, ConstraintId> idsConstraints = new HashMap<>();
    private Integer rawSpellDmg = null;
    private Item.AttackSpeed attackSpeed = Item.AttackSpeed.SUPER_SLOW;

    // temporary use values for sending arguments through threads
    private Spell currentSpellMiscUse;
    private String currentIdNameMiscUse;
    private ElementSkill currentElementMiscUse;

    public BuildQueryMessage(ACD acd, Member member, MessageChannel channel, QuerySaved query) {
        super(acd, channel);
        this.member = member;
        this.acd = acd;
        this.elements = query.getElements();
        this.majorIds = query.getMajorIds();
        this.wynnClass = query.getWynnClass();
        this.spellDmg = query.getSpellDmg();
        this.rawMainDmg = query.getRawMainDmg();
        this.mainDmg = query.getMainDmg();
        this.health = query.getHealth();
        this.healthRegen = query.getHealthRegen();
        this.spellCost = query.getSpellCost();
        this.defense = query.getDefense();
        this.idsConstraints = query.getIdsConstraints();
        this.rawSpellDmg = query.getRawSpellDmg();
        this.attackSpeed = query.getAttackSpeed();
    }

    public BuildQueryMessage(ACD acd, Member member, MessageChannel channel) {
        super(acd, channel);
        this.acd = acd;
        this.member = member;
    }

    @Override
    protected void initButtons() {
    }

    @Override
    protected long getMillisToOld() {
        return MillisTimeUnits.DAY * 3;
    }

    @Override
    protected Message makeMessage() {
        if (subPages.isEmpty()) {
            return switch (phase) {
                case ELEMENTS -> makeElementsMessage();
                case MAJOR_ID -> makeMajorIdMessage();
                case CLASS -> makeClassMessage();
                case SPELL_ATTACK -> makeSpellMessage();
                case MAIN_ATTACK -> makeMainAttackMessage();
                case MISC -> makeMiscMessage();
                case ID -> makeIdMessage();
                case CONFIRM -> makeConfirmMessage();
                case WAITING_UPDATES -> makeWaitingUpdatesMessage();
                case BUILD -> makeBuildsMessage();
                default -> new MessageBuilder("incomplete").build();
            };
        } else {
            return switch (subPages.get(0)) {
                case EDIT_HP -> makeEditSomethingMessage("Set the minimum Health", null, "Health: " + health, 100, 1000, i -> {
                    if (i == null) {
                        if (health == null) health = 0;
                        else health = null;
                    } else
                        health = health == null ? i : health + i;
                });
                case EDIT_HPR -> makeEditSomethingMessage("Set the minimum Health Regen", null, "HPR: " + healthRegen, 100, 1000, i -> {
                    if (i == null) {
                        if (healthRegen == null) healthRegen = 0;
                        else healthRegen = null;
                    } else healthRegen = healthRegen == null ? i : healthRegen + i;
                });
                case EDIT_RAW_SPELL -> makeRawSpellMessage();
                case EDIT_MR -> {
                    addManualSimpleButton(event -> idsConstraints.remove(IdNames.MANA_REGEN.getIdName()), CLEAR_ID + IdNames.MANA_REGEN.getIdName());
                    yield makeEditSomethingMessage("Set the minimum Mana Regen",
                            null,
                            "MR:" + getIdPretty(IdNames.MANA_REGEN),
                            1,
                            3,
                            i -> incrementIdConstraint(IdNames.MANA_REGEN.getIdName(), i),
                            new ButtonImpl(CLEAR_ID + IdNames.MANA_REGEN.getIdName(), "Clear", ButtonStyle.PRIMARY, false, null)
                    );
                }
                case EDIT_MS -> {
                    addManualSimpleButton(event -> idsConstraints.remove(IdNames.MANA_STEAL.getIdName()), CLEAR_ID + IdNames.MANA_STEAL.getIdName());
                    yield makeEditSomethingMessage("Set the minimum Mana Steal",
                            null,
                            "MS:" + getIdPretty(IdNames.MANA_STEAL),
                            1,
                            3,
                            i -> incrementIdConstraint(IdNames.MANA_STEAL.getIdName(), i),
                            new ButtonImpl(CLEAR_ID + IdNames.MANA_STEAL.getIdName(), "Clear", ButtonStyle.PRIMARY, false, null)
                    );
                }
                case EDIT_DEFENSE -> {
                    addManualSimpleButton(event -> this.defense[currentElementMiscUse.ordinal()] = null, CLEAR_ID + currentElementMiscUse.name());
                    yield makeEditSomethingMessage("Set the minimum " + currentElementMiscUse.prettyName() + " Defense",
                            null,
                            "Def:" + defense[currentElementMiscUse.ordinal()],
                            10,
                            100,
                            i -> {
                                if (i == null) {
                                    if (this.defense[currentElementMiscUse.ordinal()] == null)
                                        this.defense[currentElementMiscUse.ordinal()] = 0;
                                    else this.defense[currentElementMiscUse.ordinal()] = null;
                                } else {
                                    if (this.defense[currentElementMiscUse.ordinal()] == null) {
                                        this.defense[currentElementMiscUse.ordinal()] = i;
                                    } else {
                                        this.defense[currentElementMiscUse.ordinal()] += i;
                                    }
                                }
                            }, new ButtonImpl(CLEAR_ID + currentElementMiscUse.name(), "Clear", ButtonStyle.PRIMARY, false, null));
                }
                case EDIT_ATTACK_SPEED_MIN -> makeEditSomethingMessage("Set the minimum attack speed",
                        null,
                        Pretty.uppercaseFirst(attackSpeed.name()),
                        1,
                        2,
                        i -> {
                            if (i == null) attackSpeed = Item.AttackSpeed.SUPER_SLOW;
                            else attackSpeed = Item.AttackSpeed.from(attackSpeed.speed + i);
                        });
                case EDIT_SPELL_COST -> {
                    addManualSimpleButton(event -> this.spellCost[currentSpellMiscUse.spellNum - 1] = null, CLEAR_ID + currentSpellMiscUse.spellNum);
                    yield makeEditSomethingMessage("Set the spell cost for " + currentSpellMiscUse.prettyName(),
                            null,
                            Objects.requireNonNullElse(spellCost[currentSpellMiscUse.spellNum - 1] + " Mana", "Not set"),
                            1,
                            2,
                            i -> {
                                if (i == null) {
                                    if (this.spellCost[currentSpellMiscUse.spellNum - 1] == null) {
                                        this.spellCost[currentSpellMiscUse.spellNum - 1] = 0;
                                    } else {
                                        this.spellCost[currentSpellMiscUse.spellNum - 1] = null;
                                    }
                                } else {
                                    if (this.spellCost[currentSpellMiscUse.spellNum - 1] == null) {
                                        this.spellCost[currentSpellMiscUse.spellNum - 1] = i;
                                    } else {
                                        this.spellCost[currentSpellMiscUse.spellNum - 1] += i;
                                    }
                                }
                            }, new ButtonImpl(CLEAR_ID + currentSpellMiscUse.spellNum, "Clear", ButtonStyle.PRIMARY, false, null));
                }
                case EDIT_ID -> {
                    ConstraintId constraintId = idsConstraints.get(currentIdNameMiscUse);
                    addManualSimpleButton(event -> idsConstraints.remove(currentIdNameMiscUse), CLEAR_ID + currentIdNameMiscUse);
                    yield makeEditSomethingMessage("Set the minimum value for " + currentIdNameMiscUse,
                            null,
                            String.valueOf(constraintId == null ? 0 : constraintId.getValue()),
                            1,
                            10,
                            i -> {
                                if (i == null) {
                                    if (constraintId == null) {
                                        idsConstraints.put(currentIdNameMiscUse, new ConstraintId(currentIdNameMiscUse, 0));
                                    } else {
                                        idsConstraints.remove(currentIdNameMiscUse);
                                    }
                                }
                                if (constraintId == null) {
                                    idsConstraints.put(currentIdNameMiscUse, new ConstraintId(currentIdNameMiscUse, i));
                                } else {
                                    constraintId.setValue(constraintId.getValue() + i);
                                }
                            },
                            new ButtonImpl(CLEAR_ID + currentIdNameMiscUse, "Clear", ButtonStyle.PRIMARY, false, null)
                    );
                }
            };
        }
    }

    private void incrementIdConstraint(String idName, Integer i) {
        if (i == null) {
            if (idsConstraints.containsKey(idName)) {
                idsConstraints.remove(idName, new ConstraintId(idName, 0));
            } else {
                idsConstraints.put(idName, new ConstraintId(idName, 0));
            }
        } else {
            ConstraintId constraintId = idsConstraints.computeIfAbsent(idName, (c) -> new ConstraintId(idName, 0));
            constraintId.setValue(constraintId.getValue() + i);
        }
    }

    @NotNull
    private String getIdPretty(IdNames idName) {
        ConstraintId manaSteal = idsConstraints.get(idName.getIdName());
        return manaSteal == null ? "not set" : String.valueOf(manaSteal.getValue());
    }

    @GuiButton(id = SUBMIT_BUTTON_ID)
    public void submit(ComponentInteraction interaction) {
        if (phase != BuildPhase.CONFIRM) {
            this.querySaved = null;
        }
        if (subPages.isEmpty()) {
            switch (phase) {
                case ELEMENTS -> elementsSubmit(interaction);
                case CONFIRM -> finishSubmit(interaction);
                default -> simpleSubmit(interaction);
            }
        } else {
            simpleSubPageSubmit(interaction);
        }
    }

    private void simpleSubPageSubmit(ComponentInteraction interaction) {
        subPages.remove(0);
        editAsReply(interaction);
    }

    @GuiButton(id = BACK_BUTTON_ID)
    public void back(ComponentInteraction interaction) {
        phase = phase.getPreviousPage();
        editAsReply(interaction);
    }

    @GuiButton(id = ARCHER_ID)
    public void setArcher(ComponentInteraction interaction) {
        this.wynnClass = WynnClass.ARCHER;
        this.spellDmg = new Integer[wynnClass.getSpells().length];
        this.spellCost = new Integer[wynnClass.getSpells().length];
        editAsReply(interaction);
    }

    @GuiButton(id = MAGE_ID)
    public void setMage(ComponentInteraction interaction) {
        this.wynnClass = WynnClass.MAGE;
        this.spellDmg = new Integer[wynnClass.getSpells().length];
        this.spellCost = new Integer[wynnClass.getSpells().length];
        editAsReply(interaction);
    }

    @GuiButton(id = SHAMAN_ID)
    public void setShaman(ComponentInteraction interaction) {
        this.wynnClass = WynnClass.SHAMAN;
        this.spellDmg = new Integer[wynnClass.getSpells().length];
        this.spellCost = new Integer[wynnClass.getSpells().length];
        editAsReply(interaction);
    }

    @GuiButton(id = WARRIOR_ID)
    public void setWarrior(ComponentInteraction interaction) {
        this.wynnClass = WynnClass.WARRIOR;
        this.spellDmg = new Integer[wynnClass.getSpells().length];
        this.spellCost = new Integer[wynnClass.getSpells().length];
        editAsReply(interaction);
    }

    @GuiButton(id = ASSASSIN_ID)
    public void setAssassin(ComponentInteraction interaction) {
        this.wynnClass = WynnClass.ASSASSIN;
        this.spellDmg = new Integer[wynnClass.getSpells().length];
        this.spellCost = new Integer[wynnClass.getSpells().length];
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.AIR_ID)
    public void addAir(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.AIR)) {
            this.elements.add(ElementSkill.AIR);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.WATER_ID)
    public void addWater(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.WATER)) {
            this.elements.add(ElementSkill.WATER);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.EARTH_ID)
    public void addEarth(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.EARTH)) {
            this.elements.add(ElementSkill.EARTH);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.THUNDER_ID)
    public void addThunder(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.THUNDER)) {
            this.elements.add(ElementSkill.THUNDER);
        }
        editAsReply(interaction);
    }

    @GuiButton(id = ElementSkill.Constants.FIRE_ID)
    public void addFire(ComponentInteraction interaction) {
        if (!this.elements.remove(ElementSkill.FIRE)) {
            this.elements.add(ElementSkill.FIRE);
        }
        editAsReply(interaction);
    }

    @GuiMenu(id = MAJOR_ID_SELECTION_ID)
    public void majorIdMenu(SelectionMenuEvent interaction) {
        majorIds.addAll(interaction.getValues());
        editAsReply(interaction);
    }

    @GuiButton(id = MAJOR_ID_CLEAR)
    public void majorIdClear(ComponentInteraction interaction) {
        majorIds.clear();
        editAsReply(interaction);
    }


    @GuiButton(id = RAW_SPELL_DMG_ID_BIG_UP)
    public void rawSpellDmgBigUp(ComponentInteraction interaction) {
        if (rawSpellDmg == null) rawSpellDmg = 0;
        rawSpellDmg += RAW_SPELL_DMG_BIG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = RAW_SPELL_DMG_ID_UP)
    public void rawSpellDmgUp(ComponentInteraction interaction) {
        if (rawSpellDmg == null) rawSpellDmg = 0;
        rawSpellDmg += RAW_SPELL_DMG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = RAW_SPELL_DMG_ID_DOWN)
    public void rawSpellDmgDown(ComponentInteraction interaction) {
        if (rawSpellDmg == null) rawSpellDmg = 0;
        rawSpellDmg -= RAW_SPELL_DMG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = RAW_SPELL_DMG_ID_BIG_DOWN)
    public void rawSpellDmgBigDown(ComponentInteraction interaction) {
        if (rawSpellDmg == null) rawSpellDmg = 0;
        rawSpellDmg -= RAW_SPELL_DMG_BIG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = RAW_MAIN_DMG_ID_BIG_UP)
    public void rawMainDmgBigUp(ComponentInteraction interaction) {
        if (rawMainDmg == null) rawMainDmg = 0;
        rawMainDmg += RAW_SPELL_DMG_BIG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = RAW_MAIN_DMG_ID_UP)
    public void rawMainDmgUp(ComponentInteraction interaction) {
        if (rawMainDmg == null) rawMainDmg = 0;
        rawMainDmg += RAW_SPELL_DMG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = RAW_MAIN_DMG_ID_DOWN)
    public void rawMainDmgDown(ComponentInteraction interaction) {
        if (rawMainDmg == null) rawMainDmg = 0;
        rawMainDmg -= RAW_SPELL_DMG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = RAW_MAIN_DMG_ID_BIG_DOWN)
    public void rawMainDmgBigDown(ComponentInteraction interaction) {
        if (rawMainDmg == null) rawMainDmg = 0;
        rawMainDmg -= RAW_SPELL_DMG_BIG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = MAIN_DMG_ID_BIG_UP)
    public void mainDmgBigUp(ComponentInteraction interaction) {
        if (mainDmg == null) mainDmg = 0;
        mainDmg += SPELL_DMG_BIG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = MAIN_DMG_ID_UP)
    public void mainDmgUp(ComponentInteraction interaction) {
        if (mainDmg == null) mainDmg = 0;
        mainDmg += SPELL_DMG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = MAIN_DMG_ID_DOWN)
    public void mainDmgDown(ComponentInteraction interaction) {
        if (mainDmg == null) mainDmg = 0;
        mainDmg -= SPELL_DMG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = MAIN_DMG_ID_BIG_DOWN)
    public void mainDmgBigDown(ComponentInteraction interaction) {
        if (mainDmg == null) mainDmg = 0;
        mainDmg -= SPELL_DMG_BIG_INCREMENT;
        editAsReply(interaction);
    }

    @GuiButton(id = EDIT_HP_ID)
    public void editHp(ComponentInteraction interaction) {
        subPages.add(SubPhase.EDIT_HP);
        editAsReply(interaction);
    }

    @GuiButton(id = EDIT_HPR_ID)
    public void editHpr(ComponentInteraction interaction) {
        subPages.add(SubPhase.EDIT_HPR);
        editAsReply(interaction);
    }

    @GuiButton(id = EDIT_ATTACK_SPEED_MIN)
    public void setEditAttackSpeedMin(ComponentInteraction interaction) {
        subPages.add(SubPhase.EDIT_ATTACK_SPEED_MIN);
        editAsReply(interaction);
    }

    @GuiButton(id = EDIT_RAW_SPELL)
    public void editRawSpellPage(ComponentInteraction interaction) {
        subPages.add(SubPhase.EDIT_RAW_SPELL);
        editAsReply(interaction);
    }

    @GuiButton(id = SAVE_BUTTON_ID)
    public void save(ComponentInteraction interaction) {
        this.querySaved = new QuerySaved(this);
        editAsReply(interaction);
        QuerySavingService.get().queue(querySaved, member.getIdLong());
    }

    @GuiButton(id = EDIT_MR_ID)
    public void editMr(ComponentInteraction interaction) {
        subPages.add(SubPhase.EDIT_MR);
        editAsReply(interaction);
    }

    @GuiButton(id = EDIT_MS_ID)
    public void editMs(ComponentInteraction interaction) {
        subPages.add(SubPhase.EDIT_MS);
        editAsReply(interaction);
    }

    @GuiManualButton
    public void editDefense(ComponentInteraction interaction, ElementSkill defenseElement) {
        subPages.add(SubPhase.EDIT_DEFENSE);
        currentElementMiscUse = defenseElement;
        editAsReply(interaction);
    }

    @GuiManualButton
    public void editSpellCost(ComponentInteraction interaction, Spell spell) {
        currentSpellMiscUse = spell;
        subPages.add(SubPhase.EDIT_SPELL_COST);
        editAsReply(interaction);
    }

    @GuiMenu(id = ADD_ID_MENU_ID)
    public void addIdMenu(SelectionMenuEvent interaction) {
        @Nullable List<SelectOption> selections = interaction.getSelectedOptions();
        if (selections != null && !selections.isEmpty()) {
            SelectOption choice = selections.get(0);
            this.subPages.add(SubPhase.EDIT_ID);
            this.currentIdNameMiscUse = choice.getValue();
        }
        editAsReply(interaction);
    }

    @GuiButton(id = FORWARD_ID_BUTTON_ID)
    public void forwardIdMenu(ComponentInteraction interaction) {
        this.idPage++;
        editAsReply(interaction);
    }

    @GuiButton(id = BACK_ID_BUTTON_ID)
    public void backIdMenu(ComponentInteraction interaction) {
        this.idPage--;
        editAsReply(interaction);
    }

    @GuiButton(id = REFRESH)
    public void refreshButton(ComponentInteraction interaction) {
        editAsReply(interaction);
    }

    @GuiButton(id = CANCEL_BUILD)
    public void cancelButton(ComponentInteraction interaction) {
        if (taskUUID != null)
            GeneratorManager.cancel(taskUUID);
        this.phase = BuildPhase.CONFIRM;
        editAsReply(interaction);
    }

    public void elementsSubmit(ComponentInteraction interaction) {
        if (this.elements.size() > 3) {
            EmbedBuilder errorBuilder = new EmbedBuilder();
            errorBuilder.setTitle(DiscordEmoji.RED_X.getEmoji() + "  A maximum of 3 elements is enforced");
            errorBuilder.setColor(DiscordBot.RED_COLOR);
            error = errorBuilder.build();
        } else {
            phase = phase.getNextPage();
        }
        editAsReply(interaction);
    }

    private void simpleSubmit(ComponentInteraction interaction) {
        phase = phase.getNextPage();
        editAsReply(interaction);
    }

    private Message makeElementsMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Select 3 elements");
        embed.setDescription("Select up to 3 elements that the build will be based off");
        if (error == null) {
            messageBuilder.setEmbeds(embed.build());
        } else {
            messageBuilder.setEmbeds(embed.build(), error);
        }
        Collection<ButtonImpl> elementButtons = new ArrayList<>();
        for (ElementSkill element : ElementSkill.orderedElements()) {
            elementButtons.add(
                    new ButtonImpl(
                            element.name(),
                            Pretty.uppercaseFirst(element.name()),
                            elements.contains(element) ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                            false,
                            null
                    )
            );
        }

        ActionRow navigationRow = ActionRow.of(new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null));

        messageBuilder.setActionRows(ActionRow.of(elementButtons), navigationRow);
        return messageBuilder.build();
    }

    private Message makeMajorIdMessage() {
        final MessageBuilder messageBuilder = new MessageBuilder();
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Select major ids");
        embed.setAuthor("Select all the major ids you require for your build");
        embed.setDescription(majorIds.stream().map(Pretty::uppercaseFirst).collect(Collectors.joining("\n")));
        messageBuilder.setEmbeds(embed.build());
        List<SelectOption> majorIdOptions = new ArrayList<>();
        List<String> majorIdsAll = new ArrayList<>(Item.majorIdsListAll);
        majorIdsAll.sort(String::compareTo);
        for (String majorId : majorIdsAll) {
            majorIdOptions.add(
                    SelectOption.of(
                            Pretty.uppercaseFirst(majorId), majorId
                    )
            );
        }
        ActionRow majorIdSelection = ActionRow.of(
                new SelectionMenuImpl(
                        MAJOR_ID_SELECTION_ID,
                        "Major Ids",
                        1,
                        majorIdsAll.size(),
                        false,
                        majorIdOptions
                )
        );
        ActionRow navigationRow = ActionRow.of(
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null),
                new ButtonImpl(MAJOR_ID_CLEAR, "Clear", ButtonStyle.PRIMARY, false, null)
        );
        messageBuilder.setActionRows(majorIdSelection, navigationRow);
        return messageBuilder.build();
    }

    private Message makeClassMessage() {
        final MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder classEmbed = new EmbedBuilder();
        classEmbed.setTitle("Choose a class");
        messageBuilder.setEmbeds(classEmbed.build());
        ActionRow classRow = ActionRow.of(
                new ButtonImpl(ARCHER_ID, Pretty.uppercaseFirst(ARCHER_ID), wynnClass == WynnClass.ARCHER ? ButtonStyle.SUCCESS : ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(MAGE_ID, Pretty.uppercaseFirst(MAGE_ID), wynnClass == WynnClass.MAGE ? ButtonStyle.SUCCESS : ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(WARRIOR_ID, Pretty.uppercaseFirst(WARRIOR_ID), wynnClass == WynnClass.WARRIOR ? ButtonStyle.SUCCESS : ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(ASSASSIN_ID, Pretty.uppercaseFirst(ASSASSIN_ID), wynnClass == WynnClass.ASSASSIN ? ButtonStyle.SUCCESS : ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(SHAMAN_ID, Pretty.uppercaseFirst(SHAMAN_ID), wynnClass == WynnClass.SHAMAN ? ButtonStyle.SUCCESS : ButtonStyle.PRIMARY, false, null)
        );
        ActionRow navigationRow = ActionRow.of(
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null)
        );
        messageBuilder.setActionRows(classRow, navigationRow);
        return messageBuilder.build();
    }

    private Message makeSpellMessage() {
        final MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Set the minimum spell damage");
        Spell[] classSpells = wynnClass.getSpells();
        embed.setDescription(
                String.join("\n",
                        "You will get a chance to set the minimum raw spell damage later",
                        "__**Buttons key:**__",
                        "\n",
                        "**1. " + Pretty.uppercaseFirst(classSpells[0].name()) + "**",
                        "**2. " + Pretty.uppercaseFirst(classSpells[1].name()) + "**",
                        "**3. " + Pretty.uppercaseFirst(classSpells[2].name()) + "**",
                        "**4. " + Pretty.uppercaseFirst(classSpells[3].name()) + "**"
                )
        );
        messageBuilder.setEmbeds(embed.build());

        add4ManualSimpleButtons(SPELL_DMG_BIG_INCREMENT, SPELL_DMG_ID_BIG_UP, spellDmg);
        ActionRow increaseSpellLots = ActionRow.of(
                new ButtonImpl(SPELL_DMG_ID_BIG_UP + "1", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_BIG_UP + "2", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_BIG_UP + "3", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_BIG_UP + "4", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji()),
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null)
        );

        add4ManualSimpleButtons(SPELL_DMG_INCREMENT, SPELL_DMG_ID_UP, spellDmg);
        ActionRow increaseSpellSome = ActionRow.of(
                new ButtonImpl(SPELL_DMG_ID_UP + "1", "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_UP + "2", "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_UP + "3", "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_UP + "4", "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji())
        );

        ActionRow spellInfo = ActionRow.of(
                new ButtonImpl("null1", this.spellDmg[0] == null ? "NA" : String.valueOf(this.spellDmg[0]), ButtonStyle.SECONDARY, true, null),
                new ButtonImpl("null2", this.spellDmg[1] == null ? "NA" : String.valueOf(this.spellDmg[1]), ButtonStyle.SECONDARY, true, null),
                new ButtonImpl("null3", this.spellDmg[2] == null ? "NA" : String.valueOf(this.spellDmg[2]), ButtonStyle.SECONDARY, true, null),
                new ButtonImpl("null4", this.spellDmg[3] == null ? "NA" : String.valueOf(this.spellDmg[3]), ButtonStyle.SECONDARY, true, null)
        );

        add4ManualSimpleButtons(-SPELL_DMG_INCREMENT, SPELL_DMG_ID_DOWN, spellDmg);
        ActionRow decreaseSpellSome = ActionRow.of(
                new ButtonImpl(SPELL_DMG_ID_DOWN + "1", "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_DOWN + "2", "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_DOWN + "3", "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_DOWN + "4", "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji())
        );

        add4ManualSimpleButtons(-SPELL_DMG_BIG_INCREMENT, SPELL_DMG_ID_BIG_DOWN, spellDmg);
        ActionRow decreaseSpellLots = ActionRow.of(
                new ButtonImpl(SPELL_DMG_ID_BIG_DOWN + "1", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_BIG_DOWN + "2", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_BIG_DOWN + "3", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji()),
                new ButtonImpl(SPELL_DMG_ID_BIG_DOWN + "4", "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji()),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null)
        );

        messageBuilder.setActionRows(increaseSpellLots, increaseSpellSome, spellInfo, decreaseSpellSome, decreaseSpellLots);
        return messageBuilder.build();
    }

    private void add4ManualSimpleButtons(int spellDmgIncrement, String spellDmgIdUp, Integer[] array) {
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            addManualSimpleButton(e -> {
                if (array[finalI] == null) array[finalI] = 0;
                array[finalI] += spellDmgIncrement;
            }, spellDmgIdUp + (i + 1));
        }
    }

    private Message makeRawSpellMessage() {
        final MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Set the minimum __RAW__ spell damage");
        Spell[] classSpells = wynnClass.getSpells();
        if (rawSpellDmg == null) {
            embed.setDescription(
                    String.join("\n",
                            "",
                            "**" + Pretty.uppercaseFirst(classSpells[0].name()) + ":** ",
                            "**" + Pretty.uppercaseFirst(classSpells[1].name()) + ":** ",
                            "**" + Pretty.uppercaseFirst(classSpells[2].name()) + ":** ",
                            "**" + Pretty.uppercaseFirst(classSpells[3].name()) + ":** "
                    )
            );
        } else {
            embed.setDescription(
                    String.join("\n",
                            "",
                            "**" + Pretty.uppercaseFirst(classSpells[0].name()) + ":** " + classSpells[0].damage * rawSpellDmg,
                            "**" + Pretty.uppercaseFirst(classSpells[1].name()) + ":** " + classSpells[1].damage * rawSpellDmg,
                            "**" + Pretty.uppercaseFirst(classSpells[2].name()) + ":** " + classSpells[2].damage * rawSpellDmg,
                            "**" + Pretty.uppercaseFirst(classSpells[3].name()) + ":** " + classSpells[3].damage * rawSpellDmg
                    )
            );
        }
        messageBuilder.setEmbeds(embed.build());

        ActionRow increaseRawSpellLots = ActionRow.of(
                new ButtonImpl(RAW_SPELL_DMG_ID_BIG_UP, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji()),
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null)
        );
        ActionRow increaseRawSpellSome = ActionRow.of(
                new ButtonImpl(RAW_SPELL_DMG_ID_UP, "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji())
        );
        ActionRow rawSpellInfo = ActionRow.of(
                new ButtonImpl("null", this.rawSpellDmg == null ? "NA" : String.valueOf(this.rawSpellDmg), ButtonStyle.SECONDARY, true, null)
        );
        ActionRow decreaseRawSpellSome = ActionRow.of(
                new ButtonImpl(RAW_SPELL_DMG_ID_DOWN, "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji())
        );
        ActionRow decreaseRawSpellLots = ActionRow.of(
                new ButtonImpl(RAW_SPELL_DMG_ID_BIG_DOWN, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji()),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null)
        );

        messageBuilder.setActionRows(increaseRawSpellLots, increaseRawSpellSome, rawSpellInfo, decreaseRawSpellSome, decreaseRawSpellLots);
        return messageBuilder.build();
    }

    private Message makeMainAttackMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Set the minimum main attack damage");
        embed.setDescription(
                String.join("\n",
                        "",
                        "__**Buttons key:**__",
                        "**1. Raw main attack**",
                        "**2. Main attack**"
                )
        );
        messageBuilder.setEmbeds(embed.build());
        ActionRow increaseLots = ActionRow.of(
                new ButtonImpl(RAW_MAIN_DMG_ID_BIG_UP, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji()),
                new ButtonImpl(MAIN_DMG_ID_BIG_UP, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji()),
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null)
        );
        ActionRow increase = ActionRow.of(
                new ButtonImpl(RAW_MAIN_DMG_ID_UP, "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji()),
                new ButtonImpl(MAIN_DMG_ID_UP, "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji())
        );
        ActionRow value = ActionRow.of(
                new ButtonImpl("null1", String.valueOf(rawMainDmg), ButtonStyle.SECONDARY, true, null),
                new ButtonImpl("null2", String.valueOf(mainDmg), ButtonStyle.SECONDARY, true, null)
        );
        ActionRow decrease = ActionRow.of(
                new ButtonImpl(RAW_MAIN_DMG_ID_DOWN, "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji()),
                new ButtonImpl(MAIN_DMG_ID_DOWN, "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji())
        );
        ActionRow decreaseLots = ActionRow.of(
                new ButtonImpl(RAW_MAIN_DMG_ID_BIG_DOWN, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji()),
                new ButtonImpl(MAIN_DMG_ID_BIG_DOWN, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji()),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null)
        );
        messageBuilder.setActionRows(increaseLots, increase, value, decrease, decreaseLots);
        return messageBuilder.build();
    }

    private Message makeMiscMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Select a constraint to edit");
        messageBuilder.setEmbeds(embed.build());
        ActionRow healthRow = ActionRow.of(
                new ButtonImpl(EDIT_HP_ID, "Health: " + health, ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(EDIT_HPR_ID, "Health Regen: " + healthRegen, ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(EDIT_MS_ID, "Mana Steal: " + getIdPretty(IdNames.MANA_STEAL), ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(EDIT_MR_ID, "Mana Regen: " + getIdPretty(IdNames.MANA_REGEN), ButtonStyle.PRIMARY, false, null)
        );
        ActionRow attackRow = ActionRow.of(
                new ButtonImpl(EDIT_ATTACK_SPEED_MIN, "Attack Speed Min: " + attackSpeed, ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(EDIT_RAW_SPELL, "Raw Spell Damage: " + rawSpellDmg, ButtonStyle.PRIMARY, false, null)
        );

        Collection<ButtonImpl> buttons = new ArrayList<>();
        int i = 0;
        for (ElementSkill elementSkill : ElementSkill.values()) {
            buttons.add(new ButtonImpl(EDIT_DEFENSE_ID + elementSkill.name(), Pretty.uppercaseFirst(elementSkill.name()) + " Def: " + (defense[i] == null ? "not set" : defense[i]), ButtonStyle.PRIMARY, false, null));
            addManualButton((event) -> editDefense(event, elementSkill), EDIT_DEFENSE_ID + elementSkill.name());
            i++;
        }
        ActionRow defense = ActionRow.of(buttons);

        buttons = new ArrayList<>();
        i = 0;
        for (Spell spell : wynnClass.getSpells()) {
            buttons.add(new ButtonImpl(EDIT_SPELL_COST_ID + spell.spellNum, Pretty.uppercaseFirst(spell.name()) + " Cost: " + spellCost[i], ButtonStyle.PRIMARY, false, null));
            addManualButton((event) -> editSpellCost(event, spell), EDIT_SPELL_COST_ID + spell.spellNum);
            i++;
        }
        ActionRow spellCost = ActionRow.of(buttons);
        ActionRow navigationRow = ActionRow.of(
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null)
        );
        messageBuilder.setActionRows(healthRow, attackRow, defense, spellCost, navigationRow);
        return messageBuilder.build();
    }

    private Message makeIdMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Select an identification");
        messageBuilder.setEmbeds(embed.build());

        List<ActionRow> actionRows = new ArrayList<>();
        List<Component> buttons = new ArrayList<>();
        List<ConstraintId> ids = new ArrayList<>(idsConstraints.values());
        ids.sort(Comparator.comparing(ConstraintId::getStringName));

        Set<String> idsNotSet = new HashSet<>(Item.idsListAll());
        for (int indexInRow = 0, rowIndex = 2, idIndex = 0; idIndex < ids.size(); idIndex++, indexInRow++) {
            if (indexInRow == BUTTONS_IN_ACTION_ROW) {
                indexInRow = 0;
                actionRows.add(ActionRow.of(buttons));
                buttons.clear();
                if (++rowIndex == ACTION_ROWS_IN_MESSAGE) break;
            }
            String idStringName = ids.get(idIndex).getStringName();
            idsNotSet.remove(idStringName);
            buttons.add(new ButtonImpl(EDIT_ID_ID + idStringName, idStringName, ButtonStyle.PRIMARY, false, null));
            addManualSimpleButton(interaction -> {
                currentIdNameMiscUse = idStringName;
                subPages.add(SubPhase.EDIT_ID);
            }, EDIT_ID_ID + idStringName);
        }
        if (!buttons.isEmpty()) {
            actionRows.add(ActionRow.of(buttons));
        }
        List<SelectOption> selectionIdsNotSet = idsNotSet.stream().sorted(String::compareTo).map(name -> SelectOption.of(name, name)).collect(Collectors.toList());
        while (selectionIdsNotSet.size() < idPage * MAX_OPTIONS_IN_SELECTION_MENU) {
            idPage--;
        }
        idPage = Math.max(0, idPage);
        selectionIdsNotSet = selectionIdsNotSet.subList(idPage * MAX_OPTIONS_IN_SELECTION_MENU, Math.min(selectionIdsNotSet.size(), (idPage + 1) * MAX_OPTIONS_IN_SELECTION_MENU));
        actionRows.add(ActionRow.of(new SelectionMenuImpl(ADD_ID_MENU_ID, "Select an id to add", 1, 1, false, selectionIdsNotSet)));
        ActionRow navigationRow = ActionRow.of(
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null),
                new ButtonImpl(BACK_ID_BUTTON_ID, "Menu Back", ButtonStyle.SECONDARY, false, null),
                new ButtonImpl("null1", "Menu Page " + (idPage + 1), ButtonStyle.SECONDARY, true, null),
                new ButtonImpl(FORWARD_ID_BUTTON_ID, "Menu Forward", ButtonStyle.PRIMARY, false, null),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null)
        );
        actionRows.add(navigationRow);
        messageBuilder.setActionRows(actionRows);
        return messageBuilder.build();
    }

    private Message makeConfirmMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Build Confirmation");
        if (querySaved != null) {
            embed.setAuthor("Query ID: " + querySaved.id);
        }
        embed.setDescription(
                String.join("\n",
                        "Are you ready to request a build with what you've entered?",
                        "Keep in mind that this could take quite some time.",
                        "I'll ping you when I'm done'"
                )
        );
        messageBuilder.setEmbeds(embed.build());
        messageBuilder.setActionRows(ActionRow.of(
                new ButtonImpl(BACK_BUTTON_ID, "Back", ButtonStyle.DANGER, false, null),
                new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null),
                new ButtonImpl(SAVE_BUTTON_ID, "Save Query", ButtonStyle.PRIMARY, this.querySaved != null, null)
        ));
        return messageBuilder.build();
    }

    private Message makeEditSomethingMessage(String title, @Nullable String description, String valueLabel, int smallIncrement, int bigIncrement, Consumer<Integer> incrementer, ButtonImpl... buttons) {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription(description);
        messageBuilder.setEmbeds(embed.build());
        ActionRow upLots = ActionRow.of(
                new ButtonImpl(EDIT_SOMETHING_UP_LOTS, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_UP.getDiscordEmoji())
        );
        addManualSimpleButton(o -> incrementer.accept(bigIncrement), EDIT_SOMETHING_UP_LOTS);
        ActionRow up = ActionRow.of(
                new ButtonImpl(EDIT_SOMETHING_UP, "", ButtonStyle.PRIMARY, false, DiscordEmoji.UP.getDiscordEmoji())
        );
        addManualSimpleButton(o -> incrementer.accept(smallIncrement), EDIT_SOMETHING_UP);
        addManualSimpleButton(o -> incrementer.accept(null), "switchValue");
        Collection<ButtonImpl> valueButtons = new ArrayList<>();
        valueButtons.add(new ButtonImpl("switchValue", valueLabel, ButtonStyle.SECONDARY, false, null));
        valueButtons.add(new ButtonImpl(SUBMIT_BUTTON_ID, "Submit", ButtonStyle.SUCCESS, false, null));
        valueButtons.addAll(Arrays.asList(buttons));
        ActionRow value = ActionRow.of(valueButtons);
        ActionRow down = ActionRow.of(
                new ButtonImpl(EDIT_SOMETHING_DOWN, "", ButtonStyle.PRIMARY, false, DiscordEmoji.DOWN.getDiscordEmoji())
        );
        addManualSimpleButton(o -> incrementer.accept(-smallIncrement), EDIT_SOMETHING_DOWN);
        ActionRow downLots = ActionRow.of(
                new ButtonImpl(EDIT_SOMETHING_DOWN_LOTS, "", ButtonStyle.PRIMARY, false, DiscordEmoji.BIG_DOWN.getDiscordEmoji())
        );
        addManualSimpleButton(o -> incrementer.accept(-bigIncrement), EDIT_SOMETHING_DOWN_LOTS);
        messageBuilder.setActionRows(upLots, up, value, down, downLots);
        return messageBuilder.build();
    }

    private Message makeWaitingUpdatesMessage() {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("Progress: %.2f%%", progress * 100));
        embed.setFooter(String.format("Priority: %s task", Pretty.uppercaseFirst(priority.name())));
        if (querySaved != null) {
            embed.setAuthor("Query ID: " + querySaved.id);
        }
        embed.setDescription("Place in queue: " + (placeInLine == 1 ? "working" : placeInLine));
        messageBuilder.setEmbeds(embed.build());
        messageBuilder.setActionRows(ActionRow.of(
                new ButtonImpl(CANCEL_BUILD, "Cancel", ButtonStyle.DANGER, false, DiscordEmoji.RED_X.getDiscordEmoji()),
                new ButtonImpl(REFRESH, "Refresh", ButtonStyle.PRIMARY, false, DiscordEmoji.LOOP.getDiscordEmoji())
        ));
        return messageBuilder.build();
    }

    private Message makeBuildsMessage() {
        return buildsGui.makeMessage();
    }

    private void finishSubmit(ComponentInteraction interaction) {
        List<List<Item>> items = new ArrayList<>();
        for (List<Item> pieceRaw : List.of(Item.helmets, Item.chestplates, Item.leggings, Item.boots)) {
            List<Item> piece = new ArrayList<>(pieceRaw);
            piece.removeIf(item -> item.level < 73);
            items.add(piece);
        }
        for (List<Item> pieceRaw : List.of(Item.rings, Item.rings, Item.bracelets, Item.necklaces)) {
            items.add(new ArrayList<>(pieceRaw));
        }
        items.add(new ArrayList<>(wynnClass.getWeapons()));
        this.generator = new BuildGenerator(items.toArray(new ArrayList[0]), new HashSet<>(elements));
        if (this.attackSpeed != Item.AttackSpeed.SUPER_SLOW) {
            generator.addConstraint(new ConstraintMinAttackSpeed(this.attackSpeed.speed));
        }
        if (healthRegen != null)
            generator.addConstraint(new ConstraintHpr(healthRegen));
        if (health != null)
            generator.addConstraint(new ConstraintHp(health));
        for (ConstraintId id : idsConstraints.values()) generator.addConstraint(id);
        for (String majorId : majorIds) generator.addConstraint(new ConstraintMajorId(majorId));
        for (int i = 0; i < spellDmg.length; i++) {
            if (spellDmg[i] != null) {
                generator.addConstraint(new ConstraintSpellDamage(wynnClass.getSpells()[i], spellDmg[i]));
            }
        }
        for (int i = 0; i < spellCost.length; i++) {
            if (spellCost[i] != null) {
                generator.addConstraint(new ConstraintSpellCost(wynnClass.getSpells()[i], spellCost[i]));
            }
        }
        if (rawSpellDmg != null)
            generator.addConstraint(new ConstraintId(IdNames.RAW_SPELL_DMG.getIdName(), rawSpellDmg));
        if (rawMainDmg != null)
            generator.addConstraint(new ConstraintId(IdNames.MAIN_ATTACK_DMG.getIdName(), rawMainDmg));
        if (mainDmg != null)
            generator.addConstraint(new ConstraintMainDamage(mainDmg));
        for (int i = 0; i < defense.length; i++) {
            if (defense[i] != null) {
                generator.addConstraint(new ConstraintDefense(ElementSkill.values()[i], defense[i]));
            }
        }
        for (BuildConstraintExclusion exclusion : BuildConstraintExclusion.all)
            generator.addConstraint(exclusion);
        phase = BuildPhase.WAITING_UPDATES;
        this.taskUUID = GeneratorManager.queue(generator, this::onUpdate, this::onFinish, this::onPriorityChange);
        this.placeInLine = GeneratorManager.placeInLine(taskUUID);
        editAsReply(interaction);
    }

    public void onUpdate(double progress) {
        this.progress = progress;
        editMessage();
    }

    public void onFinish(GeneratorManager.GeneratorTask task) {
        this.priority = GeneratorManager.TaskType.PRIMARY;
        this.placeInLine = 0;

        if (task.isImpossible()) {
            this.phase = BuildPhase.ELEMENTS;
            editMessage();
            message.getChannel().sendMessage(member.getAsMention() + " Hey, I can't figure this build out right now. It's probably too broad. " + message.getJumpUrl()).queue();
        } else {
            Collection<Build> buildsAll = generator.getBuildsAll();
            this.buildsGui = new BuildShowListMessage(acd, message, buildsAll, this, querySaved == null ? null : querySaved.id);
            this.phase = BuildPhase.BUILD;
            editMessage();
            if (buildsAll.isEmpty()) {
                message.getChannel().sendMessage(member.getAsMention() + " Hey, I finished, but I couldn't find any matches, so it's **probably** impossible.\n " + message.getJumpUrl()).queue();
            }
        }
    }

    public void onPriorityChange(GeneratorManager.TaskType priority, int placeInLine) {
        this.priority = priority;
        this.placeInLine = placeInLine;
        editMessage();
    }

    @GuiManualButton
    public void resetEdit() {
        this.phase = BuildPhase.ELEMENTS;
        this.priority = GeneratorManager.TaskType.PRIMARY;
        this.placeInLine = 0;
        this.querySaved = null;
    }

    public List<ElementSkill> getElements() {
        return elements;
    }

    public List<String> getMajorIds() {
        return majorIds;
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
        return idsConstraints;
    }

    public Integer getRawSpellDmg() {
        return rawSpellDmg;
    }

    public Item.AttackSpeed getAttackSpeed() {
        return attackSpeed;
    }

    private enum SubPhase {
        EDIT_HP,
        EDIT_MR,
        EDIT_MS,
        EDIT_DEFENSE,
        EDIT_HPR,
        EDIT_ID,
        EDIT_ATTACK_SPEED_MIN,
        EDIT_SPELL_COST,
        EDIT_RAW_SPELL
    }

    private enum BuildPhase {
        ELEMENTS(0),
        MAJOR_ID(1),
        CLASS(2),
        SPELL_ATTACK(3),
        MAIN_ATTACK(4),
        MISC(5),
        ID(6),
        CONFIRM(7),
        WAITING_UPDATES(8),
        BUILD(9);

        private static BuildPhase[] order;
        private final int index;

        BuildPhase(int index) {
            this.index = index;
        }

        public BuildPhase getNextPage() {
            return getPage(index + 1);
        }

        public BuildPhase getPreviousPage() {
            return getPage(index - 1);
        }

        private static BuildPhase getPage(int index) {
            if (order == null) {
                order = new BuildPhase[BuildPhase.values().length];
                for (BuildPhase buildPhase : BuildPhase.values()) {
                    order[buildPhase.index] = buildPhase;
                }
            }
            return order[Math.min(order.length - 1, Math.max(0, index))];
        }
    }
}

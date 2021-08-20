package apple.build;

import apple.build.discord.DiscordBot;
import apple.build.search.Build;
import apple.build.search.BuildGenerator;
import apple.build.search.constraints.advanced_damage.ConstraintMainDamage;
import apple.build.search.constraints.advanced_damage.ConstraintSpellDamage;
import apple.build.search.constraints.advanced_skill.ConstraintSpellCost;
import apple.build.search.constraints.filter.BuildConstraintExclusion;
import apple.build.search.constraints.general.*;
import apple.build.search.enums.ElementSkill;
import apple.build.search.enums.Spell;
import apple.build.wynnbuilder.ServiceWynnbuilderItemDB;
import apple.build.wynncraft.GetItems;
import apple.build.wynncraft.items.Item;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BuildMain {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, LoginException {
        System.out.println("Starting BuildBot");
        ServiceWynnbuilderItemDB.callWynnbuilderToGetItemDB(false);
        // uncomment if you want to retrieve the items from the wynn api
        GetItems.getItems(false);
        new DiscordBot();
        System.out.println("Started BuildBot");
    }
}
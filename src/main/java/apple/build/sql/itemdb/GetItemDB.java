package apple.build.sql.itemdb;

import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Armor;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class GetItemDB {
    public static ArrayList<Item> getAllItems(Item.ItemType itemType) throws SQLException {
        synchronized (VerifyItemDB.syncDB) {
            ArrayList<Item> items = new ArrayList<>();
            String sql = GetItemSql.getAllItems(itemType);
            Statement statement = VerifyItemDB.databaseItem.createStatement();
            ResultSet response = statement.executeQuery(sql);
            if (!response.isClosed()) {
                while (response.next()) {
                    switch (itemType) {
                        case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> items.add(new Armor(response, itemType));
                        case WAND, DAGGER, SPEAR, BOW, RELIK -> items.add(new Weapon(response, itemType));
                        case RING, BRACELET, NECKLACE -> items.add(new Accessory(response, itemType));
                    }
                }
            }
            response.close();
            for (Item item : items) {
                response = statement.executeQuery(GetItemSql.getAllIds(item.name));
                item.addIds(response);
                response.close();
            }
            return items;
        }
    }
}

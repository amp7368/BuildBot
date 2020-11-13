package apple.build.sql;

import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Armor;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GetDB {
    public static List<Item> getAllItems(Item.ItemType itemType) throws SQLException {
        synchronized (VerifyDB.syncDB) {
            List<Item> items = new ArrayList<>();
            String sql = GetSql.getAllItems(itemType);
            Statement statement = VerifyDB.database.createStatement();
            ResultSet response = statement.executeQuery(sql);
            if (!response.isClosed()) {
                while (response.next()) {
                    switch (itemType) {
                        case HELMET:
                        case CHESTPLATE:
                        case LEGGINGS:
                        case BOOTS:
                            items.add(new Armor(response, itemType));
                            break;
                        case WAND:
                        case DAGGER:
                        case SPEAR:
                        case BOW:
                        case RELIK:
                            items.add(new Weapon(response, itemType));
                            break;
                        case RING:
                        case BRACELET:
                        case NECKLACE:
                            items.add(new Accessory(response, itemType));
                            break;
                    }
                }
            }
            response.close();
            for (Item item : items) {
                response = statement.executeQuery(GetSql.getAllIds(item.name));
                item.addIds(response);
                response.close();
            }
            return items;
        }
    }
}

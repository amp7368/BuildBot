package apple.build.sql.itemdb;

import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Armor;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static apple.build.sql.itemdb.VerifyItemDB.syncDB;
import static apple.build.sql.itemdb.VerifyItemDB.databaseItem;

public class InsertItemDB {
    public static void insertItems(Item[] items) throws SQLException {
        synchronized (syncDB) {
            Statement statement = databaseItem.createStatement();
            List<String> batch = new ArrayList<>();
            int size = items.length;
            int i = 0;
            for (Item item : items) {
                if (i++ % 50 == 0)
                    System.out.printf("doing %d/%d\n", i, size);
                if (!item.tier.toLowerCase().equals("normal") && statement.executeQuery(GetItemSql.existsItem(item.type.name(), item.name)).getInt(1) == 0) {
                    if (item instanceof Weapon) {
                        batch.add(GetItemSql.insertItem((Weapon) item));
                    } else if (item instanceof Armor) {
                        String e = GetItemSql.insertItem((Armor) item);
                        System.out.println(e);
                        batch.add(e);
                    } else if (item instanceof Accessory) {
                        batch.add(GetItemSql.insertItem((Accessory) item));
                    }
                    for (Map.Entry<Integer, Integer> id : item.ids.entrySet()) {
                        if (id.getValue() != 0)
                            batch.add(GetItemSql.insertId(item.name, Item.getIdName(id.getKey()), id.getValue()));
                    }
                }
                if (i % 50 == 0) {
                    for (String s : batch) {
                        statement.addBatch(s);
                    }
                    batch = new ArrayList<>();
                    statement.executeBatch();
                }
            }
            for (String s : batch) {
                statement.addBatch(s);
            }
            statement.executeBatch();
            statement.close();
        }
    }
}

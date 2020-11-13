package apple.build.sql;

import apple.build.wynncraft.items.Accessory;
import apple.build.wynncraft.items.Armor;
import apple.build.wynncraft.items.Item;
import apple.build.wynncraft.items.Weapon;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static apple.build.sql.VerifyDB.syncDB;
import static apple.build.sql.VerifyDB.database;

public class InsertDB {
    public static void insertItems(Item[] items) throws SQLException {
        synchronized (syncDB) {
            Statement statement = database.createStatement();
            List<String> batch = new ArrayList<>();
            int size = items.length;
            int i = 0;
            for (Item item : items) {
                if (i++ % 50 == 0)
                    System.out.printf("doing %d/%d\n", i, size);
                if (!item.tier.toLowerCase().equals("normal") && statement.executeQuery(GetSql.existsItem(item.type.name(), item.name)).getInt(1) == 0) {
                    if (item instanceof Weapon) {
                        batch.add(GetSql.insertItem((Weapon) item));
                    } else if (item instanceof Armor) {
                        String e = GetSql.insertItem((Armor) item);
                        System.out.println(e);
                        batch.add(e);
                    } else if (item instanceof Accessory) {
                        batch.add(GetSql.insertItem((Accessory) item));
                    }
                    for (Map.Entry<String, Integer> id : item.ids.entrySet()) {
                        if (id.getValue() != 0)
                            batch.add(GetSql.insertId(item.name, id.getKey(), id.getValue()));
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

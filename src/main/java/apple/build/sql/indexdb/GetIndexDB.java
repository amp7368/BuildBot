package apple.build.sql.indexdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GetIndexDB {
    public static long getEnumId(String name) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            ResultSet response = statement.executeQuery(GetIndexSql.Get.constraintEnumId(name));
            if (response.isClosed()) {
                return -1;
            }
            int id = response.getInt("constraint_enum_id");
            response.close();
            statement.close();
            return id;
        }
    }

    public static long getTextValId(String text) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            ResultSet response = statement.executeQuery(GetIndexSql.Get.constraintTextVal(text));
            if (response.isClosed()) {
                return -1;
            }
            int id = response.getInt("text_val_index");
            response.close();
            statement.close();
            return id;
        }
    }

    public static int getItemId(String name) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            ResultSet response = statement.executeQuery(GetIndexSql.Get.itemId(name));
            int itemId;
            if (!response.isClosed()) {
                itemId = response.getInt("item_name_id");
                response.close();
            } else {
                statement.execute(GetIndexSql.Insert.item(name, itemId = VerifyIndexDB.currentItemId++));
            }
            statement.close();
            return itemId;
        }
    }
}

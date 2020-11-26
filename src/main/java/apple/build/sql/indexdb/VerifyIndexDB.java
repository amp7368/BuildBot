package apple.build.sql.indexdb;

import apple.build.wynncraft.items.Item;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static apple.build.wynncraft.items.Item.ItemType.*;

public class VerifyIndexDB {
    public static final Object syncDB = new Object();
    private static final String DATABASE_NAME = "data/indexing.db";
    private static final String buildSearchIds = "-- the table to list all the unique ids\n" +
            "CREATE TABLE IF NOT EXISTS search_ids\n" +
            "(\n" +
            "    search_id BIGINT NOT NULL PRIMARY KEY UNIQUE\n" +
            ");";
    private static final String buildItemTypes = "-- the table to map the itemTypeId to their itemTypeName\n" +
            "CREATE TABLE IF NOT EXISTS item_types\n" +
            "(\n" +
            "    item_type_id   TINYINT NOT NULL PRIMARY KEY UNIQUE,\n" +
            "    item_type_name BIGINT  NOT NULL UNIQUE\n" +
            ");";
    private static final String buildItems = "-- the table to map item names to a corresponding id\n" +
            "CREATE TABLE IF NOT EXISTS items\n" +
            "(\n" +
            "    item_name_id SMALLINT    NOT NULL PRIMARY KEY UNIQUE,\n" +
            "    item_name    VARCHAR(50) NOT NULL UNIQUE\n" +
            ");";
    private static final String buildConstraintIds = "-- the table to list all the constraint ids\n" +
            "CREATE TABLE IF NOT EXISTS constraint_ids\n" +
            "(\n" +
            "    constraint_id BIGINT NOT NULL PRIMARY KEY UNIQUE\n" +
            ");";
    private static final String buildConstraintEnumIds = "-- the table to list all the simple constraints types and their ids\n" +
            "CREATE TABLE IF NOT EXISTS constraint_enum_ids\n" +
            "(\n" +
            "    constraint_enum_id SMALLINT    NOT NULL PRIMARY KEY UNIQUE,\n" +
            "    name               VARCHAR(30) NOT NULL UNIQUE\n" +
            ");";
    private static final String buildConstraintTextVals = "-- the table to turn the text_val that would otherwise be in all_constraints into an integer\n" +
            "CREATE TABLE IF NOT EXISTS constraint_text_vals\n" +
            "(\n" +
            "    text_val       MEDIUMTEXT NOT NULL UNIQUE,\n" +
            "    text_val_index INTEGER PRIMARY KEY\n" +
            ");";
    private static final String buildPreIndexResults = "-- the table to map the unique ids to their item answers\n" +
            "CREATE TABLE IF NOT EXISTS pre_index_results\n" +
            "(\n" +
            "    search_id    BIGINT   NOT NULL,\n" +
            "    item_type_id TINYINT  NOT NULL,\n" +
            "    item_name_id SMALLINT NOT NULL,\n" +
            "    PRIMARY KEY (search_id, item_type_id, item_name_id),\n" +
            "    UNIQUE (search_id, item_type_id, item_name_id),\n" +
            "    FOREIGN KEY (search_id) REFERENCES search_ids (search_id),\n" +
            "    FOREIGN KEY (item_type_id) REFERENCES item_types (item_type_id),\n" +
            "    FOREIGN KEY (item_name_id) REFERENCES items (item_name_id)\n" +
            ");";
    private static final String buildSearchToConstraints = "-- the table to link all the uids to their textVal constraints\n" +
            "CREATE TABLE IF NOT EXISTS search_to_constraints\n" +
            "(\n" +
            "    search_id     BIGINT NOT NULL,\n" +
            "    constraint_id BIGINT NOT NULL,\n" +
            "    PRIMARY KEY (search_id, constraint_id),\n" +
            "    UNIQUE (search_id, constraint_id),\n" +
            "    FOREIGN KEY (search_id) REFERENCES search_ids (search_id),\n" +
            "    FOREIGN KEY (constraint_id) REFERENCES constraint_ids (constraint_id)\n" +
            ");\n";
    private static final String buildAllConstraints = "-- the table to list all the simple constraints that has been tried\n" +
            "CREATE TABLE IF NOT EXISTS all_constraints\n" +
            "(\n" +
            "    constraint_id      BIGINT   NOT NULL PRIMARY KEY UNIQUE,\n" +
            "    constraint_enum_id SMALLINT NOT NULL,\n" +
            "    text_val_index     INTEGER,\n" +
            "    val                INTEGER,\n" +
            "    FOREIGN KEY (constraint_id) REFERENCES constraint_ids (constraint_id),\n" +
            "    FOREIGN KEY (text_val_index) REFERENCES constraint_text_vals (text_val_index),\n" +
            "    FOREIGN KEY (constraint_enum_id) REFERENCES constraint_enum_ids (constraint_enum_id)\n" +
            ");";
    private static final String buildArchetype = "CREATE TABLE IF NOT EXISTS archetype\n" +
            "(\n" +
            "    element   VARCHAR(10) NOT NULL,\n" +
            "    search_id BIGINT      NOT NULL,\n" +
            "    FOREIGN KEY (search_id) REFERENCES search_ids (search_id),\n" +
            "    PRIMARY KEY (element, search_id),\n" +
            "    UNIQUE (element, search_id)\n" +
            ");";
    private static final Map<Item.ItemType, Byte> itemTypes = new HashMap<>() {
        {
            put(HELMET, (byte) 0);
            put(CHESTPLATE, (byte) 1);
            put(LEGGINGS, (byte) 2);
            put(BOOTS, (byte) 3);
            put(RING, (byte) 4);
            put(BRACELET, (byte) 5);
            put(NECKLACE, (byte) 6);
            put(BOW, (byte) 7);
            put(RELIK, (byte) 8);
            put(WAND, (byte) 9);
            put(SPEAR, (byte) 10);
            put(DAGGER, (byte) 11);
        }
    };
    public static long currentConstraintId;
    public static int currentConstraintEnumId;
    public static int currentConstraintTextValIndex;
    public static long currentSearchId;
    public static int currentItemId;
    protected static Connection databaseIndex;

    public static void initialize() throws ClassNotFoundException, SQLException {
        synchronized (syncDB) {
            Class.forName("org.sqlite.JDBC");
            // never close this because we're always using it
            databaseIndex = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            verify();
        }
    }

    private static void verify() throws SQLException {
        synchronized (syncDB) {
            Statement statement = databaseIndex.createStatement();
            statement.execute(buildSearchIds);
            currentSearchId = statement.executeQuery("SELECT max(search_id) FROM search_ids;").getLong(1) + 1;
            statement.execute(buildArchetype);
            statement.execute(buildItemTypes);
            for (Map.Entry<Item.ItemType, Byte> itemType : itemTypes.entrySet())
                try {
                    statement.execute(String.format("INSERT INTO item_types (item_type_id, item_type_name) VALUES (%d,'%s');", itemType.getValue(), itemType.getKey().name()));
                } catch (SQLException ignored) {
                }// a fail is fine because it already exists
            statement.execute(buildItems);
            currentConstraintId = statement.executeQuery("SELECT max(item_name_id) FROM items;").getInt(1) + 1;
            statement.execute(buildConstraintIds);
            currentConstraintId = statement.executeQuery("SELECT max(constraint_id) FROM constraint_ids;").getLong(1) + 1;
            statement.execute(buildConstraintEnumIds);
            currentConstraintEnumId = statement.executeQuery("SELECT max(constraint_enum_id) FROM constraint_enum_ids;").getInt(1) + 1;
            statement.execute(buildConstraintTextVals);
            currentConstraintTextValIndex = statement.executeQuery("SELECT max(text_val_index) FROM constraint_text_vals;").getInt(1) + 1;
            statement.execute(buildPreIndexResults);
            statement.execute(buildSearchToConstraints);
            statement.execute(buildAllConstraints);
            statement.close();
        }
    }

    public static byte getItemTypeId(Item.ItemType type) {
        return itemTypes.get(type);
    }
}

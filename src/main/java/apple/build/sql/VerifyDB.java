package apple.build.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class VerifyDB {
    public static final Object syncDB = new Object();
    private static final String DATABASE_NAME = "data/items.db";
    private static final String TABLE_CONTENTS =
            "    name                    TEXT    NOT NULL UNIQUE PRIMARY KEY,\n" +
                    "    displayName             TEXT    NOT NULL,\n" +
                    "    level                   INTEGER NOT NULL,\n" +
                    "    strength                INTEGER NOT NULL,\n" +
                    "    dexterity               INTEGER NOT NULL,\n" +
                    "    intelligence            INTEGER NOT NULL,\n" +
                    "    agility                 INTEGER NOT NULL,\n" +
                    "    defense                 INTEGER NOT NULL,\n" +
                    "    tier                    TEXT    NOT NULL,\n" +
                    "    sockets                 INTEGER NOT NULL,\n" +
                    "    dropType                TEXT    NOT NULL,\n" +
                    "    restrictions            TEXT,\n" +
                    "    setString               TEXT,\n" +
                    "    addedLore               TEXT,\n" +
                    "    material                TEXT,\n" +
                    "    quest                   TEXT,\n" +
                    "    classRequirement        TEXT,\n" +
                    "    majorIds                TEXT,\n" +
                    "    identified              BOOLEAN";
    private static final String WEAPON_TABLE_CONTENTS =
            "    attackSpeed             TEXT,\n" +
                    "    thunderDamageUpper      INTEGER NOT NULL,\n" +
                    "    airDamageUpper          INTEGER NOT NULL,\n" +
                    "    waterDamageUpper        INTEGER NOT NULL,\n" +
                    "    earthDamageUpper        INTEGER NOT NULL,\n" +
                    "    fireDamageUpper         INTEGER NOT NULL,\n" +
                    "    thunderDamageLower      INTEGER NOT NULL,\n" +
                    "    airDamageLower          INTEGER NOT NULL,\n" +
                    "    waterDamageLower        INTEGER NOT NULL,\n" +
                    "    earthDamageLower        INTEGER NOT NULL,\n" +
                    "    fireDamageLower         INTEGER NOT NULL,\n" +
                    "    damageUpper             INTEGER NOT NULL,\n" +
                    "    damageLower             INTEGER NOT NULL";
    private static final String ARMOR_TABLE_CONTENTS = "    armorType               TEXT, " +
            "    health        INTEGER NOT NULL\n";
    private static final String ACCESSORY_TABLE_CONTENTS = "    health        INTEGER NOT NULL\n";
    private static final String CREATE_TABLE_IDS = "CREATE TABLE IF NOT EXISTS ids\n" +
            "(\n" +
            "    name    TEXT    NOT NULL,\n" +
            "    id_name TEXT    NOT NULL,\n" +
            "    value   INTEGER NOT NULL,\n" +
            "    UNIQUE (name, id_name),\n" +
            "    PRIMARY KEY (name, id_name)\n" +
            ")";
    private static final String[] ACCESORY_TABLE_NAMES = new String[]{
            "ring",
            "bracelet",
            "necklace"
    };
    private static final String[] ARMOR_TABLE_NAMES = new String[]{
            "boots",
            "leggings",
            "chestplate",
            "helmet"
    };
    private static final String[] WEAPON_TABLE_NAMES = new String[]{
            "wand",
            "dagger",
            "spear",
            "bow",
            "relik"
    };
    protected static Connection database;

    public static void initialize() throws ClassNotFoundException, SQLException {
        synchronized (syncDB) {
            Class.forName("org.sqlite.JDBC");
            // never close this because we're always using it
            database = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            verify();
        }
    }

    public static void verify() throws SQLException {
        synchronized (syncDB) {
            Statement statement = database.createStatement();
            for (String tableName : ARMOR_TABLE_NAMES) {
                String buildTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + TABLE_CONTENTS + ", " + ARMOR_TABLE_CONTENTS + " );";
                statement.execute(buildTableSql);
            }
            for (String tableName : ACCESORY_TABLE_NAMES) {
                String buildTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + TABLE_CONTENTS + ", " + ACCESSORY_TABLE_CONTENTS + " );";
                statement.execute(buildTableSql);
            }
            for (String tableName : WEAPON_TABLE_NAMES) {
                String buildTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + TABLE_CONTENTS + ", " + WEAPON_TABLE_CONTENTS + " );";
                statement.execute(buildTableSql);
            }
            statement.execute(CREATE_TABLE_IDS);
            statement.close();
        }
    }
}

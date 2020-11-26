package apple.build.sql.indexdb;

import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.enums.ElementSkill;
import org.jetbrains.annotations.NotNull;

public class GetIndexSql {
    public static class Insert {
        public static String constraintEnum(long id, String name) {
            return String.format("INSERT INTO constraint_enum_ids (constraint_enum_id, name) VALUES (%d,'%s');", id, name);
        }

        public static String constraintTextVal(long index, String text) {
            return String.format("INSERT INTO constraint_text_vals (text_val, text_val_index)\n" +
                    "VALUES ('%s', %d);", text, index);
        }

        public static String constraintId(long constraintId) {
            return String.format("INSERT INTO constraint_ids (constraint_id) VALUES (%d);", constraintId);
        }

        public static String searchId(long searchId) {
            return String.format("INSERT INTO search_ids (search_id) VALUES (%d);", searchId);
        }

        public static String constraint(long constraintId, long enumId, long textValId, ConstraintSimplified constraint) {
            return String.format("INSERT INTO all_constraints (constraint_id, constraint_enum_id, text_val_index, val)\n" +
                            "VALUES (%d, %d, %s, %s);",
                    constraintId,
                    enumId,
                    textValId == -1 ? null : String.valueOf(textValId),
                    constraint.getValSql());
        }

        public static String searchToConstraint(long searchId, long constraintId) {
            return String.format("INSERT INTO search_to_constraints (search_id, constraint_id) VALUES (%d, %d);", searchId, constraintId);
        }

        public static String item(String name, int id) {
            return String.format("INSERT INTO items (item_name_id, item_name) VALUES (%d,'%s');", id, name);
        }

        public static String result(long searchId, int itemNameId, byte itemTypeId) {
            return String.format("INSERT INTO pre_index_results (search_id, item_type_id, item_name_id) VALUES (%d,%d,%d);", searchId, itemTypeId, itemNameId);
        }

        public static String archetype(long searchId, ElementSkill element) {
            return String.format("INSERT INTO archetype (element,search_id) VALUES ('%s',%d);", element.name(), searchId);
        }
    }

    public static class Get {
        public static String constraintEnumId(String name) {
            return String.format("SELECT constraint_enum_id\n" +
                    "FROM constraint_enum_ids\n" +
                    "WHERE name = '%S';", name);
        }

        public static String constraintTextVal(@NotNull String text) {
            return String.format("SELECT text_val_index\n" +
                    "FROM constraint_text_vals\n" +
                    "WHERE text_val = '%s';", text);
        }

        public static String itemId(String name) {
            return String.format("SELECT item_name_id FROM items WHERE item_name = '%s'", name);
        }
    }
}

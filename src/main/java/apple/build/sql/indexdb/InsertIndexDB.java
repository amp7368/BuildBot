package apple.build.sql.indexdb;

import apple.build.data.Build;
import apple.build.data.BuildGenerator;
import apple.build.data.constraints.ConstraintSimplified;
import apple.build.data.enums.ElementSkill;
import apple.build.wynncraft.items.Item;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class InsertIndexDB {
    public static void insertResults(BuildGenerator generator) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            long searchId = VerifyIndexDB.currentSearchId++;
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            statement.execute(GetIndexSql.Insert.searchId(searchId));
            for (ElementSkill element : generator.getArchetype()) {
                statement.execute(GetIndexSql.Insert.archetype(searchId, element));
            }
            statement.close();
            List<ConstraintSimplified> constraints = generator.getConstraints();
            for (ConstraintSimplified constraint : constraints) {
                insertConstraint(searchId, constraint);
            }
            List<Build> builds = generator.getBuilds();
            for (Build build : builds) {
                insertBuild(searchId, build);
            }
        }
    }

    private static void insertBuild(long searchId, Build build) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            for (Item item : build.items) {
                int itemNameId = GetIndexDB.getItemId(item.name);
                byte itemTypeId = VerifyIndexDB.getItemTypeId(item.type);
                Statement statement = VerifyIndexDB.databaseIndex.createStatement();
                try {
                    statement.execute(GetIndexSql.Insert.result(searchId, itemNameId, itemTypeId));
                } catch (SQLException ignored) {
                } // it's fine if we failed. it just means the same ring was used twice
                statement.close();
            }
        }
    }

    private static void insertConstraint(long searchId, ConstraintSimplified constraint) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            long constraintId = VerifyIndexDB.currentConstraintId++;
            insertConstraintId(constraintId);
            long enumId = GetIndexDB.getEnumId(constraint.getName());
            if (enumId == -1)
                insertEnumName(enumId = VerifyIndexDB.currentConstraintEnumId++, constraint.getName());
            long textValId = -1;
            if (constraint.text != null) {
                textValId = GetIndexDB.getTextValId(constraint.text);
                if (textValId == -1)
                    insertTextVal(textValId = VerifyIndexDB.currentConstraintTextValIndex++, constraint.text);
            }
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            statement.execute(GetIndexSql.Insert.constraint(constraintId, enumId, textValId, constraint));
            statement.execute(GetIndexSql.Insert.searchToConstraint(searchId, constraintId));
            statement.close();
        }
    }

    private static void insertConstraintId(long constraintId) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            statement.execute(GetIndexSql.Insert.constraintId(constraintId));
            statement.close();
        }
    }

    private static void insertTextVal(long index, String text) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            statement.execute(GetIndexSql.Insert.constraintTextVal(index, text));
            statement.close();
        }
    }

    private static void insertEnumName(long id, String name) throws SQLException {
        synchronized (VerifyIndexDB.syncDB) {
            Statement statement = VerifyIndexDB.databaseIndex.createStatement();
            statement.execute(GetIndexSql.Insert.constraintEnum(id, name));
            statement.close();
        }
    }
}

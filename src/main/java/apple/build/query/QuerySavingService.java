package apple.build.query;

import apple.build.BuildMain;
import apple.utilities.request.AppleJsonFromFile;
import apple.utilities.request.AppleJsonToFile;
import apple.utilities.request.AppleRequestService;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class QuerySavingService extends AppleRequestService {
    private static final QuerySavingService instance = new QuerySavingService();
    private static final File fileToSave;
    private static QueriesPresets presets;

    static {
        List<String> path = List.of(BuildMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        fileToSave = new File(String.join("/", path.subList(0, path.size() - 1)) + "/queries");
    }

    public static RequestHandler<QueriesPresets> getPresets(long userID, @Nullable Consumer<QueriesPresets> callback) {
        return getPresets(String.valueOf(userID), callback);
    }

    public static RequestHandler<QueriesPresets> getPresets(String presetCategory, @Nullable Consumer<QueriesPresets> callback) {
        return get().queue(() -> {
            File file = new File(new File(fileToSave, "presets"), presetCategory + ".json");
            return file.exists() ? new AppleJsonFromFile<>(file, QueriesPresets.class).get() : new QueriesPresets(presetCategory);
        }, callback == null ? (QueriesPresets q) -> {
        } : callback);
    }

    public static QuerySavingService get() {
        return instance;
    }

    public static void queue(QuerySaved querySaved, long userId) {
        // add the querySavedId to the users queries
        get().queue(new AppleJsonToFile(querySaved.getFile(fileToSave), querySaved));
        get().queue(() -> {
            File file = new File(new File(fileToSave, "users"), userId + ".json");
            QueriesSavedForUser saved = file.exists() ? new AppleJsonFromFile<>(file, QueriesSavedForUser.class).get() : new QueriesSavedForUser(querySaved, userId);
            saved.addQuery(querySaved);
            new AppleJsonToFile(file, saved).complete();
        });
    }

    public static boolean queue(String buildId, Consumer<QuerySaved> runAfter) {
        File file = QuerySaved.getFile(fileToSave, buildId);
        if (!file.exists()) return false;
        get().queue(new AppleJsonFromFile<>(file, QuerySaved.class), runAfter);
        return true;
    }

    public static QueriesPresets getDefaultPresets() {
        if(presets == null){
            presets = getPresets("default", null).complete();
        }
        return presets;
    }

    public static void queue(QueriesPresets queriesPresets) {
        get().queue(new AppleJsonToFile(queriesPresets.getFile(fileToSave), queriesPresets));
    }

    @Override
    public int getRequestsPerTimeUnit() {
        return 100;
    }

    @Override
    public int getTimeUnitMillis() {
        return 0;
    }

    @Override
    public int getSafeGuardBuffer() {
        return 1000;
    }
}

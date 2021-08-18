package apple.build.query;

import apple.build.BuildMain;
import apple.utilities.request.AppleJsonFromFile;
import apple.utilities.request.AppleJsonToFile;
import apple.utilities.request.AppleRequestService;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class QuerySavingService extends AppleRequestService {
    private static final QuerySavingService instance = new QuerySavingService();
    private static final File fileToSave;

    static {
        List<String> path = List.of(BuildMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        fileToSave = new File(String.join("/", path.subList(0, path.size() - 1)) + "/queries");
    }

    public static QuerySavingService get() {
        return instance;
    }

    public void queue(QuerySaved querySaved, long userId) {
        // add the querySavedId to the users queries
        queue(new AppleJsonToFile(querySaved.getFile(fileToSave), querySaved));
        queue(() -> {
            File file = new File(new File(fileToSave, "users"), userId + ".json");
            QueriesSavedForUser saved = file.exists() ? new AppleJsonFromFile<>(file, QueriesSavedForUser.class).get() : new QueriesSavedForUser(querySaved, userId);
            saved.addQuery(querySaved);
            new AppleJsonToFile(file, saved).complete();
        });
    }

    public boolean queue(String buildId, Consumer<QuerySaved> runAfter) {
        File file = QuerySaved.getFile(fileToSave, buildId);
        if (!file.exists()) return false;
        queue(new AppleJsonFromFile<>(file, QuerySaved.class), runAfter);
        return true;
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

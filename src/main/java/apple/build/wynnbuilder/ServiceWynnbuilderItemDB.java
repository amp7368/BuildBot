package apple.build.wynnbuilder;

import apple.build.BuildMain;
import apple.build.wynncraft.items.Item;
import apple.utilities.request.AppleJsonFromFile;
import apple.utilities.request.AppleJsonFromURL;
import apple.utilities.request.AppleJsonToFile;
import apple.utilities.request.AppleRequestService;

import java.io.File;
import java.util.List;

public class ServiceWynnbuilderItemDB extends AppleRequestService {
    private static final String url = "https://wynnbuilder.github.io/compress.json";
    private static final File fileToSave;

    private static final ServiceWynnbuilderItemDB instance = new ServiceWynnbuilderItemDB();

    private static WynnbuilderItemDB wynnDB;

    static {
        List<String> path = List.of(BuildMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        fileToSave = new File(String.join("/", path.subList(0, path.size() - 1)) + "/wynnbuilder/itemDB.json");
    }

    private static ServiceWynnbuilderItemDB getInstance() {
        return instance;
    }

    public static void callWynnbuilderToGetItemDB(boolean shouldCall) {
        if (shouldCall) {
            getInstance().queue(new AppleJsonFromURL<>(url, RawWynnbuilderItemDB.class), (db) -> {
                wynnDB = new WynnbuilderItemDB(db);
                saveDB();
                System.out.println("Wynnbuilder itemDB downloaded");
            });

        } else {
            getInstance().queue(new AppleJsonFromFile<>(fileToSave, WynnbuilderItemDB.class), (db) -> {
                wynnDB = db;
            });
        }
    }

    private static void saveDB() {
        getInstance().queue(new AppleJsonToFile(fileToSave, wynnDB),
                () -> System.out.println("Wynnbuilder itemDB saved"),
                Throwable::printStackTrace);
    }

    public static int getItemId(Item item) {
        RawWynnbuilderItem wynnbuilderItem = wynnDB.get(item.name);
        return wynnbuilderItem == null ? -1 : wynnbuilderItem.id;
    }


    @Override
    public int getRequestsPerTimeUnit() {
        return 1;
    }

    @Override
    public int getTimeUnitMillis() {
        return 0;
    }

    @Override
    public int getSafeGuardBuffer() {
        return 2000;
    }
}

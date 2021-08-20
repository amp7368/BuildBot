package apple.build.query;

import apple.build.utils.Pair;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueriesPresets {
    public HashMap<String, Preset> nameToUUID;

    public String presetCategory;

    public QueriesPresets() {
    }

    public QueriesPresets(String presetCategory) {
        this.nameToUUID = new HashMap<>();
        this.presetCategory = presetCategory;
    }

    public synchronized List<Pair<String, Preset>> getPresets(String filter) {
        Pattern filterPattern = Pattern.compile(".*" + Pattern.quote(filter) + ".*", Pattern.CASE_INSENSITIVE);
        List<Pair<String, Preset>> presets = new ArrayList<>();
        for (Map.Entry<String, Preset> preset : nameToUUID.entrySet()) {
            if (filterPattern.matcher(preset.getKey()).matches()) {
                presets.add(new Pair<>(preset.getKey(), preset.getValue()));
            }
        }
        return presets;
    }

    public synchronized void putAndSave(String buildName, String buildUUID) {
        put(buildName, buildUUID);
        save();
    }

    private synchronized void save() {
        QuerySavingService.queue(this);
    }

    private synchronized void put(String buildName, String buildUUID) {
        this.nameToUUID.put(buildName, new Preset(buildUUID));
    }

    public synchronized File getFile(File fileToSave) {
        return new File(new File(fileToSave, "presets"), presetCategory + ".json");
    }

    public synchronized List<Pair<String, Preset>> getAll() {
        return new ArrayList<>(nameToUUID.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
    }
}

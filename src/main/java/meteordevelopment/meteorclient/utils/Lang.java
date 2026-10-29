package meteordevelopment.meteorclient.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import meteordevelopment.meteorclient.MeteorClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Lang {
    private static final Map<String, String> MAP = new HashMap<>();
    private static final Map<String, String> SETTINGS = new HashMap<>();
    private static final Map<String, String> DUMP = new LinkedHashMap<>();
    private static final Map<String, String> SETTINGS_DUMP = new LinkedHashMap<>();

    static {
        load("/assets/meteor-client/lang/ja_jp.json", MAP);
        load("/assets/meteor-client/lang/settings_ja_jp.json", SETTINGS);

        // 一時的な書き出し用(翻訳ひな形を作り終えたら削除してOK)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                Files.writeString(Path.of("lang_dump.json"), gson.toJson(DUMP), StandardCharsets.UTF_8);
                Files.writeString(Path.of("lang_settings_dump.json"), gson.toJson(SETTINGS_DUMP), StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }));
    }

    private static void load(String path, Map<String, String> target) {
        try (InputStream in = Lang.class.getResourceAsStream(path)) {
            if (in == null) {
                MeteorClient.LOG.warn("{} not found", path);
                return;
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                Map<String, String> loaded = new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
                if (loaded != null) target.putAll(loaded);
            }
        } catch (Exception e) {
            MeteorClient.LOG.error("Failed to load " + path, e);
        }
    }

    public static String get(String key, String fallback) {
        return MAP.getOrDefault(key, fallback);
    }

    // 設定項目の説明文。英語の文そのものをキーにして翻訳する
    public static String setting(String english) {
        if (english == null) return null;
        SETTINGS_DUMP.put(english, english);
        return SETTINGS.getOrDefault(english, english);
    }

    public static void record(String name, String title, String description) {
        DUMP.put("module." + name + ".description", description);
    }
}

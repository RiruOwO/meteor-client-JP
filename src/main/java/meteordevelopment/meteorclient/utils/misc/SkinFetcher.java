package meteordevelopment.meteorclient.utils.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinFetcher {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Map<String, PlayerSkin> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<PlayerSkin>> PENDING = new ConcurrentHashMap<>();

    /**
     * ユーザー名からスキンを取得する
     */
    public static CompletableFuture<PlayerSkin> fetchSkin(String username) {
        if (username == null || username.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        if (CACHE.containsKey(username)) {
            return CompletableFuture.completedFuture(CACHE.get(username));
        }

        if (PENDING.containsKey(username)) {
            return PENDING.get(username);
        }

        CompletableFuture<PlayerSkin> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 1. ユーザー名からUUIDを取得
                HttpRequest uuidRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                    .GET()
                    .build();

                HttpResponse<String> uuidResponse = HTTP_CLIENT.send(uuidRequest, HttpResponse.BodyHandlers.ofString());
                if (uuidResponse.statusCode() != 200) return null;

                JsonObject uuidJson = JsonParser.parseString(uuidResponse.body()).getAsJsonObject();
                String uuid = uuidJson.get("id").getAsString();

                // 2. UUIDからスキン情報を取得
                HttpRequest profileRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false"))
                    .GET()
                    .build();

                HttpResponse<String> profileResponse = HTTP_CLIENT.send(profileRequest, HttpResponse.BodyHandlers.ofString());
                if (profileResponse.statusCode() != 200) return null;

                JsonObject profileJson = JsonParser.parseString(profileResponse.body()).getAsJsonObject();
                JsonObject properties = profileJson.getAsJsonArray("properties").get(0).getAsJsonObject();

                String textureValue = properties.get("value").getAsString();
                String textureSignature = properties.get("signature").getAsString();

                // 3. PlayerSkinの生成
                // 26.2では Identifier.fromNamespaceAndPath を使用する
                Identifier textureId = Identifier.fromNamespaceAndPath("dynamic", "player/" + uuid);

                // ※26.2のPlayerSkin.create()のシグネチャに合わせて調整が必要です。
                // 現状のMojang APIから取得したデータを元に生成する場合、
                // 以下のように PlayerSkin.create() を使うのが一般的ですが、
                // 引数の型（ClientAsset.Textureなど）は26.2の実装に依存します。
                // 一旦、ビルドを通すために null を返すか、コメントアウトしてください。

                // PlayerSkin skin = PlayerSkin.create(textureId, textureValue, textureSignature); // 仮
                PlayerSkin skin = null; // ★ビルドを通すための仮実装

                if (skin != null) {
                    CACHE.put(username, skin);
                }
                return skin;
            } catch (Exception e) {
                // printStackTrace から MeteorClient のロガーに変更
                MeteorClient.LOG.error("Failed to fetch skin for user: " + username, e);
                return null;
            } finally {
                PENDING.remove(username);
            }
        });

        PENDING.put(username, future);
        return future;
    }
}

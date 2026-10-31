package meteordevelopment.meteorclient.utils.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinFetcher {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Map<String, PlayerSkin> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<PlayerSkin>> PENDING = new ConcurrentHashMap<>();

    public static CompletableFuture<PlayerSkin> fetchSkin(String username) {
        if (username == null || username.isEmpty()) return CompletableFuture.completedFuture(null);
        if (CACHE.containsKey(username)) return CompletableFuture.completedFuture(CACHE.get(username));
        if (PENDING.containsKey(username)) return PENDING.get(username);

        CompletableFuture<PlayerSkin> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 1. ユーザー名からUUIDを取得
                HttpRequest uuidRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                    .GET().build();
                HttpResponse<String> uuidResponse = HTTP_CLIENT.send(uuidRequest, HttpResponse.BodyHandlers.ofString());
                if (uuidResponse.statusCode() != 200) return null;
                JsonObject uuidJson = JsonParser.parseString(uuidResponse.body()).getAsJsonObject();
                String uuid = uuidJson.get("id").getAsString();

                // 2. UUIDからスキン情報を取得
                HttpRequest profileRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false"))
                    .GET().build();
                HttpResponse<String> profileResponse = HTTP_CLIENT.send(profileRequest, HttpResponse.BodyHandlers.ofString());
                if (profileResponse.statusCode() != 200) return null;

                JsonObject profileJson = JsonParser.parseString(profileResponse.body()).getAsJsonObject();
                JsonObject properties = profileJson.getAsJsonArray("properties").get(0).getAsJsonObject();
                String textureValue = properties.get("value").getAsString();

                // 3. Base64データをデコードしてNativeImageを作成
                String textureUrl = extractTextureUrl(textureValue);
                if (textureUrl == null) return null;

                // テクスチャ画像をダウンロード
                HttpRequest textureRequest = HttpRequest.newBuilder().uri(URI.create(textureUrl)).GET().build();
                HttpResponse<byte[]> textureResponse = HTTP_CLIENT.send(textureRequest, HttpResponse.BodyHandlers.ofByteArray());
                if (textureResponse.statusCode() != 200) return null;

                NativeImage image = NativeImage.read(new ByteArrayInputStream(textureResponse.body()));

                // 4. DynamicTextureとして登録
                Identifier textureId = Identifier.fromNamespaceAndPath("dynamic", "skin_" + uuid);
                Minecraft.getInstance().getTextureManager().register(
                    textureId,
                    new DynamicTexture(() -> "dynamic_skin_" + uuid, image));

                // 5. PlayerSkinを生成 (26.2のClientAsset.ResourceTextureを使用)
                // ResourceTextureのコンストラクタは(Identifier id, Identifier texturePath)の2つ
                // texturePathには実際のテクスチャIDを渡す
                net.minecraft.core.ClientAsset.ResourceTexture bodyTexture =
                    new net.minecraft.core.ClientAsset.ResourceTexture(textureId, textureId);

                // PlayerSkin.insecure() を使用して生成
                PlayerSkin skin = PlayerSkin.insecure(bodyTexture, null, null, net.minecraft.world.entity.player.PlayerModelType.WIDE);

                CACHE.put(username, skin);
                return skin;
            } catch (Exception e) {
                MeteorClient.LOG.error("Failed to fetch skin for user: " + username, e);
                return null;
            } finally {
                PENDING.remove(username);
            }
        });

        PENDING.put(username, future);
        return future;
    }

    // Base64のvalueからテクスチャURLを抽出するヘルパー
    private static String extractTextureUrl(String base64Value) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64Value));
            JsonObject json = JsonParser.parseString(decoded).getAsJsonObject();
            return json.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}

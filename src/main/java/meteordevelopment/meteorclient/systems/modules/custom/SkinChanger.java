package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.SkinFetcher;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.player.PlayerSkin;

public class SkinChanger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<String> targetName = sgGeneral.add(new StringSetting.Builder()
        .name("target-name")
        .description("スキンをコピーしたいプレイヤーの名前。")
        .defaultValue("Notch")
        .build()
    );

    private PlayerSkin cachedSkin = null;
    private int timer = 0;

    public SkinChanger() {
        super(Categories.Render, "skin-changer", "指定したプレイヤーのスキンに変更します。");
    }

    @Override
    public void onActivate() {
        cachedSkin = null;
        timer = 0;
    }

    @Override
    public void onDeactivate() {
        cachedSkin = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (cachedSkin == null) {
            if (timer <= 0) {
                timer = 20; // 1秒に1回だけリクエストを送る
                String name = targetName.get();
                if (name != null && !name.isEmpty()) {
                    SkinFetcher.fetchSkin(name).thenAccept(skin -> {
                        if (skin != null) {
                            this.cachedSkin = skin;
                        }
                    });
                }
            } else {
                timer--;
            }
        }
    }

    public PlayerSkin getCachedSkin() {
        return cachedSkin;
    }
}

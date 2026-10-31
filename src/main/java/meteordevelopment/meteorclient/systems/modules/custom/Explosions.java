package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;

public class Explosions extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // --- General ---
    private final Setting<Double> damageRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("damage-radius")
        .description("ダメージを与える範囲の半径。")
        .defaultValue(8.0)
        .min(1.0)
        .sliderMax(20.0)
        .build()
    );

    private final Setting<Double> blastRadius = sgGeneral.add(new DoubleSetting.Builder()
        .name("blast-radius")
        .description("ブロックを破壊する範囲の半径。")
        .defaultValue(4.0)
        .min(1.0)
        .sliderMax(20.0)
        .build()
    );

    // --- Render ---
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("描画モード。")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> damageSideColor = sgRender.add(new ColorSetting.Builder()
        .name("damage-side-color")
        .description("ダメージ範囲の面の色。")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final Setting<SettingColor> damageLineColor = sgRender.add(new ColorSetting.Builder()
        .name("damage-line-color")
        .description("ダメージ範囲の線の色。")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    private final Setting<SettingColor> blastSideColor = sgRender.add(new ColorSetting.Builder()
        .name("blast-side-color")
        .description("破壊範囲の面の色。")
        .defaultValue(new SettingColor(255, 128, 0, 50))
        .build()
    );

    private final Setting<SettingColor> blastLineColor = sgRender.add(new ColorSetting.Builder()
        .name("blast-line-color")
        .description("破壊範囲の線の色。")
        .defaultValue(new SettingColor(255, 128, 0, 255))
        .build()
    );

    public Explosions() {
        super(Categories.Render, "explosions", "TNTの爆発範囲を可視化します。");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            // TNTエンティティのみを対象にする
            if (!(entity instanceof PrimedTnt tnt)) continue;

            // 爆発までの残り時間（Fuse）を取得
            int fuse = tnt.getFuse();
            if (fuse <= 0) continue;

            // 爆発の中心位置（TNTの現在位置 + 少し上）
            Vec3 pos = tnt.position().add(0, 0.5, 0);

            // ダメージ範囲（赤いドーム）を描画
            drawSphere(event, pos, damageRadius.get(), damageSideColor.get(), damageLineColor.get());

            // 破壊範囲（オレンジのドーム）を描画
            drawSphere(event, pos, blastRadius.get(), blastSideColor.get(), blastLineColor.get());
        }
    }

    // 球体（ドーム）を描画するヘルパーメソッド
    // 平面の円を何層も重ねて描画することで、球体を再現します
    private void drawSphere(Render3DEvent event, Vec3 center, double radius, SettingColor sideColor, SettingColor lineColor) {
        // 球体を構成する分割数（多いほど滑らかだが重くなる）
        int resolution = 6;
        double step = (radius * 2) / resolution;

        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {
                for (int z = 0; z < resolution; z++) {
                    // 各箱の中心座標
                    double cx = center.x - radius + (x + 0.5) * step;
                    double cy = center.y - radius + (y + 0.5) * step;
                    double cz = center.z - radius + (z + 0.5) * step;

                    // 中心から箱の中心までの距離が半径より小さい場合のみ描画（球体を形成）
                    double distSq = (cx - center.x) * (cx - center.x)
                        + (cy - center.y) * (cy - center.y)
                        + (cz - center.z) * (cz - center.z);

                    if (distSq <= radius * radius) {
                        // 小さな箱を描画
                        event.renderer.box(
                            cx - step / 2, cy - step / 2, cz - step / 2,
                            cx + step / 2, cy + step / 2, cz + step / 2,
                            sideColor, lineColor, shapeMode.get(), 0
                        );
                    }
                }
            }
        }
    }
}

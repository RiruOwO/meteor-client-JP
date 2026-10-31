package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
// 先ほど確認した Categories クラスをインポートします
import meteordevelopment.meteorclient.systems.modules.Categories;

public class AntiAim extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> bodyOffset = sgGeneral.add(new DoubleSetting.Builder()
        .name("body-offset")
        .description("体の向きのずらし角度。")
        .defaultValue(90.0)
        .sliderRange(-180.0, 180.0)
        .build()
    );

    public final Setting<Double> headOffset = sgGeneral.add(new DoubleSetting.Builder()
        .name("head-offset")
        .description("頭の向きのずらし角度。")
        .defaultValue(0.0)
        .sliderRange(-180.0, 180.0)
        .build()
    );

    public AntiAim() {
        // 修正点： Category.Render から Categories.New に変更
        super(Categories.Render, "anti-aim", "見た目上の体・首の向きをずらす（検証用）。");
    }
}

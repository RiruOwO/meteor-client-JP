package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class AntiAim extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum Mode {
        Static,
        Spin
    }

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("体の向きのずらし方。")
        .defaultValue(Mode.Static)
        .build()
    );

    public final Setting<Double> bodyOffset = sgGeneral.add(new DoubleSetting.Builder()
        .name("body-offset")
        .description("体の向きのずらし角度。")
        .defaultValue(180.0)
        .sliderRange(-180.0, 180.0)
        .visible(() -> mode.get() == Mode.Static)
        .build()
    );

    public final Setting<Double> headOffset = sgGeneral.add(new DoubleSetting.Builder()
        .name("head-offset")
        .description("頭の向きのずらし角度。")
        .defaultValue(0.0)
        .sliderRange(-180.0, 180.0)
        .visible(() -> mode.get() == Mode.Static)
        .build()
    );

    public final Setting<Double> spinSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("spin-speed")
        .description("回転させる速度（度/Tick）。体と頭が同じ速度で回ります。")
        .defaultValue(15.0)
        .min(1.0)
        .sliderRange(1.0, 90.0)
        .visible(() -> mode.get() == Mode.Spin)
        .build()
    );

    private float currentSpinAngle = 0.0f;

    public AntiAim() {
        super(Categories.Render, "anti-aim", "見た目上の体・首の向きをずらす（検証用）。");
    }

    @Override
    public void onActivate() {
        currentSpinAngle = 0.0f;
    }

    @Override
    public void onDeactivate() {
        currentSpinAngle = 0.0f;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        if (mode.get() == Mode.Spin) {
            // 1つの角度を更新するだけで、体も頭も同じ角度になる
            currentSpinAngle += spinSpeed.get().floatValue();
            if (currentSpinAngle > 360.0f) {
                currentSpinAngle -= 360.0f;
            }
        }
    }

    public boolean isSpinMode() {
        return mode.get() == Mode.Spin;
    }

    public float getBodyYawOffset() {
        if (mode.get() == Mode.Spin) {
            return currentSpinAngle;
        }
        return bodyOffset.get().floatValue();
    }

    public float getHeadYawOffset() {
        if (mode.get() == Mode.Spin) {
            return currentSpinAngle; // 体と同じ角度を返す
        }
        return headOffset.get().floatValue();
    }
}

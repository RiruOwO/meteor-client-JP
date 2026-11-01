/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.CharacterImage;
import meteordevelopment.orbit.EventHandler;

public class GuiImageDisplay extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // キャラクター選択設定
    public final Setting<CharacterImage> selectedCharacter = sgGeneral.add(new EnumSetting.Builder<CharacterImage>()
        .name("character")
        .description("表示するキャラクターを選択します。")
        .defaultValue(CharacterImage.CHAR_1)
        .build()
    );

    public final Setting<Integer> imageWidth = sgGeneral.add(new IntSetting.Builder()
        .name("width")
        .description("画像の表示幅")
        .defaultValue(256)
        .sliderRange(32, 512)
        .build()
    );

    public final Setting<Integer> imageHeight = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("画像の表示高さ")
        .defaultValue(256)
        .sliderRange(32, 512)
        .build()
    );

    public final Setting<Integer> xOffset = sgGeneral.add(new IntSetting.Builder()
        .name("x-offset")
        .description("画面左端からのX座標オフセット")
        .defaultValue(00)
        .build()
    );

    public final Setting<Integer> yOffset = sgGeneral.add(new IntSetting.Builder()
        .name("y-offset")
        .description("画面上端からのY座標オフセット")
        .defaultValue(250)
        .build()
    );

    // 起動時に一度だけオンにするためのフラグ
    private boolean firstTick = true;

    public GuiImageDisplay() {
        super(Categories.Render, "gui-image-display", "GUI画面にキャラクター画像を表示します。");
        runInMainMenu = true;
        toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // 最初のTickで強制的にオンにする
        if (firstTick) {
            firstTick = false;
            if (!isActive()) {
                toggle();
            }
        }
    }

    // キーバインドなどから呼ぶ用（次のキャラクターへ）
    public void nextCharacter() {
        CharacterImage[] values = CharacterImage.values();
        int currentIndex = selectedCharacter.get().ordinal();
        int nextIndex = (currentIndex + 1) % values.length;
        selectedCharacter.set(values[nextIndex]);
    }

    public final Setting<Boolean> onlyInGui = sgGeneral.add(new BoolSetting.Builder()
        .name("only-in-gui")
        .description("GUIを開いている時だけ表示します。")
        .defaultValue(true)
        .build()
    );
}

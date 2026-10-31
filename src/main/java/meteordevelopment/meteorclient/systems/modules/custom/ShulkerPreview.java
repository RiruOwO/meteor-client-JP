package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.mixininterface.IShulkerBoxBlockEntity;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector3d;

public class ShulkerPreview extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDisplay = settings.createGroup("Display");
    private final SettingGroup sgColors = settings.createGroup("Colors");

    // --- General ---
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("シュルカーボックスの中身を表示する最大距離（ブロック単位）。")
        .defaultValue(32.0)
        .min(1.0)
        .sliderMax(128.0)
        .build()
    );

    private final Setting<Boolean> ignoreEmpty = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-empty")
        .description("中身が空のシュルカーボックスを無視します。")
        .defaultValue(true)
        .build()
    );

    // --- Display ---
    private final Setting<Boolean> showIcons = sgDisplay.add(new BoolSetting.Builder()
        .name("show-icons")
        .description("アイテムのアイコンを表示します。")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showNames = sgDisplay.add(new BoolSetting.Builder()
        .name("show-names")
        .description("アイテムの名前を表示します。")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> scale = sgDisplay.add(new DoubleSetting.Builder()
        .name("scale")
        .description("UI全体のスケール（大きさ）を変更します。")
        .defaultValue(1.0)
        .min(0.5)
        .sliderMax(2.0)
        .build()
    );

    // --- Colors ---
    private final Setting<SettingColor> backgroundColor = sgColors.add(new ColorSetting.Builder()
        .name("background-color")
        .description("UIの背景色。")
        .defaultValue(new SettingColor(0, 0, 0, 150))
        .build()
    );

    private final Setting<SettingColor> textColor = sgColors.add(new ColorSetting.Builder()
        .name("text-color")
        .description("アイテム名の文字色。")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Vector3d pos = new Vector3d();

    public ShulkerPreview() {
        super(Categories.Render, "shulker-preview", "設置されたシュルカーボックスの中身を表示します。");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (event.graphics == null) return;

        double rangeSq = range.get() * range.get();
        HudRenderer renderer = HudRenderer.INSTANCE;
        renderer.begin(event.graphics);

        int playerChunkX = mc.player.chunkPosition().x();
        int playerChunkZ = mc.player.chunkPosition().z();
        int chunkRadius = (int) Math.ceil(range.get() / 16.0);

        for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
            for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
                LevelChunk chunk = mc.level.getChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof ShulkerBoxBlockEntity shulker)) continue;

                    IShulkerBoxBlockEntity accessor = (IShulkerBoxBlockEntity) shulker;
                    var items = accessor.meteor$getItems();

                    if (items.isEmpty()) continue;

                    // 実際にアイテムが入っているスロットの数を数える
                    int nonEmptyCount = 0;
                    for (ItemStack stack : items) {
                        if (!stack.isEmpty()) nonEmptyCount++;
                    }

                    if (ignoreEmpty.get() && nonEmptyCount == 0) continue;

                    BlockPos blockPos = shulker.getBlockPos();
                    if (mc.player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) > rangeSq) continue;

                    pos.set(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5);
                    if (!NametagUtils.to2D(pos, 1.0)) continue;

                    double startX = pos.x;
                    double startY = pos.y;

                    // スケールを適用した幅と高さ（nonEmptyCount を使用）
                    int width = (int) (140 * scale.get());
                    int height = (int) ((nonEmptyCount * 18 + 10) * scale.get());

                    // 背景を描画
                    renderer.quad(startX - width / 2.0, startY, width, height, backgroundColor.get());

                    // アイテムとテキストの描画
                    int yOffset = (int) (5 * scale.get());
                    for (ItemStack stack : items) {
                        if (stack.isEmpty()) continue;

                        if (showIcons.get()) {
                            renderer.item(stack, (int)(startX - width / 2.0 + 5 * scale.get()), (int)(startY + yOffset), scale.get().floatValue(), false);
                        }

                        if (showNames.get()) {
                            double textX = startX - width / 2.0 + (showIcons.get() ? (25 * scale.get()) : (5 * scale.get()));
                            renderer.text(
                                stack.getHoverName().getString(),
                                textX,
                                startY + yOffset + (2 * scale.get()),
                                textColor.get(),
                                true,
                                scale.get()
                            );
                        }

                        yOffset += (int) (18 * scale.get());
                    }
                }
            }
        }

        renderer.end();
    }
}

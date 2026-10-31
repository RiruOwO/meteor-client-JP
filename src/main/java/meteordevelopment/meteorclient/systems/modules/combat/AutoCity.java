package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class AutoCity extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The radius in which players get targeted.")
        .defaultValue(5.5)
        .min(0)
        .sliderMax(7)
        .build()
    );

    private final Setting<Double> breakRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("break-range")
        .description("How close a block must be to you to be considered.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("switch-mode")
        .description("How to switch to a pickaxe.")
        .defaultValue(SwitchMode.Normal)
        .build()
    );

    private final Setting<Boolean> support = sgGeneral.add(new BoolSetting.Builder()
        .name("support")
        .description("If there is no block below a city block it will place one before mining.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> placeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("place-range")
        .description("How far away to try and place a block.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .visible(support::get)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically rotates you towards the city block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> chatInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-info")
        .description("Whether the module should send messages in chat.")
        .defaultValue(true)
        .build()
    );

    // Render
    private final Setting<Boolean> swingHand = sgRender.add(new BoolSetting.Builder()
        .name("swing-hand")
        .description("Whether to render your hand swinging.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> renderBlock = sgRender.add(new BoolSetting.Builder()
        .name("render-block")
        .description("Whether to render the block being broken.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(renderBlock::get)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the rendering.")
        .defaultValue(new SettingColor(225, 0, 0, 75))
        .visible(() -> renderBlock.get() && shapeMode.get().sides())
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the rendering.")
        .defaultValue(new SettingColor(225, 0, 0, 255))
        .visible(() -> renderBlock.get() && shapeMode.get().lines())
        .build()
    );

    private Player target;
    private BlockPos targetPos;
    private FindItemResult pick;
    private float progress;

    public AutoCity() {
        super(Categories.Combat, "auto-city", "Automatically mine blocks next to someone's feet.");
    }

    @Override
    public void onActivate() {
        // オンにした瞬間は何もしない（常時稼働のため）
        target = null;
        targetPos = null;
        progress = 0.0f;
    }

    @Override
    public void onDeactivate() {
        target = null;
        targetPos = null;
        progress = 0.0f;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // 常にターゲットを探す
        if (target == null || TargetUtils.isBadTarget(target, targetRange.get())) {
            target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.ClosestAngle);
            if (target == null) {
                targetPos = null;
                progress = 0.0f;
                return;
            }
        }

        // ターゲットのシティブロックを取得
        if (targetPos == null) {
            targetPos = EntityUtils.getCityBlock(target);
            if (targetPos == null || PlayerUtils.squaredDistanceTo(targetPos) > Math.pow(breakRange.get(), 2)) {
                targetPos = null;
                progress = 0.0f;
                return;
            }
        }

        // ターゲットが範囲外ならリセット
        if (PlayerUtils.squaredDistanceTo(target) > Math.pow(targetRange.get(), 2)) {
            target = null;
            targetPos = null;
            progress = 0.0f;
            return;
        }

        // ツルハシの確認
        pick = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
        if (!pick.isHotbar()) {
            if (chatInfo.get()) error("No pickaxe found... disabling.");
            toggle();
            return;
        }

        // サポートブロックの設置 (最初の1回だけ)
        if (support.get() && progress == 0.0f) {
            BlockPos supportPos = targetPos.below();
            if (PlayerUtils.squaredDistanceTo(supportPos) <= Math.pow(placeRange.get(), 2)) {
                BlockUtils.place(supportPos, InvUtils.findInHotbar(Items.OBSIDIAN), rotate.get(), 0, true);
            }
        }

        // 掘る処理
        if (progress < 1.0f) {
            progress += BlockUtils.getBreakDelta(pick.slot(), mc.level.getBlockState(targetPos));
            if (progress < 1.0f) {
                // 掘っている途中でもパケットを送り続ける
                mine(false);
                return;
            }
        }

        // 破壊完了
        mine(true);

        // 破壊したらターゲットをリセットして次の敵を探す
        targetPos = null;
        progress = 0.0f;
    }

    public void mine(boolean done) {
        if (targetPos == null) return;

        InvUtils.swap(pick.slot(), switchMode.get() == SwitchMode.Silent);
        if (rotate.get()) Rotations.rotate(Rotations.getYaw(targetPos), Rotations.getPitch(targetPos));

        Direction direction = BlockUtils.getDirection(targetPos);

        if (!done) {
            mc.gameMode.startPrediction(mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, targetPos, direction, sequence));
        }

        mc.gameMode.startPrediction(mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, targetPos, direction, sequence));

        if (swingHand.get()) mc.player.swing(InteractionHand.MAIN_HAND);
        else mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));

        if (switchMode.get() == SwitchMode.Silent) InvUtils.swapBack();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (targetPos == null || !renderBlock.get()) return;
        event.renderer.box(targetPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    public enum SwitchMode {
        Normal,
        Silent
    }
}

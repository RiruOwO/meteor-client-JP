package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;

public class PacketEat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("パケットを送信する間隔 (tick)。")
        .defaultValue(4)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> swingHand = sgGeneral.add(new BoolSetting.Builder()
        .name("swing-hand")
        .description("食べるモーション（腕の振り）を表示します。")
        .defaultValue(false)
        .build()
    );

    private int timer = 0;

    public PacketEat() {
        super(Categories.Player, "packet-eat", "モーションなしで食べます。");
    }

    @Override
    public void onDeactivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;

        // クライアント側でアイテムを使っている（食事中）場合のみ処理
        if (!mc.player.isUsingItem()) return;

        ItemStack stack = mc.player.getUseItem();
        if (!stack.has(net.minecraft.core.component.DataComponents.FOOD)) return;

        if (timer > 0) {
            timer--;
            return;
        }

        // クライアント側のモーションをキャンセル（腕を振らないようにする）
        if (!swingHand.get()) {
            mc.player.swing(InteractionHand.MAIN_HAND, false); // 見た目のキャンセル用（26.2のAPIに合わせて調整が必要な場合あり）
        }

        // サーバーに「食べている」パケットを送信
        mc.getConnection().send(new ServerboundUseItemPacket(
            InteractionHand.MAIN_HAND,
            mc.player.getId(),
            mc.player.getYRot(),
            mc.player.getXRot()
        ));

        timer = delay.get();
    }
}

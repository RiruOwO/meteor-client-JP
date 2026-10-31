package meteordevelopment.meteorclient.systems.modules.custom; // 自分のパッケージに合わせてください

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
// import powie.sixbees.utils.JoinPayload; // ※これは自前で実装する必要があります

// FreeHome.java の import 修正
import meteordevelopment.meteorclient.systems.modules.misc.JoinPayload;

public class FreeHome extends Module {

    // 6B6TのサーバーIP（適宜変更してください）
    private static final String TARGET_SERVER = "6b6t.org";

    public FreeHome() {
        // カテゴリを Categories.New に指定
        super(Categories.New, "free-home", "Gives you 2 extra homes.");
    }

    @Override
    public void onActivate() {
        if (mc.getConnection() == null || mc.player == null) return;

        // 現在接続しているサーバーのIPを取得
        String currentServerIp = mc.getCurrentServer() != null ? mc.getCurrentServer().ip : "";

        // 特定のサーバー（6B6T）に接続しているか判定
        if (currentServerIp.contains(TARGET_SERVER)) {
            // パケットを送信
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new JoinPayload()));
        }
    }
}

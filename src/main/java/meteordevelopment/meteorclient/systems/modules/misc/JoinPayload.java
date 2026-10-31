package meteordevelopment.meteorclient.systems.modules.misc; // パッケージ名を変更

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class JoinPayload implements CustomPacketPayload {
    // 名前空間とパスは元のコードのまま
    public static final Identifier ID = Identifier.fromNamespaceAndPath("anarchymod", "join");

    public static final StreamCodec<FriendlyByteBuf, JoinPayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> {
            },
            buf -> new JoinPayload()
        );

    public static final Type<JoinPayload> TYPE = new Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

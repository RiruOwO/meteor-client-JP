package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    // 1人称の手に持つアイテムを書き換える
    @ModifyVariable(method = "renderItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack modifyHeldItem(
        ItemStack original,
        net.minecraft.world.entity.LivingEntity entity,
        ItemStack itemStack,
        net.minecraft.world.item.ItemDisplayContext displayContext,
        com.mojang.blaze3d.vertex.PoseStack poseStack,
        net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector,
        int light
    ) {
        AutoTool autoTool = Modules.get().get(AutoTool.class);

        if (autoTool != null && autoTool.isActive() && autoTool.fakeMode.get()) {
            System.out.println("[HeldItemRendererMixin] modifyHeldItem called! original=" + original.getItem() + " entity=" + entity);
        }

        // 自分自身（LocalPlayer）の場合のみ処理
        if (autoTool != null && autoTool.isActive() && autoTool.fakeMode.get() && autoTool.originalSlot != -1 && entity == Minecraft.getInstance().player) {
            // ★ LivingEntity を Player にキャストして getInventory() を呼ぶ
            return ((net.minecraft.world.entity.player.Player) entity).getInventory().getItem(autoTool.originalSlot);
        }
        return original;
    }
}

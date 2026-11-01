package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.custom.GuiImageDisplay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.DeltaTracker;

@Mixin(Hud.class)
public class InGameHudMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRenderTail(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        GuiImageDisplay module = Modules.get().get(GuiImageDisplay.class);
        if (module == null || !module.isActive()) return;

        // 「GUIを開いている時だけ表示」がオンの場合、画面が開いていなければスキップ
        if (module.onlyInGui.get()) {
            if (Minecraft.getInstance().gui.screen() == null) return;
        }

        Identifier texture = module.selectedCharacter.get().getIdentifier();

        int x = module.xOffset.get();
        int y = module.yOffset.get();
        int width = module.imageWidth.get();
        int height = module.imageHeight.get();

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture,
            x, y,
            0, 0,
            width, height,
            width, height
        );
    }
}

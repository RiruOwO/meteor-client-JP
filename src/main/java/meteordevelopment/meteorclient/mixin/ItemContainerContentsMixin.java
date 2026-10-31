/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.mixininterface.IItemContainerContents;

@Mixin(ItemContainerContents.class)
public abstract class ItemContainerContentsMixin implements IItemContainerContents {
    @Shadow
    @Final
    private List<Optional<ItemStackTemplate>> items;

    // 2. 新しくメソッドを追加（@Unique は不要）
    @Override
    public List<Optional<ItemStackTemplate>> meteor$getItems() {
        return this.items;
    }

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private void onAddToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components, CallbackInfo ci) {
        if (Modules.get() == null) return;

        BetterTooltips tooltips = Modules.get().get(BetterTooltips.class);
        if (tooltips.isActive()) {
            if (tooltips.previewShulkers()) ci.cancel();
            else if (tooltips.shulkerCompactTooltip()) {
                ci.cancel();
                tooltips.applyCompactShulkerTooltip(items, consumer);
            }
        }
    }
}

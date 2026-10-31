package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IShulkerBoxBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.core.NonNullList;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin implements IShulkerBoxBlockEntity {
    // 26.2のShulkerBoxBlockEntityのインベントリフィールド名（要確認）
    @Shadow
    private NonNullList<ItemStack> itemStacks;

    @Override
    public NonNullList<ItemStack> meteor$getItems() {
        return this.itemStacks;
    }
}

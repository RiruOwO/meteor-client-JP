package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IShulkerBoxBlockEntity {
    NonNullList<ItemStack> meteor$getItems();
}

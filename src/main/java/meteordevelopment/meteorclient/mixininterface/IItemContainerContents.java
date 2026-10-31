package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.world.item.ItemStackTemplate;
import java.util.List;
import java.util.Optional;

public interface IItemContainerContents {
    List<Optional<ItemStackTemplate>> meteor$getItems();
}

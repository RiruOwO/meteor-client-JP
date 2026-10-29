package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;

public class AntiWeb extends Module {
    public AntiWeb() {
        super(Categories.New, "anti-web", "Prevents you from walking into webs. (but not falling into them)");
    }

    @EventHandler
    private void onCollisionShape(CollisionShapeEvent event) {
        if (mc.level == null || mc.player == null) return;
        if (event.state.getBlock() == Blocks.COBWEB
            && (mc.player.fallDistance <= 0 || mc.player.isFallFlying())
            && !mc.level.getBlockState(mc.player.blockPosition()).is(Blocks.COBWEB)
            && !mc.level.getBlockState(mc.player.blockPosition().above()).is(Blocks.COBWEB)
        ) {
            event.shape = Shapes.block();
        }
    }
}

package dev.ianaduarte.timber.mixin.leaves;

import dev.ianaduarte.timber.util.LeafParticleSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin implements LeafParticleSpawner {
	@Override
	public void spawnParticles(Level level, BlockPos pos, int count, double dx, double dz, double dy, double xzSpread, double ySpread) {
		throw new AssertionError();
	}
}

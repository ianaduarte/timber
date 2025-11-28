package dev.ianaduarte.timber.mixin.leaves;

import dev.ianaduarte.timber.util.LeafParticleSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TintedParticleLeavesBlock.class)
public class TintedLeavesMixin implements LeafParticleSpawner {
	@Override
	public void spawnParticles(Level level, BlockPos pos, int count, double dx, double dz, double dy, double xzSpread, double ySpread) {
		if(count == 0) return;
		var particle = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, level.getClientLeafTintColor(pos));
		var randomSource = level.random;
		
		for(int i = 0; i < count; i++) {
			double xSpeed = Math.abs(randomSource.nextGaussian()) * dx;
			double ySpeed = Math.abs(randomSource.nextGaussian()) * dy;
			double zSpeed = Math.abs(randomSource.nextGaussian()) * dz;
			double normSpread = 0.5 - xzSpread;
			double x = pos.getX() + normSpread + randomSource.nextDouble() * xzSpread * 2.0;
			double y = pos.getY() + randomSource.nextDouble() * ySpread;
			double z = pos.getZ() + normSpread + randomSource.nextDouble() * xzSpread * 2.0;
			level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}
}

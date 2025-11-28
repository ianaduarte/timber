package dev.ianaduarte.timber.mixin.leaves;

import dev.ianaduarte.timber.util.LeafParticleSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(UntintedParticleLeavesBlock.class)
public class UntintedLeavesMixin implements LeafParticleSpawner {
	@Shadow @Final protected ParticleOptions leafParticle;
	
	@Override
	public void spawnParticles(Level level, BlockPos pos, int count, double dx, double dz, double dy, double xzSpread, double ySpread) {
		if(count == 0) return;
		var randomSource = level.random;
		
		for(int i = 0; i < count; i++) {
			double xSpeed = Math.abs(randomSource.nextGaussian()) * dx;
			double ySpeed = Math.abs(randomSource.nextGaussian()) * dy;
			double zSpeed = Math.abs(randomSource.nextGaussian()) * dz;
			double normSpread = 0.5 - xzSpread;
			double x = pos.getX() + normSpread + randomSource.nextDouble() * xzSpread * 2.0;
			double y = pos.getY() + randomSource.nextDouble() * ySpread;
			double z = pos.getZ() + normSpread + randomSource.nextDouble() * xzSpread * 2.0;
			level.addParticle(this.leafParticle, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}
}

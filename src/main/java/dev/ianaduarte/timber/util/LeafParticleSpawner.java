package dev.ianaduarte.timber.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface LeafParticleSpawner {
	void spawnParticles(Level level, BlockPos pos, int count, double dx, double dz, double dy, double xzSpread, double ySpread);
}
package dev.ianaduarte.timber.mixin;

import com.mojang.datafixers.util.Pair;
import dev.ianaduarte.timber.util.LeafParticleSpawner;
import dev.ianaduarte.timber.util.TreeDirection;
import dev.ianaduarte.timber.util.TreeNode;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(Block.class)
public class LogMixin {
	@Unique private static final int MAX_LOGS = 512;
	@Unique private static final int MAX_LEAVES = 1024;
	@Unique private static final TreeDirection[] TOP_LEVEL_DIRECTIONS  = new TreeDirection[]{
		TreeDirection.NORTH, TreeDirection.NORTHUP,
		TreeDirection.NORTHEAST, TreeDirection.NORTHEASTUP,
		TreeDirection.NORTHWEST, TreeDirection.NORTHWESTUP,
		TreeDirection.SOUTH, TreeDirection.SOUTHUP,
		TreeDirection.SOUTHEAST, TreeDirection.SOUTHEASTUP,
		TreeDirection.SOUTHWEST, TreeDirection.SOUTHWESTUP,
		TreeDirection.EAST, TreeDirection.EASTUP,
		TreeDirection.WEST, TreeDirection.WESTUP,
		TreeDirection.UP
	};
	@Unique private static final double[] SPEED_MAP = new double[]{
		0.10, 0.20, 0.25, 0.30,
		0.32, 0.34, 0.36, 0.38,
		0.40, 0.42, 0.44, 0.46,
		0.48, 0.50, 0.52, 0.54,
		0.56, 0.58, 0.60, 0.62,
		0.64, 0.66, 0.68, 0.70,
		0.72, 0.74, 0.76, 0.78,
		0.80, 0.82, 0.84, 0.86
	};
	@Unique private static final double[] OFFSET_MAP = new double[]{
		0.25, 0.50, 1.00, 1.50,
		2.00, 2.50, 2.75, 3.00,
		3.25, 3.50, 3.75, 4.00,
		4.25, 4.50, 4.75, 5.00,
		5.25, 5.50, 5.75, 6.00,
		6.25, 6.50, 6.75, 7.00,
		7.25, 7.50, 7.75, 8.00,
		8.25, 8.50, 8.75, 9.00
	};
	
	@Unique
	private static boolean isSilkTouchMainHand(Player player) {
		var silkTouchEnchantmentReference = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
		return player.getMainHandItem().getEnchantments().getLevel(silkTouchEnchantmentReference) != 0;
	}
	
	@Unique
	private static IntIntPair collectPositions(Level level, BlockPos rootPos, BlockState rootState, List<Pair<BlockPos, BlockState>> outPositions, Vector3f outTendency) {
		var positionsToCheck = new ArrayDeque<TreeNode>();
		var checkedPositions = new HashSet<BlockPos>();
		checkedPositions.add(rootPos);
		outPositions.add(Pair.of(rootPos, rootState));
		
		for(var direction : TOP_LEVEL_DIRECTIONS) {
			positionsToCheck.push(new TreeNode(rootPos.offset(direction.getOffset()), direction.getOpposite()));
		}
		
		int logCount = 0;
		int leafCount = 0;
		Vector3i tendencyAccum = new Vector3i(0, 1, 0);
		
		while(!positionsToCheck.isEmpty()) {
			var cNode = positionsToCheck.pop();
			var cPos = cNode.position();
			var cState = level.getBlockState(cNode.position());
			
			if(checkedPositions.contains(cPos)) continue;
			checkedPositions.add(cPos);
			if(cState.is(BlockTags.LOGS)) {
				logCount++;
				tendencyAccum.add(cPos.getX() - rootPos.getX(), cPos.getY() - rootPos.getY(), cPos.getZ() - rootPos.getZ());
				if(logCount > MAX_LOGS) return IntIntPair.of(0, 0);
			}
			else if(cState.is(BlockTags.LEAVES)) {
				leafCount++;
				tendencyAccum.add(
					(cPos.getX() - rootPos.getX()) / 4,
					(cPos.getY() - rootPos.getY()) / 4,
					(cPos.getZ() - rootPos.getZ()) / 4
				);
				if(leafCount > MAX_LEAVES) return IntIntPair.of(0, 0);
			}
			else {
				continue;
			}
			outPositions.add(Pair.of(cPos, cState));
			for(TreeDirection direction : TreeDirection.VALUES) {
				if(direction == cNode.fromDirection()) continue;
				
				var nPos = cNode.position().offset(direction.getOffset());
				if(checkedPositions.contains(nPos)) continue;
				positionsToCheck.push(new TreeNode(nPos, direction.getOpposite()));
			}
		}
		outTendency.set(tendencyAccum).normalize();
		return IntIntPair.of(logCount, leafCount);
	}
	
	@Unique
	private static void spawnFallingBlocks(Level level, Player player, BlockPos rootPos, List<Pair<BlockPos, BlockState>> positionsToBreak) {
		double radY = Mth.DEG_TO_RAD * (player.getYRot() + 90.0F);
		double x = Math.cos(radY);
		double z = Math.sin(radY);
		boolean isXOriented = Math.abs(x) > Math.abs(z);
		
		for(var cPair : positionsToBreak) {
			var cPos = cPair.getFirst();
			var cState = cPair.getSecond();
			if(cState.is(BlockTags.LOGS) && cState.hasProperty(RotatedPillarBlock.AXIS)) {
				cState = cState.setValue(RotatedPillarBlock.AXIS, isXOriented ? Direction.Axis.X : Direction.Axis.Z);
			}
			else if(cState.is(BlockTags.LEAVES) && cState.hasProperty(LeavesBlock.DISTANCE)) {
				cState = cState.setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
			}
			
			var index = Math.min(Math.abs(rootPos.getY() - cPos.getY()), 31);
			var cX = cPos.getX() + 0.5 + x * OFFSET_MAP[index];
			var cY = cPos.getY() - OFFSET_MAP[index];
			var cZ = cPos.getZ() + 0.5 + z * OFFSET_MAP[index];
			
			var fallingBlock = new FallingBlockEntity(level, cX, cY, cZ, cState);
			var motion = SPEED_MAP[index] * 1.05;
			var movement = new Vec3(x * motion, 0.0, z * motion);
			fallingBlock.setDeltaMovement(movement);
			fallingBlock.time = 1;
			fallingBlock.xo = cX;
			fallingBlock.yo = cY;
			fallingBlock.zo = cZ;
			
			fallingBlock.setStartPos(cPos);
			if(cState.is(BlockTags.LOGS)) {
				fallingBlock.dropItem = true;
				fallingBlock.setHurtsEntities(2.0F, 40);
			} else if(cState.getBlock() instanceof LeavesBlock leavesBlock){
				fallingBlock.dropItem = false;
				//parameters do fucking NOTHING, because like, why would they :)))))
				((LeafParticleSpawner)leavesBlock).spawnParticles(
					level, cPos, level.random.nextInt(0, 3),
					x * 512, -12, z * 512,
					0.2, 0.3
				);
			}
			
			level.addFreshEntity(fallingBlock);
			level.removeBlock(cPos, true);
		}
	}
	
	@Inject(method = "playerWillDestroy", at = @At("HEAD"))
	private void logBreak(Level level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir) {
		if(!state.is(BlockTags.LOGS) || player.isShiftKeyDown() || isSilkTouchMainHand(player)) return;
		
		var positionsToBreak = new ArrayList<Pair<BlockPos, BlockState>>();
		var tendency = new Vector3f();
		var breakData = collectPositions(level, pos, state, positionsToBreak, tendency);
		
		//logCount, leafCount
		if(breakData.firstInt() < 1 || breakData.secondInt() < 2 || tendency.y <= 0.25) return;
		
		spawnFallingBlocks(level, player, pos, positionsToBreak);
		level.playSound(null, pos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS);
	}
}

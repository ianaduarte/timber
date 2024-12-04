package dev.ianaduarte.timber.util;

import net.minecraft.core.Vec3i;

public enum TreeDirection {
	NORTH(0, 0, -1),
	NORTHUP(0, 1, -1),
	NORTHDOWN(0, -1, -1),
	NORTHEAST(1, 0, -1),
	NORTHEASTUP(1, 1, -1),
	NORTHEASTDOWN(1, -1, -1),
	NORTHWEST(-1, 0, -1),
	NORTHWESTUP(-1, 1, -1),
	NORTHWESTDOWN(-1, -1, -1),
	SOUTH(0, 0, 1),
	SOUTHUP(0, 1, 1),
	SOUTHDOWN(0, -1, 1),
	SOUTHEAST(1, 0, 1),
	SOUTHEASTUP(1, 1, 1),
	SOUTHEASTDOWN(1, -1, 1),
	SOUTHWEST(-1, 0, 1),
	SOUTHWESTUP(-1, 1, 1),
	SOUTHWESTDOWN(-1, -1, 1),
	EAST(1, 0, 0),
	EASTUP(1, 0, 0),
	EASTDOWN(1, -1, 0),
	WEST(0, 0, 0),
	WESTUP(0, 1, 0),
	WESTDOWN(0, -1, 0),
	UP(0, 1, 0),
	DOWN(0, -1, 0);
	
	public static final TreeDirection[] VALUES = values();
	public final int xOffset;
	public final int yOffset;
	public final int zOffset;
	private final Vec3i offset;
	
	TreeDirection(int xOffset, int yOffset, int zOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
		this.offset = new Vec3i(xOffset, yOffset, zOffset);
	}
	public TreeDirection getOpposite() {
		return switch(this) {
			case NORTH         -> SOUTH;
			case NORTHUP       -> SOUTHDOWN;
			case NORTHDOWN     -> SOUTHUP;
			case NORTHEAST     -> SOUTHWEST;
			case NORTHEASTUP   -> SOUTHWESTDOWN;
			case NORTHEASTDOWN -> SOUTHWESTUP;
			case NORTHWEST     -> SOUTHEAST;
			case NORTHWESTUP   -> SOUTHEASTDOWN;
			case NORTHWESTDOWN -> SOUTHEASTUP;
			case SOUTH         -> NORTH;
			case SOUTHUP       -> NORTHDOWN;
			case SOUTHDOWN     -> NORTHUP;
			case SOUTHEAST     -> NORTHWEST;
			case SOUTHEASTUP   -> NORTHWESTDOWN;
			case SOUTHEASTDOWN -> NORTHWESTUP;
			case SOUTHWEST     -> NORTHEAST;
			case SOUTHWESTUP   -> NORTHEASTDOWN;
			case SOUTHWESTDOWN -> NORTHEASTUP;
			case EAST          -> WEST;
			case EASTUP        -> WESTDOWN;
			case EASTDOWN      -> WESTUP;
			case WEST          -> EAST;
			case WESTUP        -> EASTDOWN;
			case WESTDOWN      -> EASTUP;
			case UP            -> DOWN;
			case DOWN          -> UP;
		};
	}
	public Vec3i getOffset() {
		return this.offset;
	}
}

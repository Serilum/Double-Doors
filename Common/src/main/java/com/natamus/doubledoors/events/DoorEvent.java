package com.natamus.doubledoors.events;

import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.doubledoors.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class DoorEvent {
	private static final List<BlockPos> prevpoweredpos = new ArrayList<BlockPos>();
	private static final HashMap<BlockPos, Integer> prevbuttonpos = new HashMap<BlockPos, Integer>();
	
	public static void onNeighbourNotice(Level level, BlockPos blockPos, BlockState blockState, EnumSet<Direction> notifiedSides, boolean forceRedstoneUpdate) {
		if (level.isClientSide) {
			return;
		}
		
		BooleanProperty isPoweredProperty = BlockStateProperties.POWERED;
		IntegerProperty weightPowerProperty = BlockStateProperties.POWER;

		Block block = blockState.getBlock();

		if (!(block instanceof PressurePlateBlock) && !(block instanceof WeightedPressurePlateBlock)) {
			if (!(block instanceof ButtonBlock) && !(block instanceof LeverBlock)) {
				return;
			}
			else {
				if (prevbuttonpos.containsKey(blockPos)) {
					prevbuttonpos.remove(blockPos);
				}
				else {
					prevbuttonpos.put(blockPos.immutable(), 1);
					return;
				}

				if (!blockState.getValue(isPoweredProperty)) {
					if (!prevpoweredpos.contains(blockPos)) {
						return;
					}
					prevpoweredpos.remove(blockPos);
				}
			}
		}
		else if (block instanceof WeightedPressurePlateBlock) {
			if (blockState.getValue(weightPowerProperty) == 0) {
				if (!prevpoweredpos.contains(blockPos)) {
					return;
				}
			}
		}
		else {
			if (!blockState.getValue(isPoweredProperty)) {
				if (!prevpoweredpos.contains(blockPos)) {
					return;
				}
			}
		}

		boolean blockStateprop;
		if (block instanceof WeightedPressurePlateBlock) {
			blockStateprop = blockState.getValue(weightPowerProperty) > 0;
		}
		else {
			blockStateprop = blockState.getValue(isPoweredProperty);
		}

		int radius = 1;

		BlockPos doorBlockPos = null;

		for (BlockPos aroundPos : BlockPosFunctions.getBlocksAround(blockPos, false)) {
			BlockState oBlockState = level.getBlockState(aroundPos);
			if (Util.isDoorBlock(oBlockState)) {
				doorBlockPos = aroundPos.immutable();
				break;
			}
		}

		if (doorBlockPos == null) {
			for (BlockPos aroundPos : BlockPos.betweenClosed(blockPos.getX() - radius, blockPos.getY() - 1, blockPos.getZ() - radius, blockPos.getX() + radius, blockPos.getY() + 1, blockPos.getZ() + radius)) {
				BlockState oBlockState = level.getBlockState(aroundPos);
				if (Util.isDoorBlock(oBlockState)) {
					doorBlockPos = aroundPos;
					break;
				}
			}
		}


		if (doorBlockPos != null) {
			if (Util.processDoor(null, level, doorBlockPos, level.getBlockState(doorBlockPos), blockStateprop)) {
				if (blockStateprop) {
					prevpoweredpos.add(blockPos.immutable());
				}
			}
		}
	}
	
	public static void onDoorClick(Level level, Player player, InteractionHand interactionHand, BlockPos blockPos, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return;
		}

		if (!interactionHand.equals(InteractionHand.MAIN_HAND)) {
			return;
		}
		
		if (player.isCrouching()) {
			return;
		}
		
		BlockState clickstate = level.getBlockState(blockPos);

		if (!Util.isDoorBlock(clickstate)) {
			return;
		}

		if (!Util.canOpenByHand(level, blockPos, clickstate)) {
			return;
		}

		Util.processDoor(player, level, blockPos, clickstate, null);
	}
}
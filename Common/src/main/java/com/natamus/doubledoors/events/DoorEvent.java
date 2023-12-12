package com.natamus.doubledoors.events;

import com.natamus.doubledoors.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class DoorEvent {
	private static final List<BlockPos> prevpoweredpos = new ArrayList<BlockPos>();
	private static final HashMap<BlockPos, Integer> prevbuttonpos = new HashMap<BlockPos, Integer>();
	
	public static void onNeighbourNotice(Level world, BlockPos pos, BlockState state, EnumSet<Direction> notifiedSides, boolean forceRedstoneUpdate) {
		if (world.isClientSide) {
			return;
		}
		
		BooleanProperty proppowered = BlockStateProperties.POWERED;
		IntegerProperty weightedpower = BlockStateProperties.POWER;
		pos = pos.immutable();
		Block block = state.getBlock();

		if (!(block instanceof PressurePlateBlock) && !(block instanceof WeightedPressurePlateBlock)) {
			if (!(block instanceof ButtonBlock)) {
				return;
			}
			else {
				if (prevbuttonpos.containsKey(pos)) {
					prevbuttonpos.remove(pos);
				}
				else {
					prevbuttonpos.put(pos, 1);
					return;
				}

				if (!state.getValue(proppowered)) {
					if (!prevpoweredpos.contains(pos)) {
						return;
					}
					prevpoweredpos.remove(pos);
				}
			}
		}
		else if (block instanceof WeightedPressurePlateBlock) {
			if (state.getValue(weightedpower) == 0) {
				if (!prevpoweredpos.contains(pos)) {
					return;
				}
			}
		}
		else {
			if (!state.getValue(proppowered)) {
				if (!prevpoweredpos.contains(pos)) {
					return;
				}
			}
		}

		boolean stateprop;
		if (block instanceof WeightedPressurePlateBlock) {
			stateprop = state.getValue(weightedpower) > 0;
		}
		else {
			stateprop = state.getValue(proppowered);
		}

		int radius = block instanceof ButtonBlock ? 2 : 1;

		BlockPos doorpos = null;
		for (BlockPos npos : BlockPos.betweenClosed(pos.getX()-radius, pos.getY()-1, pos.getZ()-radius, pos.getX()+radius, pos.getY()+1, pos.getZ()+radius)) {
			BlockState ostate = world.getBlockState(npos);
			if (Util.isDoorBlock(ostate)) {
				doorpos = npos;
				break;
			}
		}

		if (doorpos != null) {
			if (Util.processDoor(null, world, doorpos, world.getBlockState(doorpos), stateprop, true)) {
				if (stateprop) {
					prevpoweredpos.add(pos);
				}
			}
		}
	}
	
	public static boolean onDoorClick(Level level, Player player, InteractionHand hand, BlockPos cpos, BlockHitResult hitVec) {
		if (level.isClientSide) {
			return true;
		}

		if (!hand.equals(InteractionHand.MAIN_HAND)) {
			return true;
		}
		
		if (player.isShiftKeyDown()) {
			return true;
		}
		
		BlockState clickstate = level.getBlockState(cpos);

		if (!Util.isDoorBlock(clickstate)) {
			return true;
		}
		if (clickstate.getMaterial().equals(Material.METAL)) {
			return true;
		}

		return !Util.processDoor(player, level, cpos, clickstate, null, false);
	}
}
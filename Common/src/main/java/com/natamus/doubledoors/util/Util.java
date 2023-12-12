package com.natamus.doubledoors.util;

import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.doubledoors.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Util {
	public static boolean isDoorBlock(BlockState blockstate) {
		Block block = blockstate.getBlock();
		return (block instanceof DoorBlock && ConfigHandler.enableDoors) || (block instanceof TrapDoorBlock && ConfigHandler.enableTrapdoors) || (block instanceof FenceGateBlock && ConfigHandler.enableFenceGates);
	}

	public static boolean isPressureBlock(BlockState blockstate) {
		Block block = blockstate.getBlock();
		if (block instanceof WeightedPressurePlateBlock) {
			return blockstate.getValue(BlockStateProperties.POWER) > 0;
		}
		if (block instanceof PressurePlateBlock || block instanceof ButtonBlock) {
			return blockstate.getValue(BlockStateProperties.POWERED);
		}
		return false;
	}

	public static boolean processDoor(Player player, Level level, BlockPos pos, BlockState state, Boolean isopen, boolean playsound) {
		Block block = state.getBlock();
		if (block instanceof DoorBlock) {
			if (state.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.UPPER)) {
				pos = pos.below().immutable();
				state = level.getBlockState(pos);
			}
		}

		if (isopen == null) {
			isopen = !state.getValue(BlockStateProperties.OPEN);
		}

		int yoffset = 0;
		if (!(block instanceof DoorBlock)) {
			yoffset = 1;
		}

		List<BlockPos> postoopen = recursivelyOpenDoors(new ArrayList<BlockPos>(Arrays.asList(pos.immutable())), new ArrayList<BlockPos>(), level, pos, pos, block, yoffset);
		if (postoopen.size() <= 1) {
			return false;
		}

		for (BlockPos toopen : postoopen) {
			BlockState ostate = level.getBlockState(toopen);
			Block oblock = ostate.getBlock();

			if (block instanceof DoorBlock) {
				if (!ConfigHandler.enableDoors) {
					continue;
				}

				DoorBlock door = (DoorBlock)oblock;

				if (playsound) {
					level.playSound(null, pos, isopen ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
					playsound = false;
				}

				level.setBlock(toopen, ostate.setValue(DoorBlock.OPEN, isopen), 10);
			}
			else if (block instanceof TrapDoorBlock) {
				if (!ConfigHandler.enableTrapdoors) {
					continue;
				}

				if (playsound) {
					if (isopen) {
						int i = ostate.getMaterial() == Material.METAL ? 1037 : 1007;
						level.levelEvent(null, i, pos, 0);
					} else {
						int j = ostate.getMaterial() == Material.METAL ? 1036 : 1013;
						level.levelEvent(null, j, pos, 0);
					}
					playsound = false;
				}

				level.setBlock(toopen, ostate.setValue(BlockStateProperties.OPEN, isopen), 10);
			}
			else if (block instanceof FenceGateBlock) {
				if (!ConfigHandler.enableFenceGates) {
					continue;
				}

				if (playsound) {
					level.playSound(null, pos, isopen ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
					playsound = false;
				}

				level.setBlock(toopen, ostate.setValue(DoorBlock.OPEN, isopen), 10);
			}
		}

		if (player != null) {
			player.swing(InteractionHand.MAIN_HAND);
		}

		return postoopen.size() > 1;
	}

	private static List<BlockPos> recursivelyOpenDoors(List<BlockPos> postoopen, List<BlockPos> ignoreoopen, Level level, BlockPos originalpos, BlockPos pos, Block block, int yoffset) {
		Iterator<BlockPos> blocksaround = BlockPos.betweenClosedStream(pos.getX()-1, pos.getY()-yoffset, pos.getZ()-1, pos.getX()+1, pos.getY()+yoffset, pos.getZ()+1).iterator();
		while (blocksaround.hasNext()) {
			BlockPos bpa = blocksaround.next();
			if (postoopen.contains(bpa)) {
				continue;
			}

			if (!BlockPosFunctions.withinDistance(originalpos, bpa, ConfigHandler.recursiveOpeningMaxBlocksDistance)) {
				continue;
			}

			BlockState ostate = level.getBlockState(bpa);
			Block oblock = ostate.getBlock();
			if (Util.isDoorBlock(ostate)) {
				if (oblock.getName().equals(block.getName())) {
					postoopen.add(bpa.immutable());

					if (ConfigHandler.enableRecursiveOpening) {
						recursivelyOpenDoors(postoopen, ignoreoopen, level, originalpos, bpa, block, yoffset);
					}
					continue;
				}
			}
			
			ignoreoopen.add(bpa.immutable());
		}
		
		return postoopen;
	}
}

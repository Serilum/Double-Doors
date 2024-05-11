package com.natamus.doubledoors.util;

import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.services.Services;
import com.natamus.doubledoors.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Util {
	public static boolean isDoorBlock(BlockState blockState) {
		Block block = blockState.getBlock();
		return (block instanceof DoorBlock && ConfigHandler.enableDoors) || (block instanceof TrapDoorBlock && ConfigHandler.enableTrapdoors) || (block instanceof FenceGateBlock && ConfigHandler.enableFenceGates);
	}

	public static boolean isPressureBlock(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof WeightedPressurePlateBlock) {
			return blockState.getValue(BlockStateProperties.POWER) > 0;
		}
		if (block instanceof PressurePlateBlock || block instanceof ButtonBlock) {
			return blockState.getValue(BlockStateProperties.POWERED);
		}
		return false;
	}

	public static boolean processDoor(Player player, Level level, BlockPos blockPos, BlockState blockState, Boolean isOpen) {
		Block block = blockState.getBlock();
		if (block instanceof DoorBlock) {
			if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.UPPER)) {
				blockPos = blockPos.below().immutable();
				blockState = level.getBlockState(blockPos);
			}
		}
		
		if (isOpen == null) {
			isOpen = blockState.getValue(BlockStateProperties.OPEN);
		}
		
		int yOffset = 0;
		if (!(block instanceof DoorBlock)) {
			yOffset = 1;
		}
		
		List<BlockPos> posToOpenList = recursivelyOpenDoors(new ArrayList<BlockPos>(Arrays.asList(blockPos.immutable())), new ArrayList<BlockPos>(), level, blockPos, blockPos, block, yOffset);
		if (posToOpenList.size() <= 1) {
			return false;
		}

		for (BlockPos toOpenBlockPos : posToOpenList) {
			if (toOpenBlockPos.equals(blockPos)) {
				continue;
			}

			BlockState oBlockState = level.getBlockState(toOpenBlockPos);
			Block oBlock = oBlockState.getBlock();
			
			if (block instanceof DoorBlock) {
				if (!ConfigHandler.enableDoors) {
					continue;
				}

				level.setBlock(toOpenBlockPos, oBlockState.setValue(DoorBlock.OPEN, isOpen), 10);
			}
			else if (block instanceof TrapDoorBlock) {
				if (!ConfigHandler.enableTrapdoors) {
					continue;
				}

				level.setBlock(toOpenBlockPos, oBlockState.setValue(BlockStateProperties.OPEN, isOpen), 10);
			}
			else if (block instanceof FenceGateBlock) {
				if (!ConfigHandler.enableFenceGates) {
					continue;
				}
				
				Direction facing = blockState.getValue(FenceGateBlock.FACING);
				level.setBlock(toOpenBlockPos, oBlockState.setValue(DoorBlock.OPEN, isOpen).setValue(FenceGateBlock.FACING, facing), 10);
			}
		}

		if (player != null) {
			player.swing(InteractionHand.MAIN_HAND);
		}

		return posToOpenList.size() > 1;
	}
	
	private static List<BlockPos> recursivelyOpenDoors(List<BlockPos> posToOpenList, List<BlockPos> ignoreOpenList, Level level, BlockPos originalBlockPos, BlockPos blockPos, Block block, int yOffset) {
		Iterator<BlockPos> blocksaround = BlockPos.betweenClosedStream(blockPos.getX()-1, blockPos.getY()-yOffset, blockPos.getZ()-1, blockPos.getX()+1, blockPos.getY()+yOffset, blockPos.getZ()+1).iterator();
		while (blocksaround.hasNext()) {
			BlockPos bpa = blocksaround.next();
			if (posToOpenList.contains(bpa)) {
				continue;
			}
			
			if (!BlockPosFunctions.withinDistance(originalBlockPos, bpa, ConfigHandler.recursiveOpeningMaxBlocksDistance)) {
				continue;
			}
			
			BlockState oBlockState = level.getBlockState(bpa);
			Block oBlock = oBlockState.getBlock();
			if (Util.isDoorBlock(oBlockState)) {
				if (oBlock.getName().equals(block.getName())) {
					posToOpenList.add(bpa.immutable());
					
					if (ConfigHandler.enableRecursiveOpening) {
						recursivelyOpenDoors(posToOpenList, ignoreOpenList, level, originalBlockPos, bpa, block, yOffset);
					}
					continue;
				}
			}
			
			ignoreOpenList.add(bpa.immutable());
		}
		
		return posToOpenList;
	}


	public static void checkForOtherModdedDoubleDoorFunctionality() {
		if (!ConfigHandler.enableModIncompatibilityCheck) {
			return;
		}

		try {
			if (Services.MODLOADER.isModLoaded("quark")) {
				String quarkConfigPath = DataFunctions.getConfigDirectory() + File.separator + "quark-common.toml";
				File quarkConfig = new File(quarkConfigPath);

				if (quarkConfig.exists()) {
					List<String> doorConfigKeys = Arrays.asList("\"Double Door Opening\"", "\"Enable Doors\"", "\"Enable Fence Gates\"");
					String quarkConfigContent = Files.readString(Path.of(quarkConfigPath), StandardCharsets.UTF_8);

					boolean writeOutput = false;

					StringBuilder output = new StringBuilder();
					for (String line : quarkConfigContent.split("\n")) {
						if (output.length() > 0) {
							output.append("\n");
						}

						for (String doorConfigKey : doorConfigKeys) {
							if (line.contains(doorConfigKey)) {
								if (line.contains("true")) {
									line = line.replace("true", "false");
									writeOutput = true;
									break;
								}
							}
						}

						output.append(line);
					}

					if (writeOutput) {
						try (PrintWriter printWriter = new PrintWriter(quarkConfigPath)) {
							printWriter.println(output);
						}
					}
				}
				else {
					String quarkConfigIfNewInstance = "[tweaks]\n	\"Double Door Opening\" = false\n\n	[tweaks.double_door_opening]\n		\"Enable Doors\" = false\n		\"Enable Fence Gates\" = false\n		\"Ignore Anti Overlap\" = false";

					try (PrintWriter printWriter = new PrintWriter(quarkConfigPath)) {
						printWriter.println(quarkConfigIfNewInstance);
					}
				}
			}
		}
		catch (Exception ignored) { }
	}
}

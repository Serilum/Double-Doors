package com.natamus.doubledoors.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.doubledoors.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

	@Entry public static boolean enableRecursiveOpening = true;
	@Entry(min = 1, max = 64) public static int recursiveOpeningMaxBlocksDistance = 10;
	@Entry public static boolean enableDoors = true;
	@Entry public static boolean enableFenceGates = true;
	@Entry public static boolean enableTrapdoors = true;

	public static void initConfig() {
		configMetaData.put("enableRecursiveOpening", Arrays.asList(
			"Whether the recursive opening feature should be enabled. This allows you to for example build a giant door with trapdoors which will all open at the same time, as long as they are connected. The 'recursiveOpeningMaxBlocksDistance' config option determines how far the function should search."
		));
		configMetaData.put("recursiveOpeningMaxBlocksDistance", Arrays.asList(
			"How many blocks the recursive function should search when 'enableRecursiveOpening' is enabled."
		));
		configMetaData.put("enableDoors", Arrays.asList(
			"When enables, the mod works with double doors."
		));
		configMetaData.put("enableFenceGates", Arrays.asList(
			"When enables, the mod works with double fence gates."
		));
		configMetaData.put("enableTrapdoors", Arrays.asList(
			"When enables, the mod works with double trapdoors."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}
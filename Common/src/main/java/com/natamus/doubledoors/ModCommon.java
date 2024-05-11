package com.natamus.doubledoors;

import com.natamus.doubledoors.config.ConfigHandler;
import com.natamus.doubledoors.util.Util;

public class ModCommon {

	public static void init() {
		Util.checkForOtherModdedDoubleDoorFunctionality();

		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		
	}
}
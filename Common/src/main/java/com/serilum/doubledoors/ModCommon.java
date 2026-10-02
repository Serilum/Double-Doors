package com.serilum.doubledoors;

import com.serilum.doubledoors.config.ConfigHandler;
import com.serilum.doubledoors.util.Util;

public class ModCommon {

	public static void init() {
		Util.checkForOtherModdedDoubleDoorFunctionality();

		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		
	}
}
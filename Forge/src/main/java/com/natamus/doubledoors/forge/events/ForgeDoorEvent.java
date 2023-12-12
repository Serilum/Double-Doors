package com.natamus.doubledoors.forge.events;

import com.natamus.collective.functions.WorldFunctions;
import com.natamus.doubledoors.events.DoorEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeDoorEvent {
	@SubscribeEvent
	public void onNeighbourNotice(BlockEvent.NeighborNotifyEvent e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getWorld());
		if (level == null) {
			return;
		}

		DoorEvent.onNeighbourNotice(level, e.getPos(), e.getState(), e.getNotifiedSides(), e.getForceRedstoneUpdate());
	}

	@SubscribeEvent
	public void onDoorClick(PlayerInteractEvent.RightClickBlock e) {
		if (!DoorEvent.onDoorClick(e.getWorld(), e.getPlayer(), e.getHand(), e.getPos(), e.getHitVec())) {
			e.setCancellationResult(InteractionResult.SUCCESS);
			e.setCanceled(true);
		}
	}
}
package com.natamus.doubledoors.mixin;

import com.natamus.doubledoors.events.DoorEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrapDoorBlock.class, priority = 1001)
public class TrapDoorBlockMixin {
	@Shadow private @Final BlockSetType type;

	@Inject(method = "useWithoutItem", at = @At(value = "RETURN"))
	public void use(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
		if (this.type.canOpenByHand()) {
			DoorEvent.onDoorClick(level, player, InteractionHand.MAIN_HAND, blockPos, blockHitResult);
		}
	}
}

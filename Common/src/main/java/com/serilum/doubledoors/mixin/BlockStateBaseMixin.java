package com.serilum.doubledoors.mixin;

import com.serilum.doubledoors.events.DoorEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockBehaviour.BlockStateBase.class, priority = 1001)
public class BlockStateBaseMixin {
	@Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "RETURN"))
	public void use(Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
		BlockState previousBlockState = (BlockState)(Object)this;
		if (!previousBlockState.hasProperty(BlockStateProperties.OPEN)) {
			return;
		}

		BlockPos blockPos = blockHitResult.getBlockPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (!blockState.hasProperty(BlockStateProperties.OPEN)) {
			return;
		}

		if (blockState.getValue(BlockStateProperties.OPEN).equals(previousBlockState.getValue(BlockStateProperties.OPEN))) {
			return;
		}

		DoorEvent.onDoorClick(level, player, interactionHand, blockPos, blockHitResult);
	}
}

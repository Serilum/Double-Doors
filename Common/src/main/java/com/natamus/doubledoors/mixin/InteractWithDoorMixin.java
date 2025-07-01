package com.natamus.doubledoors.mixin;

import com.mojang.datafixers.kinds.OptionalBox;
import com.natamus.doubledoors.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.Node;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(value = InteractWithDoor.class, priority = 1001)
public class InteractWithDoorMixin {
    @Inject(method = "closeDoorsThatIHaveOpenedOrPassedThrough(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/pathfinder/Node;Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Set;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DoorBlock;setOpen(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Z)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void closeDoorsThatIHaveOpenedOrPassedThrough(ServerLevel serverLevel, LivingEntity $$1, Node $$2, Node $$3, Set<GlobalPos> $$4, Optional<List<LivingEntity>> $$5, CallbackInfo ci, Iterator<?> $$6, GlobalPos $$7, BlockPos blockPos, BlockState blockState, DoorBlock $$10) {
        Util.processDoor(null, serverLevel, blockPos.immutable(), blockState, !blockState.getValue(BlockStateProperties.OPEN));
    }

    @Inject(method = "rememberDoorToClose(Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Ljava/util/Optional;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Ljava/util/Optional;", at = @At(value = "HEAD"))
    private static void rememberDoorToClose(MemoryAccessor<OptionalBox.Mu, Set<GlobalPos>> $$0, Optional<Set<GlobalPos>> $$1, ServerLevel serverLevel, BlockPos blockPos, CallbackInfoReturnable<Optional<Set<GlobalPos>>> cir) {
        Util.processDoor(null, serverLevel, blockPos, serverLevel.getBlockState(blockPos), null);
    }
}

package com.astralis.bundledarrows.mixin;

import com.astralis.bundledarrows.BundledArrowsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void onBowUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		ItemStack bowStack = user.getStackInHand(hand);
		ItemStack arrowStack = user.getProjectileType(bowStack);

		// Si no tiene flechas normales, buscar en bundles
		if (arrowStack.isEmpty()) {
			ItemStack bundleArrow = BundledArrowsMod.findArrowInBundle(user);

			if (!bundleArrow.isEmpty()) {
				// Permitir usar el arco
				user.setCurrentHand(hand);
				cir.setReturnValue(ActionResult.CONSUME);
			}
		}
	}
}
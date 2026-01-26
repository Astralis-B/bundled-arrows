package com.astralis.bundledarrows.mixin;

import com.astralis.bundledarrows.BundledArrowsMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedWeaponItem.class)
public class PlayerEntityMixin {

    @Inject(
            method = "getProjectile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;",
                    shift = At.Shift.BEFORE
            )
    )
    private static void beforeSplitProjectile(ItemStack stack, ItemStack projectileStack, LivingEntity shooter, boolean multishot, CallbackInfoReturnable<ItemStack> cir) {
        if (shooter instanceof PlayerEntity player && !player.getAbilities().creativeMode) {

            // Usar el contador optimizado
            int arrowCount = BundledArrowsMod.countArrowsInInventory(player);

            // Solo consumir del bundle si no hay flechas en el inventario
            if (arrowCount == 0) {
                ItemStack bundleArrow = BundledArrowsMod.findArrowInBundle(player);

                if (!bundleArrow.isEmpty()) {
                    boolean hasInfinity = stack.hasEnchantments() &&
                            stack.getEnchantments().getEnchantments().stream()
                                    .anyMatch(entry -> entry.getIdAsString().contains("infinity"));

                    boolean isNormalArrow = bundleArrow.getItem().toString().equals("arrow");

                    if (!hasInfinity || !isNormalArrow) {
                        BundledArrowsMod.consumeArrowFromBundle(player);
                    }
                }
            }
        }
    }
}
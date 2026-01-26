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

import java.util.function.Predicate;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {

    @Inject(method = "getProjectile", at = @At("RETURN"), cancellable = true)
    private static void getProjectileFromBundle(ItemStack weaponStack, ItemStack projectileStack, LivingEntity shooter, boolean multishot, CallbackInfoReturnable<ItemStack> cir) {
        if (cir.getReturnValue().isEmpty() && shooter instanceof PlayerEntity player) {

            // Usar el contador optimizado en lugar de iterar de nuevo
            int arrowCount = BundledArrowsMod.countArrowsInInventory(player);

            // Solo si NO hay flechas en inventario, buscar en bundle (usa caché)
            if (arrowCount == 0) {
                ItemStack bundleArrow = BundledArrowsMod.findArrowInBundle(player);

                if (!bundleArrow.isEmpty()) {
                    cir.setReturnValue(bundleArrow.copy());
                }
            }
        }
    }

    @Inject(method = "getHeldProjectile", at = @At("RETURN"), cancellable = true)
    private static void getHeldProjectileFromBundle(LivingEntity entity, Predicate<ItemStack> predicate, CallbackInfoReturnable<ItemStack> cir) {
        if (cir.getReturnValue().isEmpty() && entity instanceof PlayerEntity player) {

            // Usar el contador optimizado
            int arrowCount = BundledArrowsMod.countArrowsInInventory(player);

            // Solo si NO hay flechas en inventario, buscar en bundle (usa caché)
            if (arrowCount == 0) {
                ItemStack bundleArrow = BundledArrowsMod.findArrowInBundle(player);

                if (!bundleArrow.isEmpty() && predicate.test(bundleArrow)) {
                    cir.setReturnValue(bundleArrow.copy());
                }
            }
        }
    }
}
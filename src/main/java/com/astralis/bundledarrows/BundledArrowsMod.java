package com.astralis.bundledarrows;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BundledArrowsMod implements ModInitializer {
	public static final String MOD_ID = "bundledarrows";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Caché para almacenar información de bundles por jugador
	private static final Map<UUID, BundleCache> bundleCacheMap = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info("Bundle Arrows Mod inicializado!");
	}

	/**
	 * Clase para cachear información del bundle
	 */
	private static class BundleCache {
		int bundleSlot = -1;
		ItemStack arrowStack = ItemStack.EMPTY;
		long timestamp = 0;

		boolean isValid() {
			// El caché es válido por 50ms (1 tick de juego)
			return bundleSlot != -1 && System.currentTimeMillis() - timestamp < 50;
		}

		void invalidate() {
			bundleSlot = -1;
			arrowStack = ItemStack.EMPTY;
			timestamp = 0;
		}
	}

	/**
	 * Invalida el caché de un jugador
	 */
	public static void invalidateCache(PlayerEntity player) {
		bundleCacheMap.remove(player.getUuid());
	}

	/**
	 * Obtiene o crea el caché para un jugador
	 */
	private static BundleCache getCache(PlayerEntity player) {
		return bundleCacheMap.computeIfAbsent(player.getUuid(), k -> new BundleCache());
	}

	/**
	 * Busca flechas en bundles y cachea el resultado
	 */
	public static ItemStack findArrowInBundle(PlayerEntity player) {
		BundleCache cache = getCache(player);

		// Si el caché es válido, usar el resultado cacheado
		if (cache.isValid()) {
			return cache.arrowStack.copy();
		}

		// Caché inválido, buscar de nuevo
		cache.invalidate();

		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);

			if (stack.getItem().toString().contains("bundle")) {
				BundleContentsComponent bundleContents = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);

				if (!bundleContents.isEmpty()) {
					for (ItemStack bundleItem : bundleContents.iterate()) {
						if (bundleItem.getItem() instanceof ArrowItem) {
							// Cachear el resultado
							cache.bundleSlot = i;
							cache.arrowStack = bundleItem.copy();
							cache.timestamp = System.currentTimeMillis();
							return cache.arrowStack.copy();
						}
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	/**
	 * Consume una flecha usando el caché
	 */
	public static boolean consumeArrowFromBundle(PlayerEntity player) {
		BundleCache cache = getCache(player);

		// Si tenemos caché válido, usar el slot cacheado
		int startSlot = cache.isValid() ? cache.bundleSlot : 0;
		int endSlot = cache.isValid() ? cache.bundleSlot + 1 : player.getInventory().size();

		// Buscar solo en el slot cacheado si es válido, sino buscar en todo
		for (int i = startSlot; i < endSlot; i++) {
			ItemStack stack = player.getInventory().getStack(i);

			if (stack.getItem().toString().contains("bundle")) {
				BundleContentsComponent bundleContents = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);

				if (!bundleContents.isEmpty()) {
					for (ItemStack bundleItem : bundleContents.iterate()) {
						if (bundleItem.getItem() instanceof ArrowItem) {
							BundleContentsComponent newContents = removeOneArrowFromBundle(bundleContents);
							stack.set(DataComponentTypes.BUNDLE_CONTENTS, newContents);

							// Invalidar caché después de consumir
							cache.invalidate();

							return true;
						}
					}
				}
			}
		}

		// Si no encontró con caché, buscar en todo el inventario (fallback)
		if (cache.isValid()) {
			cache.invalidate();
			return consumeArrowFromBundle(player);
		}

		return false;
	}

	/**
	 * Cuenta las flechas en el inventario de forma optimizada
	 */
	public static int countArrowsInInventory(PlayerEntity player) {
		int count = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack invStack = player.getInventory().getStack(i);
			if (invStack.getItem() instanceof ArrowItem) {
				count += invStack.getCount();
			}
		}
		return count;
	}

	/**
	 * Reconstruye el contenido del bundle removiendo una flecha y preservando el orden
	 */
	private static BundleContentsComponent removeOneArrowFromBundle(BundleContentsComponent original) {
		List<ItemStack> items = new ArrayList<>();
		for (ItemStack item : original.iterate()) {
			items.add(item.copy());
		}

		boolean arrowRemoved = false;
		for (int i = 0; i < items.size(); i++) {
			ItemStack item = items.get(i);
			if (!arrowRemoved && item.getItem() instanceof ArrowItem) {
				if (item.getCount() > 1) {
					item.decrement(1);
				} else {
					items.remove(i);
				}
				arrowRemoved = true;
				break;
			}
		}

		java.util.Collections.reverse(items);

		BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(BundleContentsComponent.DEFAULT);
		for (ItemStack item : items) {
			if (!item.isEmpty()) {
				builder.add(item);
			}
		}

		return builder.build();
	}
}
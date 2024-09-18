package io.github.nattocb.treasure_seas.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.shop.gui.FishShopInventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;


public class ModContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, TreasureSeas.MOD_ID);

    public static final RegistryObject<MenuType<FishShopInventory>> FISH_SHOP_CONTAINER = CONTAINERS.register("fish_shop_container",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FishShopInventory(windowId, inv)));

}
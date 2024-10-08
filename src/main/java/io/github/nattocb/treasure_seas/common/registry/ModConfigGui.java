package io.github.nattocb.treasure_seas.common.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.gui.screen.ModConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModConfigGui {

    /**
     * 注册主菜单内的配置编辑页面
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, parentScreen) ->
                        new ModConfigScreen(parentScreen)));
    }

}

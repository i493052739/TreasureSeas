package io.github.nattocb.treasure_seas.core.eventsubscriber;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.RewardType;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.core.packet.FishFightPacket;
import io.github.nattocb.treasure_seas.core.packet.PacketHandler;
import io.github.nattocb.treasure_seas.core.utility.FishUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main event handler for fish fighter
 */
@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FishingRodHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFished(ItemFishedEvent event) {
        // pre-check
        if (event.getHookEntity() == null) return;
        Player player = event.getPlayer();
        if (player == null) return;
        int enchantmentLevel = FishUtils.getFishFighterRodEnchantLevel(player);
        if (enchantmentLevel == 0) return;

        // get the matched (weighted-random) fish
        FishWrapper chosenFish = findMatchedFish(event, player, enchantmentLevel);

        // start the fishing game
        startGame(event, player, chosenFish);
    }

    @NotNull
    private static FishWrapper findMatchedFish(ItemFishedEvent event, Player player, int enchantmentLevel) {
        Level world = player.level;
        String worldName = world.dimension().location().getPath();
        FishWrapper.AllowedWeather currentWeather = FishUtils.getCurrentWeatherEnum(world);
        String biomeFullName = FishUtils.getBiomeFullName(event, world);
        FishWrapper.AllowedTime currentTime = FishUtils.getCurrentTimeEnum(world);

        int waterDepth = FishUtils.calculateFluidDepth(event.getHookEntity().getOnPos(), event.getHookEntity().getLevel());
        int depthCapacity = FishUtils.getRodDepthCapacity(FishUtils.getFishFighterRodEnchantLevel(player));
        depthCapacity = Math.min(waterDepth, depthCapacity);
        // if fetched userPreferred depth successfully, use the one user preferred
        ItemStack fishingRod = FishUtils.getFishRodItemFromInv(player);
        int userPreferredDepth = 0;
        if (fishingRod != null) {
            CompoundTag nbtData = fishingRod.getOrCreateTag();
            userPreferredDepth = nbtData.getInt("preferredDepth");
            userPreferredDepth = Math.min(waterDepth, userPreferredDepth);
        }
        depthCapacity = userPreferredDepth == 0 ? depthCapacity : userPreferredDepth;
        int finalDepth = depthCapacity;

        BlockPos hookPos = event.getHookEntity().blockPosition();
        boolean isCave = FishUtils.isCave(world, hookPos);

        RewardType rewardType = FishUtils.getRandomRewardType(player, world, hookPos, enchantmentLevel);
        TreasureSeas.getLogger().dev("configs count: " + TreasureSeas.getInstance().getConfigManager().getFishConfigs().size());
        TreasureSeas.getLogger().dev("biomeFullName:{}, worldName:{}, currentWeather:{}, currentTime:{}, " +
                        "enchantmentLevel:{}, maxDepthAllowed:{}, isCave:{}",
                biomeFullName, worldName, currentWeather, currentTime, enchantmentLevel, finalDepth, isCave);
        List<FishWrapper> matchingFishes = switch (rewardType) {
            case JUNK -> TreasureSeas.getInstance().getConfigManager().getFishConfigs().stream()
                    .filter(fish -> fish.matches(biomeFullName, worldName, currentWeather, currentTime, enchantmentLevel,
                            finalDepth, isCave, true, false, false))
                    .toList();
            case TREASURE -> TreasureSeas.getInstance().getConfigManager().getFishConfigs().stream()
                    .filter(fish -> fish.matches(biomeFullName, worldName, currentWeather, currentTime, enchantmentLevel,
                            finalDepth, isCave, false, true, false))
                    .toList();
            case ULTIMATE_TREASURE -> TreasureSeas.getInstance().getConfigManager().getFishConfigs().stream()
                    .filter(fish -> fish.matches(biomeFullName, worldName, currentWeather, currentTime, enchantmentLevel,
                            finalDepth, isCave, false, false, true))
                    .toList();
            // FISH
            default -> TreasureSeas.getInstance().getConfigManager().getFishConfigs().stream()
                    .filter(fish ->
                            {
                                TreasureSeas.getLogger().dev("try matching fish: " + fish.getModNamespace() + ":" + fish.getFishItemName());
                                return fish.matches(biomeFullName, worldName, currentWeather, currentTime, enchantmentLevel,
                                        finalDepth, isCave, false, false, false);
                            }
                    )
                    .toList();
        };
        FishWrapper chosenFish = FishUtils.chooseFishBySampleWeight(player, matchingFishes);
        return chosenFish;
    }

    private static void startGame(ItemFishedEvent event, Player player, FishWrapper chosenFish) {
        event.setCanceled(true);
        PacketHandler.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> (ServerPlayer) player),
                new FishFightPacket(
                        event.getHookEntity().position(),
                        chosenFish
                )
        );
    }

}

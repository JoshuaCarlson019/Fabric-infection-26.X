package net.hmmer.infection.items.custom;

import net.hmmer.infection.Infection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class OpenBagItem extends Item {

    private final ResourceKey<LootTable> lootTable;

    public OpenBagItem(Properties properties, String lootTableName) {
        super(properties);
        lootTable = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(Infection.MOD_ID, lootTableName));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);

        // only runs this code on the server side, client side is for animations
        if (level instanceof ServerLevel serverLevel) {
            LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(lootTable);

            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.GIFT);

            for (ItemStack reward : table.getRandomItems(params)) {
                // put item in inventory or drop it at their feet if their inventory is full
                if (!player.getInventory().add(reward)) {
                    player.drop(reward, false);
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 1.0F);

            player.awardStat(Stats.ITEM_USED.get(this));
            bag.consume(1, player); // removes one bag (doesn't consume in creative mode)
        }

        return InteractionResult.SUCCESS;
    }
}

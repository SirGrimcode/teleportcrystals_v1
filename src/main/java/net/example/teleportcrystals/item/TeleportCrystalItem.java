package net.example.teleportcrystals.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * Shared logic for both teleport crystals, ported from Yarn/1.21.1 to
 * Mojang mappings for Minecraft 26.3.
 *   - Sneak + use  -> bind the current position (and dimension).
 *   - Use          -> teleport to the bound position, then let the
 *                      subclass apply its "cost" (consume, damage, etc).
 *
 * Dropped from the 1.21.1 version: the custom tooltip (appendHoverText's
 * signature is in flux and it's deprecated in favor of a components-based
 * approach) and the custom enchant-table strength override. Neither
 * affects binding/teleporting/durability/Unbreaking-Mending working; they
 * were just polish. Ask if you'd like them added back in.
 */
public abstract class TeleportCrystalItem extends Item {

    public TeleportCrystalItem(Properties properties) {
        super(properties);
    }

    /** Applies the item's cost after a successful teleport (consume, damage, etc). */
    protected abstract void onUsed(ItemStack stack, ServerPlayer player);

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (player.isShiftKeyDown()) {
            TeleportData.save(stack, player.blockPosition(), level.dimension());
            String dimName = DimensionColor.displayNameFor(level.dimension());
            player.sendSystemMessage(Component.literal("Location bound in the " + dimName + "."));
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 1.0F, 1.4F);
            return InteractionResultHolder.success(stack);
        }

        TeleportData.Saved saved = TeleportData.read(stack);
        if (saved == null) {
            player.sendSystemMessage(Component.literal("No location bound. Sneak + use to bind one first."));
            return InteractionResultHolder.fail(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel destination = serverPlayer.getServer().getLevel(saved.dimension());
        if (destination == null) {
            serverPlayer.sendSystemMessage(Component.literal("That destination no longer exists."));
            return InteractionResultHolder.fail(stack);
        }

        BlockPos pos = saved.pos();
        BlockPos fromPos = serverPlayer.blockPosition();
        Level fromLevel = level;

        serverPlayer.teleportTo(destination, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.<RelativeMovement>of(), serverPlayer.getYRot(), serverPlayer.getXRot());

        fromLevel.playSound(null, fromPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        destination.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        onUsed(stack, serverPlayer);

        return InteractionResultHolder.success(stack);
    }
}

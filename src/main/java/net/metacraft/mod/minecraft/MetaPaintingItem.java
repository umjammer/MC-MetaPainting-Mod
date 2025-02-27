package net.metacraft.mod.minecraft;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import static net.metacraft.mod.MetaPaintingMod.LOGGER;

public class MetaPaintingItem extends Item {

    /** PNG raw data */
    private byte[] colors;

    public MetaPaintingItem(EntityType<? extends AbstractDecorationEntity> type, Settings settings) {
        super(settings);
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        colors = getColors(itemStack);
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            AbstractDecorationEntity painting;
            LOGGER.info("MetaPaintingItem::useOnBlock: colors: " + colors.length);
            painting = new MetaPaintingEntity(world, blockPos2, direction, colors);
            NbtCompound nbtCompound = itemStack.getNbt();
            if (nbtCompound != null) {
                EntityType.loadFromEntityNbt(world, playerEntity, painting, nbtCompound);
            }

            if (painting.canStayAttached()) {
                if (!world.isClient) {
                    painting.onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, blockPos);
                    world.spawnEntity(painting);
                }

                itemStack.decrement(1);
                return ActionResult.success(world.isClient);
            } else {
                return ActionResult.CONSUME;
            }
        }
    }

    private byte[] getColors(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getNbt();
        if (nbt == null) {
            LOGGER.info("MetaPaintingItem::getColors: get nbt is null");
            return null;
        }
        return nbt.getByteArray("colors");
    }

    public void saveColor(ItemStack itemStack, byte[] colors) {
        itemStack.getOrCreateNbt().putByteArray("colors", colors);
        this.colors = colors;
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }
}
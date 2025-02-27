package net.metacraft.mod.minecraft;

import com.google.common.collect.Lists;
import net.metacraft.mod.MetaPaintingMod;
import net.metacraft.mod.model.MetaPaintingSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

import static net.metacraft.mod.MetaPaintingMod.LOGGER;

public class MetaPaintingEntity extends AbstractDecorationEntity {

    /** PNG raw image */
    private byte[] colors;

    private static final TrackedData<ItemStack> ITEM_STACK;

    public PaintingMotive motive;

    static {
        ITEM_STACK = DataTracker.registerData(MetaPaintingEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    }

    protected void initDataTracker() {
        this.getDataTracker().startTracking(ITEM_STACK, ItemStack.EMPTY);
    }

    public MetaPaintingEntity(EntityType<? extends MetaPaintingEntity> entityType, World world) {
        super(MetaPaintingMod.ENTITY_TYPE_META_PAINTING, world);
    }

    MetaPaintingEntity(World world, BlockPos pos, Direction direction, byte[] colors) {
        super(MetaPaintingMod.ENTITY_TYPE_META_PAINTING, world, pos);
        setColors(colors);
        List<PaintingMotive> list = Lists.newArrayList();
        int i = 0;

        for (PaintingMotive paintingMotive2 : Registry.PAINTING_MOTIVE) {
            this.motive = paintingMotive2;
            this.setFacing(direction);
            if (this.canStayAttached()) {
                list.add(paintingMotive2);
                int j = paintingMotive2.getWidth() * paintingMotive2.getHeight();
                if (j > i) {
                    i = j;
                }
            }
        }

        if (!list.isEmpty()) {
            Iterator<PaintingMotive> iterator = list.iterator();

            while (iterator.hasNext()) {
                PaintingMotive paintingMotive2 = iterator.next();
                if (paintingMotive2.getWidth() * paintingMotive2.getHeight() < i) {
                    iterator.remove();
                }
            }

            this.motive = list.get(this.random.nextInt(list.size()));
        }

        this.setFacing(direction);
        setHeldItemStack(getAsItemStack());
    }

    public byte[] getColors() {
        return colors;
    }

    public void setColors(byte[] colors) {
        this.colors = colors;
    }

    public void setAttachmentPos(BlockPos pos) {
        this.attachmentPos = pos;
    }

    public void setFacing(Direction direction) {
        super.setFacing(direction);
    }

    public void setMotive(PaintingMotive motive) {
        this.motive = motive;
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Motive", Registry.PAINTING_MOTIVE.getId(this.motive).toString());
        nbt.putByte("Facing", (byte) this.facing.getHorizontal());
        nbt.putByteArray("mapColors", colors);
        if (!this.getHeldItemStack().isEmpty()) {
            nbt.put("Item", this.getHeldItemStack().writeNbt(new NbtCompound()));
        }
        super.writeCustomDataToNbt(nbt);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.motive = Registry.PAINTING_MOTIVE.get(Identifier.tryParse(nbt.getString("Motive")));
        this.facing = Direction.fromHorizontal(nbt.getByte("Facing"));
        this.colors = nbt.getByteArray("mapColors");
        NbtCompound nbtCompound = nbt.getCompound("Item");
        if (nbtCompound != null && !nbtCompound.isEmpty()) {
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
            this.setHeldItemStack(itemStack);
        }
        setColors(colors);
        super.readCustomDataFromNbt(nbt);
        this.setFacing(this.facing);
    }

    public int getWidthPixels() {
        return this.motive == null ? 1 : this.motive.getWidth();
    }

    public int getHeightPixels() {
        return this.motive == null ? 1 : this.motive.getHeight();
    }

    private ItemStack getHeldItemStack() {
        return this.getDataTracker().get(ITEM_STACK);
    }

    private void setHeldItemStack(ItemStack value) {
        if (!value.isEmpty()) {
            value = value.copy();
            value.setCount(1);
            value.setHolder(this);
            value.getOrCreateNbt().putByteArray("colors", colors);
        }

        this.getDataTracker().set(ITEM_STACK, value);
    }

    public void onBreak(@Nullable Entity entity) {
        LOGGER.info("MetaPaintingEntity::onBreak");
        ItemStack itemStack = this.getHeldItemStack();
        this.setHeldItemStack(ItemStack.EMPTY);
        if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof PlayerEntity playerEntity) {
                if (playerEntity.getAbilities().creativeMode) {
                    return;
                }
            }
            this.dropStack(itemStack);
        }
    }

    public void onPlace() {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
    }

    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.setPosition(x, y, z);
    }

    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        BlockPos blockPos = this.attachmentPos.add(x - this.getX(), y - this.getY(), z - this.getZ());
        this.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return MetaPaintingMod.newPacket(MetaPaintingSpawnPacket.IDENTIFIER, new MetaPaintingSpawnPacket(this).toBuffer());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        BlockPos pos = ((MetaPaintingSpawnPacket) packet).getPos();
        this.attachmentPos = ((MetaPaintingSpawnPacket) packet).getPos();
        setFacing(((MetaPaintingSpawnPacket) packet).getFacing());
        motive = ((MetaPaintingSpawnPacket) packet).getMotive();
        LOGGER.info("MetaPaintingEntity::onSpawnPacket: " + motive);
        double d = pos.getX();
        double e = pos.getY();
        double f = pos.getZ();
        this.setPosition(d, e, f);
        this.setId(packet.getId());
        this.setUuid(packet.getUuid());
        this.setColors(((MetaPaintingSpawnPacket) packet).getColors());
    }

    protected ItemStack getAsItemStack() {
        return new ItemStack(MetaPaintingMod.ITEM_META_PAINTING);
    }
}

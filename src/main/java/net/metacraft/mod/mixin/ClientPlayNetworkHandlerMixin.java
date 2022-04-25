package net.metacraft.mod.mixin;

import net.metacraft.mod.MetaPaintingMod;
import net.metacraft.mod.minecraft.MetaPaintingEntity;
import net.metacraft.mod.model.MetaPaintingSpawnPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PaintingSpawnS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static net.metacraft.mod.MetaPaintingMod.LOGGER;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ClientWorld world;

    /**
     * @author wupengyang
     * @reason 1
     */
    @Overwrite
    public void onEntitySpawn(EntitySpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, client);
        EntityType<?> entityType = packet.getEntityTypeId();
        Entity entity = entityType.create(this.world);
        if (entity != null) {
            if (packet instanceof MetaPaintingSpawnPacket spawnPacket) {
                entity = new MetaPaintingEntity(MetaPaintingMod.ENTITY_TYPE_META_PAINTING, this.world);
                ((MetaPaintingEntity) entity).setAttachmentPos((spawnPacket).getPos());
                ((MetaPaintingEntity) entity).setFacing((spawnPacket).getFacing());
                ((MetaPaintingEntity) entity).setMotive((spawnPacket).getMotive());
                entity.setUuid(packet.getUuid());
            }
            int i = packet.getId();
            // onSpawnPacket cannot advance
            entity.onSpawnPacket(packet);
            this.world.addEntity(i, entity);
            if (entity instanceof AbstractMinecartEntity) {
                this.client.getSoundManager().play(new MovingMinecartSoundInstance((AbstractMinecartEntity) entity));
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "onPaintingSpawn")
    public void onPaintingSpawn(PaintingSpawnS2CPacket packet, CallbackInfo ci) {
        LOGGER.info("ClientPlayNetworkHandlerMixin::onPaintingSpawn: " +  packet.getPos().toShortString());
    }
}

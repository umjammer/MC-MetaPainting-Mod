package net.metacraft.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.metacraft.mod.model.MetaPainting;
import net.metacraft.mod.minecraft.MetaPaintingEntityRenderer;
import net.metacraft.mod.model.MetaPaintingSpawnPacket;

@Environment(EnvType.CLIENT)
public class MetaPaintingClientMod implements ClientModInitializer {

    static MetaPainting model;

    @Override
    public void onInitializeClient() {
        model = new MetaPainting();

        EntityRendererRegistry.register(MetaPaintingMod.ENTITY_TYPE_META_PAINTING, MetaPaintingEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(
                MetaPaintingSpawnPacket.IDENTIFIER,
                (client, handler, buffer, responseSender) -> {
                    MetaPaintingSpawnPacket packet = new MetaPaintingSpawnPacket(buffer);
                    client.execute(() -> model.onSpawn(packet));
                });
    }
}
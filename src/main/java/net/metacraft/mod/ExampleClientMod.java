package net.metacraft.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.metacraft.mod.painting.MetaPaintingEntityRenderer;

@Environment(EnvType.CLIENT)
public class ExampleClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(MetaEntityType.ENTITY_TYPE_META_PAINTING, MetaPaintingEntityRenderer::new);
    }
}
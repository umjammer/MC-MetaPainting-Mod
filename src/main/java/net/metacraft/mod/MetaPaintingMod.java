package net.metacraft.mod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.metacraft.mod.model.MetaPainting;
import net.metacraft.mod.minecraft.MetaPaintingEntity;
import net.metacraft.mod.minecraft.MetaPaintingItem;
import net.metacraft.mod.model.MetaPaintingSpawnPacket;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaPaintingMod implements ModInitializer {

    public static final String MOD_ID = "metapainting";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static String ENTITY_TYPE_ID = "entity_meta_painting";

    public static EntityType<MetaPaintingEntity> ENTITY_TYPE_META_PAINTING = null;

    public static Item ITEM_META_PAINTING = null;

    @Override
    public void onInitialize() {
        ITEM_META_PAINTING = Registry.register(
                Registry.ITEM,
                new Identifier(MOD_ID, "meta_painting"),
                new MetaPaintingItem(
                        MetaPaintingMod.ENTITY_TYPE_META_PAINTING,
                        new Item.Settings().maxCount(1).group(ItemGroup.DECORATIONS)));
        ENTITY_TYPE_META_PAINTING = Registry.register(
                Registry.ENTITY_TYPE,
                ENTITY_TYPE_ID,
                EntityType.Builder.create(MetaPaintingEntity::new, SpawnGroup.MISC)
                        .setDimensions(0.5F, 0.5F)
                        .maxTrackingRange(10)
                        .trackingTickInterval(2147483647)
                        .build(ENTITY_TYPE_ID));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("createMetaPainting")
                    .requires(source -> source.hasPermissionLevel(1))
                            .then(CommandManager.argument("url", StringArgumentType.greedyString())
                                    .executes(MetaPainting::processCreateCommand)));
        });
    }

    public static PacketByteBuf newPacketByteBuf() {
        return PacketByteBufs.create();
    }

    public static Packet<?> newPacket(Identifier id, PacketByteBuf buf) {
        return ServerPlayNetworking.createS2CPacket(id, buf);
    }
}

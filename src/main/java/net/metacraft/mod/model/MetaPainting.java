package net.metacraft.mod.model;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.metacraft.mod.MetaPaintingMod;
import net.metacraft.mod.minecraft.MetaPaintingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class MetaPainting {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    /** */
    public void onSpawn(MetaPaintingSpawnPacket packet) {
        EntityType<?> entityType = packet.getEntityTypeId();
        Entity entity = entityType.create(MinecraftClient.getInstance().world);
        if (entity != null) {
            entity.onSpawnPacket(packet);
            int i = packet.getId();
            MinecraftClient.getInstance().world.addEntity(i, entity);
        }
    }

    /**
     * @return 1; success, 0: failure
     */
    public static int processCreateCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayer();
        String input = StringArgumentType.getString(context, "url");

        source.sendFeedback(Text.of("creating meta painting for url..."), false);
        BufferedImage image;
        try {
            image = MetaPainting.getImage(input);
        } catch (IOException e) {
            source.sendFeedback(Text.of("That doesn't seem to be a valid image."), false);
            return 0;
        }

        if (image == null) {
            source.sendFeedback(Text.of("That doesn't seem to be a valid image."), false);
            return 0;
        }

        try {
            source.sendFeedback(Text.of("success!"), false);
            ItemStack stack = new ItemStack(MetaPaintingMod.ITEM_META_PAINTING);
            ((MetaPaintingItem) MetaPaintingMod.ITEM_META_PAINTING).saveColor(stack, MetaPainting.scale(image, TEXTURE_WIDTH, TEXTURE_HEIGHT));
            if (!player.getInventory().insertStack(stack)) {
                player.dropItem(stack, false);
            }
            return 1;
        } catch (IOException e) {
            return 0;
        }
    }

    private static BufferedImage getImage(String input) throws  IOException {
        BufferedImage image;
        if (isValid(input)) {
            URL url = new URL(input);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "the meta painting mod");
            connection.connect();
            image = ImageIO.read(connection.getInputStream());
        } else {
            File file = new File(input);
            image = ImageIO.read(file);
        }
        return image;
    }

    private static byte[] scale(BufferedImage image, int w, int h) throws IOException {
        Image resizedImage = image.getScaledInstance(w, h, Image.SCALE_DEFAULT);
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = dst.createGraphics();
        g.drawImage(resizedImage, 0, 0, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(dst, "PNG", baos);
        return baos.toByteArray();
    }

    private static boolean isValid(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

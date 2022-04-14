package net.metacraft.mod;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.metacraft.mod.network.NetworkManager;
import net.metacraft.mod.painting.MetaDecorationItem;
import net.metacraft.mod.utils.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class PaintingModInitializer implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

    @Override
    public void onInitialize() {
        MetaItems.init();
        MetaEntityType.init();
        NetworkManager.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("createMetaPainting")
                    .requires(source -> source.hasPermissionLevel(1))
                            .then(CommandManager.argument("url", StringArgumentType.greedyString())
                                    .executes(this::createMap)));
        });
    }

    private int createMap(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayer();
        String input = StringArgumentType.getString(context, "url");

        source.sendFeedback(new LiteralText("creating meta painting for url..."), false);
        BufferedImage image;
        try {
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
        } catch (IOException e) {
            source.sendFeedback(new LiteralText("That doesn't seem to be a valid image."), false);
            return 0;
        }

        if (image == null) {
            source.sendFeedback(new LiteralText("That doesn't seem to be a valid image."), false);
            return 0;
        }

        try {
            source.sendFeedback(new LiteralText("success!"), false);
            ItemStack stack = new ItemStack(MetaItems.ITEM_META_PAINTING);
            ((MetaDecorationItem) MetaItems.ITEM_META_PAINTING).saveColor(stack, scale(image, Constants.TEXTURE_WIDTH, Constants.TEXTURE_HEIGHT));
            if (!player.getInventory().insertStack(stack)) {
                player.dropItem(stack, false);
            }
            return 1;
        } catch (IOException e) {
            return 0;
        }
    }

    byte[] scale(BufferedImage image, int w, int h) throws IOException {
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
            LOGGER.warn(url, e);
            return false;
        }
    }
}

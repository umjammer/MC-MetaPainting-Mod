package net.metacraft.mod.minecraft;

import net.metacraft.mod.model.MetaPainting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static net.metacraft.mod.MetaPaintingMod.LOGGER;

public class MetaPaintingEntityRenderer extends EntityRenderer<MetaPaintingEntity> {

    private final MinecraftClient client = MinecraftClient.getInstance();

    private final NativeImageBackedTexture texture;
    private final Identifier identifier;

    boolean inited = false;

    public MetaPaintingEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        texture = new NativeImageBackedTexture(MetaPainting.TEXTURE_WIDTH, MetaPainting.TEXTURE_HEIGHT, true);
        identifier = client.getTextureManager().registerDynamicTexture("painting/1", this.texture);
        LOGGER.info("MetaPaintingEntityRenderer::<init>: " + identifier.getNamespace());
    }

    @Override
    public void render(MetaPaintingEntity paintingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        PaintingMotive paintingMotive = paintingEntity.motive;

        matrixStack.push();
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F));

        Direction direction = paintingEntity.getHorizontalFacing();

        if (direction == Direction.EAST || direction == Direction.WEST) {
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90 * paintingEntity.getHorizontalFacing().getHorizontal()));
        }
        if (direction == Direction.SOUTH) {
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-180));
        } else if (direction == Direction.NORTH) {
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180 * paintingEntity.getHorizontalFacing().getHorizontal()));
        }
        float factor = 1 / 132f;
        float weightScale = (paintingMotive.getWidth() / 16f);
        float heightScale = (paintingMotive.getHeight() / 16f);
        matrixStack.translate(-0.5 * weightScale * factor * MetaPainting.TEXTURE_WIDTH, -0.5 * heightScale * factor * MetaPainting.TEXTURE_HEIGHT, -0.04);
        matrixStack.scale(factor * weightScale, factor * heightScale, factor * weightScale);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getText(getTexture(paintingEntity)));
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(light).next();
        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(light).next();
        matrixStack.pop();
        super.render(paintingEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    public Identifier getTexture(MetaPaintingEntity paintingEntity) {
        if (!inited) {
            byte[] colors = paintingEntity.getColors();
            if (colors != null) {
                try {
                    this.texture.setImage(NativeImage.read(new ByteArrayInputStream(colors)));
                    this.texture.upload();
                    inited = true;
                } catch (IOException e) {
                    LOGGER.info("MetaPaintingEntityRenderer::updateTexture: " + e.getMessage());
                }
            }
        }
        return identifier;
    }
}
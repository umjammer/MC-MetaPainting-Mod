package net.metacraft.mod.painting;

import static net.metacraft.mod.PaintingModInitializer.LOGGER;

import net.metacraft.mod.utils.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MetaPaintingEntityRenderer extends EntityRenderer<MetaPaintingEntity> {

    private final MinecraftClient client = MinecraftClient.getInstance();

    private final NativeImageBackedTexture texture;

    private final RenderLayer renderLayer;

    public MetaPaintingEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        texture = new NativeImageBackedTexture(Constants.TEXTURE_WIDTH, Constants.TEXTURE_HEIGHT, true);
        Identifier identifier = client.getTextureManager().registerDynamicTexture("painting/1", this.texture);
        this.renderLayer = RenderLayer.getText(identifier);
        LOGGER.info("MetaPaintingEntityRenderer::<init>: " + identifier.getNamespace());
    }

    private void updateTexture(byte[] colors) {
        try {
            this.texture.setImage(NativeImage.read(new ByteArrayInputStream(colors)));
            this.texture.upload();
        } catch (IOException e) {
            LOGGER.info("MetaPaintingEntityRenderer::updateTexture: " + e.getMessage());
        }
    }

    @Override
    public void render(MetaPaintingEntity paintingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        PaintingMotive paintingMotive = paintingEntity.motive;

        matrixStack.push();
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F));

        float g = (-paintingMotive.getHeight()) / 2.0F;

        int x = 0, y = 0, z = 0;
        float aa = g + (x + 1) * 16;
        float ab = g + x * 16;

        int ac = paintingEntity.getBlockX();
        int ad = MathHelper.floor(paintingEntity.getY() + (aa + ab) / 2.0F / 16.0F);
        int ae = paintingEntity.getBlockZ();

        Direction direction = paintingEntity.getHorizontalFacing();

        if (direction == Direction.EAST || direction == Direction.WEST) {
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90 * (paintingEntity.getHorizontalFacing().getHorizontal())));
            if (direction == Direction.EAST) {
                ae = MathHelper.floor(paintingEntity.getZ() + (y + z) / 2.0F / 16.0F);
            } else /* if (direction == Direction.WEST) */ {
                ae = MathHelper.floor(paintingEntity.getZ() - (y + z) / 2.0F / 16.0F);
            }
        }
        if (direction == Direction.SOUTH) {
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-180));
            ac = MathHelper.floor(paintingEntity.getX() - (y + z) / 2.0F / 16.0F);
        } else if (direction == Direction.NORTH) {
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180 * (paintingEntity.getHorizontalFacing().getHorizontal())));
            ac = MathHelper.floor(paintingEntity.getX() + (y + z) / 2.0F / 16.0F);
        }
        float factor = 1 / 132f;
        float weightScale = (paintingMotive.getWidth() / 16f);
        float heightScale = (paintingMotive.getHeight() / 16f);
        matrixStack.translate(-0.5 * weightScale * (factor) * Constants.TEXTURE_WIDTH, -0.5 * heightScale * (factor) * Constants.TEXTURE_HEIGHT, -0.04);
        matrixStack.scale(factor * weightScale, factor * heightScale, factor * weightScale);
        int light = WorldRenderer.getLightmapCoordinates(paintingEntity.world, new BlockPos(ac, ad, ae));
        byte[] colors = paintingEntity.getColors();
        if (colors != null) {
            updateTexture(colors);
            paintingTexture(matrixStack, vertexConsumerProvider, light);
 } else {
LOGGER.info("MetaPaintingEntityRenderer::render: colors is null");
        }
        matrixStack.pop();
        super.render(paintingEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    private void paintingTexture(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.renderLayer);
        vertexConsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(light).next();
        vertexConsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(light).next();
        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(light).next();
    }

    public Identifier getTexture(MetaPaintingEntity paintingEntity) {
        return MinecraftClient.getInstance().getPaintingManager().getBackSprite().getAtlas().getId();
    }

    private void renderPainting(MatrixStack matrices, VertexConsumer vertexConsumer, MetaPaintingEntity entity, int width, int height, Sprite paintingSprite, Sprite backSprite) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();
        float f = (-width) / 2.0F;
        float g = (-height) / 2.0F;
        @SuppressWarnings("unused")
        float h = 0.5F;
        float i = backSprite.getMinU();
        float j = backSprite.getMaxU();
        float k = backSprite.getMinV();
        float l = backSprite.getMaxV();
        float m = backSprite.getMinU();
        float n = backSprite.getMaxU();
        float o = backSprite.getMinV();
        float p = backSprite.getFrameV(1.0D);
        float q = backSprite.getMinU();
        float r = backSprite.getFrameU(1.0D);
        float s = backSprite.getMinV();
        float t = backSprite.getMaxV();
        int u = width / 16;
        int v = height / 16;
        double d = 16.0D / u;
        double e = 16.0D / v;

        for (int w = 0; w < u; ++w) {
            for(int x = 0; x < v; ++x) {
                float y = f + (w + 1) * 16;
                float z = f + w * 16;
                float aa = g + (x + 1) * 16;
                float ab = g + x * 16;
                int ac = entity.getBlockX();
                int ad = MathHelper.floor(entity.getY() + (aa + ab) / 2.0F / 16.0F);
                int ae = entity.getBlockZ();
                Direction direction = entity.getHorizontalFacing();
                if (direction == Direction.NORTH) {
                    ac = MathHelper.floor(entity.getX() + (y + z) / 2.0F / 16.0F);
                }

                if (direction == Direction.WEST) {
                    ae = MathHelper.floor(entity.getZ() - (y + z) / 2.0F / 16.0F);
                }

                if (direction == Direction.SOUTH) {
                    ac = MathHelper.floor(entity.getX() - (y + z) / 2.0F / 16.0F);
                }

                if (direction == Direction.EAST) {
                    ae = MathHelper.floor(entity.getZ() + (y + z) / 2.0F / 16.0F);
                }

                int af = WorldRenderer.getLightmapCoordinates(entity.world, new BlockPos(ac, ad, ae));
                float ag = paintingSprite.getFrameU(d * (u - w));
                float ah = paintingSprite.getFrameU(d * (u - (w + 1)));
                float ai = paintingSprite.getFrameV(e * (v - x));
                float aj = paintingSprite.getFrameV(e * (v - (x + 1)));
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, ab, ah, ai, -0.5F, 0, 0, -1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, ab, ag, ai, -0.5F, 0, 0, -1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, aa, ag, aj, -0.5F, 0, 0, -1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, aa, ah, aj, -0.5F, 0, 0, -1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, aa, i, k, 0.5F, 0, 0, 1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, aa, j, k, 0.5F, 0, 0, 1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, ab, j, l, 0.5F, 0, 0, 1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, ab, i, l, 0.5F, 0, 0, 1, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, aa, m, o, -0.5F, 0, 1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, aa, n, o, -0.5F, 0, 1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, aa, n, p, 0.5F, 0, 1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, aa, m, p, 0.5F, 0, 1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, ab, m, o, 0.5F, 0, -1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, ab, n, o, 0.5F, 0, -1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, ab, n, p, -0.5F, 0, -1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, ab, m, p, -0.5F, 0, -1, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, aa, r, s, 0.5F, -1, 0, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, ab, r, t, 0.5F, -1, 0, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, ab, q, t, -0.5F, -1, 0, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, y, aa, q, s, -0.5F, -1, 0, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, aa, r, s, -0.5F, 1, 0, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, ab, r, t, -0.5F, 1, 0, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, ab, q, t, 0.5F, 1, 0, 0, af);
                this.vertex(matrix4f, matrix3f, vertexConsumer, z, aa, q, s, 0.5F, 1, 0, 0, af);
            }
        }
    }

    private void vertex(Matrix4f modelMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int light) {
        vertexConsumer.vertex(modelMatrix, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, normalX, normalY, normalZ).next();
    }
}
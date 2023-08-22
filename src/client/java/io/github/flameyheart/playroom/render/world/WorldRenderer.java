package io.github.flameyheart.playroom.render.world;

import net.krlite.equator.visual.color.AccurateColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class WorldRenderer {

    public static void render(MatrixStack matrixStack) {
        //Renderer3d.renderOutline(matrixStack, Color.RED, new Vec3d(0, 150, 0), new Vec3d(1, 1, 1));
        //Renderer3d.renderFilled(matrixStack, Color.BLUE, new Vec3d(0, 152, 0), new Vec3d(1, 1, 1));
        //Renderer3d.renderEdged(matrixStack, Color.RED, Color.CYAN, new Vec3d(0, 152, 0), new Vec3d(1, 1, 1));
        //Renderer3d.renderFadingBlock(Color.RED, Color.CYAN, new Vec3d(0, 154, 0), new Vec3d(1, 1, 1), 100);

        MinecraftClient client = MinecraftClient.getInstance();

        /*matrixStack.push();

        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();

        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        RenderSystem.setShader(PlayroomClient::getPositionColorProgram);

        AccurateColor color = AccurateColor.fromARGB(0x80FF00FFL);

        //Build buffer
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        Vec3d start = transformVec3d(new Vec3d(0, 150, 0));
        Vec3d end1 = start.add(1, -1, -1);
        Vec3d end2 = start.add(-1, -1, 1);

        renderVertex(buffer, matrixStack, start, color);
        renderVertex(buffer, matrixStack, end1, color);
        renderVertex(buffer, matrixStack, end2, color);


        //Render
        BufferUtils.draw(buffer);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        matrixStack.pop();*/
    }

    private static void renderVertex(BufferBuilder builder, MatrixStack matrix, Vec3d vertex, AccurateColor color) {
        builder.vertex(matrix.peek().getPositionMatrix(), (float) vertex.getX(), (float) vertex.getY(), (float) vertex.getZ())
            .color(color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat(), color.opacityAsFloat())
            .next();
    }

    private static Vec3d transformVec3d(Vec3d in) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        return in.subtract(camPos);
    }
}

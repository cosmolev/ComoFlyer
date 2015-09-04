package renderengine;

import com.jme3.util.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static comoflyer.StaticDepthHelpers.arrayToImage;
import static comoflyer.StaticDepthHelpers.computeAnglesScaledFromBottom;

/**
 *
 * @author Lev
 */
public class ReadableDepthRenderer extends SuperRenderer {

    public static void main(String[] args) {
        double[] anglesScaledFromBottom = /*normalize(*/ computeAnglesScaledFromBottom(1080,45) /*)*/;

        float[][] normalizedMatrix = new float[1080][1920];
        for (int x = 0; x < 1080; x++) {
            System.out.println("anglesScaledFromBottom[x] = " + (float)anglesScaledFromBottom[x]);
            for (int y = 0; y < 1920; y++) {
                normalizedMatrix[x][y] = (float)anglesScaledFromBottom[x];
            }
        }

        try{
            ImageIO.write(arrayToImage(normalizedMatrix), "PNG", new File("angleMask.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Write depth values to buffer
    private void readDepthBuffer(ByteBuffer byteBuf, int w, int h) {
        GL11.glReadPixels(0, 0, w, h, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, byteBuf);
    }

    public byte[] getDepthBytes(int w, int h){
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * w * h);
        readDepthBuffer(byteBuffer, w, h);
        byte[] byteArr = new byte[4 * w * h];

        for (int c = 0; c < w * h * 4; c++) {
            byteArr[c] = byteBuffer.get(c);
        }
        byteBuffer.rewind();
        return byteArr;
    }
}
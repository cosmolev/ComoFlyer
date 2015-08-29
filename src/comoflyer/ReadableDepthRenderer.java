package comoflyer;

import com.jme3.renderer.lwjgl.LwjglRenderer;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.BufferUtils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Lev
 */
public class ReadableDepthRenderer extends LwjglRenderer {

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



//        for(double d : anglesScaledFromBottom){
//            System.out.println("Math.cos("+ d +") = " + Math.cos(Math.toRadians(d)));
//        }
    }

    static BufferedImage arrayToImage(float[][] array){
        //BufferedImage image = new BufferedImage(array[0].length, array.length, BufferedImage.TYPE_INT_RGB);
        BufferedImage image = new BufferedImage(array[0].length, array.length, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < array.length; x++) {
            for (int y = 0; y < array[0].length; y++) {
                float f = array[x][y];
                image.getRaster().setPixel(0, 0, new float[]{f});
                //image.setRGB(y, x, Color.getHSBColor(0.0F, 0.0F, f).getRGB());
            }
        }

        return image;
    }

    static int[][] arrayToIntArrayWithInfinity(float[][] array){
        int[][] intArray = new int[array.length][array[0].length];

        float[] minMax = getMinMax(array);
        float min = minMax[0];
        float max = minMax[1];

        for (int x = 0; x < array.length; x++) {
            for (int y = 0; y < array[0].length; y++) {
                float f = array[x][y];
                if (/*(f != 0) &&*/ (f != 1) /*&& (f != min)*/ && (f != max)) {
                    //if ((f != 0) && (f != 1)) {
                    intArray[x][y] = Color.getHSBColor(0.0F, 0.0F, f).getRGB();
                    //image.setRGB(y, x, Color.getHSBColor(0.0F, 0.0F, f).getRGB());
                } else {
                    intArray[x][y] = new Color(255, 0, 0).getRGB();
                    //image.setRGB(y, x, new Color(255, 0, 0).getRGB());
                }
            }
        }

        return intArray;
    }

    static BufferedImage intArrayToImage(int[][] intArray){
        BufferedImage image = new BufferedImage(intArray[0].length, intArray.length, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < intArray.length; x++) {
            for (int y = 0; y < intArray[0].length; y++) {
                image.setRGB(y, x, intArray[x][y]);
            }
        }

        return image;
    }

    static BufferedImage arrayToImageWithInfinity(float[][] array){
        BufferedImage image = new BufferedImage(array[0].length, array.length, BufferedImage.TYPE_INT_RGB);

        float[] minMax = getMinMax(array);
        float min = minMax[0];
        float max = minMax[1];

        for (int x = 0; x < array.length; x++) {
            for (int y = 0; y < array[0].length; y++) {
                float f = array[x][y];
                if (/*(f != 0) &&*/ (f != 1) /*&& (f != min)*/ && (f != max)) {
                    //if ((f != 0) && (f != 1)) {
                    image.setRGB(y, x, Color.getHSBColor(0.0F, 0.0F, f).getRGB());
                } else {
                    image.setRGB(y, x, new Color(255, 0, 0).getRGB());
                }
            }
        }

        return image;
    }

    static double[] computeAnglesScaledFromBottom(int height, double angle){
        double[] anglesScaledFromCenter = computeAngles(height,angle);

        double[] anglesScaledFromBottom = new double[anglesScaledFromCenter.length];
        for(int i = 0; i < anglesScaledFromCenter.length/2; i++)
            anglesScaledFromBottom[i+1] = angle/2 - anglesScaledFromCenter[i];

        anglesScaledFromBottom[anglesScaledFromCenter.length/2] = angle/2;

        for(int i = anglesScaledFromCenter.length/2 + 1; i < anglesScaledFromCenter.length; i++)
            anglesScaledFromBottom[i] = angle/2 + anglesScaledFromCenter[i];

        return anglesScaledFromBottom;
    }

    static double[] computeAngles(int height, double angle){
        if ( (height & 1) == 1 )throw new IllegalArgumentException("height should be even");
        double[] halfFrustum = computeAnglesFrustumUpperPart(height/2,angle/2);

        double[] reversed = Arrays.copyOf(halfFrustum, halfFrustum.length);
        ArrayUtils.reverse(reversed);

        double[] fullFrustrum = new double[height];
        System.arraycopy(reversed, 0, fullFrustrum, 0, height/2);

        for(int rowNum = height/2; rowNum < height; rowNum++){
            fullFrustrum[rowNum] = halfFrustum[rowNum-height/2];
        }

        return fullFrustrum;
    }

    static double[] computeAnglesFrustumUpperPart(int halfHeight, double halfAngle){
        double[] angles = new double[halfHeight];

        double distanceToNearPlane = halfHeight / Math.tan(Math.toRadians(halfAngle));

        for(int rowNum = 0; rowNum < halfHeight; rowNum++)
            angles[rowNum] = Math.toDegrees( Math.atan(rowNum / distanceToNearPlane) );

        return angles;
    }

    static double[] normalize(double[] arrayToNormalize){
        double[] minMax = getMinMax(arrayToNormalize);
        double min = minMax[0];
        double max = minMax[1];
        double[] normalizedArray = new double[arrayToNormalize.length];
        for(int i = 0; i < arrayToNormalize.length;i++)
            normalizedArray[i] = normalize(arrayToNormalize[i],min,max,0,1);
        return normalizedArray;
    }

    static float[][] normalize(final float[][] matrixToNormalize, float newMin, float newMax){
        float[] oldMinMax = getMinMax(matrixToNormalize);
        float oldMin = oldMinMax[0];
        float oldMax = oldMinMax[1];
        float[][] normalizedMatrix = new float[matrixToNormalize.length][matrixToNormalize[0].length];
        for (int x = 0; x < matrixToNormalize.length; x++) {
            for (int y = 0; y < matrixToNormalize[0].length; y++) {
                normalizedMatrix[x][y] = normalize(matrixToNormalize[x][y],oldMin,oldMax,newMin,newMax);
            }
        }
        return normalizedMatrix;
    }

    static float normalize(float oldValue, float oldMin, float oldMax, float newMin, float newMax) {
        return (((oldValue - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }

    static double normalize(double oldValue, double oldMin, double oldMax, double newMin, double newMax) {
        return(((oldValue - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }

    static float[][] normalize(final float[][] matrixToNormalize, float oldMin, float oldMax, float newMin, float newMax){
        float[][] normalizedMatrix = new float[matrixToNormalize.length][matrixToNormalize[0].length];
        for (int x = 0; x < matrixToNormalize.length; x++) {
            for (int y = 0; y < matrixToNormalize[0].length; y++) {
                normalizedMatrix[x][y] = normalize(matrixToNormalize[x][y],oldMin,oldMax,newMin,newMax);
            }
        }
        return normalizedMatrix;
    }

    static float linearize(float z_b, float zNear, float zFar){
        float z_n = 2.0f * z_b - 1.0f;
        float z_e = 2.0f * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
        return z_e;
    }

    static float linearize1(float z_b, float n, float f){
        float z_n = 2.0f * z_b - 1.0f;
        float z_e = 2.0f * n * f / (f + n - z_n * (f - n));
        return z_e;
    }

    static float[][] linearize(final float[][] matrixToLinearize, float zNear, float zFar){
        float[][] linearizedMatrix = new float[matrixToLinearize.length][matrixToLinearize[0].length];
        for (int x = 0; x < matrixToLinearize.length; x++) {
            for (int y = 0; y < matrixToLinearize[0].length; y++) {
                linearizedMatrix[x][y] = linearize(matrixToLinearize[x][y],zNear,zFar);
            }
        }
        return linearizedMatrix;
    }

    static void saveRgb(final float[][] matrixToSave, String filename){
        try
        {
            FileWriter writer = new FileWriter(filename);
            for(int i = 0; i < matrixToSave.length; i++)
            {
                for (int j=0; j<(matrixToSave[0].length-1); j++)
                {
                    writer.append("" + Color.getHSBColor(0.0F, 0.0F, matrixToSave[i][j]).getRGB());
                    writer.append(',');
                }
                writer.append('\n');
                writer.flush();
            }
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static void saveToCsv(final int[][] matrixToSave, String filename){
        try
        {
            FileWriter writer = new FileWriter(filename);
            for(int i = 0; i < matrixToSave.length; i++)
            {
                for (int j=0; j<(matrixToSave[0].length-1); j++)
                {
                    writer.append("" + matrixToSave[i][j]);
                    writer.append(',');
                }
                writer.append('\n');
                writer.flush();
            }
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static void saveToCsv(final float[][] matrixToSave, String filename){
        try
        {
            FileWriter writer = new FileWriter(filename);
            for(int i = 0; i < matrixToSave.length; i++)
            {
                for (int j=0; j<(matrixToSave[0].length-1); j++)
                {
                    writer.append("" + matrixToSave[i][j]);
                    writer.append(',');
                }
                writer.append('\n');
                writer.flush();
            }
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static float[][] scaleToHeight(float[][] imageMatrix){
        //double[] angles = computeAnglesScaledFromBottom(imageMatrix.length,45);
        double[] angles = /*normalize(*/ computeAnglesScaledFromBottom(imageMatrix.length,45) /*)*/;
        ArrayUtils.reverse(angles);

        float[][] scaledImageMatrix = new float[imageMatrix.length][imageMatrix[0].length];

        for (int x = 0; x < imageMatrix.length; x++) {
            for (int y = 0; y < imageMatrix[0].length; y++) {
                scaledImageMatrix[x][y] = (float)(imageMatrix[x][y]*Math.cos(Math.toRadians(angles[x])));
            }
        }
        return scaledImageMatrix;
    }


    static float[] toFloatArr(byte[] byteArr) {
        float[] floatArr = new float[byteArr.length / 4];
        for (int i = 0; i < byteArr.length; i = i + 4) {
            byte[] bytes = Arrays.copyOfRange(byteArr, i, i + 4);
            floatArr[i / 4] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }
        return floatArr;
    }

    static float[][] toMatrix(float[] floatArr, int matrixWidth){
        if(floatArr.length % matrixWidth != 0)
            throw new IllegalArgumentException("matrixWidth does not match floatArr.length");
        float[][] floatMatrix = new float[floatArr.length/matrixWidth][matrixWidth];
        for (int x = 0; x < floatMatrix.length; x++) {
            for (int y = 0; y < floatMatrix[0].length; y++) {
                floatMatrix[x][y] = floatArr[x*matrixWidth+y];
            }
        }
        return floatMatrix;
    }

    static float[][] flip(float[][] floatArr){
        float[][] flipped = new float[floatArr.length][floatArr[0].length];
        for (int x = 0; x < floatArr.length; x++) {
            for (int y = 0; y < floatArr[0].length; y++) {
                flipped[x][y] = floatArr[floatArr.length - (x + 1)][y];
            }
        }
        return flipped;
    }

    static float[] getMinMax(float[] floatArr) {
        float min = 1;
        float max = 0;
        for (float f : floatArr) {
            if ((f != 0) && (f != 1)) {
                if (f < min)
                    min = f;

                if (f > max)
                    max = f;
            }
        }
        return new float[]{min, max};
    }

    static double[] getMinMax(double[] doubleArr) {
        double min = 1;
        double max = 0;
        for (double f : doubleArr) {
            if ((f != 0) && (f != 1)) {
                if (f < min)
                    min = f;

                if (f > max)
                    max = f;
            }
        }
        return new double[]{min, max};
    }

    static float[] getMinMax(float[][] floatMatrix) {
        float min = 1;
        float max = 0;

        for (int x = 0; x < floatMatrix.length; x++) {
            for (int y = 0; y < floatMatrix[0].length; y++) {
                float f = floatMatrix[x][y];
                if ((f != 0) && (f != 1)) {
                    if (f < min)
                        min = f;
                    if (f > max)
                        max = f;
                }
            }
        }
        return new float[]{min, max};
    }

    static double[] getMinMax(double[][] doubleMatrix) {
        double min = 1;
        double max = 0;

        for (int x = 0; x < doubleMatrix.length; x++) {
            for (int y = 0; y < doubleMatrix[0].length; y++) {
                double f = doubleMatrix[x][y];
                if ((f != 0) && (f != 1)) {
                    if (f < min)
                        min = f;
                    if (f > max)
                        max = f;
                }
            }
        }
        return new double[]{min, max};
    }

    static float getMin(float[] floatArr) {
        float min = 1;
        for (float f : floatArr) {
            if ((f != 0) && (f != 1)) {
                if (f < min) {
                    min = f;
                }
            }
        }
        return min;
    }

    static float getMax(float[] floatArr) {
        float max = 0;
        for (float f : floatArr) {
            if ((f != 0) && (f != 1)) {
                if (f > max) {
                    max = f;
                }
            }
        }
        return max;
    }

    // Write depth values to buffer
    public void readDepthBuffer(FrameBuffer fb, ByteBuffer byteBuf, int w, int h) {

//        if (fb != null) {
//
//            RenderBuffer rb = fb.getDepthBuffer();
//
//            if (rb == null) {
//
//                throw new IllegalArgumentException("Specified framebuffer"
//                        + " does not have a depthbuffer");
//
//            }
//
//
//
//            setFrameBuffer(fb);

// context is private: no access...

// if (context.boundReadBuf != rb.getSlot()) {

// glReadBuffer(GL_COLOR_ATTACHMENT0_EXT + rb.getSlot());

// context.boundReadBuf = rb.getSlot();

// }

//        } else {
//
//            setFrameBuffer(null);
//
//        }

        GL11.glReadPixels(0, 0, w, h, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, byteBuf);

        byte[] byteArr = new byte[w * h * 4];
        for (int c = 0; c < w * h * 4; c++) {
            byteArr[c] = byteBuf.get(c);
        }

        float[] floatArr = toFloatArr(byteArr);

        float[] minMax = getMinMax(floatArr);
        float min = minMax[0];
        float max = minMax[1];

        float[][] floatMatrix = flip(toMatrix(floatArr,w));
        float[][] linearizedMatrix = linearize(floatMatrix,8,15000);

        float[][] normalizedMatrix = normalize(floatMatrix,min, max, 0, 1);

        float[] normMinMax = getMinMax(normalizedMatrix);
        float normMin = normMinMax[0];
        float normMax = normMinMax[1];
        System.out.println("normMin = " + normMin);
        System.out.println("normMax = " + normMax);

        float[][] heightMatrix = scaleToHeight(normalizedMatrix);

        float[][] normalizedheightMatrix = normalize(heightMatrix, 0, 1);
        //System.out.println("Arrays.deepToString(normalizedheightMatrix) = " + Arrays.deepToString(normalizedheightMatrix));;

        try {
            //ImageIO.write(arrayToImageWithInfinity(floatMatrix), "PNG", new File("depthMask.png"));

            int[][] intArray = arrayToIntArrayWithInfinity(floatMatrix);
            saveToCsv(intArray,"rgb.csv");
            ImageIO.write(intArrayToImage(intArray), "PNG", new File("depthMask.png"));
            //ImageIO.write(arrayToImage(floatMatrix), "TIFF", new File("depthMask.tiff"));

            saveToCsv(floatMatrix,"depth.csv");
            saveToCsv(normalizedMatrix,"normalized.csv");
            saveToCsv(linearizedMatrix,"linearized.csv");
            //ImageIO.write(arrayToImageWithInfinity(normalizedMatrix), "PNG", new File("depthMaskNormalized.png"));

            int[][] normalizedIntArray = arrayToIntArrayWithInfinity(normalizedMatrix);
            saveToCsv(normalizedIntArray,"rgbNormalized.csv");
            ImageIO.write(intArrayToImage(normalizedIntArray), "PNG", new File("depthMaskNormalized.png"));
            //ImageIO.write(arrayToImage(normalizedMatrix), "TIFF", new File("depthMaskNormalized.tiff"));

            int[][] linearizedIntArray = arrayToIntArrayWithInfinity(linearizedMatrix);
            ImageIO.write(intArrayToImage(linearizedIntArray), "PNG", new File("depthMaskLinearized.png"));

            ImageIO.write(arrayToImage(heightMatrix), "PNG", new File("heightMask.png"));
            ImageIO.write(arrayToImage(normalizedheightMatrix), "PNG", new File("heightMaskNormalized.png"));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }


    }

    // Create float buffer with depth values. Convenience method for readDepthBuffer
    public FloatBuffer createDepthBuffer(int w, int h) {

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * w * h);

        readDepthBuffer(null, byteBuffer, w, h);

        byteBuffer.rewind();

//        for(int i = 0; i < 4 * w * h; i++){
//            System.out.print(byteBuffer.get()+"X");
//        }
//
//        byteBuffer.rewind();

        return byteBuffer.asFloatBuffer();

    }
}
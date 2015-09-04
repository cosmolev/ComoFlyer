package demtools;

import comoflyer.StaticDepthHelpers;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static comoflyer.StaticDepthHelpers.*;

public class HgtReader {

    private static final float NO_DATA_MARKER = -1f;
    public static final int SRTM_1_SIZE = 3601;
    private static final int SRTM_3_SIZE = 1201;

    private static final String DEM_FILENAME_TEMPLATE = "dems\\N%02dE0%02d.hgt";

    public static final int TERRAIN_SCALE = 5;

    //public static final int TERRAIN_SIZE = 2049;
    //public static final int TERRAIN_SIZE = 4097;
    public static final int TERRAIN_SIZE = 8193;
    //public static final int TERRAIN_SIZE = 16385;
    public static final int METERS_IN_SECOND = 30;

    //TODO
    static int determineMinDistanceToNODATA(float[][] matrix, int fromRow, int fromColumn){

        int distanceToRight = matrix[fromRow].length - fromColumn;
        for(int i = fromColumn; i < matrix[fromRow].length; i++){
            if(matrix[fromRow][i] == NO_DATA_MARKER){
                distanceToRight = i - fromColumn;
                break;
            }
        }
        System.out.println("distanceToRight = " + distanceToRight);

        int distanceToBottom = matrix.length - fromRow;
        for(int i = fromRow; i < matrix.length; i++){
            if(matrix[i][fromColumn] == NO_DATA_MARKER){
                distanceToBottom = i - fromRow;
                break;
            }
        }
        System.out.println("distanceToBottom = " + distanceToBottom);

        int distanceToLeft = fromColumn;
        for(int i = fromColumn; i > -1; i--){
            if(matrix[fromRow][i] == NO_DATA_MARKER){
                distanceToLeft = fromColumn - i;
                break;
            }
        }
        System.out.println("distanceToLeft = " + distanceToLeft);

        int distanceToTop = fromRow;
        for(int i = fromRow; i > -1; i--){
            if(matrix[i][fromColumn] == NO_DATA_MARKER){
                distanceToTop = fromRow - i;
                break;
            }
        }
        System.out.println("distanceToTop = " + distanceToTop);
        return Math.min(Math.min(distanceToRight,distanceToBottom),Math.min(distanceToLeft,distanceToTop));
    }

    private static float[][] uniteMatrixes(float[][] topLeft,    float[][] topCenter,    float[][] topRight,
                                           float[][] middleLeft, float[][] middleCenter, float[][] middleRight,
                                           float[][] bottomLeft, float[][] bottomCenter, float[][] bottomRight){

        float[][] resultingMatrix = new float[middleCenter.length*3][middleCenter[0].length*3];

        StaticDepthHelpers.writeOneMatrixIntoAnother(topLeft,resultingMatrix,0,0);
            StaticDepthHelpers.writeOneMatrixIntoAnother(topCenter,resultingMatrix,0,3601);
                StaticDepthHelpers.writeOneMatrixIntoAnother(topRight,resultingMatrix,0,7202);
        StaticDepthHelpers.writeOneMatrixIntoAnother(middleLeft,resultingMatrix,3601,0);
            StaticDepthHelpers.writeOneMatrixIntoAnother(middleCenter,resultingMatrix,3601,3601);
                StaticDepthHelpers.writeOneMatrixIntoAnother(middleRight,resultingMatrix,3601,7202);
        StaticDepthHelpers.writeOneMatrixIntoAnother(bottomLeft,resultingMatrix,7202,0);
            StaticDepthHelpers.writeOneMatrixIntoAnother(bottomCenter,resultingMatrix,7202,3601);
                StaticDepthHelpers.writeOneMatrixIntoAnother(bottomRight,resultingMatrix,7202,7202);
        return resultingMatrix;
    }

    static float[] readFileToArray(RandomAccessFile fileToRead) throws IOException {
        float[][] heightMatrix = readFileToMatrix(fileToRead);
        return matrixToArray(heightMatrix);
    }

    static float[] matrixToArray(float[][] matrix){
        float[] array = new float[matrix.length*matrix[0].length];
        int pos = 0;
        for (int col = 0; col < matrix.length; col++) {
            for (int row = 0; row < matrix[0].length; row++) {
                array[pos++] = matrix[col][row];
            }
        }
        return array;
    }

    private static float[][] readFileToMatrix(RandomAccessFile fileToRead) throws IOException {
        long fileLength;
        fileLength = fileToRead.length();

        /* First you need to figure out if this is an SRTM-1 or SRTM-3 image.
         SRTM-1 has 3601 x 3601 or 12967201 cells and SRTM-3 has 1201 x 1201
         or 1442401 cells. Each cell is 2 bytes.
         */
        int rows, cols;
        if (fileLength == SRTM_1_SIZE * SRTM_1_SIZE * 2) {
            rows = cols = SRTM_1_SIZE;
        } else if (fileLength == SRTM_3_SIZE * SRTM_3_SIZE * 2) {
            rows = cols = SRTM_3_SIZE;
        } else {
            throw new IllegalArgumentException("SRTM file size does supported");
        }

        ByteBuffer buf = ByteBuffer.allocate((int) fileLength);
        FileChannel inChannel = fileToRead.getChannel();
        inChannel.position(0);
        inChannel.read(buf);
        buf.order(java.nio.ByteOrder.BIG_ENDIAN);
        buf.rewind();

        float[][] heightMatrix = new float[rows][rows];

        int pos = 0;
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                float val = (float) buf.getShort(pos);
                float normalizedVal = val/METERS_IN_SECOND;
                //TODO this if is useless? delete maybe?
                if((row < 3601)&&(col < 3601)){
                    heightMatrix[col][row] = normalizedVal;
                }
                pos += 2;
            }
        }
        return heightMatrix;
    }

    static float[][] submatrix(float[][] source, int desiredLength, int startFromRow, int startFromColumn){
        //TODO
        System.out.println("Source matrix size:" + source.length + " x " + source[0].length);
        System.out.println("startFromRow = " + startFromRow);
        System.out.println("startFromColumn = " + startFromColumn);
        System.out.println("desiredLength = " + desiredLength);
        float[][] result = new float[desiredLength][desiredLength];
        for (int row = 0; row < desiredLength; row++){
            for (int col = 0; col < desiredLength; col++){
                result[row][col] = source[startFromRow+row][startFromColumn+col];
            }
        }
        return result;
    }

    public static Pair<float[],Float> processWithNegatives(int latitude, int latitudeSecondsInCenterPlaneCoordinates,
                                               int longitude, int longitudeSecondsInCenterPlaneCoordinates) throws IOException {

        int latitudeSecondsInBig9PlanesCoordinates = SRTM_1_SIZE+latitudeSecondsInCenterPlaneCoordinates;
        int longitudeSecondsInBig9PlanesCoordinates = SRTM_1_SIZE+longitudeSecondsInCenterPlaneCoordinates;

        int top = latitude+1;
        int middle = latitude;
        int bottom = latitude-1;

        int left = longitude-1;
        int center = longitude;
        int right = longitude+1;

        float[][] topLeft = readFileToMatrixOrNegative(top, left);
            float[][] topCenter = readFileToMatrixOrNegative(top, center);
                float[][] topRight = readFileToMatrixOrNegative(top, right);
        float[][] middleLeft = readFileToMatrixOrNegative(middle, left);
            float[][] middleCenter = readFileToMatrixOrNegative(middle, center);
                float[][] middleRight = readFileToMatrixOrNegative(middle, right);
        float[][] bottomLeft = readFileToMatrixOrNegative(bottom, left);
            float[][] bottomCenter = readFileToMatrixOrNegative(bottom, center);
                float[][] bottomRight = readFileToMatrixOrNegative(bottom, right);
        float[][] resulting = uniteMatrixes(topLeft,topCenter,topRight,
                                            middleLeft,middleCenter,middleRight,
                                            bottomLeft,bottomCenter,bottomRight);

        //TODO
        int largestPowerOf2 = largestPowerOf2(determineMinDistanceToNODATA(resulting,
                3*SRTM_1_SIZE-latitudeSecondsInBig9PlanesCoordinates, longitudeSecondsInBig9PlanesCoordinates));
        int maxPossibleTerraSize = largestPowerOf2 + 1;
        System.out.println("maxPossibleTerraSize = " + maxPossibleTerraSize);
        System.out.println("largestPowerOf2/2 = " + largestPowerOf2 / 2);
        //TODO
        int latToStartCut = 3*SRTM_1_SIZE - latitudeSecondsInBig9PlanesCoordinates - largestPowerOf2/2;
        System.out.println("latitudeSecondsInBig9PlanesCoordinates = " + latitudeSecondsInBig9PlanesCoordinates);
        System.out.println("latToStartCut = " + latToStartCut);
        int longToStartCut = longitudeSecondsInBig9PlanesCoordinates - largestPowerOf2/2;
        System.out.println("longitudeSecondsInBig9PlanesCoordinates = " + longitudeSecondsInBig9PlanesCoordinates);
        System.out.println("longToStartCut = " + longToStartCut);

        float height = getHeight(resulting,latitudeSecondsInCenterPlaneCoordinates,longitudeSecondsInCenterPlaneCoordinates);

        System.out.println("height = " + height);

        return new Pair<>(matrixToArray(submatrix(resulting,maxPossibleTerraSize, latToStartCut, longToStartCut)),height);
    }

    static float getHeight (float[][] matrix, int latitudeSecondsInCenterPlaneCoordinates, int longitudeSecondsInCenterPlaneCoordinates){
        int latitudeSecondsInBig9PlanesCoordinates = SRTM_1_SIZE+latitudeSecondsInCenterPlaneCoordinates;
        int longitudeSecondsInBig9PlanesCoordinates = SRTM_1_SIZE+longitudeSecondsInCenterPlaneCoordinates;
        return METERS_IN_SECOND*matrix[3*SRTM_1_SIZE-latitudeSecondsInBig9PlanesCoordinates][longitudeSecondsInBig9PlanesCoordinates];
    }

    private static int largestPowerOf2 (int n)
    {
        int res = 2;
        while (res < n) {
            res *= 2;
        }
        return res / 2;
    }

    @Deprecated
    public static float[] process9FilesTest() throws IOException {
        //int latitude = 45;
        //int longitude = 9;

        int top = 47;
        int middle = 46;
        int bottom = 45;

        int left = 8;
        int center = 9;
        int right = 10;


        float[][] topLeft = readFileToMatrix(getDemFile(top, left));
            float[][] topCenter = readFileToMatrix(getDemFile(top, center));
                float[][] topRight = readFileToMatrix(getDemFile(top, right));
        float[][] middleLeft = readFileToMatrix(getDemFile(middle, left));
            float[][] middleCenter = readFileToMatrix(getDemFile(middle, center));
                float[][] middleRight = readFileToMatrix(getDemFile(middle, right));
        float[][] bottomLeft = readFileToMatrix(getDemFile(bottom, left));
            float[][] bottomCenter = readFileToMatrix(getDemFile(bottom, center));
                float[][] bottomRight = readFileToMatrix(getDemFile(bottom, right));
        float[][] resulting = uniteMatrixes(topLeft,topCenter,topRight,middleLeft,middleCenter,middleRight,bottomLeft,bottomCenter,bottomRight);

        return matrixToArray(submatrix(resulting,TERRAIN_SIZE, 0, 0));
        //return matrixToArray(resulting);
    }

    private static RandomAccessFile getDemFile(int latitude, int longitude) throws FileNotFoundException {
        String filename = String.format(DEM_FILENAME_TEMPLATE,latitude,longitude);
        RandomAccessFile file = new RandomAccessFile(filename, "r");
        return file;
    }

    static float[][] readFileToMatrixOrNegative(int latitude, int longitude) throws IOException {
        try {
            RandomAccessFile file = getDemFile(latitude, longitude);
            return readFileToMatrix(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            float[][] negativeMatrix = new float[SRTM_1_SIZE][SRTM_1_SIZE];
            for (float[] row: negativeMatrix)
                Arrays.fill(row, NO_DATA_MARKER);
            return negativeMatrix;
        }
    }

    public static float[] processOneFileTest(int latitude,int longitude) throws IOException {
        //int latitude = 45;
        //int longitude = 9;
        //int longitude = 7;//for Matterhorn
        float[] matrix = readFileToArray(getDemFile(latitude,longitude));
        return matrix;
        //ImageIO.write(arrayToImageOLD(normalize(matrix, 0, 1)), "PNG", new File("heightValuesBIG.png"));
    }

}

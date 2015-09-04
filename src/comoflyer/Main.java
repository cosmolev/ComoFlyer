package comoflyer;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        //OffscreenComoFlyer app = new OffscreenComoFlyer(45.924008, 7.735102,-600/*manual altitude correction*/);
        OffscreenComoFlyer app = new OffscreenComoFlyer(45.5, 6.5);
        Pair<BufferedImage,float[][]> images = app.getImages();
        float[][] distanceMatrix = images.getValue();
        float[][] linearizedMatrix = StaticDepthHelpers.linearize(distanceMatrix, 8, 15000);
        BufferedImage depthMaskNormalized = StaticDepthHelpers.getDepthImage(distanceMatrix);
        File outputPanorama = new File("panorama.png");
        File outputDepth = new File("depth.png");
        try {
            ImageIO.write(images.getKey(), "png", outputPanorama);
            ImageIO.write(depthMaskNormalized, "png", outputDepth);
            StaticDepthHelpers.saveToCsv(linearizedMatrix, "linearized.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
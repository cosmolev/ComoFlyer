package comoflyer;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        OffscreenComoFlyer app = new OffscreenComoFlyer();
        Pair<BufferedImage,BufferedImage> images = app.getImages();
        File outputPanorama = new File("dpanorama.png");
        File outputDepth = new File("depth.png");
        try {
            ImageIO.write(images.getKey(), "png", outputPanorama);
            ImageIO.write(images.getValue(), "png", outputDepth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
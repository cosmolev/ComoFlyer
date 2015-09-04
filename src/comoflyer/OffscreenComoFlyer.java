package comoflyer;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import javafx.util.Pair;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class OffscreenComoFlyer extends GeoApplication {

    private FrameBuffer offBuffer;

    private static final int NUM_OF_ROTATIONS = 8;
    private int width = 200, height = 200;

    private final ByteBuffer cpuBuf = BufferUtils.createByteBuffer(width * height * 4);
    private final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);

    private boolean isRunning = false;

    private BufferedImage panoramaImg = new BufferedImage(NUM_OF_ROTATIONS*width, height, BufferedImage.TYPE_4BYTE_ABGR);

    private float[][] panoramaDistanceMatrix = new float[height][NUM_OF_ROTATIONS*width];

    private static Quaternion[] turns = new Quaternion[10];
    static {
        turns[0] = new Quaternion();turns[0].fromAngleAxis(-FastMath.PI * 0 / 180, new Vector3f(0, 1, 0));
        turns[1] = new Quaternion();turns[1].fromAngleAxis(-FastMath.PI * 45 / 180, new Vector3f(0, 1, 0));
        turns[2] = new Quaternion();turns[2].fromAngleAxis(-FastMath.PI * 90 / 180, new Vector3f(0, 1, 0));
        turns[3] = new Quaternion();turns[3].fromAngleAxis(-FastMath.PI * 135 / 180, new Vector3f(0, 1, 0));
        turns[4] = new Quaternion();turns[4].fromAngleAxis(-FastMath.PI * 180 / 180, new Vector3f(0, 1, 0));
        turns[5] = new Quaternion();turns[5].fromAngleAxis(-FastMath.PI * 225 / 180, new Vector3f(0, 1, 0));
        turns[6] = new Quaternion();turns[6].fromAngleAxis(-FastMath.PI * 270 / 180, new Vector3f(0, 1, 0));
        turns[7] = new Quaternion();turns[7].fromAngleAxis(-FastMath.PI * 315 / 180, new Vector3f(0, 1, 0));
        turns[8] = new Quaternion();turns[8].fromAngleAxis(-FastMath.PI * 360 / 180, new Vector3f(0, 1, 0));
        turns[9] = new Quaternion();turns[9].fromAngleAxis(-FastMath.PI * 405 / 180, new Vector3f(0, 1, 0));
    }

    public Pair<BufferedImage,float[][]> getImages(){
        this.setPauseOnLostFocus(false);
        AppSettings settings = new AppSettings(true);
        settings.setResolution(width, height);
        this.setSettings(settings);
        this.isRunning = true;
        start(Type.OffscreenSurface);
        while (isRunning){
            //nop
            System.out.print("");
        }
        System.out.println("App Stopped");
        return new Pair<>(panoramaImg,panoramaDistanceMatrix);
    }

    public void updateImageContents(){
        byte[] byteArr = ((ReadableDepthRenderer)renderer).getDepthBytes(cam.getWidth(), cam.getHeight());
        float[] floatArr = StaticDepthHelpers.toFloatArr(byteArr);
        float[][] floatMatrix = StaticDepthHelpers.flip(StaticDepthHelpers.toMatrix(floatArr, width));

        StaticDepthHelpers.writeOneMatrixIntoAnother(floatMatrix,panoramaDistanceMatrix,0,(rotateCounter-1) * width);

        cpuBuf.clear();
        renderer.readFrameBuffer(offBuffer, cpuBuf);

        Screenshots.convertScreenShot2(cpuBuf.asIntBuffer(), image);
        StaticDepthHelpers.flip(image);

        panoramaImg.createGraphics().drawImage(image, (rotateCounter - 1) * width, 0, null);

        if(rotateCounter == NUM_OF_ROTATIONS){
            this.stop();
            isRunning = false;
        }
    }

    public void setupOffscreenView(){


        // create a pre-view. a view that is rendered before the main view
        ViewPort offView = renderManager.createPreView("Offscreen View", cam);
        offView.setBackgroundColor(ColorRGBA.DarkGray);
        offView.setClearFlags(true, true, true);

        // this will let us know when the scene has been rendered to the
        // frame buffer
        offView.addProcessor(new OffscreenSceneProcessor(this));

        // create offscreen framebuffer
        offBuffer = new FrameBuffer(width, height, 1);

        //setup framebuffer to use renderbuffer
        // this is faster for gpu -> cpu copies
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorBuffer(Format.RGBA8);

        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        Node mainScene = setSomeCommonSettings();
        offView.attachScene(mainScene);

        cam.setFrustumPerspective(45f, 1f, 1, 30000);
    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(false);       // to hide the FPS panel
        setDisplayStatView(false);
        renderer = new ReadableDepthRenderer();

        setupOffscreenView();
        rotateCounter++;
        cam.setRotation(turns[rotateCounter]);
    }

    public OffscreenComoFlyer(double lat, double lon){
        super(lat, lon);
    }

    public OffscreenComoFlyer(double lat, double lon, int altitudeCorrectionInMeters){
        super(lat, lon, altitudeCorrectionInMeters);
    }

    public OffscreenComoFlyer(int latitude, int latitudeSeconds, int longitude, int longitudeSeconds) {
        super(latitude, latitudeSeconds, longitude, longitudeSeconds);
    }

    public OffscreenComoFlyer(int latitude, int latitudeSeconds, int longitude, int longitudeSeconds, int altitudeCorrectionInMeters) {
        super(latitude, latitudeSeconds, longitude, longitudeSeconds, altitudeCorrectionInMeters);
    }

    private int rotateCounter = 0;

    @Override
    public void simpleRender(RenderManager rm){
        rotateCounter++;
        cam.setRotation(turns[rotateCounter]);
        rootNode.updateGeometricState();
    }

}
package comoflyer;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.lwjgl.LwjglRenderer;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class OffscreenComoFlyer extends SimpleApplication {

    private FrameBuffer offBuffer;

    private static final int NUM_OF_ROTATIONS = 8;
    private static final int width = 600, height = 600;

    private final ByteBuffer cpuBuf = BufferUtils.createByteBuffer(width * height * 4);
    private final byte[] cpuArray = new byte[width * height * 4];
    private final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
    private BufferedImage depthMaskNormalized;

    private boolean isRunning = false;

    private BufferedImage panoramaImg = new BufferedImage(NUM_OF_ROTATIONS*width, height, BufferedImage.TYPE_4BYTE_ABGR);
    private BufferedImage panoramaDepth = new BufferedImage(NUM_OF_ROTATIONS*width, height, BufferedImage.TYPE_4BYTE_ABGR);

    private static Quaternion[] turns = new Quaternion[10];
    {
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

    public Pair<BufferedImage,BufferedImage> getImages(){
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
        return new Pair<BufferedImage,BufferedImage>(panoramaImg,panoramaDepth);
    }

    public void updateImageContents(){
        byte[] byteArr = ((ReadableDepthRenderer)renderer).getDepthBytes(cam.getWidth(), cam.getHeight());
        depthMaskNormalized = StaticDepthHelpers.getDepthImage(byteArr, cam.getWidth(), cam.getHeight());
        cpuBuf.clear();
        renderer.readFrameBuffer(offBuffer, cpuBuf);

        Screenshots.convertScreenShot2(cpuBuf.asIntBuffer(), image);
        StaticDepthHelpers.flip(image);

        panoramaImg.createGraphics().drawImage(image, (rotateCounter - 1) * width, 0, null);
        panoramaDepth.createGraphics().drawImage(depthMaskNormalized, (rotateCounter-1) * width, 0, null);

        if(rotateCounter == NUM_OF_ROTATIONS){
            this.stop();
            isRunning = false;
        }
    }

    public void setupOffscreenView(){
//        Camera camera = new Camera(width, height);
//        camera.setFrustumFar(3000);
//        camera.setLocation(new Vector3f(0, 30, 0));
        //cam.setLocation(new Vector3f(-700, 100, 300));
        cam.setLocation(new Vector3f(0, 40, 0));
        cam.setFrustumPerspective(45f, 1f, 1, 1000);
        //camera.setRotation(new Quaternion().fromAngles(new float[]{FastMath.PI * 0.06f, FastMath.PI * 0.65f, 0}));

        // create a pre-view. a view that is rendered before the main view
        ViewPort offView = renderManager.createPreView("Offscreen View", cam);
        offView.setBackgroundColor(ColorRGBA.DarkGray);
        offView.setClearFlags(true, true, true);

        // this will let us know when the scene has been rendered to the
        // frame buffer
        offView.addProcessor(new OffscreenSceneProcessor(this));

        // create offscreen framebuffer
        offBuffer = new FrameBuffer(width, height, 1);

        //setup framebuffer's texture
//        offTex = new Texture2D(width, height, Format.RGBA8);

        //setup framebuffer to use renderbuffer
        // this is faster for gpu -> cpu copies
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorBuffer(Format.RGBA8);
//        offBuffer.setColorTexture(offTex);

        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        Node mainScene = new Node("Main Scene");
        rootNode.attachChild(mainScene);

        mainScene.attachChild(StaticHelpers.createTerrain(assetManager));
        mainScene.addLight(StaticHelpers.getSun());
        mainScene.addLight(StaticHelpers.getLight());
        mainScene.attachChild(StaticHelpers.getSky(assetManager));
        offView.attachScene(mainScene);

//        // setup framebuffer's scene
//        Box boxMesh = new Box(Vector3f.ZERO, 1,1,1);
//        Material mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
//        Geometry offBox = new Geometry("box", boxMesh);
//        offBox.setMaterial(mat);
//
//        // attach the scene to the viewport to be rendered
//        offView.attachScene(offBox);
    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(false);       // to hide the FPS
        setDisplayStatView(false);
        renderer = new ReadableDepthRenderer();
        assetManager.registerLocator("D:\\ComoFlyer\\", FileLocator.class);
        setupOffscreenView();
        rotateCounter++;
        cam.setRotation(turns[rotateCounter]);
    }

    private int rotateCounter = 0;

//    @Override
//    public void simpleUpdate(float tpf){
//        Quaternion q = new Quaternion();
//        //angle += tpf;
//        angle += rotateCounter++;
//        cam.setLocation(new Vector3f(0, 30+(rotateCounter*40), 0));
////        angle %= FastMath.TWO_PI;
////        q.fromAngles(angle, 0, angle);
//
//        rootNode.setLocalRotation(q);
//        rootNode.updateLogicalState(tpf);
//        rootNode.updateGeometricState();
//    }

    @Override
    public void simpleRender(RenderManager rm){

//        Quaternion q = new Quaternion();
//        q.fromAngles(-5, 0, 0);
        //angle += tpf;
        rotateCounter++;
        cam.setRotation(turns[rotateCounter]);
        //cam.setRotation(q);
//        angle %= FastMath.TWO_PI;
//        q.fromAngles(angle, 0, angle);

        //rootNode.setLocalRotation(q);
        rootNode.updateGeometricState();
    }

}
package comoflyer;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

import java.nio.FloatBuffer;

/**
 *
 * @author Lev
 */
public class TestDepthOfFieldLev extends SimpleApplication {

    public static void main(String[] args) {
        TestDepthOfFieldLev app = new TestDepthOfFieldLev();
        app.start();
    }

//    @Override
//    public void simpleRender(RenderManager rm) {
//        byte[] byteArr = ((ReadableDepthRenderer)renderer).getDepthBytes(cam.getWidth(), cam.getHeight());
//        StaticDepthHelpers.processDepthBytes(byteArr, cam.getWidth(), cam.getHeight());
//    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(false);       // to hide the FPS
        setDisplayStatView(false);
        //renderer = new ReadableDepthRenderer();
        renderer = new SuperRenderer();

        assetManager.registerLocator("D:\\ComoFlyer\\", FileLocator.class);

        Node mainScene = new Node("Main Scene");
        rootNode.attachChild(mainScene);

        mainScene.attachChild(StaticHelpers.createTerrain(assetManager));
        mainScene.addLight(StaticHelpers.getSun());
        mainScene.addLight(StaticHelpers.getLight());
        mainScene.attachChild(StaticHelpers.getSky(assetManager));

        flyCam.setMoveSpeed(50);
        cam.setFrustumFar(3000);
        cam.setLocation(new Vector3f(0, 30, 0));
        cam.setRotation(new Quaternion().fromAngles(new float[]{FastMath.PI * 0.06f, FastMath.PI * 0.65f, 0}));

    }

}

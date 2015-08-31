package comoflyer;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

public class OnscreenComoFlyer extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        setDisplayFps(false);       // to hide the FPS
        setDisplayStatView(false);
        assetManager.registerLocator("D:\\ComoFlyer\\", FileLocator.class);
        Node mainScene = new Node("Main Scene");
        rootNode.attachChild(mainScene);

        mainScene.attachChild(StaticHelpers.createTerrain(assetManager));
        mainScene.addLight(StaticHelpers.getSun());
        mainScene.addLight(StaticHelpers.getLight());
        mainScene.attachChild(StaticHelpers.getSky(assetManager));

        cam.setLocation(new Vector3f(0, 30, 0));
    }

    public static void main(String[] args) {
        OnscreenComoFlyer app = new OnscreenComoFlyer();
        app.start();
    }

    private static final int NUM_OF_ROTATIONS = 10;
    private int rotateCounter = 1;

    Quaternion turn45 = new Quaternion();

//    @Override
//    public void simpleRender(RenderManager rm){
//
//        Quaternion turn45 = new Quaternion();
//        turn45.fromAngleAxis(FastMath.PI / 4, new Vector3f(0, 1, 0));
//        //angle += tpf;
//        rotateCounter++;
////        if(rotateCounter == NUM_OF_ROTATIONS){
////            cam.setLocation(new Vector3f(0, 30+(rotateCounter*40), 0));
////        }
//        cam.setRotation(turn45.mult(turn45));
////        angle %= FastMath.TWO_PI;
////        q.fromAngles(angle, 0, angle);
//
//        //rootNode.setLocalRotation(q);
//        rootNode.updateGeometricState();
//    }

}

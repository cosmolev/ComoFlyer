package comoflyer;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

public class StaticHelpers {

    static Spatial getSky(AssetManager assetManager){
        Spatial sky = SkyFactory.createSky(assetManager, "assets/Scenes/Beach/FullskiesSunset0068.dds", false);
        sky.setLocalScale(350);
        return sky;
    }

    static DirectionalLight getSun(){
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-4.9236743f, -1.27054665f, 5.896916f));
        sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
        return sun;
    }

    static DirectionalLight getLight(){
        DirectionalLight l = new DirectionalLight();
        l.setDirection(Vector3f.UNIT_Y.mult(-1));
        l.setColor(ColorRGBA.White.clone().multLocal(0.3f));
        return l;
    }

    static TerrainQuad createTerrain(AssetManager assetManager) {
        Material matRock = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);
        matRock.setBoolean("WardIso", true);

        Texture white = assetManager.loadTexture("grey.png");
        white.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("DiffuseMap", white);
        matRock.setFloat("DiffuseMap_0_scale", 64);

        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture("como512.png");
        try {
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
            heightmap.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TerrainQuad terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        terrain.setMaterial(matRock);
        terrain.setLocalScale(new Vector3f(5, 5, 5));
        terrain.setLocalTranslation(new Vector3f(0, -30, 0));
        terrain.setLocked(false); // unlock it so we can edit the height

        terrain.setShadowMode(RenderQueue.ShadowMode.Receive);
        return terrain;
    }

}

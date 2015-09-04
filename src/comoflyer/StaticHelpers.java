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
import demtools.HgtReader;
import javafx.util.Pair;

import java.io.IOException;

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

    static Pair<TerrainQuad,Float> createTerrain(AssetManager assetManager,
                                     int centerLatitude, int centerLatitudeSeconds, int centerLongitude, int centerLongitudeSeconds) {
        Material matRock = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);
        matRock.setBoolean("WardIso", true);

        Texture white = assetManager.loadTexture("grey.png");
        white.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("DiffuseMap", white);
        matRock.setFloat("DiffuseMap_0_scale", 64);

        //TerrainQuad terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());

        float[] heightMap = new float[]{
                0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,1,0,1,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,1,0,1,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,1,0,1,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,1,0,1,0,0,0,0,0,8,8,8,8,8,8,8,8,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,1,0,1,0,5,7,8,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,1,0,1,0,0,0,0,0,0,0,0,8,8,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,8,8,0,0,0,
                0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        };


        float height = 0;
        try {
            Pair<float[],Float> pair = HgtReader.processWithNegatives(centerLatitude,centerLatitudeSeconds,centerLongitude,centerLongitudeSeconds);
            heightMap = pair.getKey();
            height = pair.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TerrainQuad terrain = new TerrainQuad("terrain",2,17,heightMap);
        TerrainQuad terrain = new TerrainQuad("terrain",129,(int)Math.sqrt((double)heightMap.length),heightMap);

        terrain.setMaterial(matRock);
        terrain.setLocalScale(new Vector3f(HgtReader.TERRAIN_SCALE, HgtReader.TERRAIN_SCALE, HgtReader.TERRAIN_SCALE));
        //terrain.setLocalTranslation(new Vector3f(0, -30, 0));
        terrain.setLocked(false); // unlock it so we can edit the height

        terrain.setShadowMode(RenderQueue.ShadowMode.Receive);



        return new Pair<>(terrain,height);
    }

}

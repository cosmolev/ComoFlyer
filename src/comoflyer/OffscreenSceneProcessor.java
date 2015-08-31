package comoflyer;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;

public class OffscreenSceneProcessor implements SceneProcessor {

    private OffscreenComoFlyer app;

    public OffscreenSceneProcessor(OffscreenComoFlyer app) {
        this.app = app;
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue rq) {
    }

    /**
     * Update the CPU image's contents after the scene has
     * been rendered to the framebuffer.
     */
    @Override
    public void postFrame(FrameBuffer out) {
        app.updateImageContents();
    }

    @Override
    public void cleanup() {
    }

}

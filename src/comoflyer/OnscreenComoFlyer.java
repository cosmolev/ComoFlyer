package comoflyer;

public class OnscreenComoFlyer extends GeoApplication {

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(2000);
        cam.setFrustumNear(1f);

        setSomeCommonSettings();
    }

    public OnscreenComoFlyer(double lat, double lon){
        super(lat, lon);
    }

    public OnscreenComoFlyer(double lat, double lon, int heightCorrectionInMeters){
        super(lat, lon, heightCorrectionInMeters);
    }

    public OnscreenComoFlyer(int latitude, int latitudeSeconds, int longitude, int longitudeSeconds) {
        super(latitude, latitudeSeconds, longitude, longitudeSeconds);
    }

    public OnscreenComoFlyer(int latitude, int latitudeSeconds, int longitude, int longitudeSeconds, int heightCorrectionInMeters) {
        super(latitude, latitudeSeconds, longitude, longitudeSeconds, heightCorrectionInMeters);
    }

    public static void main(String[] args) {
        OnscreenComoFlyer app = new OnscreenComoFlyer(45.814149, 9.082836);
        app.start();
    }

}

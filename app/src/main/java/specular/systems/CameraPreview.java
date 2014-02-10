package specular.systems;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,SensorEventListener {
    String TAG = "camera";
    private SurfaceHolder mHolder;
    private static Camera mCamera;
    private SensorManager mSensorManager;
    private ArrayList<Sensor> sensors;

    public CameraPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        cp = this;
        mCamera = getCameraInstance();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> temp =  mSensorManager.getSensorList(Sensor.TYPE_ALL);
        sensors = new ArrayList<Sensor>();
        for(Sensor s:temp){
            if(s.getType()!=Sensor.TYPE_PROXIMITY&&s.getType()!=Sensor.TYPE_LIGHT&&s.getType()!=Sensor.TYPE_SIGNIFICANT_MOTION){
                sensors.add(s);
            }
        }
        sensorsData = new byte[sensors.size()][64];
        dataCollected = new int[sensors.size()];
        for(int i=0;i<dataCollected.length;i++)
            dataCollected[i]=0;
        for(Sensor sensor:sensors)
            mSensorManager.registerListener(this,sensor,50);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        frame=null;
        ready=false;
        mCamera.release();
        mSensorManager.unregisterListener(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    frame = data;
                    boolean found =false;
                    for(int i:dataCollected)
                        if(i<63){
                            found=true;
                            break;
                        }
                    if(!found)
                        ready=true;
                    else{
                        for(int a=0;a<sensors.size();a++)
                            Log.e("sensor status",sensors.get(a).getName()+" "+dataCollected[a]);
                    }
                }
            });

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private byte[] frame;
    private byte[][] sensorsData;
    private int[] dataCollected;
    public void reset(){
        for(int a =0;a<dataCollected.length;a++)
            dataCollected[a]=0;
        for(int a=0;a<sensorsData.length;a++)
            for(int b=0;b<sensorsData[a].length;b++)
                sensorsData[a][b]=0;
        for(Sensor sensor:sensors)
            mSensorManager.registerListener(this,sensor,5);
        ready=false;

    }
    public byte[] getData() {
        byte[] data = new byte[64];
        int block = frame.length/64;
        for (int a=0;a<64;a++) {
            data[a] = frame[block*a];
            for (byte[] aSensorsData : sensorsData)
                data[a] = (byte) (data[a] ^ aSensorsData[a]);

        }

        SecureRandom sr = new SecureRandom();
        byte[] rand = new byte[64];
        sr.nextBytes(rand);
        for(int a=0;a<64;a++)
            data[a] = (byte)(data[a]^rand[a]);
        reset();
        return data;
    }
    private static CameraPreview cp;
    public static CameraPreview getCameraPreview(){
        return cp;
    }

    public boolean ready = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        int i = sensors.indexOf(event.sensor);
        dataCollected[i]++;
        if(dataCollected[i]>=63)
            mSensorManager.unregisterListener(this,event.sensor);
        sensorsData[i][dataCollected[i]]=(byte)(event.values[0]*10000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

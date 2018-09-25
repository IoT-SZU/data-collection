package cn.ac.futurenet.data_collection.services;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.HashSet;

/**
 * Created by chenlin on 27/03/2018.
 */
public class DataCollectService implements SensorEventListener {
    private static DataCollectService instance;
    private static Application app;
    private static int[] sensorType;

    private HashSet<OnReceiveListener> listeners = new HashSet<>();
    private SensorManager sm;

    public static void initialize(Application app, int[] sensorType) {
        DataCollectService.app = app;
        DataCollectService.sensorType = sensorType;
    }

    public static DataCollectService getInstance() {
        if (instance == null) {
            instance = new DataCollectService();
            instance.sm = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        }
        return instance;
    }

    public void addEventListener(OnReceiveListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
            if (listeners.size() == 1) {
                for (int i = 0; i < sensorType.length; ++i) {
                    instance.sm.registerListener(instance, instance.sm.getDefaultSensor(sensorType[i]), SensorManager.SENSOR_DELAY_FASTEST);
                }
            }
        }
    }

    public void removeEventListener(OnReceiveListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            if (listeners.size() == 0) {
                sm.unregisterListener(this);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        for (OnReceiveListener listener : listeners) {
            listener.onReceive(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public interface OnReceiveListener {
        void onReceive(SensorEvent e);
    }

}

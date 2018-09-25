package cn.ac.futurenet.data_collection.services;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class StorageService {
    private static final String KEY_IP = "ip";
    private static final String KEY_DIR = "dir";
    private static final String KEY_SHOULD_SEGMENT = "segment";

    private static StorageService mInstance;

    private SharedPreferences storage;

    private String ip;
    private String dir;
    private Boolean shouldSegment;

    public static StorageService getInstance() {
        if (mInstance == null) {
            mInstance = new StorageService();
        }
        return mInstance;
    }

    public static void initialize(Application app) {
        getInstance().storage = app.getSharedPreferences("storage", Context.MODE_PRIVATE);
    }

    public void setIp(String ip) {
        if (this.ip == null || !this.ip.equals(ip)) {
            this.ip = ip;
            SharedPreferences.Editor editor = storage.edit();
            editor.putString(KEY_IP, ip);
            editor.commit();
        }
    }

    public void setDir(String dir) {
        if (this.dir == null || !this.dir.equals(dir)) {
            this.dir = dir;
            SharedPreferences.Editor editor = storage.edit();
            editor.putString(KEY_DIR, dir);
            editor.commit();
        }
    }

    public void setShouldSegment(boolean shouldSegment) {
        if (this.shouldSegment == null || this.shouldSegment != shouldSegment) {
            this.shouldSegment = shouldSegment;
            SharedPreferences.Editor editor = storage.edit();
            editor.putBoolean(KEY_SHOULD_SEGMENT, shouldSegment);
            editor.commit();
        }
    }

    public String getIp() {
        if (this.ip == null) {
            this.ip = storage.getString(KEY_IP, "172.31.73.46:8080");
        }
        return ip;
    }

    public String getDir() {
        if (this.dir == null) {
            this.dir = storage.getString(KEY_DIR, "gesture");
        }
        return dir;
    }

    public boolean isShouldSegment() {
        if (this.shouldSegment == null) {
            this.shouldSegment = storage.getBoolean(KEY_SHOULD_SEGMENT, false);
        }
        return this.shouldSegment;
    }
}

package cn.ac.futurenet.data_collection.utils;

import android.app.Activity;
import android.view.WindowManager;

public class Window {
    public static void keepScreenOn(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

package cn.ac.futurenet.data_collection.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class CommonUtil {
    private static CommonUtil commonUtil;

    private Context ctx;

    public static void initialize(Application app) {
        getInstance().ctx = app;
    }

    public static CommonUtil getInstance() {
        if (commonUtil == null) {
            commonUtil = new CommonUtil();
        }
        return commonUtil;
    }

    public void showMsg(String str) {
        Toast.makeText(this.ctx, str, Toast.LENGTH_SHORT).show();
    }

    public void showMsg(Activity activity, final String str) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMsg(str);
            }
        });
    }
}

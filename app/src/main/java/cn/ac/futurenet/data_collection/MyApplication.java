package cn.ac.futurenet.data_collection;

import android.app.Application;
import cn.ac.futurenet.data_collection.services.DataCollectService;
import cn.ac.futurenet.data_collection.services.StorageService;
import cn.ac.futurenet.data_collection.utils.CommonUtil;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DataCollectService.initialize(this, Constant.SENSOR_TYPE);
        StorageService.initialize(this);
        CommonUtil.initialize(this);
    }
}

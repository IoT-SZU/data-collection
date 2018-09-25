package cn.ac.futurenet.data_collection;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import cn.ac.futurenet.data_collection.services.StorageService;
import cn.ac.futurenet.data_collection.utils.CommonUtil;

public class ConfigActivity extends WearableActivity {
    private EditText ipEditText;
    private EditText dirEditText;
    private Switch segmentSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        ipEditText = findViewById(R.id.ipEditText);
        dirEditText = findViewById(R.id.dirEditText);
        segmentSwitch = findViewById(R.id.segmentSwitch);

        // Enables Always-on
        setAmbientEnabled();

        readConfig();
    }

    public void start(View view) {
        String ip = ipEditText.getText().toString();
        String dir = dirEditText.getText().toString();
        boolean shouldSegment = segmentSwitch.isChecked();

        if (isEmpty(ip) || isEmpty(dir)) {
            CommonUtil.getInstance().showMsg("ip or directory cannot be empty");
            return;
        }

        saveConfig(ip.trim(), dir.trim(), shouldSegment);

        startActivity(shouldSegment ?
                CollectSegmentSignalActivity.class : CollectOriginalSignalActivity.class);
    }

    private void startActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    private void saveConfig(String ip, String dir, boolean shouldSegment) {
        StorageService storageService = StorageService.getInstance();
        storageService.setIp(ip);
        storageService.setDir(dir);
        storageService.setShouldSegment(shouldSegment);
    }

    private void readConfig() {
        StorageService storageService = StorageService.getInstance();
        ipEditText.setText(storageService.getIp());
        dirEditText.setText(storageService.getDir());
        segmentSwitch.setChecked(storageService.isShouldSegment());
    }

    private boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        return str.trim().length() == 0;
    }
}

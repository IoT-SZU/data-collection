package cn.ac.futurenet.data_collection;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.AcceptDenyDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.ac.futurenet.data_collection.services.AudioService;
import cn.ac.futurenet.data_collection.services.DataCollectService;
import cn.ac.futurenet.data_collection.services.StorageService;
import cn.ac.futurenet.data_collection.utils.ArrayUtil;
import cn.ac.futurenet.data_collection.utils.CommonUtil;
import cn.ac.futurenet.data_collection.utils.FileTransfer;
import cn.ac.futurenet.data_collection.utils.Window;
import cn.ac.futurenet.data_collection.views.WaveView;

import java.util.ArrayList;

public class CollectOriginalSignalActivity extends WearableActivity
    implements DataCollectService.OnReceiveListener, FileTransfer.OnSendDataListener {

    private Button startBtn;
    private Button sendBtn;
    private TextView textViewStatus;
    private WaveView waveView;

    private int finishedCount;
    private boolean isRecording;
    private ArrayList<Float>[] aData;
    private ArrayList<Float>[] gData;

    private DataCollectService service;
    private AudioService audioService;
    private FileTransfer fileTransfer;
    private CommonUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_original_signal);

        startBtn = findViewById(R.id.startBtn);
        sendBtn = findViewById(R.id.sendBtn);
        textViewStatus = findViewById(R.id.textViewStatus);
        waveView = findViewById(R.id.waveView);

        service = DataCollectService.getInstance();
        audioService = new AudioService();
        fileTransfer = new FileTransfer(
                String.format("http://%s/file/", StorageService.getInstance().getIp()));
        util = CommonUtil.getInstance();

        aData = new ArrayList[3];
        gData = new ArrayList[3];
        for (int i = 0; i < aData.length; ++i) {
            aData[i] = new ArrayList<>();
            gData[i] = new ArrayList<>();
        }

        Window.keepScreenOn(this);
    }

    @Override
    public void onReceive(SensorEvent e) {
        ArrayList<Float>[] data = e.sensor.getType() == Sensor.TYPE_ACCELEROMETER ? aData : gData;
        if (aData[0].size() == 0) {
            util.showMsg(this, "开始采集数据");
        }
        data[0].add(e.values[0]);
        data[1].add(e.values[1]);
        data[2].add(e.values[2]);
    }

    public void clickStart(View view) {
        isRecording = !isRecording;
        startBtn.setText(isRecording ? "Stop" : "Start");

        if (isRecording) {
            sendBtn.setVisibility(View.INVISIBLE);
            waveView.setVisibility(View.VISIBLE);
            service.addEventListener(this);
            audioService.startRecord(waveView);
            for (int i = 0; i < 3; ++i) {
                aData[i].clear();
                gData[i].clear();
            }
        } else {
            sendBtn.setVisibility(View.VISIBLE);
            waveView.setVisibility(View.INVISIBLE);
            service.removeEventListener(this);
            audioService.stopRecrod();
        }
    }

    public void sendData(View view) {
        if (aData[0].size() == 0) {
            util.showMsg("请先采集数据再发送!");
            return;
        }

        finishedCount = 0;
        sendBtn.setEnabled(false);
        startBtn.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileTransfer.OnSendDataListener listener = CollectOriginalSignalActivity.this;
                String directory = StorageService.getInstance().getDir();
                Float[] tmp = new Float[0];
                String data = ArrayUtil.join(aData[0].toArray(tmp), " ");
                fileTransfer.sendFile(directory, "xAcceData", data, listener);
                data = ArrayUtil.join(aData[1].toArray(tmp));
                fileTransfer.sendFile(directory, "yAcceData", data, listener);
                data = ArrayUtil.join(aData[2].toArray(tmp));
                fileTransfer.sendFile(directory, "zAcceData", data, listener);
                data = ArrayUtil.join(gData[0].toArray(tmp));
                fileTransfer.sendFile(directory, "xGyroData", data, listener);
                data = ArrayUtil.join(gData[1].toArray(tmp));
                fileTransfer.sendFile(directory, "yGyroData", data, listener);
                data = ArrayUtil.join(gData[2].toArray(tmp));
                fileTransfer.sendFile(directory, "zGyroData", data, listener);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startBtn.setEnabled(true);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onFinished(final String err, final String dirname, final String filename, final String data) {
        if (err == null) {
            increaseFinishedCount();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AcceptDenyDialog dialog = new AcceptDenyDialog(CollectOriginalSignalActivity.this);
                    dialog.setTitle(String.format("文件 %s 发送失败", filename));
                    dialog.setMessage(String.format("错误信息: %s\n是否尝试重新发送？", err));
                    dialog.setPositiveButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fileTransfer.sendFile(dirname, filename, data,
                                    CollectOriginalSignalActivity.this);
                        }
                    });
                    dialog.setNegativeButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            increaseFinishedCount();
                        }
                    });
                    dialog.show();
                }
            });
        }
    }

    private void increaseFinishedCount() {
        synchronized (fileTransfer) {
            ++finishedCount;
            if (finishedCount == 6) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendBtn.setEnabled(true);
                        textViewStatus.setText("Finished");
                    }
                });
            }
        }
    }
}

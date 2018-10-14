package cn.ac.futurenet.data_collection;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.AcceptDenyDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.ac.futurenet.data_collection.services.StorageService;
import cn.ac.futurenet.data_collection.utils.ArrayUtil;
import cn.ac.futurenet.data_collection.utils.CommonUtil;
import cn.ac.futurenet.data_collection.utils.FileTransfer;
import cn.ac.futurenet.data_collection.utils.Window;

import java.util.ArrayList;

public class CollectSegmentSignalActivity extends SignalDetectBaseActivity
    implements FileTransfer.OnSendDataListener {
    private TextView statisticsInfo;
    private Button sendBtn;
    private Button deleteBtn;
    private Button startBtn;

    boolean isDetecting = false;
    boolean occurError;
    int finishedCount;
    FileTransfer fileTransfer;
    CommonUtil util;
    ArrayList<float[][][]> signals = new ArrayList<>();

    int totalSignalNum = 0; // 共检测到信号数量
    int hasSentSignalNum = 0; // 已发送信号数量

    public CollectSegmentSignalActivity() {
        super(Constant.SENSOR_TYPE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_segment_signal);

        statisticsInfo = findViewById(R.id.statisticsInfo);
        sendBtn = findViewById(R.id.sendBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        startBtn = findViewById(R.id.startBtn);

        fileTransfer = new FileTransfer(
                String.format("http://%s/file/", StorageService.getInstance().getIp()));
        util = CommonUtil.getInstance();

        Window.keepScreenOn(this);
    }

    public void clickStart(View view) {
        isDetecting = !isDetecting;
        startBtn.setText(isDetecting ? "Stop" : "Start");

        if (isDetecting) {
            sendBtn.setEnabled(false);
            updateStatisticsInfo();
            startDetectSignal();
        } else {
            sendBtn.setEnabled(true);
            stopDetectSignal();
        }
    }

    public void sendData(View view) {
        if (signals.size() == 0) {
            util.showMsg("没有需要发送的数据！请先采集数据。");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startBtn.setEnabled(false);
                sendBtn.setEnabled(false);
                deleteBtn.setEnabled(false);
                finishedCount = 0;
                occurError = false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileTransfer.OnSendDataListener listener = CollectSegmentSignalActivity.this;
                String directory = StorageService.getInstance().getDir();
                float[][][] signal = signals.get(0);
                String data = ArrayUtil.join(signal[0][0]);
                fileTransfer.sendFile(directory, "xAcceData", data, listener);
                data = ArrayUtil.join(signal[0][1]);
                fileTransfer.sendFile(directory, "yAcceData", data, listener);
                data = ArrayUtil.join(signal[0][2]);
                fileTransfer.sendFile(directory, "zAcceData", data, listener);
                data = ArrayUtil.join(signal[1][0]);
                fileTransfer.sendFile(directory, "xGyroData", data, listener);
                data = ArrayUtil.join(signal[1][1]);
                fileTransfer.sendFile(directory, "yGyroData", data, listener);
                data = ArrayUtil.join(signal[1][2]);
                fileTransfer.sendFile(directory, "zGyroData", data, listener);
            }
        }).start();
    }

    public void deleteData(View view) {
        if (signals.size() == 0) {
            return ;
        }

        signals.remove(signals.size() - 1);
        --totalSignalNum;
        updateStatisticsInfo();
    }

    @Override
    public void onDetect(float[][][] signal) {
        if (!isDetecting) {
            return;
        }

        signals.add(signal);
        ++totalSignalNum;
        updateStatisticsInfo();
    }

    @Override
    public void onStatusChanged(int status) {

    }

    @Override
    public void onFinished(final String err, final String dirname, final String filename, final String data) {
        if (err == null) {
            increaseFinishedCount();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AcceptDenyDialog dialog = new AcceptDenyDialog(CollectSegmentSignalActivity.this);
                    dialog.setTitle(String.format("文件 %s 发送失败", filename));
                    dialog.setMessage(String.format("错误信息: %s\n是否尝试重新发送？", err));
                    dialog.setPositiveButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fileTransfer.sendFile(dirname, filename, data,
                                    CollectSegmentSignalActivity.this);
                        }
                    });
                    dialog.setNegativeButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            occurError = true;
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
                if (!occurError) {
                    ++hasSentSignalNum;
                    signals.remove(0);
                    updateStatisticsInfo();
                    if (totalSignalNum > hasSentSignalNum) {
                        sendData(null);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startBtn.setEnabled(true);
                                sendBtn.setEnabled(true);
                                deleteBtn.setEnabled(true);
                                    }
                        });
                    }
                }
            }
        }
    }

    private void updateStatisticsInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statisticsInfo.setText(String.format(
                        "Total collected: %d\nHas sent: %d\nCurrent collected: %d\n",
                        totalSignalNum, hasSentSignalNum, totalSignalNum - hasSentSignalNum));
            }
        });
    }
}

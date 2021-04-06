package com.maple.rubbishseparator.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.util.StoreState;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;


public class RecordDialog extends Dialog {

    private Context context;

    private TextView tv_notice;
    private ImageView iv_record;


    private final int CHANGE_LEFT = 201;//更改剩余时间

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHANGE_LEFT:
                    int left = msg.arg1;
                    tv_notice.setText(context.getString(R.string.left) + String.valueOf(left) + "s");
                    break;

                default:
                    break;
            }
        }
    };

    public RecordDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }


    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setCanceledOnTouchOutside(true);
        initView();

        // 设置宽度为屏宽、靠近屏幕底部。
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        window.setWindowAnimations(R.style.dialog_animation); // 添加动画
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        @SuppressLint("InflateParams") View inflate = LayoutInflater.from(context).inflate(R.layout.dialog_record, null);
        tv_notice = inflate.findViewById(R.id.dialog_record_notice);
        iv_record = inflate.findViewById(R.id.dialog_record_icon);

        iv_record.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                //按下的时候
                startRecord();

            } else if (action == MotionEvent.ACTION_UP) {
                //松开的时候
                stopRecord();
            }
            return true;
        });
        setContentView(inflate);
    }

    MediaRecorder mediaRecorder;
    boolean isrecord = false;
    String filePath = "";
    int time = 0;//时长

    public void startRecord() {
        iv_record.setImageDrawable(context.getDrawable(R.drawable.recording_ing));
        tv_notice.setText(context.getText(R.string.release_get));
        isrecord = true;
        time = 0;
        filePath = "";
        new Thread(() -> {
            while (isrecord) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time++;
                if (time >= 5) {
                    //10秒开始，提示
                    int left = 10 - time;
                    Message msg = mHandler.obtainMessage();
                    msg.what = CHANGE_LEFT;
                    msg.arg1 = left;
                    msg.sendToTarget();
                }

                if (time == 10) {
                    stopRecord();
                    break;
                }
            }
        }).start();
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mediaRecorder == null)
            mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setMaxDuration(20000);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            String fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".m4a";
            filePath = StoreState.VOICE_STATE + fileName;
            /* ③准备 */
            mediaRecorder.setOutputFile(filePath);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /* ④开始 */
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.i("record", e.toString());
        }
    }

    public void stopRecord() {
        iv_record.setImageDrawable(context.getDrawable(R.drawable.recording));
        tv_notice.setText(context.getText(R.string.touch_record));
        if (isrecord) {
            isrecord = false;
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (RuntimeException e) {
                Log.e("record", e.toString());
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        }

        if (time < 2) {
            Toast.makeText(context, context.getString(R.string.time_min), Toast.LENGTH_SHORT).show();
        } else {
            recordBack.recordBack(filePath);
            filePath = "";
            dismiss();
        }
    }


    public interface RecordBack {
        void recordBack(String url);//返回录音的地址
    }

    public void setRecordListener(RecordBack recordListener) {
        this.recordBack = recordListener;
    }

    public RecordBack recordBack;

}

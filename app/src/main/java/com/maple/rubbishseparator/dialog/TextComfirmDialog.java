package com.maple.rubbishseparator.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.maple.rubbishseparator.R;

public class TextComfirmDialog extends Dialog {
    private final Context context;
    private final int dialog_id;

    private final String text1;
    private final String text2;
    public OnButtomClickedListener listener;
    public TextComfirmDialog(Context context, String text1, String text2, int id) {
        super(context, R.style.CustomDialog_1);
        this.context = context;
        this.text1 = text1;
        this.text2 = text2;
        this.dialog_id = id;
    }
        /*
    定义回调接口
     */

    public interface OnButtomClickedListener {
        void onButtomClick(int num, int dialog_id);
    }

    public void setButtomClickedListener(OnButtomClickedListener listener) {
        this.listener = listener;
    }

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        View view = View.inflate(context, R.layout.dialog_textcomfirm, null);
        TextView tv_title = view.findViewById(R.id.textdialog_title);
        TextView tv_text = view.findViewById(R.id.textdialog_text);
        FrameLayout layout_cancel = view.findViewById(R.id.textdialog_cancel);
        FrameLayout layout_agree = view.findViewById(R.id.textdialog_agree);

        tv_title.setText(text1);
        tv_text.setText(text2);

        layout_cancel.setOnClickListener(view1 -> dismiss());

        layout_agree.setOnClickListener(view12 -> {
            dismiss();
            listener.onButtomClick(1, dialog_id);
        });
        setContentView(view);
    }
}

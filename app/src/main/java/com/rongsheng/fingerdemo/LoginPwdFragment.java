package com.rongsheng.fingerdemo;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;



public class LoginPwdFragment extends DialogFragment implements View.OnClickListener {

    public static final String TYPE = "type";    //指纹 or pwd

    public int getType() {
        return type;
    }

    private int type = 0;

    private AppCompatEditText psw_input;


    private TextView mTextViewHint;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
        Dialog dialog = new Dialog(getActivity(), R.style.BottomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定

        Bundle bundle = getArguments();

        if (bundle != null) {
            type = bundle.getInt(TYPE,0);
            switch (type){
                case 1:
                    dialog.setContentView(R.layout.fragment_finger);
                    initFingerView(dialog);
                    break;
                default:
                    dialog.setContentView(R.layout.fragment_pwd);
                    initPwdView(dialog);
                    break;
            }
        }else{
            dialog.setContentView(R.layout.fragment_pwd);
            initPwdView(dialog);
        }

        dialog.setCanceledOnTouchOutside(false); // 外部点击取消

        // 设置宽度为屏宽, 靠近屏幕底部。
        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.AnimBottom);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);
        return dialog;
    }

    private void initFingerView(Dialog dialog) {
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(this);
        dialog.findViewById(R.id.btn_pwd).setOnClickListener(this);
        mTextViewHint = (TextView) dialog.findViewById(R.id.tv_hint);
    }

    private void initPwdView(Dialog dialog) {
        psw_input = (AppCompatEditText) dialog.findViewById(R.id.pwdView);
        dialog.findViewById(R.id.iv_close).setOnClickListener(this);
        dialog.findViewById(R.id.tv_miss_pwd).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.tv_miss_pwd:
                //确定
                ((MainActivity)getActivity()).clickOk2Intent(psw_input.getText().toString().trim());
                dismiss();
                break;
            case R.id.btn_cancel:
                ((MainActivity)getActivity()).cancelAuthenticate();
                dismiss();
                break;
            case R.id.btn_pwd:
                ((MainActivity)getActivity()).cancelAuthenticate();
                dismiss();
                ((MainActivity)getActivity()).intoPwdFragment();
                break;
        }
    }


    /**
     * 指纹错误提示
     */
    public boolean setTextHint(){
        if (mTextViewHint != null){
            mTextViewHint.setText("指纹识别失败，请重试！");
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.text_shake);
            mTextViewHint.startAnimation(shake);
            return true;
        }
        return false;
    }

}


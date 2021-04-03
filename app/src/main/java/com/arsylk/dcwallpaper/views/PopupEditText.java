package com.arsylk.dcwallpaper.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.arsylk.dcwallpaper.R;

public class PopupEditText extends android.support.v7.widget.AppCompatTextView implements View.OnClickListener {
    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }
    private AlertDialog dialog;
    private RelativeLayout dialogView;
    private EditText editText;
    private OnTextChangedListener onTextChangedListener = null;

    public PopupEditText(Context context) {
        super(context);
        init();
    }

    public PopupEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PopupEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dialogView = (RelativeLayout) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_popup_edittext, null, false);

        editText = new android.support.v7.widget.AppCompatEditText(getContext()) {
            @Override
            public boolean onKeyPreIme(int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog.dismiss();
                    return true;
                }
                return super.onKeyPreIme(keyCode, event);
            }

            @Override
            protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
                String formatted = text.toString().replace("\n", "\t");
                PopupEditText.this.setText(formatted);
                if(onTextChangedListener != null) onTextChangedListener.onTextChanged(formatted);
            }
        };
        editText.setGravity(Gravity.TOP | Gravity.START);
        editText.setBackgroundColor(Color.TRANSPARENT);

        ((RelativeLayout) dialogView.findViewById(R.id.placeholder_layout))
                .addView(editText, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setPositiveButton("Close", null);
        dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(!dialog.isShowing()) {
            dialog.show();
            editText.setText(getText().toString().replace("\t", "\n"));
            editText.setSelection(editText.getText().length());
        }else {
            dialog.dismiss();
        }
    }

    public void setDialogTitle(String title) {
        dialog.setTitle(title);
    }

    public void setOnTextChangedListener(OnTextChangedListener onTextChangedListener) {
        this.onTextChangedListener = onTextChangedListener;
    }
}

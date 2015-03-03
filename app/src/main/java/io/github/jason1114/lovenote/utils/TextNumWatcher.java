package io.github.jason1114.lovenote.utils;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;


/**
 * User: qii
 * Date: 12-9-2
 */
public class TextNumWatcher implements TextWatcher {

    private TextView tv;
    private EditText et;
    private Activity activity;

    public TextNumWatcher(TextView tv, EditText et, Activity activity) {
        this.tv = tv;
        this.et = et;
        this.activity = activity;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int sum = Utility.length(et.getText().toString());
        tv.setText(String.valueOf(sum));
        tv.setTextColor(ThemeUtility
                .getColor(activity, android.R.attr.actionMenuTextColor));
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}

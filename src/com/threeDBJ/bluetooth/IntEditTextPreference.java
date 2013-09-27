package com.threeDBJ.bluetooth.test;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.AttributeSet;

public class IntEditTextPreference extends EditTextPreference {
    static final String TAG = "BTAndroid";
    int defaultValue, min, max;

    public IntEditTextPreference(Context context) {
        super(context);
	setup();
    }

    public IntEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
	setup();
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
	setup();
    }

    private void setup() {
	getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
	setDefaultValue(0);
    }

    public void setDefaultValue(int value) {
	super.setDefaultValue(Integer.toString(value));
	defaultValue = value;
    }

    public void setBounds(final int min, final int max) {
	this.min = min;
	this.max = max;
	InputFilter filter = new InputFilter() { 
		public CharSequence filter(CharSequence source, int start, int end, 
					   Spanned dest, int dstart, int dend) { 
		    try {
			int num = Integer.parseInt(dest.toString()+source.toString());
			if(num < min || num > max) {
			    return "";
			}
		    } catch(Exception e) {
			return "";
		    }
		    return null; 
		} 
	    };
	getEditText().setFilters(new InputFilter[] { filter });
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
	int value = getPersistedInt(defaultValue);
        return String.valueOf((value < min) ? min : value);
    }

    @Override
    protected boolean persistString(String value) {
	if(value.length() == 0) {
	    DebugLog.e(TAG, "Persisted: "+defaultValue);
	    return persistInt(defaultValue);
	} else {
	    return persistInt(Integer.valueOf(value));
	}
    }
}
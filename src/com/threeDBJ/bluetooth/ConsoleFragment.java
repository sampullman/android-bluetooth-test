package com.threeDBJ.bluetooth.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ConsoleFragment extends BluetoothFragment {
    private static final String[] autocomplete = new String[] {""};
    String lineTerm;
    StringBuilder consoleText = new StringBuilder();
    int displayMode, outputMode;

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);
	//activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	//InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
	retrievePreferences();
        View v = inflater.inflate(R.layout.console, container, false);
	updateOutputMode(v);
	restoreState(savedState);
	AutoCompleteTextView input = (AutoCompleteTextView)v.findViewById(R.id.input);
	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocomplete);
	input.setAdapter(adapter);

	input.setOnEditorActionListener(new OnEditorActionListener() {        
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    if(actionId==EditorInfo.IME_ACTION_SEND) {
			sendMessage(v.getText().toString());
			return true;
		    }
		    DebugLog.e(TAG, "Action="+actionId+", sent="+v.getText() + lineTerm);
		    return false;
		}
	    });
        return v;
    }

    @Override
    public void settingsModified() {
	int oldDisplayMode = displayMode, oldOutputMode = outputMode;
	retrievePreferences();
	if(displayMode != oldDisplayMode) {
	    consoleText = TestUtil.convertMessage(consoleText, oldDisplayMode, displayMode);
	    TextView console = (TextView)getView().findViewById(R.id.output);
	    console.setText(consoleText);
	}
	if(outputMode != oldOutputMode) {
	    updateOutputMode(getView());
	    EditText outputText = (EditText)getView().findViewById(R.id.input);
	    StringBuilder currentOutput = new StringBuilder(outputText.getText());
	    outputText.setText(TestUtil.convertMessage(currentOutput, oldOutputMode, outputMode).toString());
	}
    }

    private void retrievePreferences() {
	Activity activity = getActivity();
	Context app = activity.getApplicationContext();
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
	lineTerm = Settings.getConsoleLineTerm(prefs);
	displayMode = Settings.getConsoleDisplayMode(prefs);
	outputMode = Settings.getConsoleOutputMode(prefs);
    }

    private void updateOutputMode(View root) {
	EditText text = (EditText)root.findViewById(R.id.input);
	if(outputMode == Settings.ASCII_MODE) {
	    text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	} else if(outputMode == Settings.BYTE_MODE) {
	    text.setInputType(InputType.TYPE_CLASS_PHONE);
	}
    }

    private void sendMessage(String msg) {
	if(outputMode == Settings.ASCII_MODE) {
	    // Don't worry about validation
	    btClient.write(msg + lineTerm);
	} else if(outputMode == Settings.BYTE_MODE) {
	    // Assert that the input consists of space separated values between 0 and 255
	    boolean valid = true;
	    String[] strBytes = msg.split(" ");
	    byte[] bytes = new byte[strBytes.length];
	    int b;
	    try {
		for(int i=0;i<strBytes.length;i+=1) {
		    b = Integer.parseInt(strBytes[i]);
		    if(b < 0 || b > 255) {
			valid = false;
			break;
		    }
		    bytes[i] = (byte)b;
		}
		if(valid) {
		    btClient.write(bytes);
		    if(lineTerm != null && lineTerm.length() > 0) btClient.write(lineTerm);
		}
	    } catch(Exception e) {
		valid = false;
	    }
	    if(!valid) {
		TestUtil.shortToast(getActivity(), "Error - Bytes must be space separated and between 0 and 255");
	    }
	}
	    
    }

    @Override
    public void handleMessage(Message msg) {
	DebugLog.e(TAG, "Console message");
	if(displayMode == Settings.ASCII_MODE) {
	    consoleText.append((char)msg.what);
	} else if(displayMode == Settings.BYTE_MODE) {
	    if(consoleText.length() > 0) consoleText.append(" ");
	    consoleText.append(Integer.toString(msg.what));
	}
	TextView console = (TextView)getView().findViewById(R.id.output);
	console.setText(consoleText);
    }

    @Override
    public void onPause() {
	super.onPause();
	Activity activity = getActivity();
	InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(getView().findViewById(R.id.input).getWindowToken(), 0);
	DebugLog.e(TAG, "console onPause");
    }

    public void onResume() {
	super.onResume();
	Activity activity = getActivity();
	InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	DebugLog.e(TAG, "console onResume");
    }

    @Override
    public void saveState(Bundle state) {
    }

    @Override
    public void restoreState(Bundle state) {
    }
}
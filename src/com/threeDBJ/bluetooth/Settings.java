package com.threeDBJ.bluetooth.test;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class Settings extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
    public static String TAG = "BTAndroid";

    /* Controller preference keys */
    public static final String BUTTON_TITLE_PREFIX = "buttonTitle";
    public static final String BUTTON_VALUE_PREFIX = "buttonValue";
    public static final String NUM_BUTTONS = "numButtons";
    public static final String BUTTONS_PER_ROW = "buttonsPerRow";
    public static final String NUM_SLIDERS = "numSliders";
    public static final String SLIDER_PREPEND_PREFIX = "sliderPrepend";

    /* Console preference keys */
    public static final String CONSOLE_LINE_TERMINATOR = "consoleLineTerm";
    public static final String CONSOLE_DISPLAY_MODE = "consoleDisplayMode";
    public static final String CONSOLE_OUTPUT_MODE = "consoleOutputMode";

    /* Character modes */
    public static final int ASCII_MODE = 0;
    public static final int BYTE_MODE = 1;

    public static int RESULT_CHANGED = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
	setPreferenceScreen(root);
	buildSettings();
	
	setResult(RESULT_CANCELED);
    }

    private void buildSettings() {
	generateControllerPreferences();
	generateConsolePreferences();
    }

    private void generateControllerPreferences() {
	PreferenceCategory root = new PreferenceCategory(this);
	root.setTitle("Controller");
	getPreferenceScreen().addPreference(root);

	generateButtonPreferences(root);

	IntEditTextPreference numButtons = new IntEditTextPreference(this);
	numButtons.setKey(NUM_BUTTONS);
	numButtons.setTitle("Number of buttons");
	numButtons.setSummary("Total number of action buttons");
	numButtons.setDialogTitle("Number of buttons (0 - 20)");
	numButtons.setDefaultValue(2);
	numButtons.setBounds(0, 20);
	root.addPreference(numButtons);

	IntEditTextPreference buttonsPerRow = new IntEditTextPreference(this);
	buttonsPerRow.setKey(BUTTONS_PER_ROW);
	buttonsPerRow.setTitle("Buttons per row");
	buttonsPerRow.setDialogTitle("Buttons per row (1 - 4)");
	buttonsPerRow.setDefaultValue(2);
	buttonsPerRow.setBounds(1, 4);
	root.addPreference(buttonsPerRow);

	IntEditTextPreference numSliders = new IntEditTextPreference(this);
	numSliders.setKey(NUM_SLIDERS);
	numSliders.setTitle("Number of sliders");
	numSliders.setDialogTitle("Number of sliders (0 - 4)");
	numSliders.setDefaultValue(0);
	numSliders.setBounds(0, 4);
	root.addPreference(numSliders);

	generateSliderPreferences(root);
    }

    private void generateButtonPreferences(PreferenceCategory root) {
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

	PreferenceScreen titlePrefs = getPreferenceManager().createPreferenceScreen(this);
        titlePrefs.setKey("button_titles");
        titlePrefs.setTitle("Button Titles");
        titlePrefs.setSummary("Change the text for the current buttons");
        root.addPreference(titlePrefs);
	PreferenceScreen valuePrefs = getPreferenceManager().createPreferenceScreen(this);
        valuePrefs.setKey("button_values");
        valuePrefs.setTitle("Button Values");
        valuePrefs.setSummary("Change what the button sends when pressed");
        root.addPreference(valuePrefs);

	int numButtons = getNumButtons(prefs);
	for(int i=0; i<numButtons;  i+=1) {
	    String title = getButtonTitle(prefs, i);
	    EditTextPreference titlePref = new EditTextPreference(this);
	    titlePref.setDialogTitle("Button "+i+" Title");
	    titlePref.setKey(BUTTON_TITLE_PREFIX+i);
	    titlePref.setTitle("Button "+i+" Title");
	    titlePref.setSummary("Set button "+i+" text (currently "+title+")" );
	    titlePref.setDefaultValue("Button "+i);
	    titlePrefs.addPreference(titlePref);

	    EditTextPreference valuePref = new EditTextPreference(this);
	    valuePref.setDialogTitle(title+" Command");
	    valuePref.setKey(BUTTON_VALUE_PREFIX+i);
	    valuePref.setTitle(title+" Command");
	    valuePref.setSummary("Set the command for button "+i);
	    valuePref.setDefaultValue(Integer.toString(i));
	    valuePrefs.addPreference(valuePref);
	}
    }

    private void generateSliderPreferences(PreferenceCategory root) {
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

	PreferenceScreen sliderPrepends = getPreferenceManager().createPreferenceScreen(this);
        sliderPrepends.setKey("slider_prepends");
        sliderPrepends.setTitle("Slider Prepends");
        sliderPrepends.setSummary("Characters to prepend each send slider value");
        root.addPreference(sliderPrepends);

	int numSliders = getNumSliders(prefs);
	for(int i=0; i<numSliders; i+=1) {
	    EditTextPreference prepend = new EditTextPreference(this);
	     prepend.setDialogTitle("Slider "+i+" Prepend");
	     prepend.setKey(SLIDER_PREPEND_PREFIX+i);
	     prepend.setTitle("Slider "+i+" Prepend");
	     prepend.setSummary("Set the characters to prepend to slider "+i+" messages");
	     prepend.setDefaultValue("");
	     sliderPrepends.addPreference(prepend);
	}
    }

    private void generateConsolePreferences() {
	PreferenceCategory root = new PreferenceCategory(this);
	root.setTitle("Console");
	getPreferenceScreen().addPreference(root);

	ListPreference lineTerm = new ListPreference(this);
	lineTerm.setDialogTitle("Line Terminator");
	lineTerm.setKey(CONSOLE_LINE_TERMINATOR);
	lineTerm.setTitle("Line Terminator");
	lineTerm.setSummary("Sets the characters to append to every outgoing console message");
	lineTerm.setEntries(R.array.line_term_entries);
	lineTerm.setEntryValues(R.array.line_term_entryvalues);
	lineTerm.setDefaultValue("");
	root.addPreference(lineTerm);

	ListPreference displayMode = new ListPreference(this);
	displayMode.setDialogTitle("Display Mode");
	displayMode.setKey(CONSOLE_DISPLAY_MODE);
	displayMode.setTitle("Display Mode");
	displayMode.setSummary("Determines how incomming data is displayed in the console.");
	displayMode.setEntries(R.array.message_type_entries);
	displayMode.setEntryValues(R.array.message_type_entryvalues);
	displayMode.setDefaultValue(Integer.toString(ASCII_MODE));
        root.addPreference(displayMode);

	ListPreference outputMode = new ListPreference(this);
	outputMode.setDialogTitle("Output Mode");
	outputMode.setKey(CONSOLE_OUTPUT_MODE);
	outputMode.setTitle("Output Mode");
	outputMode.setSummary("Determines how console output commands are interpreted.");
	outputMode.setEntries(R.array.message_type_entries);
	outputMode.setEntryValues(R.array.message_type_entryvalues);
	outputMode.setDefaultValue(Integer.toString(ASCII_MODE));
        root.addPreference(outputMode);
    }

    /* Controller preference access functions */
    public static int getNumButtons(SharedPreferences prefs) {
	return prefs.getInt(NUM_BUTTONS, 2);
    }

    /* Extra check since if settings get screwed up a 0 returned here will crash app */
    public static int getButtonsPerRow(SharedPreferences prefs) {
	int bpr = prefs.getInt(BUTTONS_PER_ROW, 2);
	return (bpr  <  1) ? 1 : bpr;
    }

    public static String getButtonTitle(SharedPreferences prefs, int buttonNum) {
	return prefs.getString(BUTTON_TITLE_PREFIX+buttonNum, "Button "+buttonNum);
    }

    public static String getButtonValue(SharedPreferences prefs, int buttonNum) {
	return prefs.getString(BUTTON_VALUE_PREFIX+buttonNum, Integer.toString(buttonNum));
    }

    public static int getNumSliders(SharedPreferences prefs) {
	return prefs.getInt(NUM_SLIDERS, 0);
    }

    public static String getSliderPrepend(SharedPreferences prefs, int sliderNum) {
	return prefs.getString(SLIDER_PREPEND_PREFIX+sliderNum, "");
    }

    /* Console preference access functions */
    public static String getConsoleLineTerm(SharedPreferences prefs) {
	return prefs.getString(CONSOLE_LINE_TERMINATOR, "");
    }

    public static int getConsoleDisplayMode(SharedPreferences prefs) {
	return Integer.parseInt(prefs.getString(CONSOLE_DISPLAY_MODE, Integer.toString(ASCII_MODE)));
    }
    
    public static int getConsoleOutputMode(SharedPreferences prefs) {
	return Integer.parseInt(prefs.getString(CONSOLE_OUTPUT_MODE, Integer.toString(ASCII_MODE)));
    }

    @Override
    protected void onResume() {
	super.onResume();
	// Set up a listener whenever a key changes
	getPreferenceScreen().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
	super.onPause();
	// Unregister the listener whenever a key changes
	getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)  {
	setResult(RESULT_CHANGED);
	if(NUM_BUTTONS.equals(key) || NUM_SLIDERS.equals(key)) {
	    PreferenceScreen root = getPreferenceScreen();
	    root.removeAll();
	    buildSettings();
	}   
    }

}
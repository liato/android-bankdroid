//This class  based on tutorial found here https://github.com/commonsguy/cw-lunchlist/blob/master/19-Alarm/LunchList/src/apt/tutorial/TimePreference.java
package com.liato.bankdroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {
	private int lastValue = 0;
	private TimePicker picker = null;

	public TimePreference(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);

		setPositiveButtonText("Set");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected View onCreateDialogView() {
		picker = new TimePicker(getContext());
		picker.setIs24HourView(true);
		return picker;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		picker.setCurrentHour(lastValue / 60);
		picker.setCurrentMinute(lastValue % 60);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			lastValue = picker.getCurrentHour() * 60 + picker.getCurrentMinute(); 

			if (callChangeListener(lastValue))
				persistInt(lastValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getInt(index, 0));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		int val = 0;
		
		if (restoreValue) {
			val = getPersistedInt(val);
		} else {
			try{
				val = Integer.parseInt(defaultValue.toString());
			} catch (NumberFormatException e) { }
		}
		
		lastValue = val;
	}
}
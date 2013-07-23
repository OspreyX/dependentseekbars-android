package com.oanda.dependentseekbars.example;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.dependentseekbars.R;
import com.oanda.dependentseekbars.lib.DependentSeekBar;
import com.oanda.dependentseekbars.lib.DependentSeekBarManager;

public class EditFieldSampleActivity extends Activity {
	private DependentSeekBarManager manager;
	private DependentSeekBar[] dependentSeekBars;
	private LinearLayout mainLayout;
	private EditText[] editText;
	private boolean validValue = true;
	private boolean pauseTextChangeListener = false;
	private final int ROW_LAYOUT_COUNT = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.editfield_sample_layout);

        manager = new DependentSeekBarManager();

		mainLayout = (LinearLayout) findViewById(R.id.slider_layout);

		dependentSeekBars = new DependentSeekBar[4];
		dependentSeekBars[0] = (DependentSeekBar) findViewById(R.id.seekBar1);
		dependentSeekBars[1] = (DependentSeekBar) findViewById(R.id.seekBar2);
		dependentSeekBars[2] = (DependentSeekBar) findViewById(R.id.seekBar3);
		dependentSeekBars[3] = (DependentSeekBar) findViewById(R.id.seekBar4);

		editText = new EditText[4];
		editText[0] = (EditText) findViewById(R.id.editText1);
		editText[1] = (EditText) findViewById(R.id.editText2);
		editText[2] = (EditText) findViewById(R.id.editText3);
		editText[3] = (EditText) findViewById(R.id.editText4);

		for (int i = 0; i < 4; i++) {
			manager.addSeekBar(dependentSeekBars[i]);

			final DependentSeekBar seekBar = dependentSeekBars[i];
			seekBar.setProgress(i);
			final EditText editField = editText[i];

			attachListeners(seekBar, editField);
		}

		// In additon to creating creating each individual DependentSeekBar in
		// the xml layout file, another good
		// way to create a layout is to define the layout for a row and inflate
		// multiple instances of it.
		// This makes scaling much easier if you ever want to add addition bars
		// and also allows you to rearrange the
		// rows.
		for (int i = 0; i < ROW_LAYOUT_COUNT; i++) {
			RelativeLayout rowLayout = (RelativeLayout) getLayoutInflater()
					.inflate(R.layout.editfield_sample_row, null);
			final DependentSeekBar seekBar = (DependentSeekBar) rowLayout
					.findViewById(R.id.seekbar);
			final EditText editField = (EditText) rowLayout
					.findViewById(R.id.seekbar_value);
			manager.addSeekBar(seekBar);

			seekBar.setProgress(i + 4);
			attachListeners(seekBar, editField);

			// Add the new row to the layout
			mainLayout.addView(rowLayout);

			// Set up some dependencies so that the rows are actually used
			manager.getSeekBar(0).addDependencies(DependentSeekBar.Dependency.LESS_THAN,
					seekBar);

		}

		// Set up dependencies so that 0 < 1,2,3 ; 1,2 < 3
		manager.getSeekBar(0).addDependencies(DependentSeekBar.Dependency.LESS_THAN, 1, 2,
				3);
		manager.getSeekBar(3).addDependencies(DependentSeekBar.Dependency.GREATER_THAN, 2);
		manager.getSeekBar(1).addDependencies(DependentSeekBar.Dependency.LESS_THAN, 3);

		manager.setShiftingAllowed(true);

	}

	private void attachListeners(final DependentSeekBar seekBar,
			final EditText editField) {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				pauseTextChangeListener = true;
				editField.setText(String.valueOf(seekBar.getProgress()));
				pauseTextChangeListener = false;
			}
		});
		editField.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					editField.setSelection(editField.getText().length());
                    seekBar.startShiftEvent();
				} else {
                    seekBar.endShiftEvent();
                }
                if (!hasFocus && !validValue) {
                    pauseTextChangeListener = true;
                    editField.setTextColor(Color.BLACK);
                    pauseTextChangeListener = false;
                    validValue = true;
                }
			}
		});
		editField.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (!validValue) {
					pauseTextChangeListener = true;
					editField.setTextColor(Color.BLACK);
					editField.setText(String.valueOf(seekBar.getProgress()));
					pauseTextChangeListener = false;
					validValue = true;
				}
                findViewById(R.id.slider_layout).requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromInputMethod(getWindow().getCurrentFocus
                        ().getWindowToken(), 0);
				return false;
			}
		});
		editField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (pauseTextChangeListener) {
					return;
				}
				if (editField.hasFocus()) {
					try {
						pauseTextChangeListener = true;
						if (seekBar.moveTo(Integer.parseInt(s.toString()))) {
							validValue(seekBar, editField, s);
						} else {
							notValidValue();
						}
					} catch (Exception e) {
						notValidValue();
					} finally {
					    pauseTextChangeListener = false;
					}
				}
			}

			private void validValue(final SeekBar iBar,
					final EditText iValue, CharSequence s) {
				iValue.setText(s.toString());
				iValue.setTextColor(Color.BLACK);
				iValue.setSelection(iValue.getText().length());
				validValue = true;
			}

			private void notValidValue() {
				validValue = false;
				editField.setTextColor(Color.RED);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}
}

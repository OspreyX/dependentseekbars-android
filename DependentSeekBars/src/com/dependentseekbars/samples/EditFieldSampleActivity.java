package com.dependentseekbars.samples;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.dependentseekbars.DependentSeekBar;
import com.dependentseekbars.DependentSeekBarManager;
import com.dependentseekbars.R;

public class EditFieldSampleActivity extends Activity {
    private DependentSeekBarManager manager;
    private DependentSeekBar[] dependentSeekBars;
    private LinearLayout mainLayout;
    private EditText[] editText;
    private boolean validValue = true;
    private boolean pauseTextChangeListener = false;
    private int extraBars = 8;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editfield_sample_layout);

        manager = new DependentSeekBarManager(this);

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
            final int num = i;
            manager.addSeekBar(dependentSeekBars[i]);

            final DependentSeekBar seekBar = dependentSeekBars[num];
            seekBar.setProgress(i);

            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    pauseTextChangeListener = true;
                    editText[num].setText("" + seekBar.getProgress());
                    pauseTextChangeListener = false;
                }
            });
            editText[num].setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        editText[num].setSelection(editText[num].getText().length());
                    }
                }
            });
            editText[num].setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId,
                        KeyEvent event) {
                    if (!validValue) {
                        pauseTextChangeListener = true;
                        editText[num].setTextColor(Color.BLACK);
                        editText[num].setText("" + seekBar.getProgress());
                        pauseTextChangeListener = false;
                        // manager.setShiftingAllowed(true);
                        validValue = true;
                    }
                    return false;
                }
            });
            editText[num].addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start,
                        int before, int count) {
                    if (pauseTextChangeListener) {
                        return;
                    }
                    if (editText[num].hasFocus()) {
                        try {
                            pauseTextChangeListener = true;
                            if (seekBar.moveTo(Integer.parseInt(s.toString()))) {
                                isValidValue(seekBar, editText[num], s);
                            } else {
                                notValidValue();
                            }
                            pauseTextChangeListener = false;
                        } catch (Exception e) {
                            notValidValue();
                            pauseTextChangeListener = false;
                        }
                    }
                }

                private void isValidValue(final SeekBar iBar,
                        final EditText iValue, CharSequence s) {
                    // iBar.setProgress(Integer.parseInt(s.toString()));
                    iValue.setText(s.toString());
                    iValue.setTextColor(Color.BLACK);
                    iValue.setSelection(iValue.getText().length());
                    validValue = true;
                }

                private void notValidValue() {
                    validValue = false;
                    editText[num].setTextColor(Color.RED);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                        int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        for (int i = 0; i < extraBars; i++) {
            RelativeLayout rowLayout = (RelativeLayout) getLayoutInflater().inflate(
                    R.layout.editfield_sample_row, null);
            final DependentSeekBar seekBar = (DependentSeekBar) rowLayout.findViewById(R.id.seekbar);
            manager.addSeekBar(seekBar);
            final EditText editField = (EditText) rowLayout.findViewById(R.id.seekbar_value);

            seekBar.setProgress(i + 4);

            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    pauseTextChangeListener = true;
                    editField.setText("" + seekBar.getProgress());
                    pauseTextChangeListener = false;
                }
            });
            editField.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        editField.setSelection(editField.getText().length());
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
                        editField.setText("" + seekBar.getProgress());
                        pauseTextChangeListener = false;
                        validValue = true;
                    }
                    return false;
                }
            });
            editField.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start,
                        int before, int count) {
                    if (pauseTextChangeListener) {
                        return;
                    }
                    if (editField.hasFocus()) {
                        try {
                            pauseTextChangeListener = true;
                            if (seekBar.moveTo(Integer.parseInt(s.toString()))) {
                                isValidValue(seekBar, editField, s);
                            } else {
                                notValidValue();
                            }
                            pauseTextChangeListener = false;
                        } catch (Exception e) {
                            notValidValue();
                            pauseTextChangeListener = false;
                        }
                    }
                }

                private void isValidValue(final SeekBar iBar,
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
                public void beforeTextChanged(CharSequence s, int start,
                        int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mainLayout.addView(rowLayout);
            manager.getSeekBar(0).addDependencies(DependentSeekBar.LESS_THAN,
                    seekBar);

        }

        manager.getSeekBar(0).addDependencies(DependentSeekBar.LESS_THAN, 1, 2,
                3);
        manager.getSeekBar(3).addDependencies(DependentSeekBar.GREATER_THAN, 2);
        manager.getSeekBar(1).addDependencies(DependentSeekBar.LESS_THAN, 3);

        manager.setShiftingAllowed(true);

    }
}

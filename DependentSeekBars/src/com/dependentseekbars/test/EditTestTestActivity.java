package com.dependentseekbars.test;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.dependentseekbars.DependentSeekBar;
import com.dependentseekbars.DependentSeekBarManager;
import com.dependentseekbars.R;

public class EditTestTestActivity extends Activity {
    private DependentSeekBarManager rsw;
    private EditText[] editText;
    private EditText editText1;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    private boolean validValue = true;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edittest_test);
        
        rsw = (DependentSeekBarManager) findViewById(R.id.restrictedSeekWidget1);
        
        editText = new EditText[4];
        editText[0] = (EditText) findViewById(R.id.editText1);
        editText[1] = (EditText) findViewById(R.id.editText2);
        editText[2] = (EditText) findViewById(R.id.editText3);
        editText[3] = (EditText) findViewById(R.id.editText4);
        
        final boolean[] pauseSeekChangeListener = {false, false, false, false};
        final boolean[] pauseTextChangeListener = {false, false, false, false};
        
//        DependentSeekBar seekBar;
//        
//        seekBar = rsw.createSeekBarWithLabel(0, "Bar 1").getSeekBar();
//        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                pauseTextChangeListener[0] = true;
//                editText1.setText("" + seekBar.getProgress());
//                pauseTextChangeListener[0] = false;
//            }
//        });
//        rsw.createSeekBarWithLabel(1, "Bar 2");
//        rsw.createSeekBarWithLabel(2, "Bar 3");
//        rsw.createSeekBarWithLabel(3, "Bar 4");
        
        for (int i=0; i < 4; i++) {
            final DependentSeekBar seekBar = rsw.createSeekBar(i);
            
            final int num = i;
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {}
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    pauseTextChangeListener[num] = true;
                    editText[num].setText("" + seekBar.getProgress());
//                    pauseTextChangeListener[num] = false;
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
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (!validValue) {
                        pauseTextChangeListener[num] = true;
                        editText[num].setTextColor(Color.RED);
                        pauseTextChangeListener[num] = false;
                        validValue = true;
                    }
                    return false;
                }
            });
            editText[num].addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (pauseTextChangeListener[num]) {
                        return;
                    }
                    if (editText[num].hasFocus()) {
                        try {
                            if (seekBar.moveTo(Integer.parseInt(s.toString()))) {
                                isValidValue(seekBar, editText[num],
                                        s);
                            } else {
                                notValidValue();
                            }
                        } catch (Exception e) {
                            notValidValue();
                        }
                    }
                }

                private void isValidValue(
                        final SeekBar iBar, final EditText iValue,
                        CharSequence s) {
                    pauseTextChangeListener[num] = true;
                    iBar.setProgress(Integer.parseInt(s.toString()));
                    iValue.setText(s.toString());
                    iValue.setSelection(iValue.getText().length());
                    pauseTextChangeListener[num] = false;
                    validValue = true;
                }

                private void notValidValue() {
                    validValue = false;
                    editText[num].setTextColor(Color.RED);
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
        
//        rsw.getSeekBar(0).addDependencies(RestrictedSeekBar.LESS_THAN, indices)
        
        
    }
}

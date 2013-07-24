package com.oanda.dependentseekbars.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.oanda.dependentseekbars.lib.DependentSeekBar;
import com.oanda.dependentseekbars.lib.DependentSeekBarManager;

public class TaskSchedulerActivity extends Activity {

    private final int NUM_OF_TASKS = 4;
    private DependentSeekBarManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduler_main);
        
        manager = new DependentSeekBarManager();
        final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.scheduler_main);

        for (int i = 0; i < NUM_OF_TASKS; i++) {
            final LinearLayout rowLayout = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.scheduler_row, null);
            final DependentSeekBar dsb = (DependentSeekBar) rowLayout.findViewById(R.id.seekbar);
            final EditText taskLabel = (EditText) rowLayout.findViewById(R.id.task_label);
            final TextView timeLabel = (TextView) rowLayout.findViewById(R.id.time_label);

            // set listeners
            dsb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    final String timeSuffix;
                    int time = progress + 9;
                    if (time == 12)
                        timeSuffix = "PM";
                    else if (time > 11) {
                        timeSuffix = "PM";
                        time -= 12;
                    } else {
                        timeSuffix = "AM";
                    }
                    timeLabel.setText(time + timeSuffix);
                    timeLabel.setPadding(seekBar.getWidth() / 13 * progress, 0, 0, 0);
                }
            });

            manager.addSeekBar(dsb);
            dsb.setProgress(i);
            dsb.setMax(12);
            taskLabel.setText("Task " + (i + 1));

            if (i > 0) {
                dsb.addDependencies(DependentSeekBar.Dependency.GREATER_THAN, i - 1);
            }

            mainLayout.addView(rowLayout, 1 + i);
        }
        manager.setShiftingAllowed(true);
    }

    public void onToggleClicked(View view) {
        final ToggleButton shiftingToggle = ((ToggleButton) view);
        manager.setShiftingAllowed(shiftingToggle.isChecked());
    }
}

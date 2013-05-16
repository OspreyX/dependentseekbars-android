package com.dependentseekbars.test;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.dependentseekbars.DependentSeekBarManager;
import com.dependentseekbars.R;

public class RestrictedSlidersActivity extends Activity {
    private DependentSeekBarManager rsw1;
    private DependentSeekBarManager rsw2;
    private DependentSeekBarManager rsw3;
    private LinearLayout tabLayout1;
    private LinearLayout tabLayout2;
    private LinearLayout tabLayout3;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test);
        
        rsw1 = (DependentSeekBarManager) findViewById(R.id.restrictedSeekWidget1);
        rsw2 = (DependentSeekBarManager) findViewById(R.id.restrictedSeekWidget2);
        rsw3 = (DependentSeekBarManager) findViewById(R.id.restrictedSeekWidget3);
        tabLayout1 = (LinearLayout) findViewById(R.id.tab1);
        tabLayout2 = (LinearLayout) findViewById(R.id.tab2);
        tabLayout3 = (LinearLayout) findViewById(R.id.tab3);


//        rsw1.createLabelledSeekBar(0, "Test Bar 1");
//        rsw1.createLabelledSeekBar(1, "Bar 2");
//        rsw1.createLabelledSeekBar(2, "Another Bar");
//        rsw1.createLabelledSeekBar(3, "The Last Bar");

        // Max Dependencies
        // rsw1.addMaxDependencies(0, 1);
        // rsw1.addMaxDependencies(1, 2);
        // rsw1.addMaxDependencies(2, 3);

        // Min Dependencies
        // rsw1.addMinDependencies(1, 0);
        // rsw1.addMinDependencies(2, 1);
        // rsw1.addMinDependencies(3, 2);

        // Multiple Min dependencies on single bar
//        rsw1.addMinDependencies(1, 2);
//        rsw1.addMinDependencies(1, 3);


        // rsw2.createLabelledSeekBar(value, name)

    }
}

package com.example;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.example.restrictedsliders.R;
import com.restrictedsliders.RestrictedSeekBar;
import com.restrictedsliders.DependentSeekBarManager;
import com.restrictedsliders.test.RestrictedSlidersActivity;

@RunWith(RobolectricTestRunner.class)
public class RestrictedSlidersTest {
    private DependentSeekBarManager rsw;
    
    // Changing these values will effect results
    private int[] progressValues = { 0, 1, 2, 3 };
    private RestrictedSeekBar[] restrictedSeekBars = new RestrictedSeekBar[4];
    
    @Before
    public void setup() {
        RestrictedSlidersActivity activity = new RestrictedSlidersActivity();
        activity.onCreate(null);
        rsw = (DependentSeekBarManager) activity.findViewById(R.id.restrictedSeekWidget1);
        
        for (int i = 0; i < 4; i++) {
            restrictedSeekBars[i] = rsw.createSeekBar(progressValues[i]);
        }
        
        rsw.getSeekBar(0).addDependencies(RestrictedSeekBar.LESS_THAN, 1, 2, 3);
        rsw.getSeekBar(1).addDependencies(RestrictedSeekBar.LESS_THAN, 3);
        rsw.getSeekBar(3).addDependencies(RestrictedSeekBar.GREATER_THAN, 2);
    }
    @Test
    public void testValidSetup() {
        
        // Moving bar 1 right should move all bars right
        restrictedSeekBars[0].getSeekBar().setProgress(progressValues[0] + 20);

        assertThat(restrictedSeekBars[0].getSeekBar().getProgress(), equalTo(progressValues[0] + 20));
        assertThat(restrictedSeekBars[1].getSeekBar().getProgress(), equalTo(progressValues[0] + 21));
        assertThat(restrictedSeekBars[2].getSeekBar().getProgress(), equalTo(progressValues[0] + 21));
        assertThat(restrictedSeekBars[3].getSeekBar().getProgress(), equalTo(progressValues[0] + 22));
    }
}

package com.oanda.dependentseekbars.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.oanda.dependentseekbars.lib.DependentSeekBar;
import com.oanda.dependentseekbars.lib.DependentSeekBarManager;

@RunWith(RobolectricTestRunner.class)
public class DependentSeekBarsTest {
    private DependentSeekBarManager manager;

    // Changing these values will effect results
    private int[] progressValues = { 0, 1, 2, 3 };
    private DependentSeekBar[] dependentSeekBars = new DependentSeekBar[4];
    boolean userListenerInvoked = false;
    private Context context;

    @Before
    public void setup() {
        context = Robolectric.getShadowApplication().getApplicationContext();

        for (int i = 0; i < 4; i++) {
            dependentSeekBars[i] = manager.createSeekBar(context, progressValues[i]);
        }

        manager.getSeekBar(0).addDependencies(
                DependentSeekBar.Dependency.LESS_THAN, 1, 2, 3);
        manager.getSeekBar(1).addDependencies(
                DependentSeekBar.Dependency.LESS_THAN, 3);
        manager.getSeekBar(3).addDependencies(
                DependentSeekBar.Dependency.GREATER_THAN, 2);
    }

    @Test
    public void testValidSetup() {
        // Moving bar 1 right should move all bars right
        dependentSeekBars[0].setProgress(progressValues[0] + 20);

        assertThat(dependentSeekBars[0].getProgress(),
                equalTo(progressValues[0] + 20));
        assertThat(dependentSeekBars[1].getProgress(),
                equalTo(progressValues[0] + 21));
        assertThat(dependentSeekBars[2].getProgress(),
                equalTo(progressValues[0] + 21));
        assertThat(dependentSeekBars[3].getProgress(),
                equalTo(progressValues[0] + 22));
    }

    /*
     * Tests if the listener set by the DependentSeekBars implementation of
     * setOnSeekBarChangeListener() is invoked on progress change.
     */
    @Test
    public void listenerTest() {
        dependentSeekBars[0]
                .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {}

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1,
                            boolean arg2) {
                        setUserListenerInvoked(true);
                    }
                });

        dependentSeekBars[0].setProgress(10);

        assertTrue(userListenerInvoked);
    }

    // Used for listenerTest()
    private void setUserListenerInvoked(boolean b) {
        userListenerInvoked = true;
    }
}

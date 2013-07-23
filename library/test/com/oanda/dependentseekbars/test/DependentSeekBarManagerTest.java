package com.oanda.dependentseekbars.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;

import com.oanda.dependentseekbars.lib.DependentSeekBar;
import com.oanda.dependentseekbars.lib.DependentSeekBarManager;

@RunWith(RobolectricTestRunner.class)
public class DependentSeekBarManagerTest {
    private DependentSeekBarManager manager;
    private Context context;
    
    @Before
    public void setup() {
        context = Robolectric.getShadowApplication().getApplicationContext();
        manager = new DependentSeekBarManager();
    }
    
    /*
     * Tests if adding and removing DependentSeekBars are done properly.
     */
    @Test
    public void addAndRemoveDependentSeekBarTest() {
        manager.createSeekBar(context, 10);
        manager.createSeekBar(context, 30);
        manager.createSeekBar(context, 40);
        
        //tests if seekBars were created in correct position
        assertEquals(30,manager.getSeekBar(1).getProgress());
        
        //tests if seekbars are removed successfully by checking if another seekbar takes its position
        manager.removeSeekBar(1,false);
        assertEquals(40, manager.getSeekBar(1).getProgress());
        DependentSeekBar dsb = manager.getSeekBar(0);
        manager.removeSeekBar(dsb,false);
        assertEquals(40, manager.getSeekBar(0).getProgress());
    }
}
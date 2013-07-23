package com.oanda.dependentseekbars.test;

import org.codehaus.plexus.context.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.oanda.dependentseekbars.lib.DependentSeekBar;
import com.oanda.dependentseekbars.lib.DependentSeekBarManager;

@RunWith(RobolectricTestRunner.class)
public class DependentSeekBarManagerTest {
    private DependentSeekBarManager dsbm;
    private Context context;
    
    @Before
    public void setup() {
    	context = Robolectric.getShadowApplication().getApplicationContext();
    	dsbm = new DependentSeekBarManager(context);
    }
    
    /*
     * Tests if adding and removing DependentSeekBars are done properly.
     */
    @Test
    public void addAndRemoveDependentSeekBarTest() {
        dsbm.createSeekBar(10);
        dsbm.createSeekBar(30);
        dsbm.createSeekBar(40);
        
        //tests if seekBars were created in correct position
        assertEquals(30,dsbm.getSeekBar(1).getProgress());
        
        //tests if seekbars are removed successfully by checking if another seekbar takes its position
        dsbm.removeSeekBar(1,false);
        assertEquals(40, dsbm.getSeekBar(1).getProgress());
        DependentSeekBar dsb = dsbm.getSeekBar(0);
        dsbm.removeSeekBar(dsb,false);
        assertEquals(40, dsbm.getSeekBar(0).getProgress());
    }
    
    
}
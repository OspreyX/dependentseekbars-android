package com.dependentseekbars.unittest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.dependentseekbars.DependentSeekBar;
import com.dependentseekbars.DependentSeekBarManager;
import com.dependentseekbars.R;
import com.dependentseekbars.samples.RestrictedSlidersActivity;

@RunWith(RobolectricTestRunner.class)
public class DependentSeekBarsTest {
	private DependentSeekBarManager rsw;

	// Changing these values will effect results
	private int[] progressValues = { 0, 1, 2, 3 };
	private DependentSeekBar[] dependentSeekBars = new DependentSeekBar[4];
	boolean userListenerInvoked = false;

	@Before
	public void setup() {
		RestrictedSlidersActivity activity = new RestrictedSlidersActivity();
		activity.onCreate(null);
		rsw = (DependentSeekBarManager) activity
				.findViewById(R.id.restrictedSeekWidget1);

		for (int i = 0; i < 4; i++) {
			dependentSeekBars[i] = rsw.createSeekBar(progressValues[i]);
		}

        rsw.getSeekBar(0).addDependencies(
                DependentSeekBar.Dependency.LESS_THAN, 1, 2, 3);
        rsw.getSeekBar(1).addDependencies(
                DependentSeekBar.Dependency.LESS_THAN, 3);
        rsw.getSeekBar(3).addDependencies(
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

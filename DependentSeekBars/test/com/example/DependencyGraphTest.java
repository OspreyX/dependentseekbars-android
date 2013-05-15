package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;

import com.restrictedsliders.DependencyGraph.InconsistantGraphException;
import com.restrictedsliders.DependencyGraph.Node;
import com.restrictedsliders.DependentSeekBarManager;

@RunWith(RobolectricTestRunner.class)
public class DependencyGraphTest {

	private final int NUM_OF_NODES = 4;
	private DependencyGraph dg;
	private DependentSeekBarManager rsw;
	private Context context;
	private ArrayList<Node> nodes;
	private ArrayList<SubclassedSeekBar> seekBars;

	@Before
	public void setup() {

		RestrictedSeekBar rsb;
		SubclassedSeekBar ssb;

		context = Robolectric.getShadowApplication().getApplicationContext();

		dg = new DependencyGraph();
		rsw = new DependentSeekBarManager(context);
		nodes = new ArrayList<Node>();
		seekBars = new ArrayList<SubclassedSeekBar>();

		// Create Nodes in DependencyGraph, need to explicitly create SSB so
		for (int i = 0; i < NUM_OF_NODES; i++) {
			rsb = rsw.createSeekBar(i);
			ssb = rsb.getSeekBar();
			nodes.add(dg.addSeekBar(ssb));
			seekBars.add(ssb);
		}
	}

	// removes all dependencies in a graph
	private void resetGraph() {
		SubclassedSeekBar ssb;
		dg = new DependencyGraph();
		nodes.clear();
		for (int i = 0; i < NUM_OF_NODES; i++) {
			ssb = seekBars.get(i);
			nodes.add(dg.addSeekBar(ssb));
		}
	}

	// TODO this may not run first as JUnit does not guarantee execution order
	@Test
	public void testSetup() throws Exception {

		assertEquals(nodes.size(), NUM_OF_NODES);

		for (int i = 0; i < NUM_OF_NODES; i++) {
			assertEquals(nodes.get(i).getParents().size(), 0);
			assertEquals(nodes.get(i).getChildren().size(), 0);
			assertTrue(!dg.checkForMaxCycle(nodes.get(i)));
			assertTrue(!dg.checkForMinCycle(nodes.get(i)));
		}
	}

	/*
	 * Adds dependencies and passes when: InconsistentStateException is not
	 * thrown nodes have the correct number of parents and children
	 */
	@Test
	public void addDependenciesTest() throws Exception {
		resetGraph();

		// creating SubclassedSeekBar array for dependency creation
		SubclassedSeekBar[] limitingSeekBars = new SubclassedSeekBar[3];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		limitingSeekBars[1] = nodes.get(2).getSeekBar();
		limitingSeekBars[2] = nodes.get(3).getSeekBar();
		dg.addMaxDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);
		assertEquals(nodes.get(0).getChildren().size(), 3);
		assertEquals(nodes.get(0).getParents().size(), 0);

		limitingSeekBars = new SubclassedSeekBar[2];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		limitingSeekBars[1] = nodes.get(3).getSeekBar();
		nodes.get(3).getSeekBar().setProgress(3);
		nodes.get(2).getSeekBar().setProgress(4);
		dg.addMinDependencies(nodes.get(2).getSeekBar(), limitingSeekBars);
		assertEquals(nodes.get(2).getParents().size(), 3);
		assertEquals(nodes.get(2).getChildren().size(), 0);

		limitingSeekBars = new SubclassedSeekBar[1];
		limitingSeekBars[0] = nodes.get(3).getSeekBar();
		dg.addMaxDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);
		assertEquals(nodes.get(1).getChildren().size(), 2);
		assertEquals(nodes.get(1).getParents().size(), 1);
	}

	/*
	 * removes a seekbar and make sure the graph is changed accordingly
	 * (depending on if restructuring is enabled or not) When restructuring is
	 * not enabled, the test makes sure the removed node does not appear in any
	 * other node's parent and child list
	 */
	@Test
	public void removeDependenciesTest() throws Exception {
		resetGraph();
		// creating SubclassedSeekBar array for dependency creation
		SubclassedSeekBar[] limitingSeekBars = new SubclassedSeekBar[3];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		limitingSeekBars[1] = nodes.get(2).getSeekBar();
		limitingSeekBars[2] = nodes.get(3).getSeekBar();
		dg.addMaxDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

		limitingSeekBars = new SubclassedSeekBar[2];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		limitingSeekBars[1] = nodes.get(3).getSeekBar();
		nodes.get(3).getSeekBar().setProgress(3);
		nodes.get(2).getSeekBar().setProgress(4);
		dg.addMinDependencies(nodes.get(2).getSeekBar(), limitingSeekBars);

		limitingSeekBars = new SubclassedSeekBar[1];
		limitingSeekBars[0] = nodes.get(3).getSeekBar();
		dg.addMaxDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);

		// Now remove seekbar 3 without
		dg.removeSeekBar(nodes.get(3).getSeekBar(), false);

		// make sure seekbar 3 is not in any children or parent list of any of
		// the nodes.
		for (int i = 0; i < 3; i++) {
			Node n = nodes.get(i);
			assertFalse(n.getChildren().contains(nodes.get(3)));
			assertFalse(n.getParents().contains(nodes.get(3)));
		}

		// TODO add test for when restructuring is true
	}

	/*
	 * Tests if adding already existing dependencies work.
	 * Test Case: node
	 */
	@Test
	public void duplicateAddDependencyTest() throws Exception {
		resetGraph();
		SubclassedSeekBar[] limitingSeekBars = new SubclassedSeekBar[1];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		dg.addMaxDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);
		dg.addMaxDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

		limitingSeekBars = new SubclassedSeekBar[1];
		limitingSeekBars[0] = nodes.get(0).getSeekBar();
		dg.addMinDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);
		dg.addMinDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);
		

	}

	/*
	 * Adds dependencies creating a circular dependency. Passes when
	 * InconsistentStateException is thrown. The circular dependency occurs when
	 * seekbar 0 has a min dependency on seekbar 2
	 */
	@Test(expected = InconsistantGraphException.class)
	public void circularDependencyTest1() throws Exception {
		resetGraph();

		// creating SubclassedSeekBar array for dependency creation
		SubclassedSeekBar[] limitingSeekBars = new SubclassedSeekBar[2];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		limitingSeekBars[1] = nodes.get(3).getSeekBar();
		dg.addMaxDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

		limitingSeekBars = new SubclassedSeekBar[2];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		limitingSeekBars[1] = nodes.get(3).getSeekBar();
		nodes.get(3).getSeekBar().setProgress(3);
		nodes.get(2).getSeekBar().setProgress(4);
		dg.addMinDependencies(nodes.get(2).getSeekBar(), limitingSeekBars);

		// creating circular dependency by making seekbar 0 have a min
		// dependency on seekbar 2
		limitingSeekBars = new SubclassedSeekBar[1];
		limitingSeekBars[0] = nodes.get(2).getSeekBar();
		dg.addMinDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);
	}

	/*
	 * Adds dependencies creating a circular dependency. Passes when
	 * InconsistentStateException is thrown. The circular dependency occurs when
	 * seekbar 3 has a max dependency on seekbar 0
	 */
	@Test(expected = InconsistantGraphException.class)
	public void circularDependencyTest2() throws Exception {
		resetGraph();

		// creating SubclassedSeekBar array for dependency creation
		SubclassedSeekBar[] limitingSeekBars = new SubclassedSeekBar[1];
		limitingSeekBars[0] = nodes.get(1).getSeekBar();
		dg.addMaxDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

		limitingSeekBars = new SubclassedSeekBar[2];
		limitingSeekBars[0] = nodes.get(2).getSeekBar();
		limitingSeekBars[1] = nodes.get(3).getSeekBar();
		dg.addMaxDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);

		// creating circular dependency by making seekbar 3 have a max
		// dependency on seekbar 0
		limitingSeekBars = new SubclassedSeekBar[1];
		limitingSeekBars[0] = nodes.get(0).getSeekBar();
		dg.addMaxDependencies(nodes.get(3).getSeekBar(), limitingSeekBars);
	}

	/*
	 * Creates the dependencies 0 < 1 < 2 < 3 and 3 < 0. This tests if an
	 * InconsistantStateException is thrown when the graph contains a cycle
	 */
	@Test(expected = InconsistantGraphException.class)
	public void circularDependencyTest3() throws Exception {
		resetGraph();
		SubclassedSeekBar[] limitingSeekBar1 = { nodes.get(1).getSeekBar() };
		dg.addMaxDependencies(nodes.get(0).getSeekBar(), limitingSeekBar1);

		SubclassedSeekBar[] limitingSeekBar2 = { nodes.get(2).getSeekBar() };
		dg.addMaxDependencies(nodes.get(1).getSeekBar(), limitingSeekBar2);

		SubclassedSeekBar[] limitingSeekBar3 = { nodes.get(3).getSeekBar() };
		dg.addMaxDependencies(nodes.get(2).getSeekBar(), limitingSeekBar3);

		SubclassedSeekBar[] limitingSeekBar0 = { nodes.get(0).getSeekBar() };
		dg.addMaxDependencies(nodes.get(3).getSeekBar(), limitingSeekBar0);
	}

	/*
	 * Creates the dependencies 0 > 1 > 2 > 3 and 3 > 0. This tests if an
	 * InconsistantStateException is thrown when the graph contains a cycle
	 */
	@Test(expected = InconsistantGraphException.class)
	public void circularDependencyTest4() throws Exception {
		resetGraph();
		SubclassedSeekBar[] limitingSeekBar1 = { nodes.get(1).getSeekBar() };
		dg.addMinDependencies(nodes.get(0).getSeekBar(), limitingSeekBar1);

		SubclassedSeekBar[] limitingSeekBar2 = { nodes.get(2).getSeekBar() };
		dg.addMinDependencies(nodes.get(1).getSeekBar(), limitingSeekBar2);

		SubclassedSeekBar[] limitingSeekBar3 = { nodes.get(3).getSeekBar() };
		dg.addMinDependencies(nodes.get(2).getSeekBar(), limitingSeekBar3);

		SubclassedSeekBar[] limitingSeekBar0 = { nodes.get(0).getSeekBar() };
		dg.addMinDependencies(nodes.get(3).getSeekBar(), limitingSeekBar0);
	}

}
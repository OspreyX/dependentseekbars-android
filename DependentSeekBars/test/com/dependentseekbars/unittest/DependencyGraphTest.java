package com.dependentseekbars.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;

import com.dependentseekbars.DependencyGraph;
import com.dependentseekbars.DependencyGraph.InconsistentGraphException;
import com.dependentseekbars.DependencyGraph.Node;
import com.dependentseekbars.DependentSeekBar;
import com.dependentseekbars.DependentSeekBarManager;

@RunWith(RobolectricTestRunner.class)
public class DependencyGraphTest {

    private final int NUM_NODES = 4;
	private DependencyGraph dg;
	private DependentSeekBarManager rsw;
	private Context context;
	private ArrayList<Node> nodes;
	private ArrayList<DependentSeekBar> seekBars;
    private Method isAcyclicMethod;

	@Before
	public void setup() {

		DependentSeekBar dsb;

		context = Robolectric.getShadowApplication().getApplicationContext();

		dg = new DependencyGraph();
		rsw = new DependentSeekBarManager(context);
		nodes = new ArrayList<Node>();
		seekBars = new ArrayList<DependentSeekBar>();

		// Create Nodes in DependencyGraph, need to explicitly create SSB so
        for (int i = 0; i < NUM_NODES; i++) {
			dsb = rsw.createSeekBar(i);
			nodes.add(dg.addSeekBar(dsb));
			seekBars.add(dsb);
		}

        // set isAcycilic() to public so it can be tested
        try {
            isAcyclicMethod = dg.getClass().getDeclaredMethod("containsCycle",
                    new Class[] { Node.class, int.class });
            isAcyclicMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

	}

	// removes all dependencies in a graph
	private void resetGraph() {
		DependentSeekBar dsb;
		dg = new DependencyGraph();
		nodes.clear();
        for (int i = 0; i < NUM_NODES; i++) {
			dsb = seekBars.get(i);
			nodes.add(dg.addSeekBar(dsb));
		}
	}

	// TODO this may not run first as JUnit does not guarantee execution order
	@Test
	public void testSetup() throws Exception {

        assertEquals(nodes.size(), NUM_NODES);
        


        for (int i = 0; i < NUM_NODES; i++) {
			assertEquals(nodes.get(i).getParents().size(), 0);
			assertEquals(nodes.get(i).getChildren().size(), 0);
            assertFalse((boolean) isAcyclicMethod.invoke(dg, nodes.get(i),
                    DependencyGraph.CHECK_LT_DEPENDENCIES));
            assertFalse((boolean) isAcyclicMethod.invoke(dg, nodes.get(i),
                    DependencyGraph.CHECK_GT_DEPENDENCIES));
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
        DependentSeekBar[] limitingSeekBars = new DependentSeekBar[3];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        limitingSeekBars[1] = nodes.get(2).getSeekBar();
        limitingSeekBars[2] = nodes.get(3).getSeekBar();
        dg.addLessThanDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);
        assertEquals(nodes.get(0).getChildren().size(), 3);
        assertEquals(nodes.get(0).getParents().size(), 0);

        limitingSeekBars = new DependentSeekBar[2];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        limitingSeekBars[1] = nodes.get(3).getSeekBar();
        nodes.get(3).getSeekBar().setProgress(3);
        nodes.get(2).getSeekBar().setProgress(4);
        dg.addGreaterThanDependencies(nodes.get(2).getSeekBar(),
                limitingSeekBars);
        assertEquals(nodes.get(2).getParents().size(), 3);
        assertEquals(nodes.get(2).getChildren().size(), 0);

        limitingSeekBars = new DependentSeekBar[1];
        limitingSeekBars[0] = nodes.get(3).getSeekBar();
        dg.addLessThanDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);
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
        DependentSeekBar[] limitingSeekBars = new DependentSeekBar[3];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        limitingSeekBars[1] = nodes.get(2).getSeekBar();
        limitingSeekBars[2] = nodes.get(3).getSeekBar();
        dg.addLessThanDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

        limitingSeekBars = new DependentSeekBar[2];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        limitingSeekBars[1] = nodes.get(3).getSeekBar();
        nodes.get(3).getSeekBar().setProgress(3);
        nodes.get(2).getSeekBar().setProgress(4);
        dg.addGreaterThanDependencies(nodes.get(2).getSeekBar(),
                limitingSeekBars);

        limitingSeekBars = new DependentSeekBar[1];
        limitingSeekBars[0] = nodes.get(3).getSeekBar();
        dg.addLessThanDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);

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
     * Tests if adding already existing dependencies work. Test Case: node
     */
    @Test
    public void duplicateAddDependencyTest() throws Exception {
        resetGraph();
        DependentSeekBar[] limitingSeekBars = new DependentSeekBar[1];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        dg.addLessThanDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);
        dg.addLessThanDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

        limitingSeekBars = new DependentSeekBar[1];
        limitingSeekBars[0] = nodes.get(0).getSeekBar();
        dg.addGreaterThanDependencies(nodes.get(1).getSeekBar(),
                limitingSeekBars);
        dg.addGreaterThanDependencies(nodes.get(1).getSeekBar(),
                limitingSeekBars);

    }

    /*
     * Adds dependencies creating a circular dependency. Passes when
     * InconsistentStateException is thrown. The circular dependency occurs when
     * seekbar 0 has a min dependency on seekbar 2
     */
    @Test(expected = InconsistentGraphException.class)
    public void circularDependencyTest1() throws Exception {
        resetGraph();

        // creating SubclassedSeekBar array for dependency creation
        DependentSeekBar[] limitingSeekBars = new DependentSeekBar[2];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        limitingSeekBars[1] = nodes.get(3).getSeekBar();
        dg.addLessThanDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

        limitingSeekBars = new DependentSeekBar[2];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        limitingSeekBars[1] = nodes.get(3).getSeekBar();
        nodes.get(3).getSeekBar().setProgress(3);
        nodes.get(2).getSeekBar().setProgress(4);
        dg.addGreaterThanDependencies(nodes.get(2).getSeekBar(),
                limitingSeekBars);

        // creating circular dependency by making seekbar 0 have a min
        // dependency on seekbar 2
        limitingSeekBars = new DependentSeekBar[1];
        limitingSeekBars[0] = nodes.get(2).getSeekBar();
        dg.addGreaterThanDependencies(nodes.get(0).getSeekBar(),
                limitingSeekBars);
    }

    /*
     * Adds dependencies creating a circular dependency. Passes when
     * InconsistentStateException is thrown. The circular dependency occurs when
     * seekbar 3 has a max dependency on seekbar 0
     */
    @Test(expected = InconsistentGraphException.class)
    public void circularDependencyTest2() throws Exception {
        resetGraph();

        // creating SubclassedSeekBar array for dependency creation
        DependentSeekBar[] limitingSeekBars = new DependentSeekBar[1];
        limitingSeekBars[0] = nodes.get(1).getSeekBar();
        dg.addLessThanDependencies(nodes.get(0).getSeekBar(), limitingSeekBars);

        limitingSeekBars = new DependentSeekBar[2];
        limitingSeekBars[0] = nodes.get(2).getSeekBar();
        limitingSeekBars[1] = nodes.get(3).getSeekBar();
        dg.addLessThanDependencies(nodes.get(1).getSeekBar(), limitingSeekBars);

        // creating circular dependency by making seekbar 3 have a max
        // dependency on seekbar 0
        limitingSeekBars = new DependentSeekBar[1];
        limitingSeekBars[0] = nodes.get(0).getSeekBar();
        dg.addLessThanDependencies(nodes.get(3).getSeekBar(), limitingSeekBars);
    }

    /*
     * Creates the dependencies 0 < 1 < 2 < 3 and 3 < 0. This tests if an
     * InconsistantStateException is thrown when the graph contains a cycle
     */
    @Test(expected = InconsistentGraphException.class)
    public void circularDependencyTest3() throws Exception {
        resetGraph();
        DependentSeekBar[] limitingSeekBar1 = { nodes.get(1).getSeekBar() };
        dg.addLessThanDependencies(nodes.get(0).getSeekBar(), limitingSeekBar1);

        DependentSeekBar[] limitingSeekBar2 = { nodes.get(2).getSeekBar() };
        dg.addLessThanDependencies(nodes.get(1).getSeekBar(), limitingSeekBar2);

        DependentSeekBar[] limitingSeekBar3 = { nodes.get(3).getSeekBar() };
        dg.addLessThanDependencies(nodes.get(2).getSeekBar(), limitingSeekBar3);

        DependentSeekBar[] limitingSeekBar0 = { nodes.get(0).getSeekBar() };
        dg.addLessThanDependencies(nodes.get(3).getSeekBar(), limitingSeekBar0);
    }

    /*
     * Creates the dependencies 0 > 1 > 2 > 3 and 3 > 0. This tests if an
     * InconsistantStateException is thrown when the graph contains a cycle
     */
    @Test(expected = InconsistentGraphException.class)
    public void circularDependencyTest4() throws Exception {
        resetGraph();
        DependentSeekBar[] limitingSeekBar1 = { nodes.get(1).getSeekBar() };
        dg.addGreaterThanDependencies(nodes.get(0).getSeekBar(),
                limitingSeekBar1);

        DependentSeekBar[] limitingSeekBar2 = { nodes.get(2).getSeekBar() };
        dg.addGreaterThanDependencies(nodes.get(1).getSeekBar(),
                limitingSeekBar2);

        DependentSeekBar[] limitingSeekBar3 = { nodes.get(3).getSeekBar() };
        dg.addGreaterThanDependencies(nodes.get(2).getSeekBar(),
                limitingSeekBar3);

        DependentSeekBar[] limitingSeekBar0 = { nodes.get(0).getSeekBar() };
        dg.addGreaterThanDependencies(nodes.get(3).getSeekBar(),
                limitingSeekBar0);
    }

}
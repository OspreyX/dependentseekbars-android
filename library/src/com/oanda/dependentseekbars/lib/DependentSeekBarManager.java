package com.oanda.dependentseekbars.lib;

import java.util.ArrayList;

import android.content.Context;

import com.oanda.dependentseekbars.lib.DependencyGraph.Node;

/**
 * A DependentSeekBarManager is a collection of {@link DependentSeekBar}s which
 * allows you to create relationships so that the progress of one
 * {@link DependentSeekBar} can affect the progress of other
 * {@link DependentSeekBar}s. Less than and greater than relationships can be
 * created between different {@link DependentSeekBar}s, such that one
 * DependentSeekBar must always be less/greater than another.
 *
 * @author jbeveridge and sujen
 *
 */
public class DependentSeekBarManager{
    private ArrayList<DependentSeekBar> seekBars;
    private ArrayList<ArrayList<Integer>> minDependencies;
    private ArrayList<ArrayList<Integer>> maxDependencies;
    private DependencyGraph dg;
    private boolean shiftingAllowed = true;

    private final int DEFAULT_MAXIMUM_PROGRESS = 100;

    /**
     * Creates a DependentSeekBarManager that can be used to contain
     * {@link DependentSeekBar}s. By default, the DependentSeekBarManager has a
     * vertical orientation.
     *
     * @param context the application environment
     */
    public DependentSeekBarManager() {
        seekBars = new ArrayList<DependentSeekBar>();
        minDependencies = new ArrayList<ArrayList<Integer>>();
        maxDependencies = new ArrayList<ArrayList<Integer>>();
        dg = new DependencyGraph();
    }

    /**
     * Create a new {@link DependentSeekBar} and adds it to the
     * DependentSeekBarManager. The maximum value progress will be set to 100.
     *
     * @param context the {@link Context} the view is running in
     * @param progress the initial progress of the seek bar
     * @return the {@link DependentSeekBar} which was added to the manager
     *
     * @see #createSeekBar(int, int)
     */
    public DependentSeekBar createSeekBar(Context context, int progress) {
        return createSeekBar(context, progress, DEFAULT_MAXIMUM_PROGRESS);
    }

    /**
     * Create a new {@link DependentSeekBar} and adds it to this
     * DependentSeekBarManager.
     *
     * @param context the {@link Context} the view is running in
     * @param progress the initial progress of the seek bar
     * @param maximum the maximum value which the progress can be set to
     * @return the {@link DependentSeekBar} which was added to the manager
     *
     * @see #createSeekBar(int)
     */
    public DependentSeekBar createSeekBar(Context context, int progress,
            int maximum) {
        DependentSeekBar seekBar = new DependentSeekBar(context, this,
                progress, maximum);
        seekBars.add(seekBar);

        minDependencies.add(new ArrayList<Integer>());
        maxDependencies.add(new ArrayList<Integer>());

        // Create a new node for the seek bar and add it to the dependency graph
        Node node = dg.addSeekBar(seekBar);
        seekBar.setNode(node);

        return seekBar;
    }

    /**
     * Adds a {@link DependentSeekBar} to the manager allowing the
     * {@link DependentSeekBar} to have dependency relationships with other
     * {@link DependentSeekBar}s in the manager
     *
     * @param seekBar
     */
    public void addSeekBar(DependentSeekBar seekBar) {
        if(seekBars.contains(seekBar))
            return;

        seekBars.add(seekBar);

        minDependencies.add(new ArrayList<Integer>());
        maxDependencies.add(new ArrayList<Integer>());

        Node node = dg.addSeekBar(seekBar);
        seekBar.setNode(node);
        seekBar.setManager(this);
    }

    public DependentSeekBar getSeekBar(int index) {
        if (index < 0 || index >= seekBars.size()) {
            return null;
        }

        return seekBars.get(index);
    }

    /**
     * Removes the {@link DependentSeekBar} at index from this
     * DependentSeekBarManager. The index values correspond to the order in
     * which the DependentSeekBar's were added. When a DependentSeekBar is
     * removed, the index values of the DependentSeekBars are adjusted to
     * represent their order (DependentSeekBars after the one removed will have
     * their index shifted down by 1).
     *
     * @param index the index of the {@link DependentSeekBar} to remove
     * @param restructureDependencies
     * @return true if and only if there is a {@link DependentSeekBar} with
     *         given index and it is successfully removed
     *
     * @see #removeSeekBar(DependentSeekBar, boolean)
     */
    public boolean removeSeekBar(int index, boolean restructureDependencies) {
        if (index >= seekBars.size() || index < 0)
            return false;

        dg.removeSeekBar(seekBars.get(index), restructureDependencies);
        seekBars.remove(index);
        return true;
    }

    /**
     * Removes the given {@link DependentSeekBar} from the
     * DependentSeekBarManager. When a DependentSeekBar is removed, the index
     * values of the DependentSeekBars are adjusted to represent their order
     * (DependentSeekBars after the one removed will have their index shifted
     * down by 1).
     *
     * @param dependent the DependentSeekBar to remove
     * @param restructureDependencies
     * @return true if and only if the given DependentSeekBar is contained in
     *         the manager and it is successfully removed
     *
     * @see #removeSeekBar(int, boolean)
     */
    public boolean removeSeekBar(DependentSeekBar dependent,
            boolean restructureDependencies) {
        for (DependentSeekBar seekBar : seekBars) {
            if (seekBar.equals(dependent)) {
                dg.removeSeekBar(dependent, restructureDependencies);
                seekBars.remove(seekBar);
                return true;
            }
        }
        return false;
    }

    /**
     * Add dependencies between the {@link DependentSeekBar} at dependentIndex
     * and the DependentSeekBars at limitingIndices. The dependencies will
     * ensure that the progress of the DependentSeekBar at dependentIndex is
     * always less than the progress of the DependentSeekBars at each of
     * limitingIndices. The index values correspond to the order in which the
     * DependentSeekBar's were added.
     *
     * @param dependentIndex the index of the DependentSeekBar which must have
     *        the smaller progress
     * @param limitingIndices the indices of the DependentSeekBars which must
     *        have greater progresses than the dependent DependentSeekBar
     * @return
     */
    void addLessThanDependencies(DependentSeekBar dependentSeekBar,
            int[] limitingIndices) {
        checkIndices(limitingIndices);
        dg.addLessThanDependencies(dependentSeekBar,
                getSubclassedSeekBars(limitingIndices));
    }

    void addLessThanDependencies(DependentSeekBar dependentSeekBar,
            DependentSeekBar[] limiting) {

        for (DependentSeekBar limit : limiting) {
            if (limit == null || !seekBars.contains(limit))
                throw new NullPointerException();
        }
        dg.addLessThanDependencies(dependentSeekBar, getSubclassedSeekBars(limiting));
    }

    /**
     * Add dependencies between the {@link DependentSeekBar} at dependentIndex
     * and the DependentSeekBars at limitingIndices. The dependencies will
     * ensure that the progress of the DependentSeekBar at dependentIndex is
     * always greater than the progress of the DependentSeekBars at each of
     * limitingIndices. The index values correspond to the order in which the
     * DependentSeekBar's were added.
     *
     * @param dependentIndex the index of the DependentSeekBar which must have
     *        the smaller progress
     * @param limitingIndices the indices of the DependentSeekBars which must
     *        have greater progresses than the dependent DependentSeekBar
     * @return
     */
    void addGreaterThanDependencies(DependentSeekBar dependentSeekBar,
            int[] limitingIndices) {
        checkIndices(limitingIndices);
        dg.addGreaterThanDependencies(dependentSeekBar,
                getSubclassedSeekBars(limitingIndices));
    }

    void addGreaterThanDependencies(DependentSeekBar dependentSeekBar,
            DependentSeekBar[] limiting) {

        for (DependentSeekBar limit : limiting) {
            if (limit == null || !seekBars.contains(limit))
                throw new NullPointerException();
        }

        dg.addGreaterThanDependencies(dependentSeekBar, getSubclassedSeekBars(limiting));
    }

    private DependentSeekBar[] getSubclassedSeekBars(
            DependentSeekBar[] dependentSeekBars) {
        DependentSeekBar[] limitingSeekBars = new DependentSeekBar[dependentSeekBars.length];
        for (int i = 0; i < dependentSeekBars.length; i++) {
            limitingSeekBars[i] = dependentSeekBars[i];
        }

        return limitingSeekBars;
    }

    private DependentSeekBar[] getSubclassedSeekBars(int[] indices) {
        DependentSeekBar[] limitingSeekBars = new DependentSeekBar[indices.length];
        for (int i = 0; i < indices.length; i++) {
            limitingSeekBars[i] = seekBars.get(indices[i]);
        }

        return limitingSeekBars;
    }

    private void checkIndices(int[] indices) {
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] >= seekBars.size() || indices[i] < 0)
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * When shifting is enabled, the DependentSeekBarManager will attempt to
     * move other seek bars which are dependent on seek bar being adjusted and
     * are blocking its path.
     *
     * @return true iff shifting is currently allowed
     *
     * @see #setShiftingAllowed(boolean)
     */
    public boolean isShiftingAllowed() {
        return shiftingAllowed;
    }

    /**
     * Set the value of shifting. When shifting is enabled, the
     * DependentSeekBarManager will attempt to move other seek bars which are
     * dependent on seek bar being adjusted and are blocking its path.
     *
     * @param b
     *
     * @see #isShiftingAllowed()
     */
    public void setShiftingAllowed(boolean b) {
        shiftingAllowed = b;
    }

}

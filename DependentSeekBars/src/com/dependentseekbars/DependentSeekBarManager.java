package com.dependentseekbars;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.dependentseekbars.DependencyGraph.Node;
import com.example.restrictedsliders.R;

/**
 * A DependentSeekBarManager is a collection of {@link RestrictedSeekBar}s which allows you to create relationships so that the progress of
 * one {@link RestrictedSeekBar} can affect the progress of other {@link RestrictedSeekBar}s. Less than and greater than relationships
 * can be created between different {@link RestrictedSeekBar}s, such that one RestrictedSeekBar must always be less/greater than another.
 * @author jbeveridge and sujen
 *
 */
public class DependentSeekBarManager extends LinearLayout {
    private ArrayList<DependentSeekBar> seekBars;
    private ArrayList<ArrayList<Integer>> minDependencies;
    private ArrayList<ArrayList<Integer>> maxDependencies;
    private DependencyGraph dg;
    private int spacing = 0;
    private boolean shiftingAllowed = true;
    
    /**
     * Creates a DependentSeekBarManager that can be used to contain {@link RestrictedSeekBar}s.
     * By default, the DependentSeekBarManager has a vertical orientation.
     * @param context
     */
    public DependentSeekBarManager(Context context) {
        super(context);
        
        init();
    }
    
    /**
     * Creates a DependentSeekBarManager that can be used to contain {@link RestrictedSeekBar}s.
     * By default, the DependentSeekBarManager has a vertical orientation.
     * @param context
     * @param attrs
     */
    public DependentSeekBarManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DependentSeekBarManager);
        
        String str = a.getString(R.styleable.DependentSeekBarManager_spacing);
        if (str != null && !str.equals("")) {
            try {
                spacing = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                
            }
        }
        a.recycle();
        
        init();
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public DependentSeekBarManager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DependentSeekBarManager, defStyle, 0);
        
        String str = a.getString(R.styleable.DependentSeekBarManager_spacing);
        if (str != null && !str.equals("")) {
            try {
                spacing = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                
            }
        }
        a.recycle();
        
        init();
    }
    
    private void init() {
        seekBars = new ArrayList<DependentSeekBar>();
        minDependencies = new ArrayList<ArrayList<Integer>>();
        maxDependencies = new ArrayList<ArrayList<Integer>>();
        dg = new DependencyGraph();
        
        setOrientation(LinearLayout.VERTICAL);
        
        if (isInEditMode()) {
            for (int i = 0; i < 20; i++) {
                createSeekBar(i);
            }
            
        }
    }
    
    /**
     * Create a new {@link RestrictedSeekBar} and adds it to the widget. The
     * {@link RestrictedSeekBar} returned will be shown at the bottom of the widget if
     * in vertical view, and on the right if in horizontal view. The maximum
     * value progress will be set to 100.
     * 
     * @param progress the initial progress of the seek bar
     * @return the {@link RestrictedSeekBar} which was added to the widget
     * 
     * @see #createSeekBar(int, int)
     */
    public DependentSeekBar createSeekBar(int progress) {
        return createSeekBar(progress, 100);
    }
    
    /**
     * Create a new {@link RestrictedSeekBar} and adds it to the widget. The
     * {@link RestrictedSeekBar} returned will be shown at the bottom of the widget if
     * in vertical view, and on the right if in horizontal view.
     * 
     * @param progress the initial progress of the seek bar
     * @param maximum the maximum value which the progress can be set to
     * @return the {@link RestrictedSeekBar} which was added to the widget
     * 
     * @see #createSeekBar(int)
     */
    public DependentSeekBar createSeekBar(int progress, int maximum) {
        DependentSeekBar seekBar = new DependentSeekBar(getContext(), this, progress, maximum);
        seekBars.add(seekBar);
        
        minDependencies.add(new ArrayList<Integer>());
        maxDependencies.add(new ArrayList<Integer>());
        
        // Create a new node for the seek bar and add it to the dependency graph
        Node node = dg.addSeekBar(seekBar);
        seekBar.setNode(node);
        
        addView(seekBar);
        setupMargins();
        
        return seekBar;
        
    }
    
    private void setupMargins() {
        if (seekBars.size() > 1) {
            ((LinearLayout.LayoutParams) seekBars.get(seekBars.size() - 2).getLayoutParams()).setMargins(0, 0, 0, spacing);
        }
        ((LinearLayout.LayoutParams) seekBars.get(seekBars.size() - 1).getLayoutParams()).setMargins(0, 0, 0, 0);
    }
    
    public void addSeekBar(DependentSeekBar seekBar) {
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
     * Removes the RestrictedSeekBar at index from the widget. The index values
     * correspond to the RestrictedSeekBar's visual location (with the top bar
     * being 0 and increasing downwards, or in horizontal left being 0 and right
     * being n-1). When a bar is removed, the index values of the
     * RestrictedSeekBars are adjusted to represent their new visual locations.
     * 
     * @param index the index of the {@link RestrictedSeekBar} to remove
     * @param restructureDependencies
     * @return true iff there is a {@link RestrictedSeekBar} with given index and it is
     *         successfully removed
     *         
     * @see #removeSeekBar(RestrictedSeekBar, boolean)
     */
    public boolean removeSeekBar(int index, boolean restructureDependencies) {
        if (index >= seekBars.size() || index < 0)
            return false;
        
        dg.removeSeekBar(seekBars.get(index), restructureDependencies);
        seekBars.remove(index);
        setupMargins();
        return true;
    }
    
    /**
     * Removes the given RestrictedSeekBar from the widget. When a bar is
     * removed, the index values of the RestrictedSeekBars are adjusted to
     * represent their new visual locations.
     * 
     * @param rsb the RestrictedSeekBar to remove
     * @param restructureDependencies
     * @return true iff the given RestrictedSeekBar is contained in the widget
     *         and it is successfully removed
     *         
     * @see #removeSeekBar(int, boolean)
     */
    public boolean removeSeekBar(DependentSeekBar dependent, boolean restructureDependencies) {
        for (DependentSeekBar seekBar : seekBars) {
            if (seekBar.equals(dependent)) {
                dg.removeSeekBar(dependent, restructureDependencies);
                seekBars.remove(seekBar);
                setupMargins();
                return true;
            }
        }
        return false;
    }
    
    // TODO are we going to return booleans or throw exceptions?
    /**
     * Add dependencies between the RestrictedSeekBar at dependentIndex and the
     * RestrictedSeekBars at limitingIndices. The dependencies will ensure that
     * the progress of the RestrictedSeekBar at dependentIndex is always less
     * than the progress of the RestrictedSeekBars at each of limitingIndices.
     * The index values correspond to the RestrictedSeekBar's visual location
     * (with the top bar being 0 and increasing downwards, or in horizontal left
     * being 0 and right being n-1).
     * 
     * @param dependentIndex the index of the RestrictedSeekBar which must have
     *        the smaller progress
     * @param limitingIndices the indices of the RestrictedSeekBars which must
     *        have greater progresses than the dependent RestrictedSeekBar
     * @return
     */
    boolean addLessThanDependencies(DependentSeekBar dependentSeekBar, int[] limitingIndices) {
        checkIndices(limitingIndices);
        
        return dg.addMaxDependencies(dependentSeekBar, getSubclassedSeekBars(limitingIndices));
    }
    
    boolean addLessThanDependencies(DependentSeekBar dependentSeekBar, DependentSeekBar[] limiting) {
        
        for (DependentSeekBar limit : limiting) {
            if (limit == null || !seekBars.contains(limit))
                return false;
        }
        
        return dg.addMaxDependencies(dependentSeekBar, getSubclassedSeekBars(limiting));
    }
    
    /**
     * Add dependencies between the RestrictedSeekBar at dependentIndex and the
     * RestrictedSeekBars at limitingIndices. The dependencies will ensure that
     * the progress of the RestrictedSeekBar at dependentIndex is always greater
     * than the progress of the RestrictedSeekBars at each of limitingIndices.
     * The index values correspond to the RestrictedSeekBar's visual location
     * (with the top bar being 0 and increasing downwards, or in horizontal left
     * being 0 and right being n-1).
     * 
     * @param dependentIndex the index of the RestrictedSeekBar which must have
     *        the smaller progress
     * @param limitingIndices the indices of the RestrictedSeekBars which must
     *        have greater progresses than the dependent RestrictedSeekBar
     * @return
     */
    boolean addGreaterThanDependencies(DependentSeekBar dependentSeekBar, int[] limitingIndices) {
        checkIndices(limitingIndices);
        
        return dg.addMinDependencies(dependentSeekBar, getSubclassedSeekBars(limitingIndices));
    }
    
    boolean addGreaterThanDependencies(DependentSeekBar dependentSeekBar, DependentSeekBar[] limiting) {
        
        for (DependentSeekBar limit : limiting) {
            if (limit == null || !seekBars.contains(limit))
                return false;
        }
        
        return dg.addMinDependencies(dependentSeekBar, getSubclassedSeekBars(limiting));
    }
    
    private DependentSeekBar[] getSubclassedSeekBars(DependentSeekBar[] dependentSeekBars) {
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
    
    // TODO should we be throwing an exception or returning false?
    private void checkIndices(int[] indices) {
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] >= seekBars.size() || indices[i] < 0)
                throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * When shifting is enabled, the widget will attempt to move other seek bars which are dependent on seek bar being adjusted and are
     * blocking its path.
     * @return true iff shifting is currently allowed
     * 
     * @see #setShiftingAllowed(boolean)
     */
    public boolean isShiftingAllowed() {
        return shiftingAllowed;
    }
    
    /**
     * Set the value of shifting. When shifting is enabled, the widget will attempt to move other seek bars which are dependent on 
     * seek bar being adjusted and are blocking its path.
     * @param b
     * 
     * @see #isShiftingAllowed()
     */
    public void setShiftingAllowed(boolean b) {
        shiftingAllowed = b;
    }
    
}

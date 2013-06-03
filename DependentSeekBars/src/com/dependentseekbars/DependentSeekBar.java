package com.dependentseekbars;

import java.util.ArrayList;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import com.dependentseekbars.DependencyGraph.Node;

/**
 * Type of {@link SeekBar} used for adding Dependencies.
 * 
 */
public class DependentSeekBar extends SeekBar {
    public static final String TAG = "DependentSeekBar";
    private DependentSeekBarManager mManager;
    private Node mNode;
    private int mOldProgress = 0;
    private int mTempProgress = -1;
    private boolean mUseTempProgress = false;
    private boolean mPauseProgressChangedListener = false;

    // Used for creating output strings for recursive calls to make reading
    // easier
    private String outputBuffer = "";

    public enum Dependency {
        LESS_THAN,
        GREATER_THAN;
    }

    /**
     * Creates a DependentSeekBar, and adds it to the provided
     * {@link DependentSeekBarManager}. Sets the seek bar's progress and maximum
     * progress as well.
     * 
     * @param context
     * @param manager The {@link DependentSeekBarManager} that this
     *        DependentSeekBar will be added to.
     * @param progress The progress value to initialize the DependentSeekBar to.
     * @param maximum The maximum progress value to set on the DependentSeekBar.
     */
    public DependentSeekBar(Context context, DependentSeekBarManager manager,
            int progress, int maximum) {
        this(context, manager);
        setProgress(progress);
        // This must be done even though it was already done in init, because the progress was not set to the correct
        // value before init was called.
        mOldProgress = progress;
        setMax(maximum);
    }

    /**
     * Creates a DependentSeekBar and adds it to the provided
     * {@link DependentSeekBarManager}.
     *
     * @param context
     * @param manager The {@link DependentSeekBarManager} that this
     *        DependentSeekBar will be added to.
     */
    public DependentSeekBar(Context context, DependentSeekBarManager manager) {
        super(context);
        this.mManager = manager;
        init();
    }

    /*
     * This contructor must be exposed separately from the constructors which take in a {@link DependentSeekBarManager}
     * as it is required by the android layout manager.
     */
    public DependentSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    /*
     * This contructor must be exposed separately from the constructors which take in a {@link DependentSeekBarManager}
     * as it is required by the android layout manager.
     */
    public DependentSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * This method MUST be called in the constructor. This will ensure that
     * mOldProgress is initialized to the correct value and invoke
     * setOnSeekBarChangeListener() so that we override the current listener
     * with our dependency logic.
     */
    private void init() {
        mOldProgress = getProgress();
        setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
            }
        });
    }

    /**
     * Sets node from the dependency graph which references this
     * DependentSeekBar.
     * 
     * @param n the node from the dependency graph
     */
    void setNode(Node n) {
        mNode = n;
    }

    void setManager(DependentSeekBarManager manager) {
        this.mManager = manager;
    }

    @Override
    /**
     * This function sets the listener so that the dependency logic gets
     * executed every time the slider progress is changed.
     * The dependency logic will decide if movement is allowed and by how much.
     * It executes the provided OnSeekBarChangeListener and executes it after
     * the dependency logic.
     */
    public void setOnSeekBarChangeListener(final OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                l.onStopTrackingTouch(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                l.onStartTrackingTouch(seekBar);
            }

            @Override
            /**
             * Determines if the seekbar's progress should be changed if
             * other seekbars must be shifted to ensure that the dependencies
             * still hold.
             *
             */
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // When only a progress change and UI update is desired, this
                // will only update the progress and not execute the logic
                // afterwards
                if (mPauseProgressChangedListener || mNode == null) {
                    l.onProgressChanged(seekBar, progress, fromUser);
                    return;
                }

                outputBuffer = "";

                // If the new progress isn't the same as the old one and
                // movement is allowed in that direction,
                int allowedMovement;
                final int distance = progress - mOldProgress;
                if (distance != 0) {
                    allowedMovement = canMove(distance, mOldProgress, false);

                    if ((distance < 0 && allowedMovement < 0) ||
                            (distance > 0 && allowedMovement > 0)) {
                        mOldProgress += allowedMovement;

                        if (mOldProgress != progress) {
                            setProgressWithoutUpdate(mOldProgress);
                        }
                    } else {
                        setProgressWithoutUpdate(mOldProgress);
                    }
                }
                l.onProgressChanged(seekBar, mOldProgress, fromUser);
            }
        });
    }

    private void setProgressWithoutUpdate(int progress) {
        mPauseProgressChangedListener = true;
        setProgress(progress);
        mPauseProgressChangedListener = false;
    }

    // Similar to super.getProgress() but returns mTempProgress if only a check
    // is being performed
    @Override
    public int getProgress() {
        return mUseTempProgress ? mTempProgress : super.getProgress();
    }

    // When only a check is performed, this function will set mUseTempProgress to
    // true
    private void useTempProgress() {
        if (!mUseTempProgress) {
            mTempProgress = getProgress();
            mUseTempProgress = true;
        }
    }

    boolean usingTempProgress() {
        return mUseTempProgress;
    }

    void clearTempProgress(boolean updateBeforeClearing) {
        if (updateBeforeClearing) {
            mOldProgress = mTempProgress;
            setProgressWithoutUpdate(mOldProgress);
        }
        mUseTempProgress = false;

        for (Node child : mNode.getChildren()) {
            DependentSeekBar dependent = child.getSeekBar();
            if (dependent.usingTempProgress()) {
                dependent.clearTempProgress(updateBeforeClearing);
            }
        }
        for (Node parent : mNode.getParents()) {
            DependentSeekBar dependent = parent.getSeekBar();
            if (dependent.usingTempProgress()) {
                dependent.clearTempProgress(updateBeforeClearing);
            }
        }
    }

    /**
     * Behaves the same as {@link #setProgress(int)}, but will return boolean
     * which denotes whether the seek bar was able to move to the given value
     * with its dependencies.
     *
     * @param newProgress the desired progress to move the seek bar to
     * @return true iff the seek bar was able to successfully move to the new
     *         progress
     */
    public boolean moveTo(int newProgress) {
        boolean result = false;
        int curProgress = getProgress();
        int displacement = newProgress - curProgress;
        if (displacement == 0
                || (displacement > 0 && canMove(displacement, curProgress,
                true) == displacement)) {
            mTempProgress = newProgress;
            result = true;
        }
        clearTempProgress(result);
        return result;
    }

    /**
     * Determines if the slider can move the given displacement amount and if
     * not, the furthest it can move. This function updates the progress of
     * the seekBar iff checkOnly is false and returns the amount that it
     * has changed by (or the amount that it is able to change by iff
     * checkOnly is true).
     * 
     * @param displacement the distance the seek bar is being requested to move
     *        right
     * @return 0 when it cannot move. An integer representing the amount it
     *         can move otherwise. The integer will have the same sign as the
     *         displacement provided.
     */
    private int canMove(int displacement, boolean checkOnly) {
        int movementAllowed = canMove(displacement, mOldProgress, checkOnly);
        if ((displacement < 0 && movementAllowed < 0) ||
                (displacement > 0 && movementAllowed > 0)) {
            if (!checkOnly) {
                setProgressWithoutUpdate(mOldProgress + movementAllowed);
            }
        }
        if (!checkOnly) {
            mOldProgress += movementAllowed;
        } else {
            mTempProgress += movementAllowed;
        }
        return movementAllowed;
    }

    /**
     * Determines if the slider can move the given displacement amount and if
     * not, the furthest it can move. The return value is calculated by
     * asking dependent sliders whether the desired value will conflict with
     * dependencies and asking the dependent sliders to move as far as
     * necessary.
     *
     * @param displacement the distance the seek bar is being requested to move
     *        right
     * @param oldProgress the current progress of the seek bar
     * @return 0 when it cannot move. A positive integer representing the
     *         maximum amount it is allowed to move.
     */
    int canMove(int displacement, int oldProgress, boolean checkOnly) {
        int desiredProgress = oldProgress + displacement;
        // Creates a list of all dependent sliders which conflict with the
        // current slider's desired progress
        ArrayList<Node> conflicting = new ArrayList<Node>();
        for (Node node : displacement < 0 ? mNode.getParents() : mNode.getChildren()) {
            if ((displacement < 0 && node.getProgress() >= desiredProgress) ||
                    (displacement > 0 && node.getProgress() <= desiredProgress)) {
                conflicting.add(node);
                node.getSeekBar().setOutputBuffer(outputBuffer + "\t");
                if (checkOnly) {
                    node.getSeekBar().useTempProgress();
                }
            }
        }

        /*
         * Decides based on the contents of conflictingParents the number to
         * return. If shifting is allowed and the current slider's desired
         * progress will conflict with 1 or more other sliders, those slider's
         * will be asked to move and the values which they return will be used
         * to determine the allowed movement for the current slider.When
         * shifting is disabled, the current slider can only move as much as the
         * next min. dependent slider.
         */
        if (conflicting.size() == 0 && desiredProgress <= getMax()
                && desiredProgress >= 0) {
            return displacement;
        } else if (conflicting.size() != 0) {
            int allowedDisplacement = displacement;
            final int directionFactor = displacement < 0 ? -1 : 1;

            for (Node conflict : conflicting) {
                // If the bar has been updated in the meantime, then we may not
                // need it to move
                final int conflictProgress = conflict.getProgress();
                if ((directionFactor < 0 && conflictProgress < oldProgress + allowedDisplacement) ||
                        (directionFactor > 0 && conflictProgress > oldProgress + allowedDisplacement)) {
                    continue;
                }

                final int distance = Math.abs(conflictProgress - desiredProgress) + 1;
                int temp = displacement - (directionFactor * distance);

                // Determines how much the current slider can move if the child
                // sliders are expected to move as far as necessary.
                if (mManager.isShiftingAllowed()) {
                    temp += conflict.getSeekBar().canMove(
                            directionFactor * distance,
                            checkOnly);
                }

                allowedDisplacement = directionFactor < 0 ?
                                      Math.max(allowedDisplacement, temp) :
                                      Math.min(allowedDisplacement, temp);
            }
            return allowedDisplacement;
        } else {
            return 0;
        }
    }

    private void setOutputBuffer(String newBuffer) {
        outputBuffer = newBuffer;
    }

    /**
     * Add dependencies between the currentRestrictedSeekBar and the
     * RestrictedSeekBars given. The dependency relationship is determined by
     * the provided integer value. If there is no {@link DependentSeekBarManager} set,
     * this function does nothing
     * 
     * @param relationship the relationship the current DependentSeekBar will
     *        have with the given DependentSeekBar's
     * @param indices the indices of the DependentSeekBar's to create
     *        dependencies with
     * @return true iff the dependencies were created successfully
     * 
     * @see #addDependencies(Dependency, DependentSeekBar...)
     */
    public void addDependencies(Dependency relationship, int... indices) {
        if (mManager == null)
            return;
        switch (relationship) {
        case LESS_THAN:
            mManager.addLessThanDependencies(this, indices);
            break;
        case GREATER_THAN:
            mManager.addGreaterThanDependencies(this, indices);
            break;
        }
    }

    /**
     * Add dependencies between the currentRestrictedSeekBar and the
     * RestrictedSeekBars given. The dependency relationship is determined by
     * the provided integer value. If there is no {@link DependentSeekBarManager} set,
     * this function does nothing
     * 
     * @param relationship the relationship the current DependentSeekBar will
     *        have with the given DependentSeekBar's
     * @param dependentSeekBars the DependentSeekBar's to create dependencies
     *        with
     * @return true iff the dependencies were created successfully
     * 
     * @see #addDependencies(Dependency, int...)
     */
    public void addDependencies(Dependency relationship,
            DependentSeekBar... dependentSeekBars) {
        if (mManager == null)
            return;
        switch (relationship) {
        case LESS_THAN:
            mManager.addLessThanDependencies(this, dependentSeekBars);
            break;
        case GREATER_THAN:
            mManager.addGreaterThanDependencies(this, dependentSeekBars);
            break;
        }

    }

    /**
     * Get the maximum progress which the seek bar can move to given its
     * dependencies.
     * 
     * @return maximum progress which the seek bar can move to given its
     *         dependencies
     */
    public int getRestrictedMax() {
        int movement = canMove(getMax() - getProgress() + 1, true);
        clearTempProgress(false);
        return getProgress() + movement;
    }

    /**
     * Get the minimum progress which the seek bar can move to given its
     * dependencies
     * 
     * @return minimum progress which the seek bar can move to given its
     *         dependencies
     */
    public int getRestrictedMin() {
        int movement = canMove(-(getProgress() + 1), true);
        clearTempProgress(false);
        return getProgress() - movement;
    }
}

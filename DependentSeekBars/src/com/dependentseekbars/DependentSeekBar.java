package com.dependentseekbars;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

import com.dependentseekbars.DependencyGraph.Node;

public class DependentSeekBar extends SeekBar {
    public static final String TAG = "DependentSeekBar";
    private DependentSeekBarManager manager;
    private Node node;
    private int oldProgress = 0;
    private int tempProgress = -1;
    private boolean useTempProgress = false;
    private boolean pauseProgressChangedListener = false;
    private String outputBuffer = "";

    // Relationship constants
    public static final int LESS_THAN = 0;
    public static final int GREATER_THAN = 1;

    public DependentSeekBar(Context context, DependentSeekBarManager manager) {
        super(context);
        this.manager = manager;
        init();
    }

    public DependentSeekBar(Context context, DependentSeekBarManager manager,
            int progress, int maximum) {
        super(context);
        this.manager = manager;
        setProgress(progress);
        setMax(maximum);
        init();
    }

    public DependentSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DependentSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * This method MUST be called in the constructor. This will ensure that
     * oldProgress is initialized to the correct value and invoke
     * setOnSeekBarChangeListener() so that we override the current listener
     * with our dependency logic.
     */
    private void init() {
        oldProgress = getProgress();
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
     * Sets the node which
     * 
     * @param n
     */
    void setNode(Node n) {
        node = n;
    }

    void setManager(DependentSeekBarManager manager) {
        this.manager = manager;
    }

    @Override
    /**
     * This function sets the listener so that the dependency logic gets executed everytime the slider progress is changed.
     * The dependency logic will decide if movement is allowed and by how much.
     * It executes the provided OnSeekBarChangeListener and executes it after the dependency logic.
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
             * Determines if the seekbar's progress should be changed if other seekbars must be shifted to ensure that the dependencies still hold.
             * 
             */
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // When only a progress change and UI update is desired, this
                // will only update the progress and not execute the logic
                // afterwards
                if (pauseProgressChangedListener || node == null) {
                    l.onProgressChanged(seekBar, progress, fromUser);
                    return;
                }
                
                outputBuffer = "";

                Log.e("DependentSeekBar", "attempt progress change to "
                        + progress + " from " + getProgress());
                // If the new progress isn't the same as the old one and
                // movement is allowed in that direction,
                int allowedMovement = 0;
                if ((oldProgress - progress) != 0) {
                    if (oldProgress - progress < 0) {
                        allowedMovement = canMoveRight(progress - oldProgress,
                                oldProgress, false);
                    } else if (oldProgress - progress > 0) {
                        allowedMovement = canMoveLeft(oldProgress - progress,
                                oldProgress, false);
                    }

                    if (allowedMovement > 0) {
                        if (oldProgress - progress < 0) {
                            oldProgress += allowedMovement;
                        } else {
                            oldProgress -= allowedMovement;
                        }
                        if (oldProgress != progress) {
                            pauseProgressChangedListener = true;
                            Log.e("DependentSeekBar",
                                    "successfully changing progress to "
                                            + oldProgress);
                            setProgress(oldProgress);
                            pauseProgressChangedListener = false;
                        }
                        l.onProgressChanged(seekBar, oldProgress, fromUser);
                    } else {
                        pauseProgressChangedListener = true;
                        Log.e("DependentSeekBar",
                                "successfully changing progress to "
                                        + oldProgress);
                        setProgress(oldProgress);
                        pauseProgressChangedListener = false;
                    }

                } else {
                    pauseProgressChangedListener = true;
                    Log.e("DependentSeekBar",
                            "successfully changing progress to " + oldProgress);
                    setProgress(oldProgress);
                    pauseProgressChangedListener = false;
                }

                l.onProgressChanged(seekBar, oldProgress, fromUser);
            }
        });
    }

    @Override
    public int getProgress() {
        if (useTempProgress) {
            return tempProgress;
        }
        return super.getProgress();
    }

    private void useTempProgress() {
        if (!useTempProgress) {
            tempProgress = getProgress();
            useTempProgress = true;
        }
    }

    boolean usingTempProgress() {
        return useTempProgress;
    }

    void moveDependenciesToTempProgress() {
        for (Node child : node.getChildren()) {
            DependentSeekBar dependent = child.getSeekBar();
            if (dependent.usingTempProgress()) {
                dependent.moveToTempProgress();
            }
        }
        for (Node parent : node.getParents()) {
            DependentSeekBar dependent = parent.getSeekBar();
            if (dependent.usingTempProgress()) {
                dependent.moveToTempProgress();
            }
        }
    }

    void moveToTempProgress() {
        Log.e(TAG, "tempProgress=" + tempProgress);
        oldProgress = tempProgress;

        pauseProgressChangedListener = true;
        setProgress(oldProgress);
        pauseProgressChangedListener = false;

        useTempProgress = false;

        moveDependenciesToTempProgress();
    }

    void clearTempProgress() {
        useTempProgress = false;

        for (Node child : node.getChildren()) {
            DependentSeekBar dependent = child.getSeekBar();
            if (dependent.usingTempProgress()) {
                dependent.clearTempProgress();
            }
        }
        for (Node parent : node.getParents()) {
            DependentSeekBar dependent = parent.getSeekBar();
            if (dependent.usingTempProgress()) {
                dependent.clearTempProgress();
            }
        }
    }

    /**
     * Determines whether the seekbar can move its progress to the right by
     * calling canMoveRight(). The return value of canMoveRight(),
     * movementAllowed, represents how much this seekBar will move by. This
     * function updates the progress of the seekBar and returns the amount that
     * it has changed by.
     * 
     * @param displacement
     * @return amount that this seekbar has actually moved
     */
    private int canMoveRight(int displacement, boolean checkOnly) {
        int movementAllowed = 0;
        if ((movementAllowed = canMoveRight(displacement, oldProgress,
                checkOnly)) > 0) {
            if (!checkOnly) {
                pauseProgressChangedListener = true;
                setProgress(oldProgress + movementAllowed);
                pauseProgressChangedListener = false;
            }
        }
        if (!checkOnly) {
            oldProgress += movementAllowed;
        } else {
            tempProgress += movementAllowed;
        }
        return movementAllowed;
    }

    /**
     * Determines how much the slider can move its progress to the right. The
     * return value is calculated by asking dependent sliders whether the
     * desired value will conflict with dependencies and asking the dependent
     * sliders to move as far as necessary.
     * 
     * @param displacement
     * @param oldProgress
     * @return 0 when it cannot move. A positive integer representing the
     *         maximum amount it is allowed to move.
     */
    private int canMoveRight(int displacement, int oldProgress,
            boolean checkOnly) {
        Log.e("Testing", outputBuffer + "attempting to move " + displacement
                + "; oldProgress=" + oldProgress + ", max=" + getMax());
        int desiredProgress = oldProgress + displacement;
        // Creates a list of all max. dependent sliders which are left of the
        // current slider's desired progress
        ArrayList<Node> conflictingChildren = new ArrayList<Node>();
        for (Node child : node.getChildren()) {
            if (child.getProgress() <= desiredProgress) {
                conflictingChildren.add(child);
                child.getSeekBar().setOutputBuffer(outputBuffer + "\t");
                if (checkOnly) {
                    child.getSeekBar().useTempProgress();
                }
            }
        }

        /*
         * Decides based on the contents of conflictingChildren the number to
         * return.If shifting is allowed and the current slider's desired
         * progress will conflict with 1 or more other sliders, those slider's
         * will be asked to move and the values which they return will be used
         * to determine the allowed movement for the current slider.When
         * shifting is disabled, the current slider can only move as much as the
         * next max. dependent slider.
         */
        if (conflictingChildren.size() == 0 && desiredProgress <= getMax()
                && (oldProgress + displacement) >= 0) {
            Log.e("Testing", outputBuffer
                    + "no restrictions, returning displacement " + displacement
                    + "; oldProgress=" + oldProgress + ", max=" + getMax());
            return displacement;
        } else if (conflictingChildren.size() != 0) {
            int allowedDisplacement = displacement;
            for (Node child : conflictingChildren) {
                // If the bar has been updated in the meantime, then we may not
                // need it to move
                if (child.getProgress() > desiredProgress) {
                    continue;
                }

                int temp = 0;

                temp = displacement
                        - (desiredProgress - child.getProgress() + 1);

                // Determines how much the current slider can move if the child
                // sliders are expected to move as far as necessary.
                if (manager.isShiftingAllowed()) {
                    temp += child.getSeekBar().canMoveRight(
                            desiredProgress - child.getProgress() + 1,
                            checkOnly);
                }

                allowedDisplacement = Math.min(allowedDisplacement, temp);
            }
            Log.e("Testing", outputBuffer + "returning displacement "
                    + allowedDisplacement + "; oldProgress=" + oldProgress
                    + ", max=" + getMax());
            return allowedDisplacement;
        } else
            Log.e("Testing", outputBuffer + "no displacement allowed"
                    + "; oldProgress=" + oldProgress + ", max=" + getMax());
        return 0;
    }

    /**
     * Determines whether the seekbar can move its progress to the left by
     * calling canMoveLeft(). The return value of canMoveLeft(),
     * movementAllowed, represents how much this seekBar will move by. This
     * function updates the progress of the seekBar and returns the amount that
     * it has changed by.
     * 
     * @param displacement
     * @return amount that this seekbar has actually moved
     */
    private int canMoveLeft(int displacement, boolean checkOnly) {
        int movementAllowed = 0;
        if ((movementAllowed = canMoveLeft(displacement, oldProgress, checkOnly)) > 0) {
            if (!checkOnly) {
                pauseProgressChangedListener = true;
                setProgress(oldProgress - movementAllowed);
                pauseProgressChangedListener = false;
            }
        }
        if (!checkOnly) {
            oldProgress -= movementAllowed;
        } else {
            tempProgress -= movementAllowed;
        }
        return movementAllowed;
    }

    /**
     * Determines how much the slider can move its progress to the left. The
     * return value is calculated by asking dependent sliders whether the
     * desired value will conflict with dependencies and asking the dependent
     * sliders to move as far as necessary.
     * 
     * @param displacement
     * @param oldProgress
     * @return 0 when it cannot move. A positive integer representing the
     *         maximum amount it is allowed to move.
     */
    int canMoveLeft(int displacement, int oldProgress, boolean checkOnly) {
        Log.e("Testing", outputBuffer + "attempting to move " + displacement
                + "; oldProgress=" + oldProgress + ", max=" + getMax());
        int desiredProgress = oldProgress - displacement;
        // Creates a list of all min. dependent sliders which are left of the
        // current slider's desired progress
        ArrayList<Node> conflictingParents = new ArrayList<Node>();
        for (Node parent : node.getParents()) {
            if (parent.getProgress() >= desiredProgress) {
                conflictingParents.add(parent);
                parent.getSeekBar().setOutputBuffer(outputBuffer + "\t");
                if (checkOnly) {
                    parent.getSeekBar().useTempProgress();
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
        if (conflictingParents.size() == 0 && desiredProgress <= getMax()
                && desiredProgress >= 0) {
            Log.e("Testing", outputBuffer
                    + "no restrictions, returning displacement " + displacement
                    + "; oldProgress=" + oldProgress + ", max=" + getMax());
            return displacement;
        } else if (conflictingParents.size() != 0) {
            int allowedDisplacement = displacement;
            for (Node parent : conflictingParents) {
                // If the bar has been updated in the meantime, then we may not
                // need it to move
                if (parent.getProgress() < oldProgress - allowedDisplacement) {
                    continue;
                }

                int temp = 0;

                temp = displacement
                        - (parent.getProgress() - desiredProgress + 1);

                // Determines how much the current slider can move if the child
                // sliders are expected to move as far as necessary.
                if (manager.isShiftingAllowed()) {
                    temp += parent.getSeekBar().canMoveLeft(
                            parent.getProgress() - desiredProgress + 1,
                            checkOnly);
                }

                allowedDisplacement = Math.min(allowedDisplacement, temp);
            }
            Log.e("Testing", outputBuffer + "returning displacement "
                    + allowedDisplacement + "; oldProgress=" + oldProgress
                    + ", max=" + getMax());
            return allowedDisplacement;
        } else {
            Log.e("Testing", outputBuffer
                    + "no displacement allowed; oldProgress=" + oldProgress
                    + ", max=" + getMax());
            return 0;
        }
    }

    public void setOutputBuffer(String newBuffer) {
        outputBuffer = newBuffer;
    }

    /**
     * Behaves the same as {@link #setProgress(int)}, but will return as boolean
     * which denotes whether the seek bar was able to move to the given value
     * with its dependencies.
     * 
     * @param newProgress
     * @return true iff the seek bar was able to successfully move to the new
     *         progress
     */
    public boolean moveTo(int newProgress) {
        Log.e(TAG, "seek request recieved for " + newProgress);
        int curProgress = getProgress();
        int displacement = curProgress - newProgress;
        if (displacement == 0
                || (displacement > 0 && canMoveLeft(displacement, curProgress,
                        true) == displacement)
                || (displacement < 0 && canMoveRight(-displacement,
                        curProgress, true) == -displacement)) {
            oldProgress = newProgress;
            pauseProgressChangedListener = true;
            setProgress(oldProgress);
            pauseProgressChangedListener = false;

            moveDependenciesToTempProgress();
            Log.e(TAG, "should move");
            return true;
        }
        clearTempProgress();
        return false;
    }

    /**
     * Add dependencies between the currentRestrictedSeekBar and the
     * RestrictedSeekBars given. The dependency relationship is determined by
     * the provided integer value.
     * 
     * @param restrictedSeekBars
     * @return
     * 
     * @see #addDependencies(int, RestrictedSeekBar...)
     */
    public boolean addDependencies(int relationship, int... indices) {

        switch (relationship) {
        case LESS_THAN:
            return manager.addLessThanDependencies(this, indices);
        case GREATER_THAN:
            return manager.addGreaterThanDependencies(this, indices);
        default:
            return false;
        }

    }

    /**
     * Add dependencies between the currentRestrictedSeekBar and the
     * RestrictedSeekBars given. The dependency relationship is determined by
     * the provided integer value.
     * 
     * @param restrictedSeekBars
     * @return
     * 
     * @see #addDependencies(int, int...)
     */
    public boolean addDependencies(int relationship,
            DependentSeekBar... dependentSeekBars) {

        switch (relationship) {
        case LESS_THAN:
            return manager.addLessThanDependencies(this, dependentSeekBars);
        case GREATER_THAN:
            return manager.addGreaterThanDependencies(this, dependentSeekBars);
        default:
            return false;
        }

    }

    // TODO rename
    public int getRestrictedMax() {
        int maxDisplacement = canMoveRight(getMax() - getProgress() + 1, true);
        return maxDisplacement;
    }

    // TODO rename
    public int getRestrictedMin() {
        int maxDisplacement = canMoveLeft(getProgress() + 1, true);
        return maxDisplacement;
    }
}

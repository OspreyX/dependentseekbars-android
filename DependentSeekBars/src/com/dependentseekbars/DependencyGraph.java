package com.dependentseekbars;

import java.util.ArrayList;

import android.util.Log;

/**
 * Acyclic Graph datastructure designed to maintain the dependencies of the
 * {@link DependentSeekBar}. Nodes are {@link DependentSeekBar}s and edges are
 * the dependencies.
 *
 * Dependency representation:
 *
 *  The DependencyGraph maintains relationships by making a SeekBar's respective
 *  node a child or parent of another node.
 *
 *  There are two kinds of relationships, Greater Than and Less Than.
 *
 *   Greater Than: If SeekBar1 > SeekBar2, then Node1 is a child of Node2
 *   Less Than: If SeekBar1 < SeekBar2, then Node1 is a parent of Node2
 *
 * If the graph adds an edge that creates a cycle, an InconsistentGraphException
 * is thrown.
 *
 */
public class DependencyGraph {
    private ArrayList<Node> nodes;

    public final static int CHECK_ALL_DEPENDENCIES = 0;
    public final static int CHECK_GT_DEPENDENCIES = 1;
    public final static int CHECK_LT_DEPENDENCIES = 2;

    private final boolean DEBUG = false;

    public DependencyGraph() {
        nodes = new ArrayList<Node>();
    }

    /**
     * Adds a {@link Node} to the graph corresponding to seekBar.
     * @param seekBar
     * @return The {@link Node} that has been added to the graph.
     */
    public Node addSeekBar(DependentSeekBar seekBar) {
        Node node = new Node(seekBar);
        nodes.add(node);
        return node;
    }

    /**
     * Removes the node representing the provided DependentSeekBar. If
     * restructureDependencies is true, then it will attempt to maintain
     * dependency relationships among the existing nodes instead of removing all
     * dependencies associated with the removed node.
     * @param seekBar
     * @param restructureDependencies Boolean representing whether or not the
     *                                graph will try to restructure any
     *                                dependencies that may be lost when seekBar
     *                                is removed.
     */
    public void removeSeekBar(DependentSeekBar seekBar,
            boolean restructureDependencies) {
        // TODO add logic to restructure dependencies
        Node seekNode = null;
        for (Node node : nodes) {
            if (node.sameSeekBar(seekBar)) {
                seekNode = node;
                break;
            }
        }
        for (Node node : nodes) {
            node.removeDependencies(seekNode);
        }

        nodes.remove(seekNode);
    }

    /**
     * Adds Less Than dependencies to dependent {@link DependentSeekBar} from limiting.
     * @param dependent The {@link DependentSeekBar} that will always be less than limiting seek bars.
     * @param limiting An array of {@link DependentSeekBar}s that will always be greater than dependent.
     * @throws InconsistentGraphException
     */
    public void addLessThanDependencies(DependentSeekBar dependent,
            DependentSeekBar[] limiting) throws InconsistentGraphException {
        for (int i = 0; i < limiting.length; i++) {
            addLessThanDependency(dependent, limiting[i]);
        }
    }

    /*
     * Adds a Less Than dependency. If the dependency causes the graph
     * to have a cycle, an InconsistentGraphException is thrown.
     *
     * This is used as a helper function for the public function of the same name.
     */
    private void addLessThanDependency(DependentSeekBar dependent,
            DependentSeekBar child) throws InconsistentGraphException {
        Node dependNode = null, childNode = null;
        if (DEBUG) {
            Log.d("DependencyGraph", "addMaxDependency: size of nodes array="
                    + nodes.size());
            Log.d("DependencyGraph",
                    "addMaxDependency: adding " + dependent.getProgress()
                            + " < " + child.getProgress());
        }
        for (Node node : nodes) {
            if (dependNode == null && node.sameSeekBar(dependent)) {
                dependNode = node;
            }
            if (childNode == null && node.sameSeekBar(child)) {
                childNode = node;
            }
            if (childNode != null && dependNode != null) {
                break;
            }
        }

        // TODO remove when we are done
        if (dependNode.containsChild(childNode)
                && dependNode.containsParent(childNode)) {
            throw new InconsistentGraphException(dependNode.getSeekBar()
                    + " has same parent and child.");
        }

        if (dependNode.containsChild(childNode)) {
            // dependency already exists, already done, return
            return;
        } else if (dependNode.equals(childNode)
                || (dependNode.getProgress() >= childNode.getProgress())) {
            throw new InconsistentGraphException(
                    "The dependency being added causes conflicts with the seekbar progresses");
        } else if (containsCycle(childNode, CHECK_LT_DEPENDENCIES)) {
            throw new InconsistentGraphException(
                    "The dependency being added creates a circular dependency.");
        }

        // the graph will remain acyclic with the dependency edge, so it is safe
        // to add. Adds both directions of the edge.
        dependNode.addChild(childNode);
        childNode.addParent(dependNode);
    }

    /**
     * Adds Greater Than dependencies to dependent {@link DependentSeekBar} from limiting.
     * @param dependent The {@link DependentSeekBar} that will always be greater than limiting seek bars.
     * @param limiting An array of {@link DependentSeekBar}s that will always be less than dependent.
     * @throws InconsistentGraphException
     */
    public void addGreaterThanDependencies(DependentSeekBar dependent,
            DependentSeekBar[] limiting) throws InconsistentGraphException {
        for (int i = 0; i < limiting.length; i++) {
            // adds dependencies, if there is an error, an
            // InconsistantGraphException is thrown
            addGreaterThanDependency(dependent, limiting[i]);
        }
    }

    /*
     * Adds a Greater Than dependency. If the dependency causes the graph
     * to have a cycle, an InconsistentGraphException is thrown.
     *
     * This is used as a helper function for the public function of the same name.
     */
    private void addGreaterThanDependency(DependentSeekBar dependent,
            DependentSeekBar parent) throws InconsistentGraphException {
        if (DEBUG) {
            Log.d("DependencyGraph", "addMinDependency: size of nodes array="
                    + nodes.size());
            Log.d("DependencyGraph",
                    "addMinDependency: adding " + dependent.getProgress()
                            + " > " + parent.getProgress());
        }
        Node dependNode = null, parentNode = null;

        // Searching for the dependNode and limitNode which correspond to
        // dependent and limiting seek bars
        for (Node node : nodes) {
            if (dependNode == null && node.sameSeekBar(dependent)) {
                dependNode = node;
            }
            if (parentNode == null && node.sameSeekBar(parent)) {
                parentNode = node;
            }
            if (parentNode != null && dependNode != null) {
                break;
            }
        }

        // TODO remove when we are done
        if (dependNode.containsParent(parentNode)
                && dependNode.containsChild(parentNode)) {
            throw new InconsistentGraphException(dependNode.getSeekBar()
                    + " has same parent and child.");
        }

        if (dependNode.containsParent(parentNode)) {
            // dependency already exists, already done, return
            return;
        } else if (dependNode.equals(parentNode)
                || (dependNode.getProgress() <= parentNode.getProgress())) {
            throw new InconsistentGraphException(
                    "The dependency being added causes conflicts with the seekbar progresses");
        } else if (containsCycle(parentNode, CHECK_GT_DEPENDENCIES)) {
            throw new InconsistentGraphException(
                    "The dependency being added creates a circular dependency.");
        }

        // the graph will remain acyclic with the dependency edge, so it is safe
        // to add. Adds both directions of the edge.
        dependNode.addParent(parentNode);
        parentNode.addChild(dependNode);
    }

    /*
     * Checks if a cycle exists containing limitingNode.
     * This checks all dependencies of limitingNode for a cycle
     */
    private boolean containsCycle(Node limitingNode){
        return containsCycle(limitingNode, CHECK_ALL_DEPENDENCIES);
    }

    /*
     * Checks if a cycle exists from limitingNode. Calls the helper function
     * on each child of limitingNode.
     */
    private boolean containsCycle(Node limitingNode, int checkType) {
        if(checkType != CHECK_GT_DEPENDENCIES){
            for(Node child : limitingNode.getChildren()){
                child.setVisited(false);
            }

            for (Node child : limitingNode.getChildren()) {
                if (containsCycleHelper(limitingNode, child, CHECK_LT_DEPENDENCIES))
                    return true;
            }
        }
        
        if(checkType != CHECK_LT_DEPENDENCIES){

            for(Node parent : limitingNode.getParents()){
                parent.setVisited(false);
            }

            for (Node parent : limitingNode.getParents()) {
                if (containsCycleHelper(limitingNode, parent, CHECK_GT_DEPENDENCIES))
                    return true;
            }
        }
        return false;
    }

    private boolean containsCycleHelper(Node startingNode, Node currentNode, int checkType){
        boolean containsCycle = false;
        if (currentNode.equals(startingNode)) {
            containsCycle = true;
        } else if (currentNode.isVisited()) {
            containsCycle = false;
        }else {
            currentNode.setVisited(true);
            ArrayList<Node> nodesToCheck;
            if(checkType == CHECK_LT_DEPENDENCIES)
                nodesToCheck = currentNode.getChildren();
            else{
                nodesToCheck = currentNode.getParents();
            }

            for (Node child : nodesToCheck) {
                if (containsCycleHelper(startingNode, child, checkType)) {
                    containsCycle = true;
                }
            }
        }
        return containsCycle;
    }

    //Removes the limiting DependentSeekBar dependencies from dependent
    private void revertMaxAdditions(DependentSeekBar dependent,
            DependentSeekBar[] limiting, int max) {
        Node dependNode = null;
        for (Node node : nodes) {
            if (node.sameSeekBar(dependent)) {
                dependNode = node;
                break;
            }
        }
        for (int i = 0; i < max; i++) {
            for (Node node : nodes) {
                if (node.sameSeekBar(limiting[i])) {
                    dependNode.removeChild(node);
                    node.removeParent(dependNode);
                }

            }
        }
    }

    //Removes the limiting DependentSeekBar dependencies from dependent
    private void revertMinAdditions(DependentSeekBar dependent,
            DependentSeekBar[] limiting, int max) {
        Node dependNode = null;
        for (Node node : nodes) {
            if (node.sameSeekBar(dependent)) {
                dependNode = node;
                break;
            }
        }
        for (int i = 0; i < max; i++) {
            for (Node node : nodes) {
                if (node.sameSeekBar(limiting[i])) {
                    dependNode.removeParent(node);
                    node.removeChild(dependNode);
                }

            }
        }
    }

    /**
     * Node in the graph. Each node contains the corresponding DependentSeekBar,
     * and a list of direct children and parents.
     */
    public class Node {
        private ArrayList<Node> children;
        private ArrayList<Node> parents;
        private DependentSeekBar seekBar;
        private boolean visited = false;

        /**
         * Creates a node corresponding to the given {@link DependentSeekBar}
         * @param seekBar
         */
        public Node(DependentSeekBar seekBar) {
            this.seekBar = seekBar;
            children = new ArrayList<Node>();
            parents = new ArrayList<Node>();
        }

        private boolean sameSeekBar(DependentSeekBar other) {
            if (DEBUG)
                Log.d("DependencyGraph",
                        "sameSeekBar: " + seekBar.getProgress()
                    + " " + other.getProgress());
            return seekBar.equals(other);
        }

        /*
         * Removes dependencies without restructuring by removing this node from
         * the other node's that have a dependency relationship with this one
         */
        private void removeDependencies(Node node) {
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i).equals(node)) {
                    children.remove(i);
                    break;
                }
            }
            for (int i = 0; i < parents.size(); i++) {
                if (parents.get(i).equals(node)) {
                    parents.remove(i);
                    break;
                }
            }
        }

        /**
         * Get the {@link DependentSeekBar}'s current progress level
         * @return the current progress of the {@link DependentSeekBar}
         */
        public int getProgress() {
            return seekBar.getProgress();
        }

        /**
         * Get the {@link DependentSeekBar} corresponding to the node
         * @return
         */
        public DependentSeekBar getSeekBar() {
            return seekBar;
        }

        private void addChild(Node node) {
            children.add(node);
        }

        private void removeChild(Node node) {
            children.remove(node);
        }

        private void addParent(Node node) {
            parents.add(node);
        }

        private void removeParent(Node node) {
            parents.remove(node);
        }

        private boolean isVisited() {
            return visited;
        }

        private void setVisited(boolean v) {
            visited = v;
        }

        /**
         * Get the children of this Node
         * @return List of child nodes
         */
        public ArrayList<Node> getChildren() {
            return children;
        }

        /**
         * Get the parents of this Node
         * @return List of parent Nodes
         */
        public ArrayList<Node> getParents() {
            return parents;
        }

        /**
         * Checks if the given node is a child of this Node
         * @param node
         * @return true if node is a child of this Node, false otherwise
         */
        public boolean containsChild(Node node) {
            return children.contains(node);
        }

        /**
         * Checks if the given node is a parent of this Node
         * @param node
         * @return true if node is a parent of this Node, false otherwise
         */
        public boolean containsParent(Node node) {
            return parents.contains(node);
        }
    }

    /**
     * Exception class for when the graph or dependencies are in a bad state.
     * A bad state is when the graph is not acyclic. When the graph contains
     * a cycle, a circular dependency between {@link DependentSeekBar}s exists.
     */
    public class InconsistentGraphException extends RuntimeException {

        /**
         * Constructs an InconsistentGraphException with no error message
         */
        public InconsistentGraphException() {
        }

        /**
         * Constructs an InconsistentGraphException with an error message
         * @param message the error message to construct the Exception with
         */
        public InconsistentGraphException(String message) {
            super(message);
        }
    }
}

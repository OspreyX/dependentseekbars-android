package com.dependentseekbars;

import java.util.ArrayList;

import android.util.Log;

/**
 * Acyclic Graph datastructure designed to maintain the dependencies of the
 * {@link DependentSeekBar}. Nodes are {@link DependentSeekBar}s and edges are
 * the dependencies.
 * 
 * Dependency representation: Greater Than: SeekBar1 > SeekBar2, then Node1 is a
 * child of Node2 Less Than: SeekBar1 < SeekBar2, then Node1 is a parent of
 * Node2
 * 
 * If the graph adds an edge that creates a cycle, an InconsistentGraphException
 * is thrown.
 * 
 */
public class DependencyGraph {
    private ArrayList<Node> nodes;

    public DependencyGraph() {
        nodes = new ArrayList<Node>();
    }

    // Add a node to the graph with the given DependentSeekBar
    public Node addSeekBar(DependentSeekBar seekBar) {
        Node node = new Node(seekBar);
        nodes.add(node);
        return node;
    }

    /*
     * Removes the node representing the provided DependentSeekBar. If
     * restructureDependencies is true, then it will attempt to maintain
     * dependency relationships among the existing nodes instead of removing all
     * dependencies associated with the removed node (TODO not implemented yet).
     */
    public void removeSeekBar(DependentSeekBar seekBar,
            boolean restructureDependencies) {
        // TODO use the boolean and add children of node to be removed to
        // parent... parents?
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

    /*
     * Adds maximum (less than) dependencies by calling the helper function of
     * the same name for each limiting DependentSeekBar
     */
    public void addMaxDependencies(DependentSeekBar dependent,
            DependentSeekBar[] limiting) throws InconsistantGraphException {
        for (int i = 0; i < limiting.length; i++) {
            addMaxDependency(dependent, limiting[i]);
        }
    }

    /*
     * Adds a maximum (less than) dependency. If the dependency causes the graph
     * to have a cycle, an InconsistentGraphException is thrown
     */
    private void addMaxDependency(DependentSeekBar dependent,
            DependentSeekBar limiting) throws InconsistantGraphException {
        Node dependNode = null, limitNode = null;
        Log.e("DependencyGraph", "addMaxDependency: size of nodes array="
                + nodes.size());
        Log.e("DependencyGraph",
                "addMaxDependency: adding " + dependent.getProgress() + " < "
                        + limiting.getProgress());
        for (Node node : nodes) {
            if (dependNode == null && node.sameSeekBar(dependent)) {
                dependNode = node;
            }
            if (limitNode == null && node.sameSeekBar(limiting)) {
                limitNode = node;
            }
            if (limitNode != null && dependNode != null) {
                break;
            }
        }

        // TODO remove when we are done
        if (dependNode.containsChild(limitNode)
                && dependNode.containsParent(limitNode)) {
            throw new InconsistantGraphException(dependNode.getSeekBar()
                    + " has same parent and child.");
        }

        if (dependNode.containsChild(limitNode)) {
            // dependency already exists, already done, return
            return;
        } else if (dependNode.equals(limitNode)
                || (dependNode.getProgress() >= limitNode.getProgress())) {
            throw new InconsistantGraphException(
                    "The dependency being added causes conflicts with the seekbar progresses");
        } else if (checkForMaxCycle(limitNode)) {
            throw new InconsistantGraphException(
                    "The dependency being added creates a circular dependency.");
        }

        // the graph will remain acyclic with the dependency edge, so it is safe
        // to add. Adds both directions of the edge.
        dependNode.addChild(limitNode);
        limitNode.addParent(dependNode);
    }

    /*
     * Adds minimum (greater than) dependencies by calling the helper function
     * of the same name for each limiting DependentSeekBar
     */
    public void addMinDependencies(DependentSeekBar dependent,
            DependentSeekBar[] limiting) throws InconsistantGraphException {
        for (int i = 0; i < limiting.length; i++) {
            // adds dependencies, if there is an error, an
            // InconsistantGraphException is thrown
            addMinDependency(dependent, limiting[i]);
        }
    }

    /*
     * Adds a minimum (greater than) dependency. If the dependency causes the
     * graph to have a cycle, an InconsistentGraphException is thrown
     */
    private void addMinDependency(DependentSeekBar dependent,
            DependentSeekBar limiting) throws InconsistantGraphException {
        Log.e("DependencyGraph", "addMinDependency: size of nodes array="
                + nodes.size());
        Log.e("DependencyGraph",
                "addMinDependency: adding " + dependent.getProgress() + " > "
                        + limiting.getProgress());
        Node dependNode = null, limitNode = null;

        // Searching for the dependNode and limitNode which correspond to
        // dependent and limiting seek bars
        for (Node node : nodes) {
            if (dependNode == null && node.sameSeekBar(dependent)) {
                dependNode = node;
            }
            if (limitNode == null && node.sameSeekBar(limiting)) {
                limitNode = node;
            }
            if (limitNode != null && dependNode != null) {
                break;
            }
        }

        // TODO remove when we are done
        if (dependNode.containsParent(limitNode)
                && dependNode.containsChild(limitNode)) {
            throw new InconsistantGraphException(dependNode.getSeekBar()
                    + " has same parent and child.");
        }

        if (dependNode.containsParent(limitNode)) {
            // dependency already exists, already done, return
            return;
        } else if (dependNode.equals(limitNode)
                || (dependNode.getProgress() <= limitNode.getProgress())) {
            throw new InconsistantGraphException(
                    "The dependency being added causes conflicts with the seekbar progresses");
        } else if (checkForMinCycle(limitNode)) {
            throw new InconsistantGraphException(
                    "The dependency being added creates a circular dependency.");
        }

        // the graph will remain acyclic with the dependency edge, so it is safe
        // to add. Adds both directions of the edge.
        dependNode.addParent(limitNode);
        limitNode.addChild(dependNode);
    }

    /*
     * Checks if a cycle exists from limitingNode. Calls the helper function of
     * the same name on each child of limitingNode.
     */
    public boolean checkForMaxCycle(Node limitingNode) {
        for (Node child : limitingNode.getChildren()) {
            if (checkForMaxCycle(limitingNode, child))
                return true;
        }
        return false;
    }

    /*
     * Checks for a cycle by performing a depth-first search and marking nodes
     * as visited when discovered. If a node is re-visited, a cycle exists and
     * the function returns false. Returns true when no cycle is found.
     */
    private boolean checkForMaxCycle(Node startingNode, Node currentNode) {

        if (currentNode.equals(startingNode)) {
            return true;
        } else if (currentNode.isVisited()) {
            return false;
        } else {
            currentNode.setVisited(true);
            for (Node child : currentNode.getChildren()) {

                if (checkForMaxCycle(startingNode, child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Checks if a cycle exists from limitingNode. Calls the helper function of
     * the same name on each parent of limitingNode.
     */
    public boolean checkForMinCycle(Node limitingNode) {
        for (Node child : limitingNode.getParents()) {
            if (checkForMinCycle(limitingNode, child))
                return true;
        }
        return false;
    }

    /*
     * Checks for a cycle by performing a depth-first search and marking nodes
     * as visited when discovered. If a node is re-visited, a cycle exists and
     * the function returns false. Returns true
     */
    private boolean checkForMinCycle(Node startingNode, Node currentNode) {

        if (currentNode.equals(startingNode)) {
            return true;
        } else if (currentNode.isVisited()) {
            return false;
        } else {
            currentNode.setVisited(true);
            for (Node child : currentNode.getParents()) {

                if (checkForMinCycle(startingNode, child)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Not used currently but may be useful in the future
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

    // Not used currently but may be useful in the future
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

    /*
     * Node in the graph. Each node contains the corresponding DependentSeekBar,
     * and a list of direct children and parents
     */
    public class Node {
        private ArrayList<Node> children;
        private ArrayList<Node> parents;
        private DependentSeekBar seekBar;
        private boolean visited = false;

        public Node(DependentSeekBar seekBar) {
            this.seekBar = seekBar;
            children = new ArrayList<Node>();
            parents = new ArrayList<Node>();
        }

        private boolean sameSeekBar(DependentSeekBar other) {
            Log.e("DependencyGraph", "sameSeekBar: " + seekBar.getProgress()
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

        public int getProgress() {
            return seekBar.getProgress();
        }

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

        public ArrayList<Node> getChildren() {
            return children;
        }

        public ArrayList<Node> getParents() {
            return parents;
        }

        public boolean containsChild(Node node) {
            return children.contains(node);
        }

        public boolean containsParent(Node node) {
            return parents.contains(node);
        }
    }

    /*
     * Exception for when the graph or dependencies are in a bad state (the
     * graph contains a cycle, causing a circular dependency).
     */
    public class InconsistantGraphException extends RuntimeException {
        public InconsistantGraphException() {

        }

        public InconsistantGraphException(String message) {
            super(message);
        }
    }
}

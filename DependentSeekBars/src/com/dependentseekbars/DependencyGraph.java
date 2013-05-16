package com.dependentseekbars;
import java.util.ArrayList;

import android.util.Log;

public class DependencyGraph {
    private ArrayList<Node> nodes;

    public DependencyGraph() {
        nodes = new ArrayList<Node>();
    }

    public Node addSeekBar(DependentSeekBar seekBar) {
        Node node = new Node(seekBar);
        nodes.add(node);
        return node;
    }
    
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

    public boolean addMaxDependencies(DependentSeekBar dependent,
            DependentSeekBar[] limiting) throws InconsistantGraphException {
        int i = 0;
        for (; i < limiting.length; i++) {
            if (!addMaxDependency(dependent, limiting[i])) {
                revertMaxAdditions(dependent, limiting, i);
                throw new NullPointerException();
            }
        }
        return true;
    }

    private boolean addMaxDependency(DependentSeekBar dependent,
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
        if (dependNode.containsChild(limitNode) && dependNode.containsParent(limitNode)) {
            System.out.println("graph is broken in addMaxDependency");
            throw new NullPointerException();

        }

        if (dependNode.containsChild(limitNode)) {
            return true;
        } else if (dependNode.containsParent(limitNode) || limitNode.containsChild(dependNode)) {
            throw new InconsistantGraphException();
        } else if (dependNode.equals(limitNode)
                || (dependNode.getProgress() >= limitNode.getProgress())) {
            throw new InconsistantGraphException();
        }
        dependNode.addChild(limitNode);
        limitNode.addParent(dependNode);

        return true;
    }

    public boolean addMinDependencies(DependentSeekBar dependent,
            DependentSeekBar[] limiting) throws InconsistantGraphException {
        int i = 0;
        for (; i < limiting.length; i++) {
            if (!addMinDependency(dependent, limiting[i])) {
                revertMinAdditions(dependent, limiting, i);
                throw new NullPointerException();
            }
        }
        return true;
    }

    private boolean addMinDependency(DependentSeekBar dependent,
            DependentSeekBar limiting) throws InconsistantGraphException {
        Log.e("DependencyGraph", "addMinDependency: size of nodes array="
                + nodes.size());
        Log.e("DependencyGraph",
                "addMinDependency: adding " + dependent.getProgress() + " > "
                        + limiting.getProgress());
        Node dependNode = null, limitNode = null;
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
        if (dependNode.containsParent(limitNode) && dependNode.containsChild(limitNode)) {
            System.out.println("graph is broken in addMinDependency");
            throw new NullPointerException();

        }
        
        if (dependNode.containsParent(limitNode)) {
            return true;
        } else if (dependNode.containsChild(limitNode) || limitNode.containsParent(dependNode)) {
            throw new InconsistantGraphException();
        } else if (dependNode.equals(limitNode)
                || (dependNode.getProgress() <= limitNode.getProgress())) {
            throw new InconsistantGraphException();
        }
        dependNode.addParent(limitNode);
        limitNode.addChild(dependNode);

        return true;
    }

    public boolean checkForMaxCycle(Node limitingNode) {
        for (Node child : limitingNode.getChildren()) {
            if (checkForMaxCycle(limitingNode, child))
                return true;
        }
        return false;
    }

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

    public boolean checkForMinCycle(Node limitingNode) {
        for (Node child : limitingNode.getParents()) {
            if (checkForMinCycle(limitingNode, child))
                return true;
        }
        return false;
    }

    private boolean checkForMinCycle(Node startingNode, Node currentNode) {

        if (currentNode.equals(startingNode)) {
            return true;
        } else if (currentNode.isVisited()) {
            return false;
        } else {
            currentNode.setVisited(true);
            for (Node child : currentNode.getParents()) {

                if (checkForMaxCycle(startingNode, child)) {
                    return true;
                }
            }
        }
        return false;
    }
    
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
                    + " "
                    + other.getProgress());
            return seekBar.equals(other);
        }

        private void removeDependencies(Node node) {
            for (int i=0; i < children.size(); i++) {
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

    public class InconsistantGraphException extends RuntimeException {

    }
}

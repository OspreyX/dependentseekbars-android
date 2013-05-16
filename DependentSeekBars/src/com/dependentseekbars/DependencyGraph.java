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

	public void addMaxDependencies(DependentSeekBar dependent,
			DependentSeekBar[] limiting) throws InconsistantGraphException {
		for (int i = 0; i < limiting.length; i++) {
			addMaxDependency(dependent, limiting[i]);
		}
	}

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
					"The dependency being added causes conflict with the seekbar progresses");
		} else if (checkForMaxCycle(limitNode)) {
			throw new InconsistantGraphException(
					"The dependency being added creates a circular dependency.");
		}

		// the graph will remain acyclic with the dependency edge, so it is safe
		// to add
		dependNode.addChild(limitNode);
		limitNode.addParent(dependNode);
	}

	public void addMinDependencies(DependentSeekBar dependent,
			DependentSeekBar[] limiting) throws InconsistantGraphException {
		for (int i = 0; i < limiting.length; i++) {
			// adds dependencies, if there is an error, an
			// InconsistantGraphException is thrown
			addMinDependency(dependent, limiting[i]);
		}
	}

	private void addMinDependency(DependentSeekBar dependent,
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
					"The dependency being added causes conflict with the seekbar progresses");
		} else if (checkForMinCycle(limitNode)) {
			throw new InconsistantGraphException(
					"The dependency being added creates a circular dependency.");
		}

		// the graph will remain acyclic with the dependency edge, so it is safe
		// to add
		dependNode.addParent(limitNode);
		limitNode.addChild(dependNode);
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

	public class InconsistantGraphException extends RuntimeException {
		public InconsistantGraphException() {

		}

		public InconsistantGraphException(String message) {
			super(message);
		}
	}
}

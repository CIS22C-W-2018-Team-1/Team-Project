package edu.deanza.cis22c.w2018.team1.structure.graph.maxflow;

import edu.deanza.cis22c.w2018.team1.structure.graph.Vertex;

public class MaxFlowVertex<E> extends Vertex<E> {
	private int level;

	protected MaxFlowVertex(E data) {
		super(data);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}

package com.cgi.fastordermanager.graph;

import org.jgrapht.graph.DefaultEdge;

public class RfsGraphEdge extends DefaultEdge {


	private static final long serialVersionUID = 7815123737661986680L;
	
	@Override
	public String getSource() {
		return (String)super.getSource();
	}
	
	@Override
	public String getTarget() {
		return (String)super.getTarget();
	}

}
